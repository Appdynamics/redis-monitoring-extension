package com.appdynamics.extensions.redis.metrics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

 class SlowLogTimeCache {
    private final Cache<String, Long> slowlogTimeCache;

    protected SlowLogTimeCache(int durationInSeconds){
        this.slowlogTimeCache = CacheBuilder.newBuilder().expireAfterWrite((long)durationInSeconds, TimeUnit.MINUTES).build();
    }

    protected void setMostRecentTimeStamp(String mostRecentTimeStamp, long timeStampValue){
        slowlogTimeCache.put(mostRecentTimeStamp, timeStampValue);

    }

    protected Long getMostRecentTimeStamp(){
        return slowlogTimeCache.getIfPresent("mostRecentTimeStamp");
    }
}
