package io.github.rcarlosdasilva.wenger.feature.aliyun.mq

import com.aliyun.openservices.ons.api.Action
import com.aliyun.openservices.ons.api.ConsumeContext
import com.aliyun.openservices.ons.api.Message
import com.aliyun.openservices.ons.api.MessageListener
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext
import com.aliyun.openservices.ons.api.order.MessageOrderListener
import com.aliyun.openservices.ons.api.order.OrderAction
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.AliyunMqHandler.Companion.isPrintLog
import io.github.rcarlosdasilva.wenger.feature.extension.runIf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 阿里云MQ消息监听器包装类，可实现自动订阅配置
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class AbstractConsumer {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  /**
   * 是否为集群订阅模式（默认true）.
   *
   * @return true
   */
  open fun isClusteringSuscribe(): Boolean = true

  /**
   * 是否订阅顺序消息（默认false）.
   *
   * @return false
   */
  open fun isOrdered(): Boolean = false

  /**
   * 设置每条消息消费的最大超时时间，超过设置时间则被视为消费失败，等下次重新投递再次消费。每个业务需要设置一个合理的值，单位（分钟）。默认：15.
   *
   * @return timeout
   */
  open fun timeout(): Int = 15

  /**
   * 消费端线程数，默认32.
   *
   * @return threads
   */
  open fun threads(): Int = 32

  /**
   * 消息主题
   *
   * @return topic
   */
  abstract fun topic(): String

  /**
   * 监听的CID - ConsumerID
   *
   *
   * **为规范CID的命名，只需填写ConsumerID所属的业务标识。无需关心大小写，驼峰式命名会自动转换为下划线分隔形式。例如：paperStaticize，会自动生成
   * MQ_CID_PREFIX_PAPER_STATICIZE**
   *
   * @return CID
   */
  abstract fun consumerId(): String

  /**
   * 需要监听的tag.
   *
   *
   * 无需关心前缀，tag的命名应尽量简短，不区分大小写！！可返回多个需要监听的tag，不要包含特殊字符，例如||
   *
   * @return tags
   */
  abstract fun tags(): List<String>

  /**
   * 消费消息.
   *
   * @param message 消息
   * @param ctxn    普通消息上下文（顺序消息消费监听下为null）
   * @param ctxo    顺序消息上下文（普通消息消费监听下为null）
   * @return [ConsumeResult]
   */
  abstract fun consume(message: Message, ctxn: ConsumeContext?, ctxo: ConsumeOrderContext?): ConsumeResult

  internal fun normalListener(): MessageListener =
      MessageListener { message, context ->
        var startAt = 0L
        isPrintLog.runIf {
          logger.info("[Aliyun:MQ] - 新的消息：MSGID: {}, KEY: {}", message.msgID, message.key)
          startAt = System.nanoTime()
        }

        val result = consume(message, context, null)

        isPrintLog.runIf {
          logger.info("[Aliyun:MQ] - 消息消费结束：MSGID: {}, 用时：{}ns", message.msgID, System.nanoTime() - startAt)
        }
        if (result == ConsumeResult.SUCCESS) Action.CommitMessage else Action.ReconsumeLater
      }

  internal fun orderedListener(): MessageOrderListener =
      MessageOrderListener { message, context ->
        var startAt = 0L
        isPrintLog.runIf {
          logger.info("[Aliyun:MQ] - 新的顺序消息： MSGID: {}, KEY: {}", message.msgID, message.key)
          startAt = System.nanoTime()
        }

        val result = consume(message, null, context)

        isPrintLog.runIf {
          logger.info("[Aliyun:MQ] - 顺序消息消费结束： MSGID: {}, 用时：{}ns", message.msgID, System.nanoTime() - startAt)
        }
        if (result == ConsumeResult.SUCCESS) OrderAction.Success else OrderAction.Suspend
      }

}

enum class ConsumeResult {
  /**
   * 消费成功，继续消费下一条消息
   */
  SUCCESS,
  /**
   * 普通消息时：消费失败，告知服务器稍后再投递这条消息，继续消费其他消息<br></br>
   * 顺序消息时：消费失败，挂起当前队列
   */
  FAILED
}