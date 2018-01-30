package io.github.rcarlosdasilva.wenger.feature.captcha.cache

import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa
import java.util.*

/**
 * 验证码缓存
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
interface CaptchaCache {

  /**
   * 缓存一个验证码信息.
   *
   * @param key key
   * @param qa  [CaptchaQa]
   */
  fun put(key: String, qa: CaptchaQa)

  /**
   * 获取验证码信息
   *
   * @param key key
   * @return [CaptchaQa]
   */
  fun get(key: String): Optional<CaptchaQa>

  /**
   * 移除.
   *
   * @param key key
   */
  fun remove(key: String)

}
