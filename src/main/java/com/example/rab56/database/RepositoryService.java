package com.example.rab56.database;

import com.example.rab56.entity.Numbers;
import com.example.rab56.entity.ResultPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
// сервис для работы с БД
@Service
public class RepositoryService {
    @Autowired
    private Repository repository;

    // добавление объекта в БД
    public void save(Numbers numbers, ResultPair resultPair){
        repository.save(new DbEntity(numbers, resultPair));
    }

    // получение всех объектов из БД
    public List<DbEntity> getAll(){
        return repository.findAll();
    }
}
