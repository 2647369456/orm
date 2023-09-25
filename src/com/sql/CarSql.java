package com.sql;

import com.domain.Car;
import org.orm.annotations.Insert;
import org.orm.annotations.Select;


import java.util.List;

public interface CarSql {
    @Insert(value="insert into t_car values(null,#{cname},#{color},#{price})",propertyName = "cno")
    void save(Car car);

    @Select("select * from t_car")
    List<Car> findAll();

    @Select("select * from t_car where cno = #{cno}")
    Car findById(Long cid);

}
