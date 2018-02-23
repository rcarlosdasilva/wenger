package io.github.rcarlosdasilva.wenger.feature.captcha.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.cache.CacheBuilder
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.common.constant.RedisConstant
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa
import org.springframework.data.redis.core.RedisTemplate
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache
import com.google.common.cache.Cache as GuavaCache

enum class CaptchaCacheType {
  /**
   * Caffeine cache
   */
  CAFFEINE,
  /**
   * Guava cache
   */
  GUAVA,
  /**
   * RedisConstant with Spring RedisTemplate
   */
  REDIS_SPRING
}

/**
 * 使用Caffeine本地缓存
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class CaffeineCaptchaCache(livetime: Int) : CaptchaCache {

  private val cache: CaffeineCache<String, CaptchaQa> =
    Caffeine.newBuilder().expireAfterWrite(livetime.toLong(), TimeUnit.MINUTES).build()

  override fun put(key: String, qa: CaptchaQa) = cache.put(key, qa)

  override fun get(key: String): Optional<CaptchaQa> = Optional.ofNullable(cache.getIfPresent(key))

  override fun remove(key: String) = cache.invalidate(key)

}

/**
 * 使用Guava本地缓存
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class GuavaCaptchaCache(livetime: Int) : CaptchaCache {

  private val cache: GuavaCache<String, CaptchaQa> =
    CacheBuilder.newBuilder().expireAfterWrite(livetime.toLong(), TimeUnit.MINUTES).build()

  override fun put(key: String, qa: CaptchaQa) = cache.put(key, qa)

  override operator fun get(key: String): Optional<CaptchaQa> = Optional.ofNullable(cache.getIfPresent(key))

  override fun remove(key: String) = cache.invalidate(key)

}

/**
 * 使用Spring中配置好的Redis缓存，可支持集群
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class SpringRedisCaptchaCache(
  private val redisTemplate: RedisTemplate<Any, Any>,
  private val redisKeyPrefix: String,
  private val livetime: Int
) : CaptchaCache {

  override fun put(key: String, qa: CaptchaQa) =
    redisTemplate.opsForValue().set(key(key), qa, livetime.toLong(), TimeUnit.MINUTES)

  override operator fun get(key: String): Optional<CaptchaQa> =
    Optional.ofNullable(redisTemplate.opsForValue().get(key(key)) as CaptchaQa)

  override fun remove(key: String) {
    redisTemplate.delete(key(key))
  }

  private fun key(mark: String): String =
    TextHelper.concat(
      RedisConstant.DEFAULT_KEY_SEPARATOR,
      redisKeyPrefix,
      TextHelper.trim(mark, RedisConstant.DEFAULT_KEY_SEPARATOR)
    )

}