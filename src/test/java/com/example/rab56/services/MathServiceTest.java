package com.example.rab56.services;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.exceptions.ServerException;
import com.example.rab56.services.MathService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MathServiceTest {
    private MathService service = new MathService();
    @Test
    public void testGood(){
        Numbers numbers = new Numbers(new int[]{3,2,4,1});
        ResultPair good = new ResultPair(2.0,2.0);
        ResultPair result = service.getResult(numbers);
        Assertions.assertEquals(good, result);
    }

    @Test
    public void testEmpty(){
        Numbers numbers = new Numbers(new int[]{});
        ResultPair result = new ResultPair();
        Throwable throwable = assertThrows(ServerException.class,
                () -> service.getResult(numbers));
        assertNotNull(throwable.getMessage());
    }
}
