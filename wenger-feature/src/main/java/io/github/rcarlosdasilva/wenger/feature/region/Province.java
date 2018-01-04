package io.github.rcarlosdasilva.wenger.feature.region;

import lombok.*;

import java.util.Map;

@Data
public class Province extends AbstractRegion {

  @Setter(value = AccessLevel.PACKAGE)
  private Map<String, City> cities;

  Province(String code, String name) {
    super(code, name);
  }

}
