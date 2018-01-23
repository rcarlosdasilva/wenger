package io.github.rcarlosdasilva.wenger.feature.aliyun.green.asyn;

import com.google.common.collect.Lists;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.GreenHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.github.rcarlosdasilva.wenger.feature.aliyun.green.asyn.ContentHolder.*;

/**
 * 异步检测结果轮询处理器
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public class PollingProcessor implements Runnable {

  public static final long DEFAULT_SLEEP_TIME = 5;
  public static final long MAX_SLEEP_TIME = 1005;
  public static final long SLEEP_ASCENDING_STEP = 100;

  private GreenHandler handler;
  private long sleepTime = DEFAULT_SLEEP_TIME;

  public PollingProcessor(GreenHandler handler) {
    this.handler = handler;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      request();

      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException ex) {
        log.error("[Aliyun:GREEN] - ", ex);
        Thread.currentThread().interrupt();
      }
    }
  }

  private void request() {
    Task task = task();
    if (task != null) {
      sleepTime = DEFAULT_SLEEP_TIME;

      List<String> images = Lists.newArrayList();
      List<String> videos = Lists.newArrayList();

      // 一次最多请求10个，为了缩短请求时间，尽快获取到结果
      int i = 0;
      do {
        if (task == null) {
          break;
        }

        if (task.getType() == TaskType.IMAGE) {
          images.add(task.getTaskId());
        } else {
          videos.add(task.getTaskId());
        }

        task = task();
        i++;
      } while (i < 10);

      if (!images.isEmpty()) {
        putImageResults(handler.imageResults(images));
      }
      if (!videos.isEmpty()) {
        putVideoResults(handler.videoResults(images));
      }
    } else if (sleepTime < MAX_SLEEP_TIME) {
      sleepTime += SLEEP_ASCENDING_STEP;
    }
  }

}
