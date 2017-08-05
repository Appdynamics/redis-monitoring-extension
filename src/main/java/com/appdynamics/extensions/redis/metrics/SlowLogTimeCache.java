package com.appdynamics.extensions.redis.metrics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class SlowLogTimeCache {
    private final Cache<String, Long> slowlogTimeCache;

    public SlowLogTimeCache(int durationInSeconds){
        this.slowlogTimeCache = CacheBuilder.newBuilder().expireAfterWrite((long)durationInSeconds, TimeUnit.MINUTES).build();
    }

    public void setMostRecentTimeStamp(String mostRecentTimeStamp, long timeStampValue){
        slowlogTimeCache.put(mostRecentTimeStamp, timeStampValue);

    }

    public Long getMostRecentTimeStamp(){
        return slowlogTimeCache.getIfPresent("mostRecentTimeStamp");
    }
}
