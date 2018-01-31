package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.GreenProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.MqProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.OssProperties;

/**
 * 阿里云功能配置
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class AliyunProperties {

  /**
   * 阿里access key id
   */
  private String accessId;
  /**
   * 阿里access key secret
   */
  private String accessSecret;
  /**
   * 消息队列配置
   */
  private MqProperties mq = new MqProperties();
  /**
   * 文件存储配置
   */
  private OssProperties oss = new OssProperties();
  /**
   * 内容安全配置
   */
  private GreenProperties green = new GreenProperties();

  public String getAccessId() {
    return accessId;
  }

  public void setAccessId(String accessId) {
    this.accessId = accessId;
  }

  public String getAccessSecret() {
    return accessSecret;
  }

  public void setAccessSecret(String accessSecret) {
    this.accessSecret = accessSecret;
  }

  public MqProperties getMq() {
    return mq;
  }

  public void setMq(MqProperties mq) {
    this.mq = mq;
  }

  public OssProperties getOss() {
    return oss;
  }

  public void setOss(OssProperties oss) {
    this.oss = oss;
  }

  public GreenProperties getGreen() {
    return green;
  }

  public void setGreen(GreenProperties green) {
    this.green = green;
  }
}
