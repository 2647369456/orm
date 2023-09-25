package org.orm;

import org.orm.annotations.Delete;
import org.orm.annotations.Insert;
import org.orm.annotations.Select;
import org.orm.annotations.Update;
import org.orm.exceptions.ConfigFileNotFoundException;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    @Override
    public String toString() {
        return "Configuration{" +
                "driver='" + driver + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SqlHandler getHandler(String sqlid) {
        return sqlMap.get(sqlid);
    }

    private String driver;
    private String url;
    private String username;
    private String password;
    private Map<String, SqlHandler> sqlMap = new HashMap<>();

    /**
     * 支持在classpath目录中读取配置文件 <br/>
     * new Configuration("com/mysql-jdbc.properties");
     *
     * @param classpathFile
     */
    public Configuration(String classpathFile) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile);
        if (is == null) {
            throw new ConfigFileNotFoundException(classpathFile);
        }
        readConfig(is);
    }

    /**
     * 支持任意路径下的配置文件<br/>
     * File file =new File("f:/z/oracle-jdbc.properties); <br/>
     * new Configuration(file)
     *
     * @param file
     */
    public Configuration(File file) {
        if (!file.exists()) {
            //配置文件不存在
            throw new ConfigFileNotFoundException(file.getPath());
        }
        try {
            FileInputStream is = new FileInputStream(file);
            readConfig(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void readConfig(InputStream is) {
        try {
            Properties p = new Properties();
            p.load(is);
            this.driver = p.getProperty("driver");
            this.url = p.getProperty("url");
            this.username = p.getProperty("username");
            this.password = p.getProperty("password");
            String sqlMapper = p.getProperty("sql-mapper");
            executorSqls(sqlMapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void executorSqls(String sqlMapper) {
        if (sqlMapper == null || "".equalsIgnoreCase(sqlMapper)) {
            return;
        }
        String[] interfaceNames = sqlMapper.replace(" ", "").split(",");
        for (String interfaceName : interfaceNames) {
            try {
                Class<?> c = Class.forName(interfaceName);
                executeSql(c);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("----sql executor over------");
    }

    private void executeSql(Class c) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            Annotation annotation;
            if ((annotation = method.getAnnotation(Insert.class)) != null) {
                Insert sql = (Insert) annotation;
                String newSql = sql.value();
                String propertyName = sql.propertyName();
                SqlHandler handler = new SqlHandler();
                handler.executeSql(newSql);
                handler.setPropertyName(propertyName);
                String key = c.getName() + "." + method.getName();
                sqlMap.put(key, handler);
            } else if ((annotation = method.getAnnotation(Update.class)) != null) {
                Update sql = (Update) annotation;
                String newSql = sql.value();
                SqlHandler handler = new SqlHandler();
                handler.executeSql(newSql);
                String key = c.getName() + "." + method.getName();
                sqlMap.put(key, handler);
            } else if ((annotation = method.getAnnotation(Delete.class)) != null) {
                Delete sql = (Delete) annotation;
                String newSql = sql.value();
                SqlHandler handler = new SqlHandler();
                handler.executeSql(newSql);
                String key = c.getName() + "." + method.getName();
                sqlMap.put(key, handler);
            } else if ((annotation = method.getAnnotation(Select.class)) != null) {
                //方法上有Select注解，需要额外考虑返回类型
                Select sql = (Select) annotation;
                String newSql = sql.value();
                SqlHandler handler = new SqlHandler();
                handler.executeSql(newSql);
                //还需要获得结果类型，也就是返回类型 Car 或 返回类型List中的泛型 List<Car>
                Class<?> returnType = method.getReturnType();
                if (List.class.isAssignableFrom(returnType)) {
                    //返回类型是一个List集合，我们需要的是泛型
                    //ParameterizedType表示带参数的类型，有泛型
                    ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                    Class rowType = (Class) parameterizedType.getActualTypeArguments()[0];
                    handler.setRowType(rowType);
                } else {
                    handler.setRowType(returnType);
                }
                String key = c.getName() + "." + method.getName();
                sqlMap.put(key, handler);
            }
        }
    }
}
