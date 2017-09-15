package com.appdynamics.extensions.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ATaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ATaskExecutor.class);
    private static final long TASK_TIME_THRESHOLD_IN_MS = 60 * 1000l;
    private AtomicInteger taskCounter;
    private ABaseMonitor aBaseMonitor;

    public ATaskExecutor(ABaseMonitor aBaseMonitor) {
        taskCounter = new AtomicInteger(aBaseMonitor.getTaskCount());
        this.aBaseMonitor = aBaseMonitor;
    }

    public void submit(final String name, final Runnable aServerTask){
        aBaseMonitor.configuration.getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try{
                    long startTime = System.currentTimeMillis();
                    aServerTask.run();
                    long diffTime = System.currentTimeMillis() - startTime;
                    if(diffTime > TASK_TIME_THRESHOLD_IN_MS){  //time limit for each server task to finish
                        logger.warn("Server Task {} took {} ms to complete",name,diffTime);
                    }
                }
                finally {
                    if(taskCounter.decrementAndGet() <= 0){
                        aBaseMonitor.onComplete();
                    }
                }
            }
        });
    }
}
