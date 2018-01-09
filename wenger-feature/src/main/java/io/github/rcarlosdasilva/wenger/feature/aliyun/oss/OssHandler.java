package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import io.github.rcarlosdasilva.kits.io.FileHelper;
import io.github.rcarlosdasilva.kits.io.FileSignatures;
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.aliyun.oss.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class OssHandler implements SmartInitializingSingleton, DisposableBean {

  @Autowired
  private AppProperties appProperties;
  private OSSClient client;
  private String bucket;

  @Override
  public void afterSingletonsInstantiated() {
    this.bucket = appProperties.getAliyun().getOss().getBucket();
    if (Strings.isNullOrEmpty(bucket)) {
      throw new OssException("[Aliyun:OSS] - 未配置OSS Bucket Name");
    }

    ClientConfiguration conf = new ClientConfiguration();
    conf.setConnectionTimeout(30000);
    conf.setRequestTimeoutEnabled(true);
    conf.setMaxErrorRetry(5);

    client = new OSSClient(appProperties.getAliyun().getOss().getAddress(), appProperties.getAliyun().getAccessId(),
        appProperties.getAliyun().getAccessSecret(), conf);

    log.info("[Aliyun:OSS] - 初始化完毕");
  }

  @Override
  public void destroy() {
    if (client != null) {
      client.shutdown();
    }
  }

  /**
   * 下载文件.
   *
   * @param path     {@link OssPath}
   * @param filePath 保存本地文件路径
   */
  public void download(OssPath path, String filePath) {
    Preconditions.checkNotNull(path);
    download(path.get(), filePath);
  }

  /**
   * 下载文件.
   *
   * @param path     OSS端文件路径，可使用 {@link OssPath#get()}方法生成，也可提前将路径保存到数据库
   * @param filePath 保存本地文件路径
   */
  public void download(String path, String filePath) {
    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(filePath);

    try {
      client.getObject(new GetObjectRequest(bucket, path),
          new File(filePath));
    } catch (OSSException | ClientException ex) {
      throw new OssException("[Aliyun:OSS] - 下载文件失败", ex);
    }
  }

  /**
   * 导出{@link OSSObject}。
   *
   * @param path {@link OssPath}
   * @return 请求结果{@link OSSObject}实例。使用完之后需要手动关闭其中的ObjectContent释放请求连接
   */
  public OSSObject getObject(OssPath path) {
    Preconditions.checkNotNull(path);
    try {
      return client.getObject(bucket, path.get());
    } catch (OSSException | ClientException ex) {
      throw new OssException("[Aliyun:OSS] - 下载文件失败", ex);
    }
  }

  /**
   * 上传文件.
   *
   * @param object {@link OssObject}
   * @return {@link OssResult}
   */
  public OssResult upload(OssObject object) {
    Preconditions.checkNotNull(object);

    ObjectMetadata meta = generateMeta(object);
    String key = object.getPath().get();

    if (object.isSimpleUpload()) {
      return uploadFileWithSimple(key, object.toInputStream(), meta);
    } else {
      return uploadFileWithBreakpoint(key, object.toLocalFile(), meta,
          object.getThreads(), object.getSizeOfPart());
    }
  }

  private OssResult uploadFileWithSimple(String key, InputStream is, ObjectMetadata meta) {
    try {
      PutObjectResult result = client.putObject(bucket, key, is, meta);
      return new OssResult(result.getRequestId(), result.getETag(), key);
    } catch (OSSException ex) {
      log.error("[Aliyun:OSS] - 服务器端异常，Error Message: {}, Error Code: {}, Request ID: {}, Host ID: {}",
          ex.getErrorMessage(), ex.getErrorCode(), ex.getRequestId(), ex.getHostId());
    } catch (ClientException ex) {
      log.error("[Aliyun:OSS] - 客户端异常，Error Message: {}, Error Code: {}, Request ID: {}",
          ex.getErrorMessage(), ex.getErrorCode(), ex.getRequestId());
    }
    return OssResult.FAILED;
  }

  private OssResult uploadFileWithBreakpoint(String key, File file, ObjectMetadata meta, int threadNumber,
                                             long sizeOfPart) {
    UploadFileRequest request = new UploadFileRequest(bucket, key);
    request.setUploadFile(file.getAbsolutePath());
    request.setObjectMetadata(meta);
    request.setTaskNum(threadNumber);
    request.setPartSize(sizeOfPart);
    request.setEnableCheckpoint(true);

    try {
      UploadFileResult uploadResult = client.uploadFile(request);
      CompleteMultipartUploadResult cmResult = uploadResult.getMultipartUploadResult();
      return new OssResult(cmResult.getRequestId(), cmResult.getETag(), key);
    } catch (Throwable ex) {
      log.error("[Aliyun:OSS] - 断点续传异常", ex);
    }
    return OssResult.FAILED;
  }

  /**
   * 生成meta元信息
   *
   * @param object {@link OssObject}
   * @return {@link ObjectMetadata}
   */
  private ObjectMetadata generateMeta(OssObject object) {
    InputStream is = object.toInputStream();
    OssPath.Extension realExtension = identifyRealType(is);
    if (realExtension == null) {
      realExtension = object.getPath().getExtension();
    }

    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentEncoding(GeneralConstant.DEFAULT_ENCODING);
    meta.setCacheControl("max-age: " + object.getMaxAge());
    meta.setLastModified(DateTime.now().toDate());
    meta.setContentType(mimetype(realExtension));

    md5(meta, is, object.getStreamSize());

    return meta;
  }

  private String mimetype(OssPath.Extension extension) {
    switch (extension) {
      case HTML:
        return MimeTypeUtils.TEXT_HTML_VALUE;
      case JPG:
        return MimeTypeUtils.IMAGE_JPEG_VALUE;
      case PNG:
        return MimeTypeUtils.IMAGE_PNG_VALUE;
      case GIF:
        return MimeTypeUtils.IMAGE_GIF_VALUE;
      case BMP:
        return OssConstant.IMAGE_BMP_VALUE;
      case MP3:
        return OssConstant.AUDIO_MP3_VALUE;
      case WAV:
        return OssConstant.AUDIO_WAV_VALUE;
      case MP4:
        return OssConstant.VIDEO_MP4_VALUE;
      case AVI:
        return OssConstant.VIDEO_AVI_VALUE;
      case FLV:
        return OssConstant.VIDEO_FLV_VALUE;
      case WMV:
        return OssConstant.VIDEO_WMV_VALUE;
      case JSON:
        return MimeTypeUtils.APPLICATION_JSON_VALUE;
      case XML:
        return MimeTypeUtils.APPLICATION_XML_VALUE;
      default:
        return MimeTypeUtils.TEXT_PLAIN_VALUE;
    }
  }

  /**
   * 对内容做MD5签名
   *
   * @param meta             原信息
   * @param is               输入流
   * @param realStreamLength 真实的流长度（如果InputStream是从URL读出，以Connection的content-length为准）
   */
  @SuppressWarnings("unused")
  private void md5(ObjectMetadata meta, InputStream is, int realStreamLength) {
    byte[] content = null;
    try {
      is.mark(realStreamLength + 1);
      content = ByteStreams.toByteArray(is);
      is.reset();
    } catch (IOException ex) {
      throw new OssException("[Aliyun:OSS] - 读取内容失败", ex);
    }

    final String md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(content));
    meta.setContentMD5(md5);
    meta.setContentLength(content.length);
  }

  /**
   * 从流中识别文件真实类型.
   *
   * @param is 流
   * @return {@link OssPath.Extension}
   */
  @SuppressWarnings("unused")
  private OssPath.Extension identifyRealType(InputStream is) {
    FileSignatures signature = FileHelper.type(is);
    if (signature == null) {
      log.warn("[Aliyun:OSS] - 无法识别正确的文件类型");
      return null;
    }
    OssPath.Extension ext = OssPath.Extension.get(signature);
    if (ext == null) {
      log.warn("[Aliyun:OSS] - 文件头中未匹配到已知的类型，将使用指定的文件类型");
    }
    return ext;
  }

}
