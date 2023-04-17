package com.example.rab56.controllers;

import com.example.rab56.database.DbEntity;
import com.example.rab56.database.RepositoryService;
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

@RestController
public class BulkDbResultPairController {
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
    private MathService mathService;                                                  //сервис
    private NumbersValidator validator;                                               //валидатор
    private InMemoryStorage inMemoryStorage;                                          //хранилище
    private RepositoryService repositoryService;                                      // БД

    @Autowired
    public BulkDbResultPairController(MathService service, NumbersValidator validator,
                                      InMemoryStorage inMemoryStorage, RepositoryService repositoryService){
        this.mathService = service;
        this.validator = validator;
        this.inMemoryStorage = inMemoryStorage;
        this.repositoryService = repositoryService;
    }

    @PostMapping(value = "/getdbresultpairs", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> getResultPairs(@ModelAttribute("parameters") BulkParameterList parameters){
        Map<Numbers, ResultPair> resultMap = new HashMap<>();       // map входных и выходных значений
        Errors errors = new Errors();                               // ошибки валидации
        parameters.getParameters().forEach(bulkParameter -> {       // проход по списку bulk параметров
            Numbers numbers = new Numbers(bulkParameter);               // создание входного объекта
            Errors tempErrors = validator.Validate(numbers);            // валидация
            if(!tempErrors.isEmpty()) errors.add(tempErrors);           // есть ошибки - заносим их в список
            else {                                                      // нет ошибок
                ResultPair resultPair = mathService.getResult(numbers);     // создание выходного объекта
                resultMap.put(numbers, resultPair);                         // сохраняем в map
                repositoryService.save(numbers, resultPair);                // сохраняем в БД
            }
        });

        resultMap.entrySet().stream().forEach(                      // добавление map в кэш
                e -> inMemoryStorage.add(e.getKey(), e.getValue()));



        String response =   "<table>" +                             // таблица с кэшем и БД
                            "<th>БД</th><th>Кэш</th>" +
                            "<tr><td>"+ getTable(repositoryService.getAll())+ "</td>" +
                            "<td>" + getTable(resultMap) + "</td></tr>" +
                            "</table><br/><br/>";

        // значения агрегирующего функционала для медианы
        response = response + "Минимальная медиана: " + getMinMediana(resultMap) + "<br/>";
        response = response + "Максимальная медиана: " + getMaxMediana(resultMap) + "<br/>";
        response = response + "Средняя медиана: " + getMiddleMediana(resultMap) + "<br/><br/>";

        // значение агрегирующего функционала для среднего значения
        response = response + "Минимальное среднее значение: " + getMinMiddleValue(resultMap) + "<br/>";
        response = response + "Максимальное среднее значение: " + getMaxMiddleValue(resultMap) + "<br/>";
        response = response + "Среднее среднее значение: " + getMiddleMiddleValue(resultMap) + "<br/>";

        return ResponseEntity.ok(response + getErrorList(errors));
    }


    // получить таблицу данных из БД
    public String getTable(List<DbEntity> entities){
        String response = "";
        if(!entities.isEmpty()){
            response = response + "<table border=10 bgcolor='orange'>";
            response= response + "<th>Numbers</th><th>Mediana</th><th>Middle value</th>";
            for(DbEntity entity:entities){
                response = response + "<tr><td align='center'>" + entity.getNumbers() + "</td>";
                response = response + "<td align='center'>" + entity.getMediana() + "</td>";
                response = response + "<td align='center'>" + entity.getMiddleValue() + "</td></tr>";
            }
            response = response + "</table>";
        }
        return response;
    }

    // получить таблицу значений map
    public String getTable(Map<Numbers, ResultPair> map){
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
            response = response + "</table>";
        }
        return response;
    }

    // получить список ошибок
    public String getErrorList(Errors errors){
        String response = "";
        if(!errors.isEmpty()){
            response = "<p><ol>";
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
