package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.List;

public class Test12NewSqlQuery {
    public static void main(String[] args) {
        String cname="奔驰4";
        CarDao dao=new CarDao();
        List<Car> cars=dao.savaNew(cname);
        System.out.println(cars);
    }
}
