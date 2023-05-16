package org.orm.session;

import org.orm.Configuration;
import org.orm.JdbcExecutor;
import org.orm.SqlHandler;
import org.orm.exceptions.ResultCountException;
import org.orm.exceptions.SQLFormatException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class DefaultSqlSession implements SqlSession{
    private JdbcExecutor executor;

    public DefaultSqlSession(){
        this(false);
    }

    public DefaultSqlSession(boolean isAutoCommit){
        executor = new JdbcExecutor(isAutoCommit);
    }

    public void commit(){
        executor.commit();
    }

    public void rollback(){
        executor.rollback();
    }

    public void close(){
        executor.close();
    }

    private int insert(String sql,Object paramObj , boolean isGeneratedKey){
        if("insert".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            //是一个insert语句
            return executor.doUpdate(sql, isGeneratedKey, paramObj);
        }
        //代码至此，说明不是一个insert语句，不执行。 需要给使用者反馈（异常）
        throw new SQLFormatException("not a insert statement : " + sql);
    }
    public int insert(String sql,Object paramObj){
        return insert(sql,paramObj,false);
    }

    public int insert(String sql){
        return insert(sql,null);
    }

    /**
     * 实现数据保存后，需要获得自增主键值，并将其存储在paramObj对象的指定属性中
     * @param sql
     * @param paramObj
     * @param propertyName cno  , c-->setC
     * @return
     */
    public int insert(String sql ,Object paramObj , String propertyName){
        int count = insert(sql,paramObj,true) ;
        //额外处理一下关于自增主键值问题
        Long generatedKey = executor.getGeneratedKey();
        //利用反射实现复制
        //setCno(int cno) , setCno(long cno) , setCno(short cno)
        //因为set方法的参数类型不确定，所以不能通过名字直接找到方法
        //遍历查找
        String methodName ;
        if(propertyName.length()==1){
            //c
            methodName = "set" + propertyName.toUpperCase() ;
        }else {
            //                  set     c->C                                        no
            methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        }
        Method[] methods = paramObj.getClass().getMethods();
        for(Method method : methods){
            String mname = method.getName() ;
            if(mname.equalsIgnoreCase(methodName)){
                //此时就只根据名字找到了方法
                //generatedKey -- Long -->(Integer)Long --> int
                //set(long) , set(int) , set(short)
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

    public int update(String sql,Object paramObj){
        if("update".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            return executor.doUpdate(sql, false, paramObj);
        }
        throw new SQLFormatException("not a update statement : " + sql);
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




    public <T> List<T> selectList(String sql , Object paramObj , Class<T> rowType ){
        if("select".equalsIgnoreCase( sql.trim().substring(0,6) )){
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


    //-----------------加入configuration-------------------------
    private Configuration configuration;
    public DefaultSqlSession(Configuration configuration){
        this(configuration,false);
    }
    public DefaultSqlSession(Configuration configuration,boolean isAutoCommit){
        this.configuration = configuration ;
        executor = new JdbcExecutor(configuration,isAutoCommit);
    }
    //---------------动态sql新增API-----------------------
    @Override
    public int save(String sqlid) {
        return save(sqlid,null);
    }

    @Override
    public int save(String sqlid, Object paramObj) {
        SqlHandler handler = configuration.getHandler(sqlid);
        String propertyName = handler.getPropertyName();
        if (propertyName!=null && "".equalsIgnoreCase(propertyName)){
            return save(sqlid,paramObj,false);
        }
        int count=save(sqlid,paramObj,true);
        Long generatedKey = executor.getGeneratedKey();
        String methodName ;
        if(propertyName.length()==1){
            //c
            methodName = "set" + propertyName.toUpperCase() ;
        }else {
            //                  set     c->C                                        no
            methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        }
        Method[] methods = paramObj.getClass().getMethods();
        for(Method method : methods){
            String mname = method.getName() ;
            if(mname.equalsIgnoreCase(methodName)){
                //此时就只根据名字找到了方法
                //generatedKey -- Long -->(Integer)Long --> int
                //set(long) , set(int) , set(short)
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
    public int save(String sqlid,Object paramObj,boolean isGeneratedKey){
        SqlHandler handler = configuration.getHandler(sqlid);
        String sql=handler.getOldSql();
        if("insert".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            //是一个insert语句
            return executor.executeUpdate(sqlid, isGeneratedKey, paramObj);
        }
        //代码至此，说明不是一个insert语句，不执行。 需要给使用者反馈（异常）
        throw new SQLFormatException("not a insert statement : " + sql);
    }

    public int modify(String sqlid,Object paramObj){
        SqlHandler handler = configuration.getHandler(sqlid);
        String sql = handler.getOldSql();
        if("update".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            return executor.executeUpdate(sqlid, false, paramObj);
        }
        throw new SQLFormatException("not a update statement : " + sql);
    }
    public int modify(String sqlid){
        return modify(sqlid,null);
    }

    public int remove(String sqlid,Object paramObj){
        SqlHandler handler = configuration.getHandler(sqlid);
        String sql = handler.getOldSql();
        if("delete".equalsIgnoreCase( sql.trim().substring(0,6) )) {
            return executor.executeUpdate(sqlid, false, paramObj);
        }
        throw new SQLFormatException("not a delete statement : " + sql);
    }
    public int remove(String sqlid){
        return remove(sqlid,null);
    }


    public <T> List<T> searchList(String sqlid , Object paramObj ){
        SqlHandler handler = configuration.getHandler(sqlid);
        String sql = handler.getOldSql();
        Class rowType = handler.getRowType();
        if("select".equalsIgnoreCase( sql.trim().substring(0,6) )){
            return executor.executeQuery(sqlid,rowType,paramObj);
        }
        throw new SQLFormatException("not a select statement : " + sql) ;
    }
    public <T> List<T> searchList(String sqlid ){
        return searchList(sqlid,null);
    }


    public <T> T searchOne(String sql  , Object paramObj ){
        List<T> list = searchList(sql, paramObj);
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

    public <T> T searchOne(String sql ){
        return searchOne(sql,null);
    }

    @Override
    @SuppressWarnings("all")
    public <T> T getDaoProxy(Class<T> interfaceType) {
        final SqlSession currentSession = this ;
        //动态代理需要3个参数  ClassLoader ， interfaces , invocationHandler
        //动态代理执行时，会根据指定的接口产生代理类  （这个类以前没有，新产生的）
        //  新产生的类需要加载到jvm中才能使用， 利用类加载ClassLoader来加载。
        //      jdk提供了3个   bootstrap（jre） ， ext（ext.jar) , app（自定义）
        //类可以实现多个接口，所以需要提供接口数组（至少要有一个）
        //invocationhandler用来实现动态代理的调用功能。
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaceType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //正常来说，我们需要调用目标方法 。 目标是谁？ 应该是dao？
                        //后来我们发现，因为dao中的代码比较少，并且比较统一，所以代理就可以完成dao的操作
                        //  所以最终代理就代替了dao 。这个代理就可以理解成dao了。
                        //原来的dao做了什么事呢？
                        //  1 获得一个session 。
                        //      发现当前这个getDaoProxy这个方法，就是session对象的方法
                        //      这方法被执行了，说明session存在了
                        //      可以直接使用当前这个session
                        //  2 需要知道调用的是session的哪个方法，具体的sqlid是什么， 具体的参数是什么
                        //      参数就是代理得到的args参数
                        //      method可以方法名
                        //      interfaceType可知接口
                        //      接口名+"."+方法名 = sqlid
                        //      根据sqlid就可以到handler，就可以得到sql，就可以到curd语句
                        //SqlSession session = DefaultSqlSession.this ;
                        SqlSession session = currentSession ;

                        String sqlid = interfaceType.getName()+"."+method.getName();
                        SqlHandler handler = configuration.getHandler(sqlid);
                        String sqlOption = handler.getOldSql().substring(0, 6);

                        Object paramObj = null ;
                        if(args != null && args.length > 0){
                            //有参数
                            paramObj = args[0] ;
                        }

                        Object value = null ;
                        if("insert".equalsIgnoreCase(sqlOption)) {
                            value = session.save(sqlid,paramObj);
                        }else if("update".equalsIgnoreCase(sqlOption)){
                            value = session.modify(sqlid,paramObj);
                        }else if("delete".equalsIgnoreCase(sqlOption)){
                            value = session.remove(sqlid,paramObj) ;
                        }else if("select".equalsIgnoreCase(sqlOption)){
                            Class<?> returnType = method.getReturnType();
                            if(List.class.isAssignableFrom(returnType)){
                                value = session.searchList(sqlid,paramObj);
                            }else{
                                value = session.searchOne(sqlid,paramObj);
                            }
                        }

                        return value;
                    }
                }
        );
    }


}
