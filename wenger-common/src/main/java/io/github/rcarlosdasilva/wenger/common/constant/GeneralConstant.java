package io.github.rcarlosdasilva.wenger.common.constant;

import java.nio.charset.Charset;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class GeneralConstant {

  public static final String DEFAULT_APPLICATION_NAME = "wenger";
  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

  public static final String URL_SEPARATOR = "/";

  private GeneralConstant() {
    throw new IllegalStateException();
  }

}
