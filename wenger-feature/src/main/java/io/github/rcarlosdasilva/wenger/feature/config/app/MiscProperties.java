package io.github.rcarlosdasilva.wenger.feature.config.app;

import io.github.rcarlosdasilva.wenger.feature.config.app.misc.MailProperties;
import lombok.Data;

@Data
public class MiscProperties {

  /**
   * 邮件配置
   */
  private MailProperties mail;

}
