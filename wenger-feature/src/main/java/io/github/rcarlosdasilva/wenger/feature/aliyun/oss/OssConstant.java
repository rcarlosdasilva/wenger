package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import org.springframework.util.MimeTypeUtils;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
final class OssConstant {

  public static final String IMAGE_BMP_VALUE = MimeTypeUtils.parseMimeType("image/bmp").toString();
  public static final String AUDIO_MP3_VALUE = MimeTypeUtils.parseMimeType("audio/mpeg3").toString();
  public static final String AUDIO_WAV_VALUE = MimeTypeUtils.parseMimeType("audio/wav").toString();
  public static final String VIDEO_MP4_VALUE = MimeTypeUtils.parseMimeType("video/mp4").toString();
  public static final String VIDEO_AVI_VALUE = MimeTypeUtils.parseMimeType("video/avi").toString();
  public static final String VIDEO_FLV_VALUE = MimeTypeUtils.parseMimeType("video/x-flv").toString();
  public static final String VIDEO_WMV_VALUE = MimeTypeUtils.parseMimeType("video/x-ms-wmv").toString();

  private OssConstant() {
    throw new IllegalStateException();
  }

}
