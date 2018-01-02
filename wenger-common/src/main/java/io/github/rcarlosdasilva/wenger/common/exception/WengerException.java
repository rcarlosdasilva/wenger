package io.github.rcarlosdasilva.wenger.common.exception;

public class WengerException extends Exception {

  public WengerException() {
    super();
  }

  public WengerException(String message) {
    super(message);
  }

  public WengerException(String message, Throwable cause) {
    super(message, cause);
  }

  public WengerException(Throwable cause) {
    super(cause);
  }

  protected WengerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
