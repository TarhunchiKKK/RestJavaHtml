package com.example.rab56.memory;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryStorage {
    //хранилище (ключ - входные числа(Numbers), значение - полученный результат(ResultPair))
    private Map<Numbers, ResultPair> dataStorage = new HashMap<Numbers, ResultPair>();

    //добавление
    public void add(Numbers numbers,ResultPair resultPair){
        dataStorage.put(numbers, resultPair);
    }

    //размер хранилища
    public Integer size() {
        return dataStorage.size();
    }

    //получение ключей
    public Set<Numbers> getKeys(){
        return dataStorage.keySet();
    }

    //получение значения по ключу
    public ResultPair get(Numbers numbers){
        return dataStorage.get(numbers);
    }

    //получение результата по его медиане
    public ResultPair getByMediana(double mediana){
        for(ResultPair resultPair: dataStorage.values()){
            if(mediana == resultPair.getMediana()) return resultPair;
        }
        return null;
    }

    //получение результата по его среднему значению
    public ResultPair getByMiddleValue(double middleValue){
        for(ResultPair resultPair: dataStorage.values()){
            if(middleValue == resultPair.getMediana()) return resultPair;
        }
        return null;
    }

    //получение значений
    public List<ResultPair> getValues(){
        List<ResultPair> resultPairs = new ArrayList<ResultPair>();
        for(ResultPair resultPair: dataStorage.values()){
            resultPairs.add(resultPair);
        }
        return resultPairs;
    }
}
