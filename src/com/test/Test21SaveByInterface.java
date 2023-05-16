package com.test;

import com.dao.CarDao;
import com.domain.Car;

public class Test21SaveByInterface {

    public static void main(String[] args) {
        //正常开发中，应该是在页面或者控制台输入要添加的数据
        String cname = "大众12" ;
        String color = "white" ;
        double price = 250000.0 ;

        //准备将汽车信息存储起来，存储在数据库中
        //  谁负责和数据库交互？ 从技术而言，谁都可以通过jdbc与数据库交互， 从结构设计而言，dao负责交互
        //  应该使用dao来完成保存操作，传参时应该将参数组成对象
        Car car = new Car(null,cname,color,price);
        CarDao dao =new CarDao();
        dao.saveByInterface(car);
    }

}
