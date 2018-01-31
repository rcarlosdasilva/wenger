package io.github.rcarlosdasilva.wenger.feature.config.app

import io.github.rcarlosdasilva.wenger.feature.config.app.misc.*
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 系统杂项功能配置
 */
@ConfigurationProperties(prefix = "app.misc")
class MiscProperties {

  /**
   * 邮件配置
   */
  var mail = MailProperties()
  /**
   * 地域（暂时仅支持国内）配置
   */
  var region = RegionProperties()
  /**
   * 分布式ID配置
   */
  var sequence = SequenceProperties()
  /**
   * 验证码配置
   */
  var captcha = CaptchaProperties()
  /**
   * IP解析配置
   */
  var ip = IpProperties()

}
