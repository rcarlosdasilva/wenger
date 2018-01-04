package io.github.rcarlosdasilva.wenger.feature.region;

import lombok.*;

@Data
public abstract class AbstractRegion {

  @NonNull
  private String code;
  @NonNull
  private String name;

}
