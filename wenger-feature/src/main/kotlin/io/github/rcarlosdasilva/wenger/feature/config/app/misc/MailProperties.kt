package io.github.rcarlosdasilva.wenger.feature.config.app.misc

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.misc.mail")
class MailProperties : AbleProperties() {

  /**
   * 发件人地址，当发件人邮件地址与SMTP认证用户名相同时，可为null
   */
  var sender: String? = null

}
