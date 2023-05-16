package com.test;

import com.dao.CarDao;
import com.domain.Car;

public class Test01 {
    public static void main(String[] args) {
        String cname = "宝马" ;
        String color = "白色" ;
        double price = 50000.0 ;
        Car car = new Car(null,cname,color,price);
        CarDao dao =new CarDao();
        dao.saveByExecutor(car);
    }
}
