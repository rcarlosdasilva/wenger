package io.github.rcarlosdasilva.wenger.ms.upms;

import io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity.Authority;
import io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.mapper.AuthorityMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AuthorityMapper.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestBoostrap.class})
public class OrmTest {

  @Autowired
  private AuthorityMapper authorityMapper;

  @Test
  public void normal() {
    Authority authority = new Authority();
    authority.setType("tst");

    int result = authorityMapper.insert(authority);
    Assert.assertEquals(1, result);
  }

}
