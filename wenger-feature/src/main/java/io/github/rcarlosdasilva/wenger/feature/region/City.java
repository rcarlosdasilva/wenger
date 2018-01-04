package io.github.rcarlosdasilva.wenger.feature.region;

import lombok.*;

import java.util.Map;

@Data
public class City extends AbstractRegion {

  @Setter(value = AccessLevel.PACKAGE)
  private Map<String, District> districts;

  City(String code, String name) {
    super(code, name);
  }

}
