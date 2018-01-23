package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import io.github.rcarlosdasilva.wenger.feature.aliyun.green.term.TextScene;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * GreenHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/10/2018</pre>
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GreenHandler.class}, properties = "app.aliyun.green.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
public class GreenHandlerTest {

  @Autowired
  private GreenHandler greenHandler;

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

  /**
   * Method: text(List<GreenContent> contents, TextScene scene)
   */
  @Test
  public void testText() {
    List<GreenResult> result = greenHandler.text(GreenContent.ofText(null, "以日欧文破方面"), TextScene.ANTISPAM);
    Assert.assertNotNull(result);
    System.out.println(result);
  }


} 
