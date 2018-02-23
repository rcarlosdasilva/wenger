package io.github.rcarlosdasilva.wenger.feature.aliyun.mns

import com.aliyun.mns.client.CloudAccount
import com.aliyun.mns.client.CloudQueue
import com.aliyun.mns.client.CloudTopic
import com.aliyun.mns.client.MNSClient
import com.aliyun.mns.common.ClientException
import com.aliyun.mns.common.ServiceException
import com.aliyun.mns.model.Base64TopicMessage
import com.aliyun.mns.model.Message
import com.aliyun.mns.model.Message.MessageBodyType.BASE64
import com.aliyun.mns.model.Message.MessageBodyType.RAW_STRING
import com.aliyun.mns.model.RawTopicMessage
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.config.app.AliyunProperties
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.MnsProperties
import io.github.rcarlosdasilva.wenger.feature.extension.applyIf
import io.github.rcarlosdasilva.wenger.feature.extension.runIf
import mu.KotlinLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.thread


/**
 * 阿里云MNS功能封装
 *
 * 阿里云消息服务(Message Service，简称 MNS)是一种高效、可靠、安全、便捷、可弹性扩展的分布式消息服务，与阿里云的消息队列是两个产品
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.aliyun.mns.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AliyunProperties::class])
class AliyunMnsHandler @Autowired constructor(
  @Autowired(required = false) private val queueReceivers: List<AliyunMnsQueueReceiver>?,
  private val aliyunProperties: AliyunProperties
) : SmartInitializingSingleton, DisposableBean {

  private val logger = KotlinLogging.logger {}

  private lateinit var client: MNSClient
  private lateinit var queues: Map<String, CloudQueue>
  private lateinit var topics: Map<String, CloudTopic>

  override fun afterSingletonsInstantiated() {
    with(aliyunProperties) {
      val account = CloudAccount(accessId, accessSecret, mns.endpoint)
      client = account.mnsClient

      configQueue(mns.queues)
      configTopic(mns.topics)
    }

    Companion.queueReceivers = queueReceivers
  }

  override fun destroy() = client.close()

  private fun configQueue(queues: List<MnsProperties.QueueProperties>?) {
    this.queues = if (queues == null) {
      logger.info { "[Aliyun:MNS] - 未配置任何队列(QUEUE)" }
      mapOf()
    } else {
      queues.mapNotNull { prop ->
        client.getQueueRef(prop.name).applyIf(prop.receive) {
          logger.info { "[Aliyun:MNS] - 开启新的线程用于队列(${prop.name})接收消息" }
          thread(start = true, isDaemon = true, name = "$QUEUE_THREAD_NAME${prop.name}") {
            QueueReceiver(this, prop.queueReceiveSize, prop.pollingTimeout).start()
          }
        }.also { logger.info { "[Aliyun:MNS] - 队列(${prop.name})已经配置完毕" } }
      }.associateBy { it.attributes.queueName }
    }
  }

  private fun configTopic(topics: List<String>?) {
    this.topics = if (topics == null) {
      logger.info { "[Aliyun:MNS] - 未配置任何主题(TOPIC)" }
      mapOf()
    } else {
      topics.mapNotNull {
        client.getTopicRef(it).also { logger.info { "[Aliyun:MNS] - 主题($it)已经配置完毕" } }
      }.associateBy { it.attribute.topicName }
    }
  }

// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ functions ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

  /**
   * 推送队列消息
   *
   * @param queueName 推送队列
   * @param content 消息内容
   * @param isRawContent 明文发送信息，为false时，使用Base64发送内容，默认false
   * @param delay 延迟消息，单位：秒，该属性会覆盖队列的配置。默认：null 阿里云控制台创建队列时指定的消息延时（DelaySeconds）为准。
   * @param priority 优先级，取值：1-16，默认8
   * @return 消息id(message id)，在一个队列中唯一，如有多个队列，不能单独作为查询的唯一标记
   */
  @JvmOverloads
  fun pushQueue(
    queueName: String,
    content: String,
    isRawContent: Boolean? = false,
    delay: Int? = null,
    priority: Int? = 8
  ): String {
    val queue = queues[queueName] ?: run {
      logger.warn { "[Aliyun:MNS] - 找不到队列$queueName，请检查配置" }
      return ""
    }

    val ct = if (isRawContent == true) RAW_STRING else BASE64
    return try {
      queue.putMessage(Message().apply {
        this.setMessageBody(content, ct)
        this.delaySeconds = delay
        this.priority = priority
      }).messageId
    } catch (ex: Exception) {
      when (ex) {
        is ClientException -> throw WengerAliyunMnsException("[Aliyun:MNS] - 和MNS服务器之间的网络连接出现问题", ex)
        is ServiceException -> throw WengerAliyunMnsException("[Aliyun:MNS] - 请求:${ex.requestId}异常", ex)
        else -> throw WengerAliyunMnsException("[Aliyun:MNS] - 未知异常(Queue)")
      }
    }
  }

  /**
   * 发布主题消息
   *
   * @param topicName 主题
   * @param content 消息内容
   * @param tag 消息标签
   * @param isRawContent 明文发送信息，为false时，使用Base64发送内容，默认false
   * @return 消息id(topic id)，在一个主题中唯一，如有多个主题，不能单独作为查询的唯一标记
   */
  @JvmOverloads
  fun publishTopic(
    topicName: String,
    content: String,
    tag: String? = null,
    isRawContent: Boolean? = false
  ): String {
    val topic = topics[topicName] ?: run {
      logger.warn { "[Aliyun:MNS] - 找不到主题$topicName，请检查配置" }
      return ""
    }

    return try {
      val tm = if (isRawContent == true) RawTopicMessage() else Base64TopicMessage()
      topic.publishMessage(tm.apply {
        this.messageBody = content
        this.messageTag = tag
      }).messageId
    } catch (ex: Exception) {
      when (ex) {
        is ClientException -> throw WengerAliyunMnsException("[Aliyun:MNS] - 和MNS服务器之间的网络连接出现问题", ex)
        is ServiceException -> throw WengerAliyunMnsException("[Aliyun:MNS] - 请求:${ex.requestId}异常", ex)
        else -> throw WengerAliyunMnsException("[Aliyun:MNS] - 未知异常(Topic)")
      }
    }
  }

  companion object {
    private const val QUEUE_THREAD_NAME = "aliyun-mns-queue-receiver-"

    internal var queueReceivers: List<AliyunMnsQueueReceiver>? = null
  }

  internal class QueueReceiver(
    private val queue: CloudQueue,
    private val wait: Int,
    private val size: Int
  ) {
    private val logger = KotlinLogging.logger {}
    private val queueName = queue.attributes.queueName

    fun start() {
      loop@ while (true) {
        val messages: List<Message>? = try {
          queue.batchPopMessage(size, wait)
        } catch (ex: Exception) {
          when (ex) {
            is ClientException -> logger.error { "[Aliyun:MNS] - 和MNS服务器之间的网络连接出现问题，Exception: $ex" }
            is ServiceException -> {
              if (ex.errorCode == "MessageNotExist") {
                logger.trace { "[Aliyun:MNS] - 当前无活跃的消息" }
                continue@loop
              }
              logger.error { "[Aliyun:MNS] - 消息:${ex.requestId}异常，Exception: $ex" }
            }
            else -> logger.error { "[Aliyun:MNS] - 未知异常" }
          }
          null
        }

        messages?.forEach { msg ->
          queueReceivers?.forEach { rcv ->
            runIf(rcv.support(queueName, msg.priority, msg.enqueueTime, msg.dequeueCount)) {
              logger.debug { "[Aliyun:MNS] - 接收到新的消息: (queue) $queueName, (message id) ${msg.messageId}" }
              rcv.process(QueueMessageWrapper(msg, queue))
            }
          }
        }
      }
    }

  }

  /**
   * 队列消息装饰类，提供便捷的消息删除与修改下次可见时间的方法
   *
   * 如果队列消息来自主题订阅途径，请使用[QueueMessageWrapper.getMessageBody]或[QueueMessageWrapper.getMessageBodyAsString]获取未经过Base64转换的字符串，再获取“Message”节点，此值才是主题发布消息时的真正消息内容
   */
  class QueueMessageWrapper(
    private val target: Message,
    private val queue: CloudQueue
  ) {
    fun getRequestId() = target.requestId
    fun getMessageId() = target.messageId
    fun getMessageBodyMD5() = target.messageBodyMD5
    fun getMessageBody() = target.messageBody
    fun getMessageBodyAsRawString() = target.messageBodyAsRawString
    fun getMessageBodyAsString() = target.messageBodyAsString
    fun getReceiptHandle() = target.receiptHandle
    fun getPriority() = target.priority
    fun getEnqueueTime() = target.enqueueTime
    fun getNextVisibleTime() = target.nextVisibleTime
    fun getFirstDequeueTime() = target.firstDequeueTime
    fun getDequeueCount() = target.dequeueCount
    fun getDelaySeconds() = target.delaySeconds
    fun getErrorMessage() = target.errorMessage
    /**
     * 删除当前消息
     */
    fun delete() = queue.deleteMessage(target.receiptHandle)

    /**
     * 修改当前消息的下次可见时间
     *
     * @param timeout 消息不可见时间，单位：秒
     */
    fun changeVisibility(timeout: Int): Boolean =
      try {
        queue.changeMessageVisibilityTimeout(target.receiptHandle, timeout) != null
      } catch (ex: Exception) {
        false
      }
  }

}

/**
 * 阿里云MNS下，自动接收队列模式下的消息
 *
 * 实现该接口，并注册Spring Bean，有队列消息时，[AliyunMnsQueueReceiver.process]方法会被调用
 */
interface AliyunMnsQueueReceiver {
  /**
   * 是否支持处理该消息，返回false，将不会调用process方法
   *
   * @param queue 队列名称
   * @param priority 消息优先级
   * @param enqueueTime 消息发送到队列的时间
   * @param dequeueCount 消息总共被消费的次数(即被receive的次数)
   * @return 支持该消息？
   */
  fun support(queue: String, priority: Int, enqueueTime: Date, dequeueCount: Int): Boolean

  /**
   * 处理消息
   *
   * @param message 消息[AliyunMnsHandler.QueueMessageWrapper]
   */
  fun process(message: AliyunMnsHandler.QueueMessageWrapper)
}

class WengerAliyunMnsException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(
    message: String?,
    cause: Throwable?,
    enableSuppression: Boolean,
    writableStackTrace: Boolean
  ) : super(
    message,
    cause,
    enableSuppression,
    writableStackTrace
  )
}

// TODO 支持事务队列
// TODO 支持一个队列开启多线程（高并发量）