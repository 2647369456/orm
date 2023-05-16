package org.orm;

import org.orm.annotations.Column;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * 将结果集处理成指定的类型
 */
public class ResultHandler<T> {
    public List<T> handle(ResultSet rs,Class<T> rowType) throws SQLException, NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<T> list=new ArrayList<>();
            while (rs.next()){
                Object row=cast(rs,rowType);
                list.add((T)row);
            }
        return list;
    }
    /**
     *将结果集中当前和一条记录组成对象
     * @param rs
     * @param rowType
     * @return
     */
    private Object cast(ResultSet rs,Class rowType) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        Object v=getValueByType(rs,1,rowType);
        if (v!=null){
            return v;
        }
        if(Map.class.isAssignableFrom(rowType)){
            Map<String,Object> row=new HashMap<>();
            ResultSetMetaData metaData= rs.getMetaData();
            for (int i=1;i<=metaData.getColumnCount();i++){
                String key= metaData.getColumnLabel(i);
                String typeName=metaData.getColumnTypeName(i);
                Object value=getValueByTypeName(rs,i,typeName);
                row.put(key,value);
            }
            return row;
        }
        //至此，传递的是实体类
        Object obj=rowType.newInstance();
        Method[] methods=rowType.getMethods();
        for (Method method:methods){
            String mname=method.getName();
            if(mname.startsWith("set")){
                String key;
                Column annotation=method.getAnnotation(Column.class);
                if (annotation!=null){
                    key=annotation.value();
                }else {
                    key=mname.substring(3);
                    if (key.length()==1){
                        key=key.toLowerCase();
                    }else {
                        //cno--->Cno
                        key=key.substring(0,1).toLowerCase()+key.substring(1);
                    }
                    Field field=rowType.getDeclaredField(key);
                    annotation=field.getAnnotation(Column.class);
                    if (annotation!=null){
                        //属性上设置了这个注解， 使用指定的字段名
                        key=annotation.value();
                    }else {
                        //没有注解，就是用属性名作为字段名，而这个属性名就是key
                        key=field.getName();
                    }
                }
                //代码至此，就获得了当前这个set方法（属性）对应的字段名
                //获得set方法的参数类型， 理论上set方法应该只有一个参数
                Class<?> paramType=method.getParameterTypes()[0];
                Object value=getValueByType(rs,key,paramType);
                if (value!=null){
                    method.invoke(obj,value);
                }
            }
        }
        return obj;
    }

    /**
     * 从结果集中按照字段的类型获得对应的字段值
     * @param rs
     * @param index
     * @param columnTypeName
     * @return
     */
    private Object getValueByTypeName(ResultSet rs , int index,String columnTypeName) throws SQLException {
        if("BIGINT".equals(columnTypeName)){
            return rs.getLong(index);
        }

        if("VARCHAR".equals(columnTypeName)){
            return rs.getString(index);
        }

        if("DOUBLE".equals(columnTypeName)){
            return rs.getDouble(index);
        }

        if("INT".equals(columnTypeName)){
            return rs.getInt(index);
        }

        if("CHAR".equals(columnTypeName)){
            return rs.getString(index);
        }

        if("DATE".equals(columnTypeName)){
            return rs.getDate(index);
        }

        return null ;
    }
    /**
     *
     * @param rs
     * @param key   有两种情况   int - index , string - name
     * @param type
     * @return
     * @throws SQLException
     */
    private Object getValueByType(ResultSet rs , Object key , Class type) throws SQLException {
        try {
            if (type == int.class || type == Integer.class) {
                if(key instanceof String) {
                    return rs.getInt((String)key);
                }else{
                    return rs.getInt((int)key);
                }
            }

            if (type == long.class || type == Long.class) {
                if(key instanceof String) {
                    return rs.getLong((String)key);
                }else{
                    return rs.getLong((int)key);
                }
            }

            if (type == double.class || type == Double.class) {
                if(key instanceof String) {
                    return rs.getDouble((String)key);
                }else{
                    return rs.getDouble((int)key);
                }
            }

            if (type == String.class) {
                if(key instanceof String) {
                    return rs.getString((String)key);
                }else{
                    return rs.getString((int)key);
                }
            }

            if (type == Date.class) {
                if(key instanceof String) {
                    return rs.getDate((String)key);
                }else{
                    return rs.getDate((int)key);
                }
            }
        }catch (SQLException e){
            System.out.println("[warning] Column '"+key+"' not found"+
                    "\n sql语句使用别名，但是你传递的sql语句没有使用别名");
        }
        return null ;
    }
}
