package com.example.rab56.entity;

import java.util.List;

// класс списка bulk параметров
public class BulkParameterList {
    private List<BulkParameter> parameters;         //список
    public BulkParameterList(List<BulkParameter> parameters){
        this.parameters = parameters;
    }

    public List<BulkParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<BulkParameter> parameters) {
        this.parameters = parameters;
    }
}
