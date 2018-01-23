package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun;

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class GreenProperties extends AbleProperties {

  /**
   * 内容安全Region，暂时只支持上海
   */
  private String region = "cn-shanghai";
  /**
   * 内容安全地址
   */
  private String address = "green.cn-shanghai.aliyuncs.com";
  /**
   * 使用异步检测（适用于图片与视频），不设置但使用异步检测的话，会获取不到结果，默认false
   */
  private boolean useAsyn = false;
  /**
   * 异步检测间隔时间（异步检测请求发送后间隔多久发送结果查询请求），单位：秒
   */
  private int asynInterval = 30;

}
