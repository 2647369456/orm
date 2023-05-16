package com.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPatten {
    public static void main(String[] args) {
        String str = "123456abc222333abc654321abc";
        //1.利用Pattern类创建一个模式   理解为是一个正则表达式对象
        Pattern pattern = Pattern.compile("\\d{6}");//邮编
        //2.需要提供一个字符串
        //3.利用pattern模式对象创建一个匹配器
        Matcher matcher = pattern.matcher(str);
        //4.找寻字符串中出现满足上述格式的字串
        while(matcher.find()){//判断str是否满足规则
            System.out.println(matcher.group());//找到满足字符串格式的那一串文字
        }
    }
}
