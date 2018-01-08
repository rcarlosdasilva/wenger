package io.github.rcarlosdasilva.wenger.feature.config.app.misc;

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

@Data
public class RegionProperties extends AbleProperties {

  /**
   * 地域数据文件位置
   * <p>默认为从resources下读取。如果地域文件经常需要变更，建议将文件位置指定为URL或本地文件，无需重启服务</p>
   */
  private String location = "region.properties";

}
