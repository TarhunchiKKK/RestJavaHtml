package com.example.rab56.entity;

import com.example.rab56.validators.Errors;

//объект для отправки ответа
public class ResultObject {
    private ResultPair resultPair;  //результат
    private Errors errors ;         //ошибки валидации
    public ResultObject(ResultPair r, Errors e){
        resultPair = r;
        errors = e;
    }

    public ResultPair getResultPair() {
        return resultPair;
    }

    public void setResultPair(ResultPair resultPair) {
        this.resultPair = resultPair;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }
    @Override
    public String toString() {
        String result = resultPair.toString();
        if(!errors.isEmpty()) result = result + errors.toString();
        return result;
    }
}
