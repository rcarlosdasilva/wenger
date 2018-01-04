package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.misc.MailProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.RegionProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.SequenceProperties;
import lombok.Data;

@Data
public class MiscProperties {

  /**
   * 邮件配置
   */
  private MailProperties mail = new MailProperties();
  /**
   * 地域（国内）配置
   */
  private RegionProperties region = new RegionProperties();
  /**
   * 分布式ID配置
   */
  private SequenceProperties sequence = new SequenceProperties();

}
