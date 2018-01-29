package io.github.rcarlosdasilva.wenger.feature.mail;

import io.github.rcarlosdasilva.wenger.feature.TestBoostrap;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * MailHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12/25/2017</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MailHandler.class}, properties = "app.misc.mail.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestBoostrap.class})
public class MailHandlerTest {

  @Autowired
  private MailHandler mailHandler;

  @Test
  public void testSend1() {
    Mail mail = new Mail("Wenger Mail Test A", "通过MailHandler发送普通邮件");
    mailHandler.send(mail, Lists.newArrayList("zhaochangsheng@yingxinhuitong.com"));
    Assert.assertTrue(true);
  }

  @Test
  public void testSend2() {
    Mail mail = new Mail("Wenger Mail Test B", "通过MailHandler发送带附件的邮件");
    mail.addAttachment(new Attachment("图片文件.ext", new File("C:\\Users\\Dean\\Pictures\\gogs.png")));
    mailHandler.send(mail, Lists.newArrayList("zhaochangsheng@yingxinhuitong.com"));
    Assert.assertTrue(true);
  }

}
