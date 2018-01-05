package io.github.rcarlosdasilva.wenger.feature.config.app.misc;

import io.github.rcarlosdasilva.wenger.feature.captcha.cache.CaptchaCacheType;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.SimpleTextDuplicateCaptchaQa;
import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

@Data
public class CaptchaProperties extends AbleProperties {

  /**
   * 验证码缓存类型，默认CAFFEINE
   */
  private CaptchaCacheType cache = CaptchaCacheType.CAFFEINE;
  /**
   * 验证码问题/答案对（QA）实现类，默认字母数字简单验证
   */
  private Class<? extends CaptchaQa> qa = SimpleTextDuplicateCaptchaQa.class;
  /**
   * 如果需要将验证码信息缓存到Redis，指定key的前缀，不要以冒号开头结尾
   */
  private String redisKeyPrefix = "basic:captcha";
  /**
   * 验证码有效时间（单位分钟），默认5
   */
  private int livetime = 5;

}
