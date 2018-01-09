package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun;

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class OssProperties extends AbleProperties {

  /**
   * OSS地址，默认外网地址
   */
  private String address = "https://oss-cn-shanghai.aliyuncs.com";
  /**
   * OSS Bucket
   */
  private String bucket;
  /**
   * 默认OSS文件缓存时间（Cache-Control: max-age），单位：秒，默认2,592,000秒（30天）
   */
  private long maxAge = 2592000L;

}
