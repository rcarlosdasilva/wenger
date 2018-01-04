package io.github.rcarlosdasilva.wenger.feature.region;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地区数据解析器
 *
 * @author Dean Zhao (rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = "app.misc.region.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class RegionHandler implements SmartInitializingSingleton {

  @Autowired
  private AppProperties appProperties;
  private final China china = new China();
  private BiMap<String, String> mapping;
  private String json;

  @Override
  public void afterSingletonsInstantiated() {
    String dataLocation = appProperties.getMisc().getRegion().getLocation();
    // 默认jar包内数据文件
    Resource resource = new ClassPathResource(dataLocation);

    // 尝试URL读取
    if (!resource.exists()) {
      try {
        resource = new UrlResource(dataLocation);
      } catch (MalformedURLException ex) {
        resource = null;
      }
    }

    // 尝试本地文件读取
    if (resource == null || !resource.exists()) {
      resource = new FileSystemResource(dataLocation);
    }

    // 无法读取
    if (!resource.exists()) {
      throw new RegionDataException("[地域] - 无法找到地域数据文件，请确保app.misc.china.location配置的文件地址存在");
    }

    Properties properties;
    try (InputStream is = resource.getInputStream();
         InputStreamReader isr = new InputStreamReader(is)) {
      properties = new Properties();
      properties.load(isr);
    } catch (IOException ex) {
      throw new RegionDataException("[地域] - 文件读取失败", ex);
    }

    // 解析
    BiMap<String, String> temMapping = HashBiMap.create();
    List<String> keys = new ArrayList<>(properties.stringPropertyNames());
    keys = ImmutableList.copyOf(Ordering.natural().sortedCopy(keys));
    Iterator<String> ki = keys.iterator();

    List<Province> temProvinces = Lists.newArrayList();
    Multimap<String, City> temCities = LinkedListMultimap.create();
    Multimap<String, District> temDistricts = LinkedListMultimap.create();

    // 归类
    while (ki.hasNext()) {
      String code = ki.next();
      String name = properties.getProperty(code);
      temMapping.forcePut(code, name);

      if (isProvince(code)) {
        temProvinces.add(new Province(code, name));
      } else if (isCity(code)) {
        temCities.put(fragmentProvince(code), new City(code, name));
      } else {
        temDistricts.put(fragmentCity(code), new District(code, name));
      }
    }

    // 连接省市区
    Map<String, Province> provinces = Maps.newLinkedHashMap();
    temProvinces.forEach(province -> {
      Collection<City> cc = temCities.get(province.getCode());
      Map<String, City> cities = Maps.newLinkedHashMap();

      cc.forEach(city -> {
        Collection<District> dc = temDistricts.get(city.getCode());
        Map<String, District> districts = dc.stream().collect(Collectors.toMap(District::getCode, district ->
            district, (ek, nk) -> ek, LinkedHashMap::new));

        city.setDistricts(ImmutableSortedMap.copyOf(districts));
        cities.put(city.getCode(), city);
      });

      province.setCities(ImmutableSortedMap.copyOf(cities));
      provinces.put(province.getCode(), province);
    });

    china.setProvinces(ImmutableSortedMap.copyOf(provinces));
    mapping = ImmutableBiMap.copyOf(temMapping);
  }

  private boolean isProvince(String code) {
    return code != null && code.matches(RegionConst.DEFAULT_PROVINCE_PATTERN);
  }

  private boolean isCity(String code) {
    return code != null && code.matches(RegionConst.DEFAULT_CITY_PATTERN);
  }

  private String fragmentProvince(String code) {
    return TextHelper.concat(TextHelper.sub(code, 0, 2), RegionConst.PROVINCE_SUFFIX);
  }

  private String fragmentCity(String code) {
    return TextHelper.concat(TextHelper.sub(code, 0, 4), RegionConst.CITY_SUFFIX);
  }

  /**
   * 将地区信息转成字符串并返回
   */
  public String toJSONString() {
    if (Strings.isNullOrEmpty(json)) {
      JSONObject root = new JSONObject();
      JSONArray jps = new JSONArray();
      root.put("provinces", jps);

      china.getProvinces().values().forEach(province -> {
        JSONObject jp = new JSONObject();
        JSONArray jcs = new JSONArray();
        jp.put("code", province.getCode());
        jp.put("name", province.getName());
        jp.put("cities", jcs);

        province.getCities().values().forEach(city -> {
          JSONObject jc = new JSONObject();
          JSONArray jds = new JSONArray();
          jc.put("code", city.getCode());
          jc.put("name", city.getName());
          jc.put("districts", jds);

          city.getDistricts().values().forEach(district -> {
            JSONObject jd = new JSONObject();
            jd.put("code", district.getCode());
            jd.put("name", district.getName());

            jds.add(jd);
          });

          jcs.add(jc);
        });

        jps.add(jp);
      });

      json = root.toJSONString();
    }
    return json;
  }

  /**
   * 根据code获取地区名
   *
   * @param code code
   * @return name
   */
  public Optional<String> getName(String code) {
    return code == null ? Optional.empty() : Optional.ofNullable(mapping.get(code));
  }

  /**
   * 根据地区名获取code，可能为null
   * <p>因为每个市下都有一个市辖区，所以不保证获取市辖区的code正确</p>
   *
   * @param name name
   * @return code
   */
  public Optional<String> getCode(String name) {
    return name == null ? Optional.empty() : Optional.ofNullable(mapping.inverse().get(name));
  }

  /**
   * 获取所有省
   *
   * @return 省
   */
  public Collection<Province> getProvinces() {
    return china.getProvinces().values();
  }

  /**
   * 根据code（不一定非要是省code，也可以是市或区code），获取所有市
   *
   * @param code code
   * @return 市
   */
  public Collection<City> getCities(String code) {
    Province province = china.getProvinces().get(fragmentProvince(code));
    if (province == null) {
      return Collections.emptyList();
    }

    return province.getCities().values();
  }

  /**
   * 根据code（市或区code），获取所有行政区
   *
   * @param code code
   * @return 行政区
   */
  public Collection<District> getDistricts(String code) {
    Province province = china.getProvinces().get(fragmentProvince(code));
    if (province == null) {
      return Collections.emptyList();
    }

    City city = province.getCities().get(fragmentCity(code));
    if (city == null) {
      return Collections.emptyList();
    }

    return city.getDistricts().values();
  }

  /**
   * 根据code，模糊取出code下所有省/市/区（code为null时返回所有省），如果code是行政区，返回null
   *
   * @param code code
   * @return data
   */
  public Collection<? extends AbstractRegion> getAmbiguousRegions(String code) {
    if (Strings.isNullOrEmpty(code)) {
      return getProvinces();
    } else if (isProvince(code)) {
      return getCities(code);
    } else if (isCity(code)) {
      return getDistricts(code);
    }

    return Collections.emptyList();
  }

  /**
   * 根据code，返回当前及上级所有地区名，例如：传入市的code，返回省与市
   *
   * @param code code
   * @return regions
   */
  public Collection<AbstractRegion> getFullRegion(String code) {
    List<AbstractRegion> result = Lists.newArrayList();

    Province province = china.getProvinces().get(fragmentProvince(code));
    if (province != null) {
      result.add(province);

      City city = province.getCities().get(fragmentCity(code));
      if (city != null) {
        result.add(city);

        District district = city.getDistricts().get(code);
        if (district != null) {
          result.add(district);
        }
      }
    }

    return result;
  }

  /**
   * 根据code，返回当前及上级所有的地区名称
   *
   * @param code code
   * @return names
   */
  public String getFullRegionName(String code) {
    return getFullRegion(code).stream().map(AbstractRegion::getName).collect(Collectors.joining());
  }

}
