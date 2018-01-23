package io.github.rcarlosdasilva.wenger.feature.aliyun.green;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.AcsRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.green.model.v20170112.*;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.asyn.ContentHolder;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.asyn.PollingProcessor;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.term.ImageScene;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.term.TextScene;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.term.VideoScene;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.aliyun.green.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class GreenHandler implements SmartInitializingSingleton {

  @Autowired
  private AppProperties appProperties;
  private IAcsClient client;
  private String region;

  @Override
  public void afterSingletonsInstantiated() {
    region = appProperties.getAliyun().getGreen().getRegion();
    String address = appProperties.getAliyun().getGreen().getAddress();

    IClientProfile profile = DefaultProfile.getProfile(region, appProperties.getAliyun().getAccessId(),
        appProperties.getAliyun().getAccessSecret());
    try {
      DefaultProfile.addEndpoint(region, region, GreenConstant.DEFAULT_GREEN_PRODUCT_NAME, address);
    } catch (ClientException ex) {
      throw new GreenException("[Aliyun:GREEN] - 初始化异常", ex);
    }
    client = new DefaultAcsClient(profile);

    if (appProperties.getAliyun().getGreen().isUseAsyn()) {
      ContentHolder.init(appProperties.getAliyun().getGreen().getAsynInterval());

      ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1, 0L,
          TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), r -> {
        Thread thread = new Thread(r, "aliyun-green-asyn-polling");
        thread.setDaemon(true);
        return thread;
      });
      singleThreadPool.execute(new PollingProcessor(this));
    }
  }

  //todo 视频API

  /**
   * 检测文本
   *
   * @param contents {@link GreenContent}
   * @param scene    检测场景
   * @return {@link GreenResult}
   */
  public List<GreenResult> text(List<GreenContent> contents, TextScene scene) {
    JSONObject data = new JSONObject();
    data.put("scenes", Lists.newArrayList(scene.toString()));
    data.put("tasks", contents);

    return send(configRequest(new TextScanRequest()), data.toJSONString());
  }

  /**
   * 检测图片
   *
   * @param contents {@link GreenContent}
   * @param sync     是否是否同步（同步调用时图片大小限制为5M，异步调用时图片大小限制为20M，图片下载时间限制为3s内，如果下载时间超过3s返回下载超时），建议使用oss存储或者cdn做缓存
   * @param scenes   如果一次需要检测多种场景
   * @return {@link GreenResult}
   */
  public List<GreenResult> image(List<GreenContent> contents, boolean sync, ImageScene... scenes) {
    JSONObject data = new JSONObject();
    data.put("scenes", Stream.of(scenes).map(ImageScene::toString).collect(Collectors.joining()));
    data.put("tasks", contents);

    if (sync) {
      return send(configRequest(new ImageSyncScanRequest()), data.toJSONString());
    } else {
      List<GreenResult> results = send(configRequest(new ImageAsyncScanRequest()), data.toJSONString());
      results.forEach(gr -> ContentHolder.addImageTask(gr.getTaskId()));
      return results;
    }
  }

  /**
   * 检测图片
   *
   * @param content {@link GreenContent}
   * @param sync    是否是否同步（同步调用时图片大小限制为5M，异步调用时图片大小限制为20M，图片下载时间限制为3s内，如果下载时间超过3s返回下载超时），建议使用oss存储或者cdn做缓存
   * @param scenes  如果一次需要检测多种场景
   * @return {@link GreenResult}
   */
  public List<GreenResult> image(GreenContent content, boolean sync, ImageScene... scenes) {
    return image(Lists.newArrayList(content), sync, scenes);
  }

  /**
   * 检测视频（只有异步方式）
   *
   * @param contents {@link GreenContent}
   * @param scenes   如果一次需要检测多种场景
   * @return {@link GreenResult}
   */
  public List<GreenResult> video(List<GreenConstant> contents, VideoScene... scenes) {
    JSONObject data = new JSONObject();
    data.put("", null);
    data.put("tasks", contents);

    return send(configRequest(new VideoAsyncScanRequest()), data.toJSONString());
  }

  /**
   * 检测视频（只有异步方式）
   *
   * @param content {@link GreenContent}
   * @param scenes  如果一次需要检测多种场景
   * @return {@link GreenResult}
   */
  public List<GreenResult> video(GreenConstant content, VideoScene... scenes) {
    return video(Lists.newArrayList(content), scenes);
  }

  /**
   * 异步查询图片检测结果，开发者无需调用，可使用{@link ContentHolder#imageResults()}获取
   *
   * @param tasks 任务ID列表
   * @return {@link GreenResult}
   */
  public List<GreenResult> imageResults(List<String> tasks) {
    JSONArray data = new JSONArray();
    data.addAll(tasks);
    return send(configRequest(new ImageAsyncScanResultsRequest()), data.toJSONString());
  }


  /**
   * 异步查询视频检测结果，开发者无需调用，可使用{@link ContentHolder#videoResults()}获取
   *
   * @param tasks 任务ID列表
   * @return {@link GreenResult}
   */
  public List<GreenResult> videoResults(List<String> tasks) {
    JSONArray data = new JSONArray();
    data.addAll(tasks);
    return send(configRequest(new VideoAsyncScanResultsRequest()), data.toJSONString());
  }

  private List<GreenResult> send(AcsRequest<?> request, String data) {
    try {
      request.setContent(data.getBytes(GeneralConstant.DEFAULT_ENCODING),
          GeneralConstant.DEFAULT_ENCODING, FormatType.JSON);
      HttpResponse response = client.doAction(request);
      if (!response.isSuccess()) {
        log.error("[Aliyun:GREEN] - 文本安全请求失败，response status: {}", response.getStatus());
        return Collections.emptyList();
      }

      return parseResponse(new String(response.getContent(), GeneralConstant.DEFAULT_ENCODING));
    } catch (UnsupportedEncodingException | ClientException ex) {
      throw new GreenException("[Aliyun:GREEN] - 文本安全请求异常", ex);
    }
  }

  private AcsRequest configRequest(AcsRequest<?> request) {
    request.setAcceptFormat(FormatType.JSON);
    request.setContentType(FormatType.JSON);
    request.setMethod(MethodType.POST);
    request.setEncoding(GeneralConstant.DEFAULT_ENCODING);
    request.setRegionId(region);
    request.setConnectTimeout(GreenConstant.DEFAULT_CONNECT_TIMEOUT);
    request.setReadTimeout(GreenConstant.DEFAULT_READ_TIMEOUT);
    return request;
  }

  private List<GreenResult> parseResponse(String responseText) {
    if (Strings.isNullOrEmpty(responseText)) {
      return Collections.emptyList();
    }

    JSONObject response = JSON.parseObject(responseText);
    HttpStatus status = HttpStatus.valueOf(response.getInteger("code"));

    if (!status.is2xxSuccessful()) {
      log.warn("文本检测结果不成功，code: {}", status);
      return Collections.emptyList();
    }

    JSONArray datas = response.getJSONArray("data");
    Iterator<Object> dataIterator = datas.iterator();

    List<GreenResult> results = Lists.newArrayList();
    while (dataIterator.hasNext()) {
      JSONObject data = (JSONObject) dataIterator.next();
      // json转bean
      GreenResult result = data.toJavaObject(GreenResult.class);
      result.setSuccess(data.getInteger("code") == HttpStatus.OK.value());
      results.add(result);
    }

    return results;
  }

}
