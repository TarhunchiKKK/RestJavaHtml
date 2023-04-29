package com.example.rab56.controllers;

import com.example.rab56.database.DbEntity;
import com.example.rab56.database.RepositoryService;
import com.example.rab56.entity.BulkParameterList;
import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultObject;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.exceptions.*;
import com.example.rab56.memory.InMemoryStorage;
import com.example.rab56.services.CounterService;
import com.example.rab56.services.MathService;
import com.example.rab56.validators.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.CompletableFuture;


@RestController
public class ResultPairController {
    private String leaveBack = "<br/><a href='http://localhost:8080/redirect'>" +
            "Вернуться к результатам</a>";
    private String inputLink = "<br/><a href='http://localhost:8080/form?count=1'>Ввести еще</a>";
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
    private CounterService counterService;
    private InMemoryStorage inMemoryStorage;                                          //хранилище
    private RepositoryService repositoryService;                                      // БД

    @Autowired
    public ResultPairController(MathService service, NumbersValidator validator,CounterService counterService,
                                      InMemoryStorage inMemoryStorage, RepositoryService repositoryService){
        this.mathService = service;
        this.validator = validator;
        this.counterService = counterService;
        this.inMemoryStorage = inMemoryStorage;
        this.repositoryService = repositoryService;
    }

    @GetMapping(value = "/resultpair", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> getResultPair(@ModelAttribute("numbers") Numbers numbers) {
        Errors errors = validator.Validate((numbers));                          //получение ошибок валидации
        if(!errors.isEmpty()) {                                                 //есть ошибки валидации
            errors.setStatus(HttpStatus.BAD_REQUEST.name());
            logger.error("Parameter is not valid");
            return new ResponseEntity<>(
                    new ResultObject(new ResultPair(), errors).toString() + leaveBack
                    , HttpStatus.BAD_REQUEST);
        }

        try{
            counterService.incrementSynchronizedCount();
            counterService.incrementUnsynchronizedCount();
            ResultPair resultPair = mathService.getResult(numbers);             //получение результата
            CompletableFuture.runAsync(()->inMemoryStorage.add(numbers, resultPair));                           //сохранение полученного результата
            CompletableFuture.runAsync(()->repositoryService.save(numbers, resultPair));
//            inMemoryStorage.add(numbers, resultPair)
//            repositoryService.save(numbers, resultPair);
            return getTables(errors);
        } catch(ServerException exc){                                           //ловим ошибку сервера
            logger.error(exc.getMessage());                                     //логгирование
            errors.add(exc.getMessage());                                       //+ 1 ошибка
            errors.setStatus(HttpStatus.BAD_REQUEST.name());                    //установка статуса
            return new ResponseEntity<>(new ResultObject(new ResultPair(), errors).toString()
                    + leaveBack, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping(value="/form", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String form(int count){
        String response = "<p><form method='GET' action='http://localhost:8080/form'>" +
                "               <input type='number' name='count' value='" + count + "'>" +
                "               <input type='submit' value='Применить'></form></p>";
        if(count == 1){
            response = response + "<form method='GET' action='http://localhost:8080/resultpair' modelAttribute='numbers'>";
            for(int i = 0; i < 4; i++){
                response = response + "<label>Number " + i + "</label>";        //тег <label>
                response = response + "<input type='number' required name='nums'><br/>"; //тег <input>
            }
            response = response + "<input type='submit' value='Добавить:'>";    //отправка на сервер
            response = response + "</form><br/>";                               //конец формы
            return response;
        }
        else{
            response = response + "<form method='POST' action='http://localhost:8080/getdbresultpairs' modelAttribute='parameters'>";
            for(int j = 0; j < count; j++) {
                response = response +
                        "<p>" +
                        "        <label>Число:</label>" +
                        "        <input type = 'number' name='parameters[" + j+ "].a' required>" +
                        "    </p>" +
                        "    <p>" +
                        "        <label>Число:</label>" +
                        "        <input type = 'number' name='parameters[" + j + "].b' required>" +
                        "    </p>" +
                        "    <p>" +
                        "        <label>Число:</label>" +
                        "        <input type = 'number' name='parameters[" + j+ "].c' required>" +
                        "    </p>" +
                        "    <p>" +
                        "        <label>Число:</label>" +
                        "        <input type = 'number' name='parameters[" + j+ "].d' required>'" +
                        "    </p><br/>";
            }
            response = response + "<input type='submit' value='Добавить:'>";    //отправка на сервер
            response = response + "</form><br/>";                               //конец формы
            return response;
        }
    }

    @GetMapping(value="/redirect", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Object> redirect(){
        return getTables(new Errors());
    }


    //поиск результата по медиане
    @GetMapping("/getbymediana")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getByMediana(double mediana){
        ResultPair response = inMemoryStorage.getByMediana(mediana);
        if(response == null) return ResponseEntity.ok("No element with such mediana");
        else return ResponseEntity.ok(response);
    }

    //поиск результата по среднему значению
    @GetMapping("/getbymiddlevalue")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getByMiddleValue(double middleValue){
        ResultPair response = inMemoryStorage.getByMiddleValue(middleValue);
        if(response == null) return ResponseEntity.ok("No element with such mediana");
        else return ResponseEntity.ok(response);
    }

    //кол-во элементов в хранилище
    @GetMapping("/storagesize")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getSize(){
        return new ResponseEntity<>(inMemoryStorage.size(), HttpStatus.OK);
    }




    @GetMapping("/resetcounters")
    public void resetCounters(){
        counterService.resetCounters();
    }

    @GetMapping(value = "/getcounters", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String getCounters(){
        String response = "<html><table border='7' bgcolor='#808000'>";
        response = response + "<th>Синхронизированный</th><th>Несинхронизированный</th>";
        response = response + "<tr><td>" + counterService.getSynchronizedCount() + "</td>";
        response = response + "<td>" + counterService.getUnsynchronizedCount() + "</td></tr>";
        response = response = response + "</table></html>";
        return response;
    }

    /*----------------------------------------------------------------------------------------------*/

    @PostMapping(value = "/getdbresultpairs", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> getResultPairs(@ModelAttribute("parameters") BulkParameterList parameters){
        Map<Numbers, ResultPair> resultMap = new HashMap<>();       // map входных и выходных значений
        Errors errors = new Errors();                               // ошибки валидации
        parameters.getParameters().forEach(bulkParameter -> {       // проход по списку bulk параметров
            Numbers numbers = bulkParameter.toNumbers();                // создание входного объекта
            Errors tempErrors = validator.Validate(numbers);            // валидация
            if(!tempErrors.isEmpty()) errors.add(tempErrors);           // есть ошибки - заносим их в список
            else {                                                      // нет ошибок
                counterService.incrementSynchronizedCount();
                counterService.incrementUnsynchronizedCount();
                ResultPair resultPair = mathService.getResult(numbers);     // создание выходного объекта
                CompletableFuture.runAsync(()->resultMap.put(numbers, resultPair));
                CompletableFuture.runAsync(() -> repositoryService.save(numbers, resultPair));
//                resultMap.put(numbers, resultPair);                         // сохраняем в map
//                repositoryService.save(numbers, resultPair);                // сохраняем в БД
            }
        });

        resultMap.entrySet().stream().forEach(                      // добавление map в кэш
                e -> inMemoryStorage.add(e.getKey(), e.getValue()));
        return getTables(errors);
    }

    @PostMapping(value = "/gettables", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> getTables(Errors errors){
        String response =   "<table>" +                             // таблица с кэшем и БД
                "<th>БД</th><th>Кэш</th>" +
                "<tr><td>"+ getTable(repositoryService.getAll())+ "</td>" +
                "<td>" + getTable(inMemoryStorage.getMap()) + "</td></tr>" +
                "</table><br/><br/>";

        // значения агрегирующего функционала для медианы
        response = response + "Минимальная медиана: " + getMinMediana(inMemoryStorage.getMap()) + "<br/>";
        response = response + "Максимальная медиана: " + getMaxMediana(inMemoryStorage.getMap()) + "<br/>";
        response = response + "Средняя медиана: " + getMiddleMediana(inMemoryStorage.getMap()) + "<br/><br/>";

        // значение агрегирующего функционала для среднего значения
        response = response + "Минимальное среднее значение: " + getMinMiddleValue(inMemoryStorage.getMap()) + "<br/>";
        response = response + "Максимальное среднее значение: " + getMaxMiddleValue(inMemoryStorage.getMap()) + "<br/>";
        response = response + "Среднее среднее значение: " + getMiddleMiddleValue(inMemoryStorage.getMap()) + "<br/>";

        return ResponseEntity.ok(response + getErrorList(errors) + inputLink);
    }


    // получить таблицу данных из БД
    public String getTable(List<DbEntity> entities){
        String response = "";
        if(!entities.isEmpty()){
            response = response + "<table border=10 bgcolor='#228B22'>";
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
            response = response + "<table border='10' bgcolor='#48D1CC'>";
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
