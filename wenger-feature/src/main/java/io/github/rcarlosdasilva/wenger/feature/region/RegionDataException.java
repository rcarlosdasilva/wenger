package io.github.rcarlosdasilva.wenger.feature.region;

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException;

public class RegionDataException extends WengerRuntimeException {

  public RegionDataException() {
  }

  public RegionDataException(String message) {
    super(message);
  }

  public RegionDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public RegionDataException(Throwable cause) {
    super(cause);
  }

  public RegionDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
