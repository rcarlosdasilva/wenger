package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class OssException extends WengerRuntimeException {

  public OssException() {
  }

  public OssException(String message) {
    super(message);
  }

  public OssException(String message, Throwable cause) {
    super(message, cause);
  }

  public OssException(Throwable cause) {
    super(cause);
  }

  public OssException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
