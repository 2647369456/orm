package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.List;

/**
 * 测试将结果集处理成domain对象（Car）
 */
public class Test14FindDomainNotAs {

    public static void main(String[] args) {
        CarDao dao = new CarDao();
        List<Car> cars = dao.findDomainNotAs();
        for(Car car : cars){
            System.out.println(car);
        }
    }
}
