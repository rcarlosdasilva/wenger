package io.github.rcarlosdasilva.wenger.feature.captcha;

import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * CaptchaHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/05/2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CaptchaHandler.class}, properties = "app.misc.captcha.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {CaptchaCacheAdaptor.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaptchaHandlerTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  public static final String TEST_CAPTCHA_KEY = "key";

  @Autowired
  private CaptchaHandler captchaHandler;

  private static String answer;

  /**
   * Method: justQuestion(String key)
   */
  @Test
  public void test2JustQuestion() {
    String question = captchaHandler.justQuestion(TEST_CAPTCHA_KEY, 10);
    answer = question;
    Assert.assertNotNull(question);
    Assert.assertEquals(10, question.length());
  }

  /**
   * Method: image(String key)
   */
  @Test
  public void test1Image() {
    BufferedImage image = captchaHandler.image(TEST_CAPTCHA_KEY);
    Assert.assertNotNull(image);

    String tempPath = System.getProperty("java.io.tmpdir");
    String fileName = TextHelper.random(5, Characters.NUMBERS_AND_LETTERS);
    File file = new File(tempPath + fileName + ".jpg");
    file.mkdirs();
    try {
      ImageIO.write(image, "jpg", file);
      Assert.assertTrue(file.exists());
      Assert.assertTrue(file.length() > 0);
      logger.info("输出地址：{}", file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Method: validate(String key, String answer)
   */
  @Test
  public void test3Validate() {
    boolean sucess = captchaHandler.validate(TEST_CAPTCHA_KEY, answer);
    Assert.assertTrue(sucess);
  }

}
