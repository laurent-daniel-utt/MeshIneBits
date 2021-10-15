package meshIneBits.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadServiceExecutor implements ITheardServiceExecutor{
    public static final MultiThreadServiceExecutor instance=new MultiThreadServiceExecutor(ThreadPoolType.FIXED,5);

    public enum ThreadPoolType {SINGLE, FIXED, CACHED}
    private ExecutorService service;
    public MultiThreadServiceExecutor(ThreadPoolType type, int nbThread){
        switch (type){
            case SINGLE:;
                service=Executors.newSingleThreadExecutor();
                break;
            case FIXED:
                service=Executors.newFixedThreadPool(nbThread);
                break;
            case CACHED:
                service=Executors.newCachedThreadPool();
                break;
            default:
                service = Executors.newSingleThreadExecutor();
                break;
        }
    }
    public void execute(Runnable runnable){
        service.execute(runnable);
    }
    public <V> Future<V> submit(Callable<V> callable){
        Future<V> result = service.submit(callable);
        return result;
    }
    public void shutdownService(){
        service.shutdown();
    }
    public List<Runnable> shutdownNowService() {
        return service.shutdownNow();
    }
}
