package io.github.rcarlosdasilva.wenger.feature.sequence;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 借用 http://git.oschina.net/yu120/sequence
 * <p>
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <p>
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）
 * <p>
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道
 * <p>
 * 后台定时更新时钟，JVM退出时，线程自动回收
 * <p>
 * 10亿：43410,206,210.72815533980582%
 * <p>
 * 1亿：4699,29,162.0344827586207%
 * <p>
 * 1000万：480,12,40.0%
 * <p>
 * 100万：50,10,5.0%
 * <p>
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class SystemClock {

  private final long period;
  private final AtomicLong now;

  private SystemClock(long period) {
    this.period = period;
    this.now = new AtomicLong(System.currentTimeMillis());
    scheduleClockUpdating();
  }

  private static class Holder {
    private static final SystemClock INSTANCE = new SystemClock(1);

    private Holder() {
      throw new IllegalStateException();
    }
  }

  private static SystemClock instance() {
    return Holder.INSTANCE;
  }

  private void scheduleClockUpdating() {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
      Thread thread = new Thread(r, "System Clock");
      thread.setDaemon(true);
      return thread;
    });

    executor.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
  }

  public static long now() {
    return instance().now.get();
  }

}
