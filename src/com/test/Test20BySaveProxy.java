package com.test;

import com.domain.Car;
import com.sql.CarSql;
import com.util.SqlSessionFactoryUtil;
import org.orm.session.SqlSession;

public class Test20BySaveProxy {
    static SqlSession session = SqlSessionFactoryUtil.mysqlFactory.openSession(true);
    static CarSql dao = session.getDaoProxy(CarSql.class);
    public static void main(String[] args) {
        //正常开发中，应该是在页面或者控制台输入要添加的数据
        String cname = "别克" ;
        String color = "white" ;
        double price = 300000.0 ;

        //准备将汽车信息存储起来，存储在数据库中
        //  谁负责和数据库交互？ 从技术而言，谁都可以通过jdbc与数据库交互， 从结构设计而言，dao负责交互
        //  应该使用dao来完成保存操作，传参时应该将参数组成对象
        Car car = new Car(null,cname,color,price);
        dao.save(car);


    }
}
