package org.orm;

import org.orm.exceptions.ResultCountException;
import org.orm.exceptions.SQLFormatException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 提供与数据库交互相关的api <br/>
 *      提供的方法只支持新规则sql
 */
@Deprecated
public class SqlSession {
    private JdbcExecutor executor;
    public SqlSession(){this(false);}
    public SqlSession(boolean isAutoCommit){
        executor=new JdbcExecutor(isAutoCommit);
    }
    public void commit(){executor.commit();}
    public void rollback(){executor.rollback();}
    public void close(){executor.close();}

    private int insert(String sql,Object paramObj,boolean isGeneratedKey){
        if ("insert".equalsIgnoreCase(sql.trim().substring(0,6))){
            return executor.doUpdate(sql,isGeneratedKey,paramObj);
        }
        throw new SQLFormatException("not a insert statement:"+sql);
    }
    public int insert(String sql,Object paramObj){
        return this.insert(sql,paramObj,false);
    }
    public int insert(String sql){
        return this.insert(sql,null);
    }
    /**
     * 实现数据保存后，需要获得自增主键值，并将其存储在paramObj对象的指定属性中
     * @param sql
     * @param paramObj
     * @param propertyName cno  , c-->setC
     * @return
     */
    public int insert(String sql,Object paramObj,String propertyName){
        int count=this.insert(sql,paramObj,true);
        Long generatedKey= executor.getGeneratedKey();
        //反射
        String methodName;
        if (propertyName.length()==1){
            methodName="set"+propertyName.toUpperCase();
        }else {
            methodName="set"+propertyName.substring(0,1).toUpperCase()+propertyName.substring(1);
        }
        Method[] methods = paramObj.getClass().getMethods();
        for (Method method:methods){
            String mname=method.getName();
            if (mname.equalsIgnoreCase(methodName)){
                try {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType == int.class || parameterType == Integer.class) {
                        method.invoke(paramObj, generatedKey.intValue());
                    } else if (parameterType == short.class || parameterType == Short.class) {
                        method.invoke(paramObj, generatedKey.shortValue());
                    } else if (parameterType == long.class || parameterType == Long.class) {
                        method.invoke(paramObj, generatedKey);
                    }
                }catch (Exception e){
                    throw new RuntimeException(e);
                }

                break ;
            }
        }
        return count ;
    }

    /**
     *
     * update 和 delete 操作目前还没有在底层 JdbcExecutor 完善，这部分暂时搁置
     * 两种解决：1. 在当前类中使用原生的jdbc 操作，但是在读取配置url和driver就可以直接使用 JdbcExecutor ，给JdbcExecutor 提供 get方法获取 属性值
     *          在此类中 update和delete 的操作 方法中提供sql 和 参数
     *        2. 完善 JdbcExecutor
     * @param sql
     * @param paramObj
     * @return int
     * @see org.orm.SqlSession#update & delete
     */
    public int update(String sql,Object... paramObj){
        if ("update".equalsIgnoreCase(sql.trim().substring(0,6))){
            return executor.doUpdate(sql,false,paramObj);
        }
        throw new SQLFormatException("not a delete statement : " + sql);
    }
    public int update(String sql){
        return update(sql,null);
    }
    public int delete(String sql,Object paramObj){
        if("delete".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            return executor.doUpdate(sql, false, paramObj);
        }
        throw new SQLFormatException("not a delete statement : " + sql);
    }

    public int delete(String sql){
        return delete(sql,null);
    }

    public <T>List<T> selectList(String sql,Object paramObj,Class<T> rowType){
        if ("select".equalsIgnoreCase(sql.trim().substring(0,6))){
            return executor.doQuery(sql,rowType,paramObj);
        }
        throw new SQLFormatException("not a select statement : " + sql) ;
    }
    public <T> List<T> selectList(String sql  , Class<T> rowType ){
        return selectList(sql,null,rowType);
    }
    public <T> T selectOne(String sql  , Object paramObj ,  Class<T> rowType ){
        List<T> list = selectList(sql, paramObj, rowType);
        if(list == null || list.size() == 0){
            //没有找到数据
            return null ;
        }else if(list.size() == 1){
            //找到了那一条数据
            return list.get(0);
        }else{
            //找得了多条记录，说明存在问题，需要给与使用者反馈（异常）
            throw new ResultCountException("it was expected to be null or one , but it was " + list.size()) ;
        }
    }

    public <T> T selectOne(String sql  ,  Class<T> rowType ){
        return selectOne(sql,null,rowType);
    }
    private Configuration configuration  ;
    public SqlSession(Configuration configuration){
        this(configuration,false);
    }

    public SqlSession(Configuration configuration , boolean isAutoCommit){
        this.configuration = configuration ;
        executor = new JdbcExecutor(configuration,isAutoCommit);
    }

}
