package com.example.rab56.database;

import org.springframework.data.jpa.repository.JpaRepository;

// интерфейс для взаимодействия с таблицами БД
public interface Repository extends JpaRepository<DbEntity, Integer> {
}
