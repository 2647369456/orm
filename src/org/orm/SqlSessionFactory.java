package org.orm;

import org.orm.exceptions.ConfigFileNotFoundException;
import org.orm.session.DefaultSqlSession;

import java.io.File;

/**
 * 负责创建configuration<br/>
 *      一个工厂可以创建多个SqlSession对象与数据库交互n次<br/>
 *      一个工厂创建的session应该是和一个数据库交互的<br/>
 *      如果需要和多个数据库交互，就应该产生多个工厂。<br/>
 *      一个工厂只需要读取一次配置信息 <br/>
 * 负责创建SqlSession<br/>
 *      每和数据库交互一次，都需要创建一个session (多实例）<br/>
 * 负责为session传递config
 */
public class SqlSessionFactory {
    private Configuration configuration ;

    public SqlSessionFactory(String classpathFile){
        this.configuration = new Configuration(classpathFile) ;
    }

    public SqlSessionFactory(File file){
        if (file!=null && file.exists()){
            this.configuration = new Configuration(file) ;
        }else {
            throw new ConfigFileNotFoundException(file+"路径为空或者不是一个文件");
        }
    }


    public org.orm.session.SqlSession openSession(){
        return new DefaultSqlSession(configuration,false);
    }

    public org.orm.session.SqlSession openSession(boolean isAutoCommit){
        return  new DefaultSqlSession(configuration,isAutoCommit) ;
    }

}
