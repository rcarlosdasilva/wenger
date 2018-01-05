package io.github.rcarlosdasilva.wenger.feature.captcha.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;

import java.util.concurrent.TimeUnit;

/**
 * 使用Caffeine本地缓存
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class CaffeineCaptchaCache implements CaptchaCache {

  private final Cache<String, CaptchaQa> cache;

  public CaffeineCaptchaCache(int livetime) {
    cache = Caffeine.newBuilder().expireAfterWrite(livetime, TimeUnit.MINUTES).build();
  }

  @Override
  public void put(String key, CaptchaQa qa) {
    cache.put(key, qa);
  }

  @Override
  public CaptchaQa get(String key) {
    return cache.getIfPresent(key);
  }

  @Override
  public void remove(String key) {
    cache.invalidate(key);
  }

}
