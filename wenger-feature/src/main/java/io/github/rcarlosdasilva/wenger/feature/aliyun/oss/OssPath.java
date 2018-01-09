package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;


import io.github.rcarlosdasilva.kits.io.FileSignatures;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant;
import io.github.rcarlosdasilva.wenger.feature.context.RuntimeProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

/**
 * OSS 存储路径描述及构建
 * <p>
 * 路径由当前运行环境、目录、文件名、扩展名四个部分组成
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Setter(AccessLevel.PACKAGE)
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class OssPath {

  private String project;
  private RuntimeProfile profile;
  private String module;
  private String category;
  private String fileName;
  private Extension extension;

  /**
   * 生成OSS路径.
   *
   * @return 路径
   */
  public String get() {
    String path = TextHelper.join(GeneralConstant.URL_SEPARATOR, project, profile.toString().toLowerCase(),
        module, category, fileName);
    return TextHelper.concat(path, ".", extension.toString().toLowerCase());
  }

  /**
   * 支持的扩展名
   *
   * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
   */
  public enum Extension {
    /**
     * HTML
     */
    HTML,
    /**
     * JPG
     */
    JPG,
    /**
     * PNG
     */
    PNG,
    /**
     * GIF
     */
    GIF,
    /**
     * BMP
     */
    BMP,
    /**
     * MP3
     */
    MP3,
    /**
     * WAV
     */
    WAV,
    /**
     * MP4
     */
    MP4,
    /**
     * AVI
     */
    AVI,
    /**
     * FLV
     */
    FLV,
    /**
     * WMV
     */
    WMV,
    /**
     * PROP
     */
    PROP,
    /**
     * JSON
     */
    JSON,
    /**
     * XML
     */
    XML,
    /**
     * TXT
     */
    TXT;

    public static Extension get(FileSignatures signatures) {
      return Stream.of(Extension.values()).filter(ext -> signatures.is(ext.toString())).findFirst().orElse(null);
    }
  }

}

