package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import com.google.common.base.Strings;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import io.github.rcarlosdasilva.wenger.feature.context.EnvironmentHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里OSS路径构建器
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.aliyun.oss.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class OssBuilder {

  private static final String DEFAULT_PATH_PATTERN = "^[a-zA-Z0-9-]+(/[a-zA-Z0-9-]+)+$";
  private static final String DEFAULT_FILENAME_PATTERN = "^[a-zA-Z0-9-]+$";

  @Autowired
  private AppProperties appProperties;
  @Autowired
  private EnvironmentHandler environmentHandler;

  /**
   * OSS 存放路径
   *
   * @param module    文件分模块存储，只能包含大小写英文字母、数字和斜线(/)，不能以斜线开头和结尾
   * @param category  存放子目录，只能包含大小写英文字母、数字和斜线(/)，不能以斜线开头和结尾
   * @param fileName  文件名，只能包含大小写英文字母、数字
   * @param extension 文件扩展名 {@link OssPath.Extension}
   * @return {@link OssPath}
   */
  public OssObject with(String module, String category, String fileName, OssPath.Extension extension) {
    OssObject ossObject = new OssObject(path(module, category, fileName, extension));
    ossObject.setMaxAge(appProperties.getAliyun().getOss().getMaxAge());
    return ossObject;
  }

  public OssPath path(String module, String category, String fileName, OssPath.Extension extension) {
    OssPath path = new OssPath(environmentHandler.getApplicationName(), environmentHandler.getRuntimeProfile(), module,
        category, fileName, extension);
    validate(path);
    return path;
  }

  private void validate(OssPath path) {
    if (Strings.isNullOrEmpty(path.getModule())) {
      throw new OssException("[Aliyun:OSS] - 未设置文件存放模块");
    }
    if (Strings.isNullOrEmpty(path.getCategory())) {
      throw new OssException("[Aliyun:OSS] - 未设置文件存放子目录");
    }
    if (Strings.isNullOrEmpty(path.getFileName())) {
      throw new OssException("[Aliyun:OSS] - 未设置文件名");
    }
    if (path.getExtension() == null) {
      throw new OssException("[Aliyun:OSS] - 未设置文件扩展名");
    }

    path.setModule(path.getModule().trim());
    path.setCategory(path.getCategory().trim());
    path.setFileName(path.getFileName().trim());

    if (!path.getModule().matches(DEFAULT_PATH_PATTERN)) {
      throw new OssException("[Aliyun:OSS] - 文件存放模块包含非法字符");
    }
    if (!path.getCategory().matches(DEFAULT_PATH_PATTERN)) {
      throw new OssException("[Aliyun:OSS] - 文件存放子目录包含非法字符");
    }
    if (!path.getFileName().matches(DEFAULT_FILENAME_PATTERN)) {
      throw new OssException("[Aliyun:OSS] - 文件名包含非法字符");
    }
  }


}
