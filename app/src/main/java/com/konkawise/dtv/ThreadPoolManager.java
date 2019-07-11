package com.konkawise.dtv;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor mThreadPoolExecutor;

    private static class ThreadPoolManagerHolder {
        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    private ThreadPoolManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, sPoolWorkQueue);
        mThreadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    public static ThreadPoolManager getInstance() {
        return ThreadPoolManagerHolder.INSTANCE;
    }

    public void setThreadPoolFactory(ThreadFactory threadFactory) {
        mThreadPoolExecutor.setThreadFactory(threadFactory);
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        mThreadPoolExecutor.setRejectedExecutionHandler(handler);
    }

    public void execute(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    public void remove(Runnable runnable) {
        mThreadPoolExecutor.remove(runnable);
    }

    public void shutdown() {
        mThreadPoolExecutor.shutdown();
    }

    public void shutdownNow() {
        mThreadPoolExecutor.shutdownNow();
    }
}
