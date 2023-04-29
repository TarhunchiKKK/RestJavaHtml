package com.example.rab56.controllers;

import com.example.rab56.database.DbEntity;
import com.example.rab56.database.RepositoryService;
import com.example.rab56.entity.BulkParameter;
import com.example.rab56.entity.BulkParameterList;
import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.exceptions.ServerException;
import com.example.rab56.memory.InMemoryStorage;
import com.example.rab56.services.CounterService;
import com.example.rab56.services.MathService;
import com.example.rab56.validators.Errors;
import com.example.rab56.validators.NumbersValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResultPairControllerTest {
    @Mock
    private MathService mathService;

    @Mock
    private NumbersValidator validator;

    @Mock
    private CounterService counterService;

    @Mock
    private InMemoryStorage inMemoryStorage;

    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private ResultPairController controller = new ResultPairController(mathService, validator, counterService,
            inMemoryStorage, repositoryService);

    @Test
    public void testBadGetResultPair(){
        Numbers badNumbers = new Numbers(new int[]{1,1,1,1});
        Errors errors = new Errors();
        errors.add("All numbers are equals");
        when(validator.Validate(badNumbers)).thenReturn(errors);
        ResponseEntity<Object> responseEntity = controller.getResultPair(badNumbers);
        assertTrue(((String)responseEntity.getBody()).contains("All numbers are equals"));
    }

    @Test
    public void testGoodResultPair(){
        Numbers goodNumbers = new Numbers(new int[] {1,2,3,4});
        when(validator.Validate(goodNumbers)).thenReturn(new Errors());
        when(mathService.getResult(goodNumbers)).thenReturn(new ResultPair(2.0, 2.0));
        Map<Numbers, ResultPair> map = new HashMap<>();
        map.put(goodNumbers, new ResultPair(2.0,2.0));
        when(inMemoryStorage.getMap()).thenReturn(map);
        ResponseEntity<Object> responseEntity = controller.getResultPair(goodNumbers);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testErrorGetResultPair(){
        Numbers emptyNumbers = new Numbers(new int[]{});
        when(validator.Validate(emptyNumbers)).thenReturn(new Errors());
        when(mathService.getResult(emptyNumbers)).thenThrow(ServerException.class);
        ResponseEntity<Object> responseEntity = controller.getResultPair(emptyNumbers);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetResultPairs(){
        List<BulkParameter> parameters = new ArrayList<>();
        BulkParameter b1 = new BulkParameter(1,2,3,4);
        BulkParameter b2 = new BulkParameter(7,7,7,7);
        BulkParameter b3 = new BulkParameter(9,10,11,12);
        BulkParameter b4 = new BulkParameter(13,14,15,16);
        parameters.add(b1);parameters.add(b2);parameters.add(b3);parameters.add(b4);
        Errors errors = new Errors();
        errors.add("All numbers are equals");
        when(validator.Validate(b1.toNumbers())).thenReturn(new Errors());
        when(validator.Validate(b2.toNumbers())).thenReturn(errors);
        when(validator.Validate(b3.toNumbers())).thenReturn(new Errors());
        when(validator.Validate(b4.toNumbers())).thenReturn(new Errors());
        Map<Numbers, ResultPair> map = new HashMap<>();
        map.put(new Numbers(new int[]{1,2,3,4}), new ResultPair(2.0,2.0));
        map.put(new Numbers(new int[]{9,10,11,12}), new ResultPair(10.0, 10.0));
        map.put(new Numbers(new int[]{13,14,15,16}), new ResultPair(14.0,14.0));
        when(inMemoryStorage.getMap()).thenReturn(map);
        ResponseEntity<Object> responseEntity = controller.getResultPairs(new BulkParameterList(parameters));
        assertNotNull((String)responseEntity.getBody());
        assertFalse(((String) responseEntity.getBody()).contains("#228B22"));
    }

    @Test
    public void testRedirect(){
        Map<Numbers, ResultPair> map = new HashMap<>();
        map.put(new Numbers(new int[]{1,2,3,4}), new ResultPair(2.0,2.0));
        map.put(new Numbers(new int[]{9,10,11,12}), new ResultPair(10.0, 10.0));
        map.put(new Numbers(new int[]{13,14,15,16}), new ResultPair(14.0,14.0));
        List<DbEntity> list = new ArrayList<>();
       // when(repositoryService.getAll()).thenReturn(list);
        when(inMemoryStorage.getMap()).thenReturn(map);
        assertNotNull(controller.redirect());
    }

    @Test
    public void testForm(){
        String form1 = controller.form(1);
        String form2 = controller.form(2);
        assertNotNull(form1);
        assertNotNull(form2);
    }

    @Test
    public void testGetCounters(){
        when(counterService.getSynchronizedCount()).thenReturn(0);
        when(counterService.getUnsynchronizedCount()).thenReturn(0);

        String resultString = controller.getCounters();
        assertTrue(resultString.contains("<td>"));
    }

    @Test
    public void testResetCounters(){
        controller.resetCounters();
        assertEquals(Integer.valueOf(0), counterService.getSynchronizedCount());
        assertEquals(Integer.valueOf(0), counterService.getUnsynchronizedCount());
    }

    @Test
    public void testRepositoryService(){
        Assertions.assertNotNull(repositoryService.getAll());
        repositoryService.save(new Numbers(new int[] {1,2,3,4}), new ResultPair(2.0,2.0));
        repositoryService.save(new Numbers(new int[] {5,6,7,8}), new ResultPair(6.0,6.0));
        Assertions.assertEquals(repositoryService.getAll().size(), 2);
    }
}
