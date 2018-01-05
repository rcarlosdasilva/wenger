package io.github.rcarlosdasilva.wenger.feature.captcha.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;

import java.util.concurrent.TimeUnit;

/**
 * 使用Guava本地缓存
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class GuavaCaptchaCache implements CaptchaCache {

  private final Cache<String, CaptchaQa> cache;

  public GuavaCaptchaCache(int livetime) {
    cache = CacheBuilder.newBuilder().expireAfterWrite(livetime, TimeUnit.MINUTES).build();
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
