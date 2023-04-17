package com.example.rab56.services;

import org.springframework.stereotype.Service;

@Service
public class CounterService {
    private Integer synchronizedCount = Integer.valueOf(0);
    private Integer unsynchronizedCount = Integer.valueOf(0);
    public synchronized void incrementSynchronizedCount(){
        synchronizedCount++;
    }

    public Integer getSynchronizedCount() {
        return synchronizedCount;
    }

    public void incrementUnsynchronizedCount(){
        unsynchronizedCount++;
    }

    public Integer getUnsynchronizedCount() {
        return unsynchronizedCount;
    }

    public void resetCounters(){
        synchronizedCount = unsynchronizedCount = Integer.valueOf(0);
    }
}
