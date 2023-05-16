package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.Arrays;
import java.util.List;

public class Test02 {
    public static void main(String[] args) {
        List<Car> cars = Arrays.asList(
                new Car(null, "奔驰7", "black", 250000.0),
                new Car(null, "奔驰8", "black", 250000.0),
                new Car(null, "奔驰9", "black", 250000.0)
        );
        CarDao dao = new CarDao();
        dao.savesByExecutor(cars);
    }
}
