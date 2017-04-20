package com.dhaya.utils.servers;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by dhaya on 4/20/17.
 */
public class ExecutorFactory {
    public static Executor newForkJoinPoolExecutor(int size) {
        return new ForkJoinPool(size,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }
}
