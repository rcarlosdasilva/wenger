package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.misc.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统杂项功能配置
 */
@ConfigurationProperties(prefix = "app.misc")
public class MiscProperties {

  /**
   * 邮件配置
   */
  private MailProperties mail = new MailProperties();
  /**
   * 地域（暂时仅支持国内）配置
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

  public MailProperties getMail() {
    return mail;
  }

  public void setMail(MailProperties mail) {
    this.mail = mail;
  }

  public RegionProperties getRegion() {
    return region;
  }

  public void setRegion(RegionProperties region) {
    this.region = region;
  }

  public SequenceProperties getSequence() {
    return sequence;
  }

  public void setSequence(SequenceProperties sequence) {
    this.sequence = sequence;
  }

  public CaptchaProperties getCaptcha() {
    return captcha;
  }

  public void setCaptcha(CaptchaProperties captcha) {
    this.captcha = captcha;
  }

  public IpProperties getIp() {
    return ip;
  }

  public void setIp(IpProperties ip) {
    this.ip = ip;
  }
}
