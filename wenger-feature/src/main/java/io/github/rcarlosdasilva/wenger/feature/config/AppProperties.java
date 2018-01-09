package io.github.rcarlosdasilva.wenger.feature.config;

import io.github.rcarlosdasilva.wenger.feature.config.app.AliyunProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.MiscProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统统一配置入口
 */
@Data
@ConfigurationProperties(prefix = AppProperties.APP_PREFIX)
public class AppProperties {

  public static final String APP_PREFIX = "app";

  /**
   * 临时文件目录，默认取系统临时目录
   */
  private String tempDir;
  /**
   * 更多配置
   */
  private MiscProperties misc = new MiscProperties();
  /**
   * 阿里功能配置
   */
  private AliyunProperties aliyun = new AliyunProperties();

}
