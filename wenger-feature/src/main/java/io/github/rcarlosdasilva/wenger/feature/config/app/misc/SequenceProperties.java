package io.github.rcarlosdasilva.wenger.feature.config.app.misc;

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

@Data
public class SequenceProperties extends AbleProperties {

  /**
   * 数据中心ID (取值范围0~31)，默认0
   */
  private long dataCenterId = 0;
  /**
   * 工作ID (取值范围0~31)，默认0
   */
  private long workerId = 0;

}
