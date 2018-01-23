package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class GreenException extends WengerRuntimeException {

  public GreenException() {
  }

  public GreenException(String message) {
    super(message);
  }

  public GreenException(String message, Throwable cause) {
    super(message, cause);
  }

  public GreenException(Throwable cause) {
    super(cause);
  }

  public GreenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
