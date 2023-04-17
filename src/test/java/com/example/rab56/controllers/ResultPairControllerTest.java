package com.example.rab56.controllers;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultObject;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.controllers.ResultPairController;
import com.example.rab56.exceptions.ServerException;
import com.example.rab56.memory.InMemoryStorage;
import com.example.rab56.services.MathService;
import com.example.rab56.validators.Errors;
import com.example.rab56.validators.NumbersValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResultPairControllerTest {
    @Mock
    private MathService service;

    @Mock
    private NumbersValidator validator;

    @Mock
    private InMemoryStorage inMemoryStorage;

    @InjectMocks
    private ResultPairController controller = new ResultPairController(service, validator, inMemoryStorage);

    @Test
    public void testWithValidationErrors(){
        Numbers numbers = new Numbers(new int[]{3,3,3,3});
        Errors validationErrors = new Errors();
        validationErrors.setStatus(HttpStatus.BAD_REQUEST.name());
        validationErrors.add("All numbers are equals");
        when(validator.Validate(numbers)).thenReturn(validationErrors);

        ResponseEntity<Object> responseEntity = controller.getResultPair(numbers);
        String resultString = (String)responseEntity.getBody();
        assertNotNull(resultString);
        assertTrue(resultString.contains("Errors"));
    }

    @Test
    public void testEmpty(){
        Numbers numbers = new Numbers(new int[]{});
        Errors validationErrors = new Errors();
        when(validator.Validate(numbers)).thenReturn(validationErrors);
        when(service.getResult(numbers)).thenThrow(ServerException.class);

        ResponseEntity<Object> responseEntity = controller.getResultPair(numbers);
        String resultString = (String)responseEntity.getBody();
        assertNotNull(resultString);
        assertTrue(!resultString.contains("Set is empty"));
    }

    @Test
    public void testGood(){
        Numbers numbers = new Numbers(new int[]{1,2,3,4});
        ResultPair resultPair = new ResultPair(2.0,2.0);
        Errors validationErrors = new Errors();
        when(validator.Validate(numbers)).thenReturn(validationErrors);
        when(service.getResult(numbers)).thenReturn(resultPair);

        ResponseEntity<Object> responseEntity = controller.getResultPair(numbers);
        String resultString = (String)responseEntity.getBody();
        assertNotNull(resultString);
        assertTrue(!resultString.contains("Errors"));
    }

    @Test
    public void testGetByMiddleValue(){
        when(inMemoryStorage.getByMiddleValue(0.0)).thenReturn(null);
        ResponseEntity<Object> responseEntity = controller.getByMiddleValue(0.0);
        String resultString = (String)responseEntity.getBody();
        assertNotNull(resultString);

        when(inMemoryStorage.getByMiddleValue(0.0)).thenReturn(new ResultPair(2.0, 2.0));
        responseEntity = controller.getByMiddleValue(0.0);
        ResultPair resultPair = (ResultPair) responseEntity.getBody();
        assertEquals(resultPair, new ResultPair(2.0,2.0));
    }

    @Test
    public void testByMediana(){
        when(inMemoryStorage.getByMediana(0.0)).thenReturn(null);
        ResponseEntity<Object> responseEntity = controller.getByMediana(0.0);
        String resultString = (String)responseEntity.getBody();
        assertNotNull(resultString);

        when(inMemoryStorage.getByMediana(0.0)).thenReturn(new ResultPair(2.0, 2.0));
        responseEntity = controller.getByMediana(0.0);
        ResultPair resultPair = (ResultPair) responseEntity.getBody();
        assertEquals(resultPair, new ResultPair(2.0,2.0));
    }

    @Test
    public void testGetSize(){
        when(inMemoryStorage.size()).thenReturn(0);

        ResponseEntity<Object> responseEntity = controller.getSize();
        Integer size = (Integer)responseEntity.getBody();
        assertEquals(Integer.valueOf(0), size);
    }

    @Test
    public void testGetAllResultPairs(){
        Set<Numbers> set = new HashSet<>();
        Numbers key = new Numbers(new int[]{4,1,3,2});
        set.add(key);
        ResultPair resultPair = new ResultPair(2.0, 2.0);
        when(inMemoryStorage.size()).thenReturn(1);
        when(inMemoryStorage.getKeys()).thenReturn(set);
        when(inMemoryStorage.get(key)).thenReturn(resultPair);

        ResponseEntity<Object> resultObject = controller.redirect();
        String resultString = (String)resultObject.getBody();
        assertTrue(resultString.contains("<th>"));
    }

    @Test
    public void testInput(){
        String resultString = controller.input();
        assertNotNull(resultString.contains("<input type='submit' value='Send'>"));
    }

    @Test
    public void testGetCounters(){
        when(service.getSynchronizedCounter()).thenReturn(0);
        when(service.getUnsynchronizedCounter()).thenReturn(0);

        String resultString = controller.getCounters();
        assertTrue(resultString.contains("<td>"));
    }

    @Test
    public void testResetCounters(){
        controller.resetCounters();
        assertEquals(Integer.valueOf(0), service.getSynchronizedCounter());
        assertEquals(Integer.valueOf(0), service.getUnsynchronizedCounter());
    }
}
