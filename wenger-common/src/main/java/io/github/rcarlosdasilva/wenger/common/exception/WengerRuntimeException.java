package io.github.rcarlosdasilva.wenger.common.exception;

public class WengerRuntimeException extends RuntimeException {

  public WengerRuntimeException() {
  }

  public WengerRuntimeException(String message) {
    super(message);
  }

  public WengerRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public WengerRuntimeException(Throwable cause) {
    super(cause);
  }

  public WengerRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
