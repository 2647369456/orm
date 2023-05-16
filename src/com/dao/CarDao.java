package com.dao;

import com.domain.Car;
import com.util.SqlSessionFactoryUtil;
import org.orm.Configuration;
import org.orm.JdbcExecutor;
import org.orm.SqlSession;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CarDao {
    /**
     * 利用executor工具实现保存
     * @param car
     */
    public void saveByExecutor(Car car){
        String sql = "insert into t_car(cname,color,price) values(?,?,?)" ;
        JdbcExecutor executor = new JdbcExecutor(true) ;
        executor.doUpdate(sql,true,new Object[]{car.getCname(),car.getColor(),car.getPrice()}) ;
        System.out.println(executor.getGeneratedKey());
    }
    /**
     * 传递参数<list>集合实现批量保存
     * @param cars
     */
    public void savesByExecutor(List<Car> cars ){
        JdbcExecutor executor = new JdbcExecutor();
        String sql = "insert into t_car values(?,?,?,?)" ;
        int count=0;
        for(Car car : cars){
            executor.doUpdate(sql,true,new Object[]{car.getCno(),car.getCname(),car.getColor(),car.getPrice()});
            System.out.print(executor.getGeneratedKey()+"\t");
            count++;
        }
        System.out.println("\n"+"受影响行数:"+count);
        executor.commit();
    }
    public int findCount(){
        String sql = "select count(*) from t_car" ;
        JdbcExecutor executor=new JdbcExecutor();
        List<Integer> list = executor.doQuery(sql,int.class);
        return list.get(0);
    }
    public Map findMap(){
        String sql = "select count(*) , max(price) , min(price) from t_car " ;

        JdbcExecutor executor = new JdbcExecutor( );
        List<Map> list = executor.doQuery(sql,Map.class);
        return list.get(0);
    }

    public List<Car> findDomain(){
        String sql = "select cno,cname as car_name,color,price from t_car" ;
        JdbcExecutor executor = new JdbcExecutor();
        List<Car> list = executor.doQuery(sql,Car.class);
        return list;
    }
    public List<Car> findDomainNotAs(){
        String sql = "select cno,cname as car_name,color,price from t_car" ;
        JdbcExecutor executor = new JdbcExecutor();
        List<Car> list = executor.doQuery(sql,Car.class);
        return list;
    }
    public List<Car> findDomainNotAsOne(Long[] cno){
        String sql = "select cno,cname ,color,price from t_car where cno=?" ;
        JdbcExecutor executor = new JdbcExecutor();
        List<Car> list = executor.doQuery(sql,Car.class,cno);
        return list;
    }
    public void saveByNewSql(Car car){
        String sql = "insert into t_car(cname,color,price) value(#{cname},#{color},#{price})" ;
        JdbcExecutor executor = new JdbcExecutor() ;
        executor.doUpdate(sql,true,car) ;
        System.out.println(executor.getGeneratedKey());
        executor.commit();
    }
    public List<Car> savaNew(String cname){
        String sql="select cno,cname,color,price from t_car where cname=#{cname}";
        JdbcExecutor executor=new JdbcExecutor(true);
        List<Car> car=executor.doQuery(sql,Car.class,cname);
        return car;
    }
    //==============SqlSession====================

    public int saveBySqlSession(Car car){
        //准备将数据存入数据库
        String sql = "insert into t_car(cname,color,price) values" +
                "(#{cname},#{color},#{price})" ;

        //原来自己写jdbc
        //后来使用executor工具
        //现在使用SqlSession

        SqlSession session = new SqlSession();
        int count=session.insert(sql,car,"cno");
        session.commit();
        System.out.println("自增主键值为："+car.getCno()); //有值
        return count;
    }

//    public int sessionUpdate(Object... cname){
//        String sql="update t_car set price=500000 where=?";
//        int count=new SqlSession(true).update(sql,cname);
//        return count;
//    }
    public List<Car> findAllOfSession(){
        String sql="select * from t_car";
        return new SqlSession().selectList(sql,Car.class);
    }
    public List<Car> findOneOfSession(String cname){
        String sql="select * from t_car where cname=#{cname}";
        return new SqlSession().selectList(sql,cname,Car.class);
    }
    public void saveByConfiguration(Car car){
        //准备将数据存入数据库
        String sql = "insert into t_car(cname,color,price) values" +
                "(#{cname},#{color},#{price})" ;

        //此时需要人为指定配置信息
        Configuration config= new Configuration("com/mysql-jdbc.properties") ;
        SqlSession session = new SqlSession(config);
        System.out.println(car.getCno()); //无值
        session.insert(sql,car,"cno");
        session.commit();
        System.out.println(car.getCno()); //有值

    }
    public void saveByInterface(Car car){
        //准备将数据存入数据库
        org.orm.session.SqlSession session = SqlSessionFactoryUtil.mysqlFactory.openSession(true);
        session.save("com.sql.CarSql.save",car);
    }
    public List<Car> findAllByInterface(){
        org.orm.session.SqlSession session = SqlSessionFactoryUtil.mysqlFactory.openSession();
        List<Car> cars = session.searchList("com.sql.CarSql.findAll");
        return cars ;
    }


}
