package io.github.rcarlosdasilva.wenger.feature.aliyun.mq

import com.aliyun.openservices.ons.api.*
import com.aliyun.openservices.ons.api.order.OrderProducer
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter
import com.aliyun.openservices.ons.api.transaction.TransactionProducer
import io.github.rcarlosdasilva.kits.bean.SerializeHelper
import io.github.rcarlosdasilva.kits.string.Characters
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.AliyunMqHandler.Companion.isPrintLog
import io.github.rcarlosdasilva.wenger.feature.extension.runIf
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * 阿里云MQ消息生产者基本实现
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class AbstractProducer {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  /**
   * 关闭MQ生产者
   */
  internal abstract fun shutdown()

  /**
   * 重试
   *
   * @param executor 执行代码
   * @return Message id
   */
  protected fun send(executor: () -> String): String {
    lateinit var exception: Exception
    for (i in 0..AliyunMqHandler.producerSendRetries) {
      try {
        return executor()
      } catch (ex: WengerAliyunMqException) {
        exception = ex
        isPrintLog.runIf {
          logger.error("[Aliyun:MQ] - 发送消息失败", ex)
        }

        Thread.sleep(1000L)
      }
    }
    throw exception
  }

  /**
   * 为方便调试，如果Key为空，则产生随机标识符，用于日志打印调试使用
   */
  protected fun mark(key: String?): String =
    key ?: run {
      val mark = TextHelper.random(5, Characters.NUMBERS_AND_LETTERS)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送消息：Key为空，不利于调试，将使用随机标识符({})代替，不会影响业务处理", mark)
      }
      mark
    }

}

/**
 * 阿里云常用生产者类型，包括最常使用的同步、异步消息，定时、延时消息
 */
class NormalProducer(private val config: Properties) : AbstractProducer() {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val producer: Producer = ONSFactory.createProducer(config).apply {
    this.start()
    logger.info("[Aliyun:MQ] - 注册无序消息生产者(NORMAL PRODUCER)：PID: {}", config[PropertyKeyConst.ProducerId])
  }

  override fun shutdown() = producer.shutdown()

  /**
   * 发送同步消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发。注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  fun sendSync(topic: String, tag: String, key: String?, body: Any): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送同步消息(SYNC MESSAGE)：Topic: {}, Tag: {}, Key: {}", topic, tag, k)
      }

      val sr = producer.send(Message(topic, tag, k, SerializeHelper.serialize(body)))
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 同步消息：Key: {}, MessageId: {}", k, sr.messageId)
      }
      sr.messageId
    }

  /**
   * 发送异步消息.
   *
   * @param topic    消息主题
   * @param tag      标签
   * @param key      设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发。注意：不设置也不会影响消息正常收发
   * @param body     消息体
   * @param callback [SendCallback]
   * @return 消息ID
   */
  fun sendAsync(topic: String, tag: String, key: String?, body: Any, callback: SendCallback?): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送异步消息(ASYNC MESSAGE)：Topic: {}, Tag: {}, Key: {}", topic, tag, k)
      }
      val message = Message(topic, tag, key, SerializeHelper.serialize(body))
      producer.sendAsync(message, callback)
      message.msgID
    }

  /**
   * 发送定时消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   * 注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @param time  发送时间，时间戳
   * @return 消息ID
   */
  fun sendTiming(topic: String, tag: String, key: String?, body: Any, time: Long): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送定时消息(TIMING MESSAGE)：Topic: {}, Tag: {}, Key: {}, Timing: {}", topic, tag, k, time)
      }

      val sr =
        producer.send(Message(topic, tag, key, SerializeHelper.serialize(body)).apply { this.startDeliverTime = time })
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 定时消息：Key: {}, MessageId: {}", k, sr.messageId)
      }
      sr.messageId
    }

  /**
   * 发送延时消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   * 注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @param time  延时，单位毫秒
   * @return 消息ID
   */
  fun sendDelay(topic: String, tag: String, key: String?, body: Any, time: Long): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送延时消息(TIMING MESSAGE)：Topic: {}, Tag: {}, Key: {}, Timing: {}", topic, tag, k, time)
      }

      val sr = producer.send(
        Message(
          topic,
          tag,
          key,
          SerializeHelper.serialize(body)
        ).apply { this.startDeliverTime = System.currentTimeMillis() + time })
      isPrintLog.run {
        logger.info("[Aliyun:MQ] - 延时消息：Key: {}, MessageId: {}", k, sr.messageId)
      }
      sr.messageId
    }

  /**
   * 发送单向消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   * 注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  fun sendOneway(topic: String, tag: String, key: String?, body: Any): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送单向消息(ONEWAY MESSAGE)：Topic: {}, Tag: {}, Key: {}", topic, tag, k)
      }

      val message = Message(topic, tag, key, SerializeHelper.serialize(body))
      producer.sendOneway(message)

      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 单向消息：Key: {}, MessageId: {}", k, message.msgID)
      }
      message.msgID
    }

}

/**
 * 阿里云顺序生产者类型
 */
class OrderedProducer(private val config: Properties) : AbstractProducer() {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val producer: OrderProducer = ONSFactory.createOrderProducer(config).apply {
    this.start()
    logger.info("[Aliyun:MQ] - 注册有序消息生产者(ORDERED PRODUCER)：PID: {}", config[PropertyKeyConst.ProducerId])
  }

  override fun shutdown() = producer.shutdown()

  /**
   * 发送顺序消息，使用分区顺序消息.
   *
   * @param topic  消息主题
   * @param region 分区顺序消息中区分不同分区的关键字段
   * @param tag    标签
   * @param key    设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   * 注意：不设置也不会影响消息正常收发
   * @param body   消息体
   * @return 消息ID
   */
  fun sendOrder(topic: String, region: String, tag: String, key: String?, body: Any): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info(
          "[Aliyun:MQ] - 发送顺序消息(ORDERED MESSAGE)：Topic: {}, Region: {}, Tag: {}, Key: {}",
          topic,
          region,
          tag,
          k
        )
      }

      val sr = producer.send(Message(topic, tag, key, SerializeHelper.serialize(body)), region)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 同步消息：Key: {}, MessageId: {}", k, sr.messageId)
      }
      sr.messageId
    }

}

class TransactionalProducer(
  private val config: Properties,
  checker: LocalTransactionChecker?
) : AbstractProducer() {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  init {
    checker ?: throw WengerAliyunMqException("[Aliyun:MQ] - 事务消息生产者必须提供一个LocalTransactionChecker")
  }

  private val producer: TransactionProducer = ONSFactory.createTransactionProducer(config, checker).apply {
    this.start()
    logger.info("[Aliyun:MQ] - 注册事务消息生产者(TRANSACTION PRODUCER)：PID: {}", config[PropertyKeyConst.ProducerId])
  }

  override fun shutdown() = producer.shutdown()

  /**
   * 发送事务消息.
   *
   * @param topic    消息主题
   * @param tag      标签
   * @param key      设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   * 注意：不设置也不会影响消息正常收发
   * @param body     消息体
   * @param executer 本地事务执行器
   * @param param    附加参数
   * @return 消息ID
   */
  fun sendTransaction(
    topic: String,
    tag: String,
    key: String?,
    body: Any,
    executer: LocalTransactionExecuter,
    param: Any
  ): String =
    send {
      val k = mark(key)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 发送事务消息(TRANSACTION MESSAGE)：Topic: {}, Tag: {}, Key: {}", topic, tag, k)
      }

      val sr = producer.send(Message(topic, tag, key, SerializeHelper.serialize(body)), executer, param)
      isPrintLog.runIf {
        logger.info("[Aliyun:MQ] - 事务消息：Key: {}, MessageId: {}", k, sr.messageId)
      }
      sr.messageId
    }

}