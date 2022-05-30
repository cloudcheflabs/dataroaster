package com.cloudcheflabs.dataroaster.operators.spark.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class SparkApplicationExecutor {

    private static Logger LOG = LoggerFactory.getLogger(SparkApplicationExecutor.class);

    public static <T> void runTask(Callable<T> task) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            // execute task with timeout.
            List<Future<T>> futureList =
                    executor.invokeAll(Arrays.asList(task), 15, TimeUnit.MINUTES);

            for (Future<T> fut : futureList) {
                LOG.info(new Date() + "::" + fut.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }

        executor.shutdown();
    }
}
