package com.example.rab56.controllers;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultObject;
import com.example.rab56.entity.ResultPair;
import com.example.rab56.exceptions.*;
import com.example.rab56.memory.InMemoryStorage;
import com.example.rab56.services.MathService;
import com.example.rab56.validators.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
public class ResultPairController {
    private String leaveBack = "<br/><a href='http://localhost:8080/redirect'>" +
            "Вернуться к результатам</a>";
    private static Logger logger = LoggerFactory.getLogger(ResultPairController.class);  //логгирование
    private MathService service;                                                      //сервис
    private NumbersValidator validator;                                               //валидатор
    private InMemoryStorage inMemoryStorage;                                          //хранилище
    @Autowired
    public ResultPairController(MathService service, NumbersValidator validator,
                                InMemoryStorage inMemoryStorage){          //конструктор
        this.service = service;
        this.validator = validator;
        this.inMemoryStorage = inMemoryStorage;
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
            ResultPair resultPair = service.getResult(numbers);                 //получение результата
            inMemoryStorage.add(numbers, resultPair);                           //сохранение полученного результата
            return ResponseEntity.ok(
                    new ResultObject(resultPair, new Errors()).toString()+
                            leaveBack);     //все ок => возвращаем ответ
        } catch(ServerException exc){                                           //ловим ошибку сервера
            logger.error(exc.getMessage());                                     //логгирование
            errors.add(exc.getMessage());                                       //+ 1 ошибка
            errors.setStatus(HttpStatus.BAD_REQUEST.name());                    //установка статуса
            return new ResponseEntity<>(new ResultObject(new ResultPair(), errors).toString()
                    + leaveBack, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    //ввод еще одной последовательности из 4 чисел
    @GetMapping(value = "/input", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String input(){
        //String response = getAllResultPairs("0", 0.0);
        String response = "<form method='GET' action='http://localhost:8080/resultpair' modelAttribute='numbers'>";
        for(int i = 0; i < 4; i++){
            response = response + "<label>Number " + i + "</label>";        //тег <label>
            response = response + "<input type='number' required name='nums'><br/>"; //тег <input>
        }
        response = response + "<input type='submit' value='Добавить:'>";    //отправка на сервер
        response = response + "</form><br/>";                               //конец формы
        return response;
    }

    @GetMapping(value="/redirect", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Object> redirect(){
        return getAllResultPairs(0, 0.0);
    }

    //вывод всех результатов
    @GetMapping(value = "/allresultpairs", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getAllResultPairs(int operation, double valueToFind){
        String response = "<html>";
        if(inMemoryStorage.size() != 0) {
            response = response + "<table border='10' bgcolor='#808000'>";          //оливковый цвет таблицы
            response = response + "<th>Numbers</th><th>Mediana</th><th>Middle value</th>";
            for (Numbers key : inMemoryStorage.getKeys()) {                            //проход по ключам
                String numbers = "";                                                    //строка со входными числами
                for (int number : key.getNumbers()) {                                   //добавление входных чисел в строку
                    numbers = numbers + number + ", ";
                }
                response = response + "<tr><td align='center'>" + numbers + "</td>";                     //столбец со входными числами
                ResultPair resultPair = inMemoryStorage.get(key);                                        //результат для данного ключа
                response = response + "<td align='center'>" + resultPair.getMediana() + "</td>";         //столбец с медианой
                response = response + "<td align='center'>" + resultPair.getMiddleValue() + "</td></tr>";//столбец со средним значением
            }
            response = response + "</table>";                                       //конец таблицы
        }

        //форма для поиска по медиане
        response = response + "<p><form method='GET' action='http://localhost:8080/allresultpairs'>" +
                "<label>Find by mediana:</label>" +
                "<input type='number' name='valueToFind'>" +
                "<input type='hidden' value='1' name='operation'>" +
                "<input type='submit' value='Find'>" +
                "</form></p>";
        //форма для поиска по среднему значению
        response = response + "<p><form method='GET' action='http://localhost:8080/allresultpairs'>" +
                "<label>Find by middle value:</label>" +
                "<input type='number' name='valueToFind'>" +
                "<input type='hidden' value='2'  name='operation'>" +
                "<input type='submit' value='Find'>" +
                "</form></p>";
        response = response + input();
        //ссылка на еще один ввод данных
        // response = response + "<a href='http://localhost:8080/input'>Input more</a>";
        response = response + "</html>";
        switch(operation){
            case 1:
                response = response + "<br/>Искомое значение:" + getByMediana(valueToFind).getBody().toString();
                break;
            case 2:
                response = response + "<br/>Искомое значение:" + getByMiddleValue(valueToFind).getBody().toString();
                break;
        }
        return  ResponseEntity.ok(response);
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
        service.resetCounters();
    }

    @GetMapping(value = "/getcounters", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String getCounters(){
        String response = "<html><table border='7' bgcolor='#808000'>";
        response = response + "<th>Синхронизированный</th><th>Несинхронизированный</th>";
        response = response + "<tr><td>" + service.getSynchronizedCounter() + "</td>";
        response = response + "<td>" + service.getUnsynchronizedCounter() + "</td></tr>";
        response = response = response + "</table></html>";
        return response;
    }
}
