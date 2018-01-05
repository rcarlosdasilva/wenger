package io.github.rcarlosdasilva.wenger.feature.captcha.cache;

import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;

/**
 * 验证码缓存
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public interface CaptchaCache {

  /**
   * 缓存一个验证码信息.
   *
   * @param key key
   * @param qa  {@link CaptchaQa}
   */
  void put(String key, CaptchaQa qa);

  /**
   * 获取验证码信息
   *
   * @param key key
   * @return {@link CaptchaQa}
   */
  CaptchaQa get(String key);

  /**
   * 移除.
   *
   * @param key key
   */
  void remove(String key);

}
