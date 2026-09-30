package com.astrotech.transport.core;


import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ApiCacheControl {

    public CacheControl publicMaxAgeHours(int hours) {
        return CacheControl.maxAge(hours, TimeUnit.HOURS)
                .cachePublic();
    }
    public CacheControl publicMaxAgeMinutes(int minutes) {
        return CacheControl.maxAge(minutes, TimeUnit.MINUTES)
                .cachePublic();
    }



    public CacheControl noStore() {
        return CacheControl.noStore();
    }
}
