package io.github.rcarlosdasilva.wenger.feature.ip;

import lombok.Data;
import lombok.NonNull;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class IpDetail {

  @NonNull
  private String ip;
  private String country;
  private String area;
  private String province;
  private String city;
  private String isp;

}
