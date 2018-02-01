package io.github.rcarlosdasilva.wenger.common.constant

import java.nio.charset.Charset

/**
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class GeneralConstant private constructor() {

  companion object {

    const val DEFAULT_APPLICATION_NAME = "wenger"
    const val DEFAULT_ENCODING = "UTF-8"
    val DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING)

    const val URL_SEPARATOR = "/"
  }

}
