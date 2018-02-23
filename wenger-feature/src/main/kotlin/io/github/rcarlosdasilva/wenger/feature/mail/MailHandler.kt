package io.github.rcarlosdasilva.wenger.feature.mail

import com.google.common.base.Strings
import com.google.common.collect.Lists
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.io.File
import javax.mail.MessagingException
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.MailProperties as WengerMailProperties

/**
 * 依赖Spring Boot Mail的邮件发送工具类
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.misc.mail.enable"], havingValue = "true")
@ConditionalOnClass(value = [JavaMailSender::class])
@Component
@EnableConfigurationProperties(value = [WengerMailProperties::class, MailProperties::class])
class MailHandler @Autowired constructor(
  private val wmailProperties: WengerMailProperties,
  private val mailProperties: MailProperties,
  private val javaMailSender: JavaMailSender
) {

  private val logger = KotlinLogging.logger {}

  private fun Mail.validate() {
    val msg = when {
      Strings.isNullOrEmpty(this.subject) -> "[邮件] - 没有邮件标题"
      Strings.isNullOrEmpty(this.content) -> "[邮件] - 没有邮件内容"
      else -> return
    }
    throw WengerMailException(msg)
  }

  /**
   * 发送邮件
   *
   * @param mail [Mail]
   * @throws WengerMailException 发送邮件时任何异常
   */
  fun send(mail: Mail, receivers: List<String>) {
    mail.validate()

    val message = javaMailSender.createMimeMessage()
    try {
      val helper = MimeMessageHelper(message, true)
      val sender = wmailProperties.sender ?: mailProperties.username
      helper.setFrom(sender)
      receivers.forEach(helper::addTo)
      helper.setSubject(mail.subject)
      helper.setText(mail.content, mail.isHtml)

      mail.ccs?.forEach(helper::addCc)
      mail.bccs?.forEach(helper::addBcc)
      mail.attachments?.forEach { helper.addAttachment(it.name, it.file) }

      logger.debug { "[邮件] - Sending：${mail.subject}" }
      javaMailSender.send(message)
    } catch (ex: MessagingException) {
      throw WengerMailException("[邮件] - 具体配置异常", ex)
    } catch (ex: MailException) {
      throw WengerMailException("[邮件] - 具体配置异常", ex)
    }
  }

}

/**
 * 邮件详细信息Bean，默认为html格式邮件
 *
 * @param subject 邮件主题
 * @param content 邮件主体内容
 */
data class Mail(
  val subject: String,
  val content: String
) {

  /**
   * 邮件内容是否为HTML格式
   */
  var isHtml: Boolean = true
  /**
   * 抄送列表
   */
  var ccs: MutableList<String>? = null
  /**
   * 密送列表
   */
  var bccs: MutableList<String>? = null
  /**
   * 附件列表
   */
  var attachments: MutableList<Attachment>? = null

  fun addCc(cc: String) {
    ccs?.add(cc) ?: let { ccs = Lists.newArrayList(cc) }
  }

  fun addBcc(bcc: String) {
    bccs?.add(bcc) ?: let { bccs = mutableListOf(bcc) }
  }

  fun addAttachment(att: Attachment) {
    attachments?.add(att) ?: let { attachments = mutableListOf(att) }
  }

  fun addAttachments(atts: List<Attachment>) {
    attachments?.addAll(atts) ?: let { attachments = atts.toMutableList() }
  }

  fun addAttachment(name: String, file: File) {
    addAttachment(Attachment(name, file))
  }

}

/**
 * 邮件附件
 *
 * @param name 文件名
 * @param file 文件
 */
data class Attachment(
  val name: String,
  val file: File
)

class WengerMailException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
    message,
    cause,
    enableSuppression,
    writableStackTrace
  )
}

// TODO 发件人昵称设定
// TODO 定时发送或设置发送时间
// TODO 支持Reply邮件格式