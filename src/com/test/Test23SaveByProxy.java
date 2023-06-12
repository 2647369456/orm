package com.test;

import com.domain.Car;
import com.sql.CarSql;
import com.util.SqlSessionFactoryUtil;
import org.orm.session.SqlSession;

import java.util.List;

public class Test23SaveByProxy {
    static SqlSession session = SqlSessionFactoryUtil.mysqlFactory.openSession(true);
    static CarSql dao = session.getDaoProxy(CarSql.class);
    public static void main(String[] args) {
        //正常开发中，应该是在页面或者控制台输入要添加的数据
        String cname = "保时捷" ;
        String color = "white" ;
        double price = 1500000.0 ;
        Car car1 = new Car(null,cname,color,price);

        //需要dao，原来自己new
        //现在使用代理(dao)
        //代理如何产生，由session产生，所以需要先获得一个session
        //session谁产生？ 工厂

        //dao.save(car);

        List<Car> cars = dao.findAll();
        for(Car c : cars){
            System.out.println(c);
        }
        Car car=dao.findById(2L);
        System.out.println("\33[33m"+car+"\33[m");
        System.out.println("测试");

    }
}
