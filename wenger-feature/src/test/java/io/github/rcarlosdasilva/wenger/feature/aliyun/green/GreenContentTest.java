package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * GreenContent Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/10/2018</pre>
 */
public class GreenContentTest {

  /**
   * Method: ofText(String content)
   */
  @Test
  public void testOfTextContent() {
    String text = "abcdefghijklmnopqrstuvwxyz";
    List<GreenContent> contents = GreenContent.Builder.ofText(null, text);
    Assert.assertNotNull(contents);
    Assert.assertEquals(1, contents.size());
  }


} 
