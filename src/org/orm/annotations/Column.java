package org.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用在domain类中，用来指定某一个属性与字段的对应关系 <br/>
 * <pre>
 * eg.
 * private String cname ; //cname属性对应的是结果集中cname字段  rs.getString("cname");
 *
 * @Column("car_name")
 * private String cname ; //cname属性定的是结果集中car_name字段 rs.getString("car_name");
 * </pre>
 *
 */
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
/**
 * 指定当前属性的对应的字段名
 * @return
 */
 String value();
}
