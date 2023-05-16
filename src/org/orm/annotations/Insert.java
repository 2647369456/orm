package org.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来管理sql语句
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
    String value();
    /**
     * 在insert保存时，如果需要获得自增主键值，就通过该方法提供装载自增主键值的属性名
     * @return
     */
    String propertyName() default "" ;
}
