package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.List;

public class Test17SelectOneSession {
    public static void main(String[] args) {
        List<Car> cars=new CarDao().findOneOfSession("宝马13");
        System.out.println(cars);
    }
}
