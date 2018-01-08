package io.github.rcarlosdasilva.wenger.feature.ip;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import io.github.rcarlosdasilva.kits.net.AddressHelper;
import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.kits.sys.SystemHelper;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;

/**
 * IP解析
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 * @date Create at 2018/1/5 11:49
 */
@Slf4j
@ConditionalOnProperty(name = "app.misc.ip.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class IpHandler implements SmartInitializingSingleton {

  private static final String DATA_BLOCK_REGION_SEPARATOR = "\\|";

  @Autowired
  private AppProperties appProperties;
  private DbSearcher searcher;
  private IpSearchArithmetic arithmetic;
  private boolean usable = false;

  @Override
  public void afterSingletonsInstantiated() {
    String dataLocation = appProperties.getMisc().getIp().getLocation();
    arithmetic = appProperties.getMisc().getIp().getArithmetic();

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
      throw new IpDataException("[IP] - 无法找到IP解析数据文件，请确保app.misc.ip.location配置的文件地址存在");
    }

    String tempFilePath = SystemHelper.tempDir() + TextHelper.random(5, Characters.NUMBERS_AND_LETTERS) + ".db";
    File tempFile = new File(tempFilePath);
    // 转文件
    try (InputStream is = resource.getInputStream();
         OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile))
    ) {
      ByteStreams.copy(is, os);
    } catch (IOException ex) {
      throw new IpDataException("[IP] - 数据文件无法读取", ex);
    }

    try {
      searcher = new DbSearcher(new DbConfig(), tempFilePath);
      usable = true;
    } catch (DbMakerConfigException | FileNotFoundException ex) {
      throw new IpDataException("[IP] - 初始化ip2region DbSearcher失败", ex);
    }
  }

  /**
   * 解析IP详细信息
   */
  public IpDetail detail(String ip) {
    if (!usable || Strings.isNullOrEmpty(ip)) {
      return null;
    }

    if (!AddressHelper.isLegalIpv4(ip)) {
      log.warn("[IP] - 不规范的IP地址：{}", ip);
      return null;
    }

    DataBlock data = null;
    try {
      switch (arithmetic) {
        case BINARY:
          data = searcher.binarySearch(ip);
          break;
        case B_TREE:
          data = searcher.btreeSearch(ip);
          break;
        case MEMORY:
          data = searcher.memorySearch(ip);
          break;
        default:
      }
    } catch (IOException ex) {
      throw new IpDataException("[IP] - 搜索IP详情异常");
    }

    return convert(ip, data);
  }

  private IpDetail convert(String ip, DataBlock dataBlock) {
    if (dataBlock == null) {
      return null;
    }
    if (StringUtils.isBlank(dataBlock.getRegion())) {
      log.warn("[IP] - 搜索不到IP的详情：{}", ip);
    }

    IpDetail detail = new IpDetail(ip);
    String[] parts = dataBlock.getRegion().split(DATA_BLOCK_REGION_SEPARATOR);
    detail.setCountry(parts[0]);
    detail.setArea(parts[1]);
    detail.setProvince(parts[2]);
    detail.setCity(parts[3]);
    detail.setIsp(parts[4]);
    return detail;
  }

}
