package com.example.rab56.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CounterServiceTest {
    private CounterService service = new CounterService();
    @Test
    public void testIncrementing(){
        service.incrementSynchronizedCount();
        service.incrementUnsynchronizedCount();
        Assertions.assertEquals(service.getSynchronizedCount(), 1);
        Assertions.assertEquals(service.getUnsynchronizedCount(), 1);
        service.resetCounters();
        Assertions.assertEquals(service.getSynchronizedCount(), 0);
        Assertions.assertEquals(service.getUnsynchronizedCount(), 0);
    }
}
