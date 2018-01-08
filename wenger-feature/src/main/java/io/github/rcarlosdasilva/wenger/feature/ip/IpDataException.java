package io.github.rcarlosdasilva.wenger.feature.ip;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

public class IpDataException extends WengerRuntimeException {

  public IpDataException() {
  }

  public IpDataException(String message) {
    super(message);
  }

  public IpDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public IpDataException(Throwable cause) {
    super(cause);
  }

  public IpDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
