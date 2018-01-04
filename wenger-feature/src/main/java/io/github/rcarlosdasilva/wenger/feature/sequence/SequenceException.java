package io.github.rcarlosdasilva.wenger.feature.sequence;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

public class SequenceException extends WengerRuntimeException {

  public SequenceException() {
  }

  public SequenceException(String message) {
    super(message);
  }

  public SequenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public SequenceException(Throwable cause) {
    super(cause);
  }

  public SequenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
