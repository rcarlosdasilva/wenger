package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.List;

/**
 * GreenContent Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/10/2018</pre>
 */
public class GreenContentTest {

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

  /**
   * Method: ofText(String content)
   */
  @Test
  public void testOfTextContent() {
    String text = "abcdefghijklmnopqrstuvwxyz";
    List<GreenContent> contents = GreenContent.ofText(null, text);
    Assert.assertNotNull(contents);
    Assert.assertEquals(1, contents.size());
  }


} 
