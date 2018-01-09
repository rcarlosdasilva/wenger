package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.MqProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.OssProperties;
import lombok.Data;

/**
 * 阿里云功能配置
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
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

}
