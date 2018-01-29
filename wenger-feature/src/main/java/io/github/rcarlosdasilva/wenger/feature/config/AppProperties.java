package io.github.rcarlosdasilva.wenger.feature.config;

import io.github.rcarlosdasilva.wenger.feature.config.app.AliyunProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.MiscProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统统一配置入口
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

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

  public String getTempDir() {
    return tempDir;
  }

  public void setTempDir(String tempDir) {
    this.tempDir = tempDir;
  }

  public MiscProperties getMisc() {
    return misc;
  }

  public void setMisc(MiscProperties misc) {
    this.misc = misc;
  }

  public AliyunProperties getAliyun() {
    return aliyun;
  }

  public void setAliyun(AliyunProperties aliyun) {
    this.aliyun = aliyun;
  }
}
