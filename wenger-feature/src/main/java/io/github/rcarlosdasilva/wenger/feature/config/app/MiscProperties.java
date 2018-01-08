package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.misc.*;
import lombok.Data;

@Data
public class MiscProperties {

  /**
   * 邮件配置
   */
  private MailProperties mail = new MailProperties();
  /**
   * 地域（国内）配置
   */
  private RegionProperties region = new RegionProperties();
  /**
   * 分布式ID配置
   */
  private SequenceProperties sequence = new SequenceProperties();
  /**
   * 验证码配置
   */
  private CaptchaProperties captcha = new CaptchaProperties();
  /**
   * IP解析配置
   */
  private IpProperties ip = new IpProperties();

}
