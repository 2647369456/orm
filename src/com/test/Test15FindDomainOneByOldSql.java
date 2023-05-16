package com.test;

import com.dao.CarDao;
import com.domain.Car;

import java.util.List;

public class Test15FindDomainOneByOldSql {
    public static void main(String[] args) {
        Long[] cno={2L};
        List<Car> car=new CarDao().findDomainNotAsOne(cno);
        System.out.println(car);
    }
}
