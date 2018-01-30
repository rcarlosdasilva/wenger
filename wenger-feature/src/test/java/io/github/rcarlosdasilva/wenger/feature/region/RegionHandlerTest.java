package io.github.rcarlosdasilva.wenger.feature.region;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.rcarlosdasilva.wenger.feature.TestBoostrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Optional;

/**
 * RegionHandler Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/03/2018</pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RegionHandler.class}, properties = "app.misc.region.enable=true", webEnvironment =
    SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestBoostrap.class})
public class RegionHandlerTest {

  @Autowired
  private RegionHandler regionHandler;

  /**
   * Method: toJSONString()
   */
  @Test
  public void testJsonString() {
    String string = regionHandler.json();
    JSONObject obj = JSON.parseObject(string);
    Assert.assertTrue(obj.containsKey("provinces"));
    Assert.assertNotNull(obj.getJSONArray("provinces"));
    Assert.assertEquals("110000", obj.getJSONArray("provinces").getJSONObject(0).getString("code"));
  }

  /**
   * Method: getName(String code)
   */
  @Test
  public void testGetName() {
    Optional<String> name = regionHandler.name("370202");
    Assert.assertTrue(name.isPresent());
    Assert.assertEquals("市南区", name.get());
  }

  /**
   * Method: getCode(String name)
   */
  @Test
  public void testGetCode() {
    Optional<String> code = regionHandler.code("青岛市");
    Assert.assertTrue(code.isPresent());
    Assert.assertEquals("370200", code.get());
  }

  /**
   * Method: getProvinces()
   */
  @Test
  public void testGetProvinces() {
    Collection<Region> provinces = regionHandler.getProvinces();
    Assert.assertNotNull(provinces);
    Assert.assertEquals(34, provinces.size());
  }

  /**
   * Method: getCities(String code)
   */
  @Test
  public void testGetCities() {
    Collection<Region> cities = regionHandler.getCities("370202");
    Assert.assertNotNull(cities);
    Assert.assertEquals(17, cities.size());
    Assert.assertEquals("济南市", cities.iterator().next().getName());
  }

  /**
   * Method: getDistricts(String code)
   */
  @Test
  public void testGetDistricts() {
    Collection<Region> districts = regionHandler.getDistricts("370202");
    Assert.assertNotNull(districts);
    Assert.assertEquals(11, districts.size());
    Assert.assertEquals("370201", districts.iterator().next().getCode());
  }

  /**
   * Method: getLower(String code)
   */
  @Test
  public void testGetLower() {
    Collection<Region> regions = regionHandler.getLower("370200");
    Assert.assertNotNull(regions);
    Assert.assertEquals(11, regions.size());
    Assert.assertEquals("370201", regions.iterator().next().getCode());
  }

  /**
   * Method: getFullRegion(String code)
   */
  @Test
  public void testGetFullRegion() {
    Collection<Region> regions = regionHandler.getFullRegion("370202");
    Assert.assertNotNull(regions);
    Assert.assertEquals(3, regions.size());
    Assert.assertEquals("370000", regions.iterator().next().getCode());
  }

  /**
   * Method: getFullRegionName(String code)
   */
  @Test
  public void testGetFullRegionName() {
    String regions = regionHandler.getFullRegionName("370202", "-");
    Assert.assertNotNull(regions);
    Assert.assertEquals("山东省-青岛市-市南区", regions);
  }

}
