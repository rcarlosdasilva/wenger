package io.github.rcarlosdasilva.wenger.feature.mail;

import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;

/**
 * 邮件详细信息Bean
 * <p>
 * 默认为html格式邮件
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Mail {

  @NonNull
  private String sender;
  @NonNull
  private List<String> receivers;
  @NonNull
  private String subject;
  @NonNull
  private String content;

  @Setter
  private boolean html = true;
  @Setter
  private List<String> ccs;
  @Setter
  private List<String> bccs;
  @Setter
  private List<Attachment> attachments;

  public void addCc(String cc) {
    if (this.ccs == null) {
      this.ccs = Lists.newArrayList();
    }
    this.ccs.add(cc);
  }

  public void addBcc(String bcc) {
    if (this.bccs == null) {
      this.bccs = Lists.newArrayList();
    }
    this.bccs.add(bcc);
  }

  public void addAttachment(Attachment attachment) {
    if (this.attachments == null) {
      this.attachments = Lists.newArrayList();
    }
    this.attachments.add(attachment);
  }

}
