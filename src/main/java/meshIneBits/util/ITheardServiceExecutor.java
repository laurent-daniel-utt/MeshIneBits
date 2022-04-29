package meshIneBits.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ITheardServiceExecutor {

  void execute(Runnable runnable);

  <V> Future<V> submit(Callable<V> callable);

  void shutdownService();

  List<Runnable> shutdownNowService();
}
