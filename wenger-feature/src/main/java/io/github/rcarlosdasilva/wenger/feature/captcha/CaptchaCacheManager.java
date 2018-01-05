package io.github.rcarlosdasilva.wenger.feature.captcha;

import com.google.common.base.Strings;
import io.github.rcarlosdasilva.wenger.feature.captcha.cache.*;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 验证码缓存管理器
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.misc.captcha.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class CaptchaCacheManager implements SmartInitializingSingleton {

  @Autowired
  private AppProperties appProperties;
  @SuppressWarnings("rawtypes")
  @Autowired(required = false)
  private RedisTemplate redisTemplate;
  private CaptchaCache captchaCache;

  @Override
  public void afterSingletonsInstantiated() {
    // 可在各个build方法中对缓存做进一步配置
    CaptchaCacheType cacheType = appProperties.getMisc().getCaptcha().getCache();
    int livetime = appProperties.getMisc().getCaptcha().getLivetime();

    switch (cacheType) {
      case CAFFEINE:
        buildCaffeineCache(livetime);
        break;
      case GUAVA:
        buildGuavaCache(livetime);
        break;
      case REDIS_SPRING:
        buildRedisCacheFromSpring(appProperties.getMisc().getCaptcha().getRedisKeyPrefix(), livetime);
        break;
      default:
    }
  }

  public void put(String key, CaptchaQa qa) {
    if (Strings.isNullOrEmpty(key) || qa == null) {
      log.error("[验证码] - 无法缓存验证码信息：{}, {}", key, qa);
      return;
    }

    captchaCache.put(key, qa);
  }

  public CaptchaQa get(String key) {
    if (Strings.isNullOrEmpty(key)) {
      log.warn("[验证码] - 无法处理Key为空的验证码缓存信息");
      return null;
    }

    return captchaCache.get(key);
  }

  public void remove(String key) {
    if (Strings.isNullOrEmpty(key)) {
      return;
    }

    captchaCache.remove(key);
  }

  private void buildCaffeineCache(int livetime) {
    captchaCache = new CaffeineCaptchaCache(livetime);
  }

  private void buildGuavaCache(int livetime) {
    captchaCache = new GuavaCaptchaCache(livetime);
  }

  private void buildRedisCacheFromSpring(String redisKeyPrefix, int livetime) {
    captchaCache = new SpringRedisCaptchaCache(redisTemplate, redisKeyPrefix, livetime);
  }

}
