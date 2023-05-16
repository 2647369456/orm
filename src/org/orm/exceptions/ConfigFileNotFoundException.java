package org.orm.exceptions;

/**
 * 配置文件没有找到时，抛出该异常
 */
public class ConfigFileNotFoundException extends RuntimeException{

    public ConfigFileNotFoundException(){}

    public ConfigFileNotFoundException(String msg){
        super(msg) ;
    }
}
