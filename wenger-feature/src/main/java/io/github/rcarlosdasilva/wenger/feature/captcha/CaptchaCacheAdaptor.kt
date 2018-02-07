package io.github.rcarlosdasilva.wenger.feature.captcha

import io.github.rcarlosdasilva.wenger.feature.captcha.cache.*
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.CaptchaProperties
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * 验证码缓存管理器
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.misc.captcha.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [CaptchaProperties::class])
class CaptchaCacheAdaptor @Autowired constructor(
    @Autowired(required = false) private val redisTemplate: RedisTemplate<Any, Any>?,
    private val captchaProperties: CaptchaProperties
) : SmartInitializingSingleton {

  private lateinit var captchaCache: CaptchaCache

  override fun afterSingletonsInstantiated() {
    // 后期完善，可在各个build方法中对缓存做进一步配置
    with(captchaProperties) {
      captchaCache = when (this.cache) {
        CaptchaCacheType.CAFFEINE -> buildCaffeineCache(this.livetime)
        CaptchaCacheType.GUAVA -> buildGuavaCache(this.livetime)
        CaptchaCacheType.REDIS_SPRING -> buildRedisCacheFromSpring(this.redisKeyPrefix, this.livetime)
      }
    }
  }

  fun put(key: String, qa: CaptchaQa) = captchaCache.put(key, qa)

  fun get(key: String): CaptchaQa? = captchaCache.get(key).orElse(null)

  fun remove(key: String) = captchaCache.remove(key)

  private fun buildCaffeineCache(livetime: Int) = CaffeineCaptchaCache(livetime)

  private fun buildGuavaCache(livetime: Int) = GuavaCaptchaCache(livetime)

  private fun buildRedisCacheFromSpring(redisKeyPrefix: String, livetime: Int) = SpringRedisCaptchaCache(redisTemplate!!, redisKeyPrefix, livetime)

}
