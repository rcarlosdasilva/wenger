package io.github.rcarlosdasilva.wenger.feature.config.app.misc

import io.github.rcarlosdasilva.wenger.feature.captcha.cache.CaptchaCacheType
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.AbstractCaptchaQa
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.SimpleTextDuplicateCaptchaQa
import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.misc.captcha")
class CaptchaProperties : AbleProperties() {

  /**
   * 验证码缓存类型，默认CAFFEINE
   */
  var cache: CaptchaCacheType = CaptchaCacheType.CAFFEINE
  /**
   * 验证码问题/答案对（QA）实现类，默认字母数字简单验证
   */
  var qa: Class<out AbstractCaptchaQa> = SimpleTextDuplicateCaptchaQa::class.java
  /**
   * 如果需要将验证码信息缓存到Redis，指定key的前缀，不要以冒号开头结尾
   */
  var redisKeyPrefix = "wenger:basic:captcha"
  /**
   * 验证码有效时间（单位：分钟），默认5
   */
  var livetime = 5

}
