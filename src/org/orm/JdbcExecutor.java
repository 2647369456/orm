package org.orm;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExecutor {
    //    static{
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver") ;
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
    //存储自增主键值
    private static final List<Long> DEFAULT_PARAM_VALUES = null;
    private List<Long> generatedKeys;
    private boolean isDriver = false;
    private Connection conn;
    private DataSource pool;
    private boolean isAutoCommit;

    public JdbcExecutor() {
        this(false);
    }

    public JdbcExecutor(boolean isAutoCommit) {
        this.isAutoCommit = isAutoCommit;
        conn = getConnection();
        try {
            conn.setAutoCommit(isAutoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDriver() {
        try {
            Class.forName(configuration.getDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        //判断，如果存在连接池，就从连接池中获得连接。 如果没有连接池，就创建连接
        try {
            if (pool != null) {
                return pool.getConnection();
            } else {
//                return DriverManager.getConnection(
//                        "jdbc:mysql://localhost:3306/duyi_orm?characterEncoding=utf8",
//                        "root",
//                        "root");
                return DriverManager.getConnection(
                        configuration.getUrl(),
                        configuration.getUsername(),
                        configuration.getPassword()
                );
            }
            //代码至此，就一定有一个连接了。 就可以设置其提交方式 （自动，手动）
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 为删除，修改和不需要自增主键值的insert来使用的
     *
     * @param sql
     * @param params
     * @return
     * @see org.orm.JdbcExecutor#doUpdate (String,boolean,Object)
     */
    @Deprecated
    public int doUpdate(String sql, Object... params) {
        return doUpdate(sql, false, params);
    }

    /**
     * 执行增删改操作<br/>
     * 用户1： executor.doUpdate( "insert..values(?,?)" , 1,"bmw" ); <br>
     * 用户2： executor.doUpdate( "delete..where cno=?" , 1); <br/>
     * 注意：理论上  参数值数量，类型，逻辑顺序，应该与sql中的?匹配的。 如果不匹配，也是使用者传参问题，和executor没有关系 <br>
     * executor直接抛出异常<br/>
     *
     * @see org.orm.JdbcExecutor#doUpdate(String, boolean, Object...)
     */
    @Deprecated
    public int doUpdate(String sql, boolean isGeneratedKeys, Object... params) {
        PreparedStatement stmt = null;
        int count = 0;
        try {
            if (isGeneratedKeys) {
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                stmt = conn.prepareStatement(sql);
            }
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }


            count = stmt.executeUpdate();
            if (isGeneratedKeys) {
                ResultSet rs = stmt.getGeneratedKeys();
                generatedKeys = new ArrayList<>();
                while (rs.next()) {
                    long id = rs.getLong(1);
                    generatedKeys.add(id);
                }
            }
            return count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {

            close(stmt);
        }
    }

    /**
     * 用来处理新规则的sql <br/>
     * 在新规则的sql中，所有?对应的参数最终组成一个paramObj
     *
     * @param sql
     * @param isGeneratedKeys
     * @param paramObj
     * @return
     * @see this#doUpdate(String, boolean, Object)
     */
    @Deprecated
    public int doUpdate(String sql, boolean isGeneratedKeys, Object paramObj) {
        try {
            SqlHandler handler = new SqlHandler();
            handler.executeSql(sql);
            handler.executeParam(paramObj);
            return this.doUpdate(handler.getOldSql(), isGeneratedKeys, handler.getParamValues());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行查询操作<br/>
     * 使用泛型
     * 第一个<T> 表示定义泛型，表示当前这个方法中，有一个类型不确定，暂时使用T来表示 <br/>
     * 哪个类型不确定呢？<br/>
     * 传参时指定的结果集中每条记录需要组成的对象类型不确定（Class<？>） ， 自然返回值List集合中每一个元素的类型也不确定（List<？>)<br/>
     * 但能确定的时，传参时，指定的rowtype是什么类型，最终返回值List中的对象就是什么类型<br/>
     * 第二个也就是List后面<T>，表示使用泛型 <br/>
     * 返回值List集合中的元素类型不确定，暂时使用之前定义的T来表示
     *
     * @param sql
     * @param rowType
     * @param params
     * @return list<T> <br/>
     * @see org.orm.JdbcExecutor#doQuery(String, Class, Object...)
     */
    @Deprecated
    public <T> List<T> doQuery(String sql, Class<T> rowType, Object... params) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            this.setPreparedParams(stmt, params);
            rs = stmt.executeQuery();
            ResultHandler handler = new ResultHandler();
            List<T> list = handler.handle(rs, rowType);
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, stmt);
        }
    }

    /**
     * @return 数组。放置直接返回集合时，利用引用传递对原集合数据造成破坏（不完全）
     */
    public Long[] getGeneratedKeys() {
        return generatedKeys.toArray(new Long[]{});
    }

    /**
     * 绝大多数的insert操作，都是单条的。
     *
     * @return
     */
    public Long getGeneratedKey() {
        return generatedKeys.get(0);
    }

    private void close(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        close(stmt);
    }

    private void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void commit() {
        try {
            if (!isAutoCommit && conn != null) {
                conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        if (!isAutoCommit && conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setPreparedParams(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * 处理新sql的查询方法
     *
     * @param sql
     * @param rowType
     * @param paramObj
     * @param <T>
     * @return
     * @see this#doQuery(String, Class, Object...) (String, Class, Object)
     */
    @Deprecated
    public <T> List<T> doQuery(String sql, Class<T> rowType, Object paramObj) {
        try {
            SqlHandler handler = new SqlHandler();
            handler.executeSql(sql);
            handler.executeParam(paramObj);
            return this.doQuery(handler.getOldSql(), rowType, handler.getParamValues());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //-----------------加入configuration-------------------------

    private Configuration configuration;

    public JdbcExecutor(Configuration configuration) {
        this(configuration, false);
    }

    public JdbcExecutor(Configuration configuration, boolean isAutoCommit) {
        this.configuration = configuration;
        this.isAutoCommit = isAutoCommit;
        //动态(信息)加载驱动
        if (!isDriver) {
            this.loadDriver();
        }
        conn = getConnection();
        try {
            conn.setAutoCommit(isAutoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //---------------动态sql新增API----------------

    public int executeUpdate(String sqlid, boolean isGeneratedKeys, Object paramObj) {

        //将新规则的sql 处理成原始sql，并完成相关的其他处理 （整理sql中？对应的参数名）（从paramObj中获得对应的参数值）
        //再调用之前的doUpdate完成操作
        try {
            SqlHandler handler = configuration.getHandler(sqlid);
            handler.executeParam(paramObj);

            return this.doUpdate(handler.getOldSql(), isGeneratedKeys, handler.getParamValues());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> executeQuery(String sqlid, Class<T> rowType, Object paramObj) {
        try {
            SqlHandler handler = configuration.getHandler(sqlid);
            handler.executeParam(paramObj);

            return this.doQuery(handler.getOldSql(), rowType, handler.getParamValues());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
