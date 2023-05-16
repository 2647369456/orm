package org.orm;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 对新规则进行处理 ， 主要分两部分 <br/>
 * 1. 将新sql 处理成 原始sql。在处理的过程中，存储?对应的参数名 <br/>
 *    insert into t_car(cname,color,price) values(null,#{cname},#{color},#{price})
 *    insert into t_car(cname,color,price) values(null,?,?,?) + [cname, color, price]
 * 2. 根据解析出来的参数名，从参数对象中获取对应的参数值 组成数组 [bmw ,red ,300000.0]
 */
public class SqlHandler {
    private String newSql;//新sql
    private String oldSql;//旧sql
    //装载#{}中的参数
    private List<String> paramNames;

   private static final Object[] DEFAULT_PARAM_VALUES={};
   private Object[] paramValues=DEFAULT_PARAM_VALUES;
    //insert独有
   private String propertyName;
   //select独有
    private Class rowType;
    /**
     *
     * @param sql insert into t_car(cname,color,price) values(null,#{cname},#{color},#{price}) <br/>
     *            “insert into t_car(cname,color,price) values(null,” <br/>
     *            ? <br/>
     *            “,#{color},#{price})“ <br/>
     *            eg. select * from t_car where cno = #{cno} <br/>
     *            "select * from t_car where cno = "
     *            ?
     */
    final static Pattern pattern=Pattern.compile("#\\{\\w+\\}");
    public void executeSql(String sql){
        paramNames=new ArrayList<>();
        Matcher matcher=pattern.matcher(sql);
        while (matcher.find()){
            //找到匹配正则的那组字符串 #{cname} #{color} #{price}
            String paramName=matcher.group();
            paramName=paramName.replaceAll("[#\\{\\}]","");
            paramNames.add(paramName.trim());
        }
        this.oldSql=matcher.replaceAll("?");
        System.out.println("sql : " + oldSql);
        System.out.println("params : " + paramNames);
    }

    /**
     * 根据之前得到的参数名，从传递的参数对象中获得对应的参数值
     * @param paramObj  这个对象有3种可能 （实际上可能更多） <br/>
     *                  是一个domain对象    executeParam(car)<br/>
     *                  是一个map对象  executeParam(map)<br/>
     *                  是一个简单类型对象（值） executeParam(10)<br/>
     */

    public void executeParam(Object paramObj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (paramObj==null){
            return;
        }
        paramValues=new Object[paramNames.size()];
        if(isSimple(paramObj.getClass())){
            for (int i = 0; i < paramValues.length; i++) {
                paramValues[i]=paramObj;
            }
        }else if(paramObj instanceof Map){
            Map map=(Map) paramObj;
            for (int i = 0; i < paramValues.length; i++) {
                String paramName=paramNames.get(i);
                paramValues[i]=map.get(paramName);
            }
        }else {
            Class c=paramObj.getClass();
            for (int i = 0; i < paramValues.length; i++) {
                String paramName=paramNames.get(i);
                String methodName;
                if (paramName.length()==1){
                    methodName="get"+paramName.toUpperCase();
                }else {
                    methodName="get"+paramName.substring(0,1).toUpperCase()+
                            paramName.substring(1);
                }
                Method method=c.getMethod(methodName);
                Object paramValue=method.invoke(paramObj);
                paramValues[i]=paramValue;
            }
        }
        System.out.println("values : " + Arrays.toString(paramValues));
    }
    private boolean isSimple(Class type){
        return  (type == int.class || type == Integer.class) ||
                (type == double.class || type == Double.class) ||
                (type == long.class || type == Long.class) ||
                (type == String.class) ||
                (type == Date.class) ;
    }
    /*---------------------getters------------------------*/
    public String getNewSql() {
        return newSql;
    }

    public String getOldSql() {
        return oldSql;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public Object[] getParamValues() {
        return paramValues;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class getRowType() {
        return rowType;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    public void setRowType(Class rowType) {
        this.rowType = rowType;
    }

}
