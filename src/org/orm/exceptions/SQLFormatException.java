package org.orm.exceptions;

/**
 * 当调用session的api执行sql，但调用的方法与执行的sql不同时，抛出该异常 <br/>
 * session.insert( "delete.." );
 *
 */
public class SQLFormatException extends RuntimeException {

    public SQLFormatException(){}

    public SQLFormatException(String msg){
        super(msg) ;
    }

}
