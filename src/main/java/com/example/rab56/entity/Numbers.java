package com.example.rab56.entity;

import java.util.Arrays;

//класс входных данных
public class Numbers {
    private int[] numbers;           //значения
    public Numbers(int[] nums){
        numbers = nums;
    }

    public Numbers(BulkParameter bulkParameter){    // инициализация посредством bulk параметра
        numbers = new int[]{
                bulkParameter.getA(),
                bulkParameter.getB(),
                bulkParameter.getC(),
                bulkParameter.getD()
        };
    }

    public int[] getNumbers(){
        return numbers;
    }

    @Override
    public String toString(){
        String result = "";
        for(int number:numbers){
            result = result + number + " ";
        }
        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Numbers numbers1 = (Numbers) o;
        return Arrays.equals(numbers, numbers1.numbers);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(numbers);
    }
}
