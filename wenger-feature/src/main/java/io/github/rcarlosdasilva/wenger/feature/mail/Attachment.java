package io.github.rcarlosdasilva.wenger.feature.mail;

import lombok.Value;

import java.io.File;

/**
 * 邮件附件
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Value(staticConstructor = "with")
public class Attachment {

  /**
   * 附件文件名
   */
  private String name;
  /**
   * 文件
   */
  private File file;

}
