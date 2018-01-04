package io.github.rcarlosdasilva.wenger.feature.region;

import lombok.Data;

@Data
public class District extends AbstractRegion {

  public District(String code, String name) {
    super(code, name);
  }

}
