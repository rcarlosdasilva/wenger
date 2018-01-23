package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import com.alibaba.fastjson.annotation.JSONField;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.term.Suggestion;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class GreenResult {

  @Setter(AccessLevel.PACKAGE)
  private boolean success;
  /**
   * 错误描述信息
   */
  @JSONField(name = "msg")
  private String message;
  /**
   * 对应请求中的dataId，也是{@link GreenContent}中的mark属性
   */
  private String dataId;
  /**
   * 阿里端执行的任务id.
   * <p>
   * 云盾内容安全服务器返回的唯一标识该检测任务的ID
   */
  private String taskId;
  private String content;
  private String url;
  /**
   * 检测详细内容
   */
  @JSONField(name = "results")
  private List<Detail> details;

  /**
   * 文本检测结果详细内容
   *
   * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
   */
  @Data
  public static class Detail {

    /**
     * 风险场景，一般与请求时设置的scenes相同
     */
    private String scene;
    /**
     * 建议用户处理，取值范围：[“pass”, “review”, “block”],
     * pass:文本正常，review：需要人工审核，block：文本违规，可以直接删除或者做限制处理
     */
    private Suggestion suggestion;
    /**
     * 该文本的分类，取值范围参考1.1小节
     */
    private String label;
    /**
     * 结果为该分类的概率；值越高，越趋于该分类；取值为[0.00-100.00], 分值仅供参考，您只需要关注label和suggestion的取值即可
     */
    private float rate;

    public void setSuggestion(String suggestion) {
      this.suggestion = Suggestion.valueOf(suggestion.toUpperCase());
    }

  }

}
