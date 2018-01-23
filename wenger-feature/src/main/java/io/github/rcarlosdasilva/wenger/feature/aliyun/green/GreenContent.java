package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * 待检测内容，使用GreenContent.ofXXX()创建
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@Getter
public class GreenContent implements Serializable {

  @JSONField(name = "dataId")
  private String mark;
  @Setter(AccessLevel.PRIVATE)
  private String content;
  @Setter(AccessLevel.PRIVATE)
  private String url;

  private GreenContent(String mark) {
    this.mark = mark;
  }

  /**
   * 创建文本内容
   *
   * @param mark 文本内容唯一标识，通常为文本所属的资源id，对应dataId，可为null，会自动生成随机字符串作为标识
   * @param text 文本字符串
   * @return {@link GreenContent}
   * @throws GreenException 内容为空时
   */
  public static List<GreenContent> ofText(String mark, final String text) {
    if (Strings.isNullOrEmpty(text)) {
      throw new GreenException("[Aliyun:GREEN] - 待检测文本内容为空");
    }
    if (Strings.isNullOrEmpty(mark)) {
      mark = TextHelper.random(10, Characters.NUMBERS_AND_LETTERS);
    }

    List<GreenContent> contents = Lists.newArrayList();
    int i = 0;
    String subContent;
    while ((subContent = TextHelper.sub(text, i, GreenConstant.MAX_TEXT_CONTENT_LENGTH)).length() > 0) {
      GreenContent content = new GreenContent(mark);
      content.setContent(subContent);
      contents.add(content);
      i += GreenConstant.MAX_TEXT_CONTENT_LENGTH;
    }
    if (contents.size() > GreenConstant.MAX_TASK_SIZE) {
      String brief = TextHelper.brief(contents.get(0).getContent(), 100, "...");
      log.warn("[Aliyun:GREEN] - 文本过长，单个任务最多4000个字符，一次请求最多100个任务，可能导致请求失败，文本开头：{}", brief);
    }
    return contents;
  }

  /**
   * 创建图片内容
   *
   * @param mark 图片内容唯一标识，通常为文本所属的资源id，对应dataId，可为null，会自动生成随机字符串作为标识
   * @param url  图片URL
   * @return {@link GreenContent}
   */
  public static GreenContent ofImage(String mark, final String url) {
    if (Strings.isNullOrEmpty(url)) {
      throw new GreenException("[Aliyun:GREEN] - 待检测图片内容为空");
    }
    if (Strings.isNullOrEmpty(mark)) {
      mark = TextHelper.random(10, Characters.NUMBERS_AND_LETTERS);
    }

    GreenContent content = new GreenContent(mark);
    content.setUrl(url);
    return content;
  }

}
