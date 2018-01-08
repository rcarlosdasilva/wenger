package io.github.rcarlosdasilva.wenger.feature.aliyun.mq;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class MqException extends WengerRuntimeException {

  public MqException() {
  }

  public MqException(String message) {
    super(message);
  }

  public MqException(String message, Throwable cause) {
    super(message, cause);
  }

  public MqException(Throwable cause) {
    super(cause);
  }

  public MqException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
