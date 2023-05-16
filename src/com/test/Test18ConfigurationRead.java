package com.test;

import org.orm.Configuration;

public class Test18ConfigurationRead {
    public static void main(String[] args) {
        Configuration configuration=new Configuration("com/mysql-jdbc.properties");
        System.out.println(configuration.toString());
    }
}
