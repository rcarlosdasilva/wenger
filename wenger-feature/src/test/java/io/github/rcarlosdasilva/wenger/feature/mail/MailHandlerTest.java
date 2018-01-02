package io.github.rcarlosdasilva.wenger.feature.mail;

import io.github.rcarlosdasilva.wenger.feature.TestBoostrap;
import org.assertj.core.util.Lists;
import org.junit.*;
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
@SpringBootTest(classes = {MailHandler.class}, properties = "app.misc.mail.enable=true", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = TestBoostrap.class)
public class MailHandlerTest {

  @Autowired(required = false)
  private MailHandler mailHandler;

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }

  /**
   * Method: send(@NonNull Mail mail)
   */
  @Test
  public void testSend1() throws Exception {
    Mail mail = new Mail("rcarlosdasilva@qq.com", Lists.newArrayList("zhaochangsheng@yingxinhuitong.com"), "Wenger " +
        "Mail Test",
        "通过MailHandler发送");
    mailHandler.send(mail);
    Assert.assertTrue(true);
  }

  @Ignore
  @Test
  public void testSend2() throws Exception {
    Mail mail = new Mail("rcarlosdasilva@qq.com", Lists.newArrayList("zhaochangsheng@yingxinhuitong.com"), "Wenger " +
        "Mail Test",
        "通过MailHandler发送带附件的邮件");
    mail.addAttachment(Attachment.with("图片文件.ext", new File("C:\\Users\\Dean\\Pictures\\gogs.png")));
    mailHandler.send(mail);
    Assert.assertTrue(true);
  }

}
