package io.github.rcarlosdasilva.wenger.feature.aliyun.green.asyn;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.GreenResult;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 异步检测内容持有
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class ContentHolder {

  public static final ContentHolder INSTANCE = new ContentHolder();

  private Cache<String, Task> taskCache;
  private Queue<Task> readyTask = Queues.newArrayDeque();
  private Multimap<String, GreenResult> imageResults = HashMultimap.create();
  private Multimap<String, GreenResult> videoResults = HashMultimap.create();

  private ReadWriteLock imageLock = new ReentrantReadWriteLock();
  private ReadWriteLock videoLock = new ReentrantReadWriteLock();

  private static RemovalListener<String, Task> removalListener = (k, v, rc) -> INSTANCE.readyTask.add(v);

  private ContentHolder() {
  }

  public static void init(int asynInterval) {
    INSTANCE.taskCache = Caffeine.newBuilder()
        .expireAfterWrite(asynInterval, TimeUnit.SECONDS)
        .removalListener(removalListener).build();
  }

  public static void addImageTask(String taskId) {
    INSTANCE.taskCache.put(taskId, new Task(TaskType.IMAGE, taskId));
  }

  public static void addVideoTask(String taskId) {
    INSTANCE.taskCache.put(taskId, new Task(TaskType.VIDEO, taskId));
  }

  static Task task() {
    return INSTANCE.readyTask.poll();
  }

  public static void putImageResults(List<GreenResult> results) {
    INSTANCE.imageLock.writeLock().lock();
    try {
      results.forEach(r -> INSTANCE.imageResults.put(r.getTaskId(), r));
    } finally {
      INSTANCE.imageLock.writeLock().unlock();
    }
  }

  public static void putVideoResults(List<GreenResult> results) {
    INSTANCE.videoLock.writeLock().lock();
    try {
      results.forEach(r -> INSTANCE.videoResults.put(r.getTaskId(), r));
    } finally {
      INSTANCE.videoLock.writeLock().unlock();
    }
  }

  public static Multimap<String, GreenResult> imageResults() {
    INSTANCE.imageLock.readLock().lock();
    try {
      Multimap<String, GreenResult> copy = INSTANCE.imageResults;
      INSTANCE.imageResults = HashMultimap.create();
      return copy;
    } finally {
      INSTANCE.imageLock.readLock().unlock();
    }
  }

  public static Multimap<String, GreenResult> videoResults() {
    INSTANCE.videoLock.readLock().lock();
    try {
      Multimap<String, GreenResult> copy = INSTANCE.videoResults;
      INSTANCE.videoResults = HashMultimap.create();
      return copy;
    } finally {
      INSTANCE.videoLock.readLock().unlock();
    }
  }

  @Data
  static class Task {

    @NonNull
    private TaskType type;
    @NonNull
    private String taskId;

  }

  enum TaskType {
    IMAGE, VIDEO
  }

}
