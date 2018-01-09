package io.github.rcarlosdasilva.wenger.feature.aliyun.oss;

import com.google.common.base.Preconditions;
import io.github.rcarlosdasilva.kits.io.FileHelper;
import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.feature.context.EnvironmentHandler.EnvironmentHandlerHolder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class OssObject implements AutoCloseable {

  @NonNull
  @Getter
  private OssPath path;
  private Object object;
  /**
   * OSS文件的Cache-Control: max-age，单位：秒
   */
  @Getter
  @Setter
  private long maxAge;
  private ObjectType type;
  /**
   * 是否为简单上传，为false时，使用断点续传，适用于大文件，或网络环境差的情况
   */
  @Getter
  private boolean simpleUpload = true;

  /**
   * 断点续传（分片）执行的线程数（默认5）.
   */
  @Getter
  @Setter
  private int threads = 5;
  /**
   * 断点续传（分片）的分片大小（默认200k）.
   * <p>
   * 阿里封装SDK中默认100k，如果传值小于100k，则取默认值
   */
  @Getter
  @Setter
  private long sizeOfPart = 204800;

  private InputStream stream;
  @Getter
  private int streamSize;
  private File file;

  /**
   * 上传文本信息.
   *
   * @param text 文本
   */
  public void setObject(String text) {
    this.object = text;
    this.type = ObjectType.TEXT;
  }

  /**
   * 上传文件（本地）.
   *
   * @param file 文件
   */
  public void setObject(File file) {
    this.object = file;
    this.type = ObjectType.FILE;
  }

  /**
   * 上传网络资源
   *
   * @param link 网络资源
   */
  public void setObject(URL link) {
    this.object = link;
    this.type = ObjectType.URL;
  }

  /**
   * 上传输入流信息
   *
   * @param is 输入流
   */
  public void setObject(InputStream is) {
    this.object = is;
    this.type = ObjectType.STREAM;
  }

  /**
   * 使用断点续传.
   * <p>
   * 适用于大文件，或网络环境差的情况
   */
  public void useBreakpointUpload() {
    this.simpleUpload = false;
  }

  /**
   * 将上传内容转换成输入流形式（markSupported input stream）.
   *
   * @return {@link InputStream}
   * @throws OssException {@link OssException}流处理异常
   */
  InputStream toInputStream() {
    Preconditions.checkNotNull(this.object);

    if (this.stream != null) {
      return this.stream;
    }

    try {
      switch (this.type) {
        case TEXT:
          String text = (String) this.object;
          this.stream = new ByteArrayInputStream(text.getBytes(Charset.forName("UTF-8")));
          break;
        case URL:
          URL link = (URL) this.object;
          URLConnection connection = link.openConnection();
          connection.setConnectTimeout(2000);
          connection.setReadTimeout(2000);
          InputStream isOfUrl = connection.getInputStream();
          byte[] bytes = StreamUtils.copyToByteArray(isOfUrl);
          isOfUrl.close();
          this.stream = new ByteArrayInputStream(bytes);
          break;
        case FILE:
          File temp = (File) this.object;
          if (!temp.exists() || !temp.isFile()) {
            throw new OssException("[Aliyun:OSS] - 文件不存在或不是文件：" + temp.getAbsoluteFile());
          }

          // 这里的FileInputStream流会利用BufferedInputStream关闭
          this.stream = new BufferedInputStream(new FileInputStream(temp));
          break;
        case STREAM:
          InputStream is = (InputStream) this.object;
          this.stream = is.markSupported() ? is : new BufferedInputStream(is);
          break;
        default:
          return null;
      }

      this.streamSize = this.stream.available();
    } catch (IOException ex) {
      throw new OssException("[Aliyun:OSS] - 上传内容读取输入流异常", ex);
    }

    return this.stream;
  }

  /**
   * 将上传内容转换成本地文件形式.
   *
   * @return {@link File}
   * @throws OssException {@link OssException}流处理异常
   */
  File toLocalFile() {
    Preconditions.checkNotNull(this.object);

    if (this.file != null) {
      return this.file;
    }

    if (this.type != ObjectType.FILE) {
      String filename = TextHelper.random(16, Characters.NUMBERS_AND_LETTERS);
      String filePath = TextHelper.concat(EnvironmentHandlerHolder.get().getTempDir(), filename, "" +
          ".", this.path.getExtension()
          .toString().toLowerCase());
      this.file = new File(filePath);
    } else {
      this.file = (File) this.object;
    }

    switch (this.type) {
      case TEXT:
        String text = (String) this.object;
        try (FileWriter fileWriter = new FileWriter(this.file)) {
          fileWriter.write(text);
        } catch (Exception ex) {
          throw new OssException("[Aliyun:OSS] - 文本字符串写文件失败", ex);
        }
        break;
      case URL:
        URL link = (URL) this.object;
        if (!FileHelper.copy(link, this.file)) {
          throw new OssException("[Aliyun:OSS] - 读取网络资源失败：" + link.toString());
        }
        break;
      case FILE:
        if (!file.exists() || !file.isFile()) {
          throw new OssException("[Aliyun:OSS] - 读取文件失败" + file.getAbsolutePath());
        }
        break;
      case STREAM:
        try (InputStream is = (InputStream) this.object;
             OutputStream os = new FileOutputStream(this.file)) {
          byte[] flush = new byte[1024];
          int len = 0;
          while (0 <= (len = is.read(flush))) {
            os.write(flush, 0, len);
          }
        } catch (Exception ex) {
          throw new OssException("[Aliyun:OSS] - 输入流写文件失败", ex);
        }
        break;
      default:
    }

    return this.file;
  }

  /**
   * 关闭流.
   * <p>
   * 如果上传内容，本身从外部传入的就是输入流，则不关闭
   *
   * @throws OssException {@link OssException}流处理异常
   */
  void closeStream() {
    if (this.stream == null) {
      return;
    }

    InputStream is = this.stream;
    this.stream = null;
    if (this.type != ObjectType.STREAM) {
      try {
        is.close();
      } catch (IOException ex) {
        throw new OssException("[Aliyun:OSS] - 关闭流失败", ex);
      }
    }
  }

  /**
   * 移除文件.
   *
   * @throws OssException {@link OssException}流处理异常
   */
  void removeFile() {
    if (this.file == null) {
      return;
    }

    File f = this.file;
    this.file = null;
    try {
      Files.delete(f.toPath());
    } catch (IOException | SecurityException ex) {
      throw new OssException("[Aliyun:OSS] - 临时文件删除异常", ex);
    }
  }

  @Override
  public void close() {
    closeStream();
    removeFile();
  }

  enum ObjectType {

    /**
     * 文本内容
     */
    TEXT,
    /**
     * 本地文件
     */
    FILE,
    /**
     * URL
     */
    URL,
    /**
     * 输入流
     */
    STREAM

  }

}
