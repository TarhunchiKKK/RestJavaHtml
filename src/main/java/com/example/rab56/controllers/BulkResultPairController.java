package com.example.rab56.controllers;

import com.example.rab56.entity.BulkParameterList;
import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.memory.InMemoryStorage;
import com.example.rab56.services.MathService;
import com.example.rab56.validators.Errors;
import com.example.rab56.validators.NumbersValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// контроллер для работы с bulk запросами
@RestController
public class BulkResultPairController {
    // компаратор для агрегирующего функционала со средним значением
    private static Comparator<Map.Entry<Numbers, ResultPair>> middleValueComparator
            = new Comparator<Map.Entry<Numbers, ResultPair>>() {
        @Override
        public int compare(Map.Entry<Numbers, ResultPair> e1, Map.Entry<Numbers, ResultPair> e2) {
            return Double.compare(e1.getValue().getMiddleValue(), e2.getValue().getMiddleValue());
        }
    };

    // компаратор для агрегирующего функционала с медианой
    private static Comparator<Map.Entry<Numbers, ResultPair>> medianaComparator
            = new Comparator<Map.Entry<Numbers, ResultPair>>() {
        @Override
        public int compare(Map.Entry<Numbers, ResultPair> e1, Map.Entry<Numbers, ResultPair> e2) {
            return Double.compare(e1.getValue().getMediana(), e2.getValue().getMediana());
        }
    };

    private static Logger logger = LoggerFactory.getLogger(ResultPairController.class);//логгирование
    private MathService mathService;                                                   //сервис
    private NumbersValidator validator;                                               //валидатор
    private InMemoryStorage inMemoryStorage;                                          //хранилище

    @Autowired
    public BulkResultPairController(MathService service,  NumbersValidator validator,
                                    InMemoryStorage inMemoryStorage){          //конструктор
        this.mathService = service;
        this.validator = validator;
        this.inMemoryStorage = inMemoryStorage;
    }

    @PostMapping(value = "/getresultpairs", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> getResultPairs(@ModelAttribute("parameters") BulkParameterList parameters){
        Map<Numbers, ResultPair> resultMap = new HashMap<>();       // map из пар Numbers-ResultPair
        Errors errors = new Errors();                               // ошибки валидации
        parameters.getParameters().forEach(bulkParameter -> {       // проход по параметрам bulk запроса
            Numbers numbers = new Numbers(bulkParameter);               // создание входного объекта
            Errors tempErrors = validator.Validate(numbers);            // создание выходного объекта
            if(!tempErrors.isEmpty()) errors.add(tempErrors);           // если есть ошибки - занести из в список ошибок
            else resultMap.put(numbers, mathService.getResult(numbers));    // ошибок нет - заносим в map
        });
        resultMap.entrySet().stream().forEach(                      // сохранение входных и выходных данных в кэше
                e -> inMemoryStorage.add(e.getKey(), e.getValue()));


        String response = getMapTable(resultMap);                   // тадлица, содержащая map

        // значения агрегирующего функционала по медианному значению
        response = response + "Минимальная медиана: " + getMinMediana(resultMap) + "<br/>";
        response = response + "Максимальная медиана: " + getMaxMediana(resultMap) + "<br/>";
        response = response + "Средняя медиана: " + getMiddleMediana(resultMap) + "<br/><br/>";

        //значение агрегирующего функционала по среднему значению
        response = response + "Минимальное среднее значение: " + getMinMiddleValue(resultMap) + "<br/>";
        response = response + "Максимальное среднее значение: " + getMaxMiddleValue(resultMap) + "<br/>";
        response = response + "Среднее среднее значение: " + getMiddleMiddleValue(resultMap) + "<br/>";

        return ResponseEntity.ok(response + getErrorList(errors));
    }



    // получить таблицу значений map
    public String getMapTable(Map<Numbers, ResultPair> map){
        String response = "";
        if(!map.isEmpty()){
            response = response + "<table border='10' bgcolor='#808000'>";
            response = response + "<th>Numbers</th><th>Mediana</th><th>Middle value</th>";
            for(Map.Entry<Numbers, ResultPair> entry:map.entrySet()){
                String numbers = "";
                for(int number:entry.getKey().getNumbers()){
                    numbers = numbers + number + " ";
                }
                response = response + "<tr><td align='center'>" + numbers + "</td>";
                response = response + "<td align='center'>" + entry.getValue().getMediana() + "</td>";
                response = response + "<td align='center'>" + entry.getValue().getMiddleValue() + "</td></tr>";
            }
            response = response + "</table><br/><br/>";
        }
        return response;
    }

    // получить список ошибок
    public String getErrorList(Errors errors){
        String response = "";
        if(!errors.isEmpty()){
            response = "<br/>Список ошибок:<p><ol>";
            for(String error: errors.getErrors()){
                response = response + "<li>" + error + "</li>";
            }
            response = response + "</ol></p>";
        }
        return response;
    }



    // минимальное среднее значение
    public double getMinMiddleValue(Map<Numbers, ResultPair> map){
        return map.entrySet().stream().min(middleValueComparator).get().getValue().getMiddleValue();
    }

    // максимальное среднее значение
    public double getMaxMiddleValue(Map<Numbers, ResultPair> map){
        return map.entrySet().stream().max(middleValueComparator).get().getValue().getMiddleValue();
    }

    // среднее среднее значение
    public double getMiddleMiddleValue(Map<Numbers, ResultPair> map){
        int sum = 0;
        for(ResultPair value:map.values()){
            sum += value.getMiddleValue();
        }
        return sum / map.size();
    }


    // минимальная медиана
    public double getMinMediana(Map<Numbers, ResultPair> map){
        return map.entrySet().stream().min(medianaComparator).get().getValue().getMediana();
    }

    // максиммальная медиана
    public double getMaxMediana(Map<Numbers, ResultPair> map){
        return map.entrySet().stream().max(medianaComparator).get().getValue().getMediana();
    }

    // среднее значение медианы
    public double getMiddleMediana(Map<Numbers, ResultPair> map){
        int sum = 0;
        for(ResultPair value:map.values()){
            sum += value.getMediana();
        }
        return sum / map.size();
    }
}
