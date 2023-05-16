package com.test;

import com.domain.Car;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class TestReflect {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {Scanner sc=new Scanner(System.in);

        Object[] objects={12345L,"宝马","白色",255555.00};

        Class clazz=Class.forName("com.domain.Car");
        Car car= (Car) clazz.newInstance();
        Field[] fields=clazz.getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            String methodName=fields[i].getName();
            Class type=fields[i].getType();
            methodName="set"+methodName.substring(0,1).toUpperCase()+methodName.substring(1);
            System.out.println(methodName);
           Method method=clazz.getMethod(methodName,type);
           method.setAccessible(true);
           method.invoke(car,objects[i]);


        }
        System.out.println(car);
    }

}
