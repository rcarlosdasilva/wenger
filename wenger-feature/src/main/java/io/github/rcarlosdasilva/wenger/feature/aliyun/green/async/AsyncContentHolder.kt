package io.github.rcarlosdasilva.wenger.feature.aliyun.green.async

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Queues
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.GreenResult
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 异步检测内容持有
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class AsyncContentHolder private constructor() {

  private lateinit var taskCache: Cache<String, Task>
  private val readyTask: Deque<Task> = Queues.newArrayDeque<Task>()
  private var imageResults: Multimap<String, GreenResult> = HashMultimap.create()
  private var videoResults: Multimap<String, GreenResult> = HashMultimap.create()

  private val imageLock = ReentrantReadWriteLock()
  private val videoLock = ReentrantReadWriteLock()

  internal data class Task(
    internal val type: TaskType,
    internal val taskId: String
  )

  internal enum class TaskType { IMAGE, VIDEO }

  companion object {
    private val INSTANCE = AsyncContentHolder()

    fun init(asyncInterval: Long) {
      INSTANCE.taskCache = Caffeine.newBuilder().expireAfterWrite(asyncInterval, TimeUnit.SECONDS)
        .removalListener<String, Task>({ _, value, _ -> INSTANCE.readyTask.add(value) }).build()
    }

    fun addImageTask(taskId: String) = INSTANCE.taskCache.put(taskId, Task(TaskType.IMAGE, taskId))

    fun addVideoTask(taskId: String) = INSTANCE.taskCache.put(taskId, Task(TaskType.VIDEO, taskId))

    internal fun task(): Task? = INSTANCE.readyTask.poll()

    fun putImageResults(results: List<GreenResult>) {
      INSTANCE.imageLock.writeLock().lock()
      try {
        results.forEach { INSTANCE.imageResults.put(it.taskId, it) }
      } finally {
        INSTANCE.imageLock.writeLock().unlock()
      }
    }

    fun putVideoResults(results: List<GreenResult>) {
      INSTANCE.videoLock.writeLock().lock()
      try {
        results.forEach { INSTANCE.videoResults.put(it.taskId, it) }
      } finally {
        INSTANCE.videoLock.writeLock().unlock()
      }
    }

    fun imageResults(): Multimap<String, GreenResult> {
      INSTANCE.imageLock.readLock().lock()
      try {
        val copy = INSTANCE.imageResults
        INSTANCE.imageResults = HashMultimap.create()
        return copy
      } finally {
        INSTANCE.imageLock.readLock().unlock()
      }
    }

    fun videoResults(): Multimap<String, GreenResult> {
      INSTANCE.videoLock.readLock().lock()
      try {
        val copy = INSTANCE.videoResults
        INSTANCE.videoResults = HashMultimap.create()
        return copy
      } finally {
        INSTANCE.videoLock.readLock().unlock()
      }
    }
  }

}
