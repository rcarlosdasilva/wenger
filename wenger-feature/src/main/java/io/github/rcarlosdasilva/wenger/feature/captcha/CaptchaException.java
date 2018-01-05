package io.github.rcarlosdasilva.wenger.feature.captcha;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

public class CaptchaException extends WengerRuntimeException {

  public CaptchaException() {
  }

  public CaptchaException(String message) {
    super(message);
  }

  public CaptchaException(String message, Throwable cause) {
    super(message, cause);
  }

  public CaptchaException(Throwable cause) {
    super(cause);
  }

  public CaptchaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}