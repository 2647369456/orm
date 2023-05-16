package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.Arrays;
import java.util.List;

public class Test16SelectAllSession {
    public static void main(String[] args) {
        CarDao dao = new CarDao();
        List<Car> cars = dao.findAllOfSession();
        for (Car car : cars) {
            System.out.println(car);
        }
    }
}
