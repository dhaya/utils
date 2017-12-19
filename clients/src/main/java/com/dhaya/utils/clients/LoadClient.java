package com.dhaya.utils.clients;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.http.HttpClient;
import com.linecorp.armeria.client.http.HttpClientFactory;
import com.linecorp.armeria.common.http.AggregatedHttpMessage;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class LoadClient {
    private final String url;
    private final int threads;
    private final int duration;
    private final HttpClient httpClient;
    private volatile boolean stop = false;

    private final AtomicInteger success = new AtomicInteger();
    private final AtomicInteger failures = new AtomicInteger();


    public static void main(String[] args) throws Exception {
        String url = args[0];
        int numThreads = Integer.parseInt(args[1]);
        int durationInSecs = Integer.parseInt(args[2]);

        LoadClient lc = new LoadClient(url, numThreads, durationInSecs);
        lc.startLoad();

    }

    public LoadClient(String url, int threads, int duration) {
        this.url = url;
        this.threads = threads;
        this.duration = duration;

        ClientFactory factory = new HttpClientFactory();
        this.httpClient = Clients.newClient(url, HttpClient.class);
    }

    private void startLoad() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {stop = true;}, duration, TimeUnit.SECONDS);

        Runnable runnable = () -> {
            while (!stop) {
                CompletableFuture<AggregatedHttpMessage> aggregate = httpClient.get("/").aggregate();
                aggregate.whenCompleteAsync((a, ex) -> {
                    if (a != null) {
                        success.incrementAndGet();
                    } else {
                        failures.incrementAndGet();
                    }
                }, executor);
            }
        };

        executor.submit(runnable);

        executor.shutdown();
        executor.awaitTermination(duration + 5, TimeUnit.SECONDS);

        scheduler.shutdown();

        System.out.println("Success = " + success.get());
        System.out.println("Failures = " + failures.get());
        System.out.println("Throughput = " + (success.get() * 1.0f)/duration);
    }

}
