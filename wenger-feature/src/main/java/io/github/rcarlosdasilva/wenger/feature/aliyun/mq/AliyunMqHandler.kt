package io.github.rcarlosdasilva.wenger.feature.aliyun.mq

import com.aliyun.openservices.ons.api.Consumer
import com.aliyun.openservices.ons.api.ONSFactory
import com.aliyun.openservices.ons.api.PropertyKeyConst
import com.aliyun.openservices.ons.api.order.OrderConsumer
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

/**
 * 阿里云MQ功能封装
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.aliyun.mq.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class AliyunMqHandler @Autowired constructor(
    @Autowired(required = false) private val consumers: List<AbstractConsumer>?,
    @Autowired(required = false) private val localTransactionChecker: LocalTransactionChecker?,
    private val appProperties: AppProperties
) : SmartInitializingSingleton, DisposableBean {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val normalProducers: MutableMap<String, NormalProducer> = mutableMapOf()
  private val orderedProducers: MutableMap<String, OrderedProducer> = mutableMapOf()
  private val transactionalProducers: MutableMap<String, TransactionalProducer> = mutableMapOf()
  private val normalConsumers: MutableList<Consumer> = mutableListOf()
  private val orderedConsumers: MutableList<OrderConsumer> = mutableListOf()

  override fun afterSingletonsInstantiated() {
    with(appProperties.aliyun.mq) {
      Companion.producerSendRetries = this.producerRetries
      Companion.isPrintLog = this.printLog

      configProducers(this.producers)
      configConsumers(consumers)
    }
  }

  private fun configProducers(producers: Map<String, MqProducerType>?) {
    if (producers?.isEmpty() != false) {
      logger.warn("[Aliyun:MQ] - 未配置消息队列生产者")
      return
    }

    producers.forEach {
      val config = producerConfig(it.key)
      when (it.value) {
        MqProducerType.NORMAL -> {
          normalProducers[it.key] = NormalProducer(config)
          logger.info("[Aliyun:MQ] - 注册普通消息生产者(Normal Producer)：PID: {}", it.key)
        }
        MqProducerType.ORDERED -> {
          orderedProducers[it.key] = OrderedProducer(config)
          logger.info("[Aliyun:MQ] - 注册顺序消息生产者(Ordered Producer)：PID: {}", it.key)
        }
        MqProducerType.TRANSACTIONAL -> {
          transactionalProducers[it.key] = TransactionalProducer(config, localTransactionChecker)
          logger.info("[Aliyun:MQ] - 注册事务消息生产者(Transactional Producer)：PID: {}", it.key)
        }
      }
    }
  }

  private fun configConsumers(consumers: List<AbstractConsumer>?) {
    if (consumers?.isEmpty() != false) {
      logger.warn("[Aliyun:MQ] - 未配置消息队列消费者")
      return
    }

    consumers.forEach {
      val config = consumerConfig(it.consumerId(), it.threads(), it.timeout())

      if (it.isOrdered()) {
        ONSFactory.createOrderedConsumer(config).apply {
          this.subscribe(it.topic(), TextHelper.join("||", it.tags()), it.orderedListener())
          this.start()
          orderedConsumers.add(this)
          logger.info("[Aliyun:MQ] - 注册顺序消息消费者(ORDERED CONSUMER)：Topic: {}, CID: {}, Tags: {}", it.topic(), it.consumerId(), it.tags())
        }
      } else {
        ONSFactory.createConsumer(config).apply {
          this.subscribe(it.topic(), TextHelper.join("||", it.tags()), it.normalListener())
          this.start()
          normalConsumers.add(this)
          logger.info("[Aliyun:MQ] - 注册消息消费者(NORMAL CONSUMER)：Topic: {}, CID: {}, Tags: {}", it.topic(), it.consumerId(), it.tags())
        }
      }
    }
  }

  override fun destroy() {
    normalProducers.forEach { it.value.shutdown() }
    orderedProducers.forEach { it.value.shutdown() }
    transactionalProducers.forEach { it.value.shutdown() }

    normalConsumers.forEach { it.shutdown() }
    orderedConsumers.forEach { it.shutdown() }
  }

  fun normal(producerId: String): NormalProducer? = normalProducers[producerId]
  fun ordered(producerId: String): OrderedProducer? = orderedProducers[producerId]
  fun transactional(producerId: String): TransactionalProducer? = transactionalProducers[producerId]

  private fun producerConfig(producerId: String): Properties =
      Properties().apply {
        this[PropertyKeyConst.AccessKey] = appProperties.aliyun.accessId
        this[PropertyKeyConst.SecretKey] = appProperties.aliyun.accessSecret
        this[PropertyKeyConst.ONSAddr] = appProperties.aliyun.mq.address
        this[PropertyKeyConst.ProducerId] = producerId
        // 设置发送超时时间，单位毫秒
        this[PropertyKeyConst.SendMsgTimeoutMillis] = appProperties.aliyun.mq.sendTimeout
      }

  private fun consumerConfig(consumerId: String, threads: Int, timeout: Int): Properties =
      Properties().apply {
        this[PropertyKeyConst.AccessKey] = appProperties.aliyun.accessId
        this[PropertyKeyConst.SecretKey] = appProperties.aliyun.accessSecret
        this[PropertyKeyConst.ONSAddr] = appProperties.aliyun.mq.address
        this[PropertyKeyConst.ConsumerId] = consumerId
        this[PropertyKeyConst.ConsumeThreadNums] = threads
        this[PropertyKeyConst.ConsumeTimeout] = timeout
        // 顺序消息消费失败进行重试前的等待时间，单位(毫秒)
        this[PropertyKeyConst.SuspendTimeMillis] = DEFAULT_ORDERED_MESSAGE_SUSPEND_TIME_MILLIS
        // 消息消费失败时的最大重试次数
        this[PropertyKeyConst.MaxReconsumeTimes] = DEFAULT_MAX_RECONSUMER_TIMES
      }

  companion object {
    private const val DEFAULT_ORDERED_MESSAGE_SUSPEND_TIME_MILLIS = 500
    private const val DEFAULT_MAX_RECONSUMER_TIMES = 5

    var producerSendRetries = -1
    var isPrintLog = false
  }

}

enum class MqProducerType {
  /**
   * 无序消息
   */
  NORMAL,
  /**
   * 顺序消息
   */
  ORDERED,
  /**
   * 事务消息
   */
  TRANSACTIONAL
}

class WengerAliyunMqException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}