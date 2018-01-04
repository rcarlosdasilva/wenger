package io.github.rcarlosdasilva.wenger.feature.region;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Map;

@Data
public class China {

  @Setter(value = AccessLevel.PACKAGE)
  private Map<String, Province> provinces;

}
