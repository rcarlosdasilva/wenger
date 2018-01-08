package io.github.rcarlosdasilva.wenger.feature.config.app.misc;

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import io.github.rcarlosdasilva.wenger.feature.ip.IpSearchArithmetic;
import lombok.Data;

@Data
public class IpProperties extends AbleProperties {

  /**
   * IP地址解析，数据文件地址
   * <p>使用lionsoul2014的ip2region实现，https://github.com/lionsoul2014/ip2region</p>
   * <p>默认为从resources下读取。因为IP数据经常变更，建议将文件位置指定为URL或本地文件，这样可以动态加载无需重启服务</p>
   */
  private String location = "ip2region.db";
  /**
   * ip2region算法，默认使用内存算法
   */
  private IpSearchArithmetic arithmetic = IpSearchArithmetic.MEMORY;

}
