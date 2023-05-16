package com.test;

import com.dao.CarDao;

import java.util.Collection;
import java.util.Map;

/**
 * 测试查询结果组成map
 */
public class Test10FindMap {

    public static void main(String[] args) {
        CarDao dao =new CarDao();
        Map map = dao.findMap();
        System.out.println(map);
        Collection values = map.values();
        for(Object value : values){
            System.out.println(value + " : " + value.getClass().getName());
        }

    }

}
