package io.github.rcarlosdasilva.wenger.feature.captcha.cache;

public enum CaptchaCacheType {

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
