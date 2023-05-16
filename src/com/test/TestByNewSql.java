package com.test;

import com.dao.CarDao;
import com.domain.Car;

public class TestByNewSql {
    public static void main(String[] args) {
        String cname = "哈弗5" ;
        String color = "white" ;
        double price = 200000.0 ;
        Car car = new Car(null,cname,color,price);
        CarDao dao =new CarDao();
        dao.saveByNewSql(car);
    }
}
