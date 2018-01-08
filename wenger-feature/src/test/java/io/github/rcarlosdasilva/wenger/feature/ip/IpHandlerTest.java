package io.github.rcarlosdasilva.wenger.feature.ip;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * IpHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/08/2018</pre>
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {IpHandler.class}, properties = "app.misc.ip.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
public class IpHandlerTest {

  @Autowired
  private IpHandler ipHandler;

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

  /**
   * Method: detail(String ip)
   */
  @Test
  public void testDetail() {
    IpDetail detail = ipHandler.detail("27.221.49.46");
    Assert.assertNotNull(detail);
    Assert.assertNotNull(detail.getCountry());

    log.info(":{}:{}:{}:{}:{}:{}", detail.getIp(), detail.getCountry(), detail.getArea(), detail.getProvince(),
        detail.getCity(), detail.getIsp());
  }


} 
