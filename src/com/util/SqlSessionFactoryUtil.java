package com.util;
import org.orm.SqlSessionFactory;

import java.io.File;

public class SqlSessionFactoryUtil {
    public final static SqlSessionFactory mysqlFactory  ;
    public  static SqlSessionFactory oracleFactory ;

    static String filePath;
    public static void setFilePath(String path){
        filePath=path;
    }
    static{
        mysqlFactory = new SqlSessionFactory("com/mysql-jdbc.properties");
            if (filePath!=null && !"".equals(filePath)) {
                oracleFactory = new SqlSessionFactory(new File(filePath));
            }
    }
}
