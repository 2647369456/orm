package org.orm.exceptions;

/**
 * 当使用selectOne方法查询的结果大于1条记录是抛出该异常
 */
public class ResultCountException extends RuntimeException{
    public ResultCountException(){}

    public ResultCountException(String msg){
        super(msg);
    }
}
