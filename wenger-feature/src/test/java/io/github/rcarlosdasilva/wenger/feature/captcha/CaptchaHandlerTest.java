package io.github.rcarlosdasilva.wenger.feature.captcha;

import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CaptchaHandler.class}, properties = "app.misc.captcha.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {CaptchaCacheManager.class})
public class CaptchaHandlerTest {

  public static final String TEST_CAPTCHA_KEY = "key";

  @Autowired
  private CaptchaHandler captchaHandler;

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

  /**
   * Method: justQuestion(String key)
   */
  @Test
  public void testJustQuestion() {
    String question = captchaHandler.justQuestion(TEST_CAPTCHA_KEY, 10);
    Assert.assertNotNull(question);
    Assert.assertEquals(10, question.length());
  }

  /**
   * Method: image(String key)
   */
  @Test
  public void testImage() {
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
      log.info(file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Method: validate(String key, String answer)
   */
  @Ignore
  @Test
  public void testValidate() {
    // can not test yet
  }

} 
