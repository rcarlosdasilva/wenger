package io.github.rcarlosdasilva.wenger.feature.sequence;

import io.github.rcarlosdasilva.wenger.feature.TestBoostrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * SnowFlakeHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/04/2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SequenceHandler.class}, properties = "app.misc.sequence.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestBoostrap.class})
public class SequenceHandlerTest {

  @Autowired
  private SequenceHandler sequenceHandler;

  /**
   * Method: id()
   */
  @Test
  public void testId() {
    Long id1 = sequenceHandler.id();
    Long id2 = sequenceHandler.id();

    Assert.assertNotNull(id1);
    Assert.assertNotNull(id2);
    Assert.assertNotEquals(id1, id2);
  }


} 
