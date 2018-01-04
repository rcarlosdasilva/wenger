package io.github.rcarlosdasilva.wenger.feature.mail;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 依赖Spring Boot Mail的邮件发送工具类
 *
 * @author Dean Zhao (rcarlosdasilva@qq.com)
 */
@Slf4j
@ConditionalOnProperty(name = "app.misc.mail.enable", havingValue = "true")
@Component
public class MailHandler implements SmartInitializingSingleton {

  @Autowired(required = false)
  private JavaMailSender javaMailSender;
  private boolean usable = false;

  @Override
  public void afterSingletonsInstantiated() {
    if (javaMailSender == null) {
      log.error("[邮件] - 功能开启，但未找到(JavaMailSender)，请确认已导入(spring-boot-starter-mail)");
    } else {
      usable = true;
    }
  }

  /**
   * 发送邮件
   *
   * @param mail {@link Mail}
   * @throws MailSendException 发送邮件时任何异常
   */
  public void send(@NonNull Mail mail) {
    if (!usable || !validate(mail)) {
      return;
    }

    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper;
    try {
      helper = new MimeMessageHelper(message, true);
      helper.setFrom(mail.getSender());
      for (String receiver : mail.getReceivers()) {
        helper.addTo(receiver);
      }
      helper.setSubject(mail.getSubject());
      helper.setText(mail.getContent(), mail.isHtml());

      if (mail.getCcs() != null) {
        for (String cc : mail.getCcs()) {
          helper.addCc(cc);
        }
      }
      if (mail.getBccs() != null) {
        for (String bcc : mail.getBccs()) {
          helper.addBcc(bcc);
        }
      }
      if (mail.getAttachments() != null) {
        for (Attachment attachment : mail.getAttachments()) {
          helper.addAttachment(attachment.getName(), attachment.getFile());
        }
      }

      javaMailSender.send(message);
    } catch (MessagingException | MailException ex) {
      log.error("[邮件] - 具体配置异常");
      throw new MailSendException(ex);
    }
  }

  private boolean validate(Mail mail) {
    if (Strings.isNullOrEmpty(mail.getSender())) {
      log.warn("[邮件] - 没有发件人");
    } else if (mail.getReceivers() == null || mail.getReceivers().isEmpty()) {
      log.warn("[邮件] - 没有收件人");
    } else if (Strings.isNullOrEmpty(mail.getSubject())) {
      log.warn("[邮件] - 没有邮件标题");
    } else if (Strings.isNullOrEmpty(mail.getContent())) {
      log.warn("[邮件] - 没有邮件内容");
    } else {
      return true;
    }
    return false;
  }

}
