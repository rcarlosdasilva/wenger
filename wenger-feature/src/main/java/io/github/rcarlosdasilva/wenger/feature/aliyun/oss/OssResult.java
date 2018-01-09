package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import lombok.Data;

/**
 * OSS请求结果
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class OssResult {

  public static final OssResult FAILED = new OssResult();

  private boolean success = true;
  private String requestId;
  private String etag;
  private String path;

  private OssResult() {
    this.success = false;
  }

  public OssResult(String requestId, String etag, String path) {
    this.requestId = requestId;
    this.etag = etag;
    this.path = path;
  }

}