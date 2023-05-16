package com.test;

import com.dao.CarDao;

/**
 * 测试查询结果组成简单的类型
 */
public class Test09FindCount {

    public static void main(String[] args) {
        CarDao dao =new CarDao();
        int count = dao.findCount();
        System.out.println(count);
    }

}
