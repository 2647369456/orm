package org.orm.session;

import java.util.List;

public interface SqlSession {
    void commit();
    void close();
    void rollback();
    int insert(String sql,Object paramObj,String propertyName);
    int insert(String sql,Object paramObj);
    int insert(String sql);
    int update(String sql,Object paramObj);
    int update(String sql);
    int delete(String sql,Object paramObj);
    int delete(String sql);
    <T> List<T> selectList(String sql , Object paramObj , Class<T> rowType) ;
    <T> List<T> selectList(String sql  , Class<T> rowType) ;
    <T> T selectOne(String sql , Object paramObj , Class<T> rowType) ;
    <T> T selectOne(String sql  , Class<T> rowType) ;
    //---------------动态sql新增API----------------
    int save(String sqlid) ;
    int save(String sqlid,Object paramObj) ;
    int modify(String sqlid) ;
    int modify(String sqlid,Object paramObj) ;
    int remove(String sqlid) ;
    int remove(String sqlid,Object paramObj) ;
    <T> List<T> searchList(String sqlid) ;
    <T> List<T> searchList(String sqlid,Object paramObj) ;
    <T> T searchOne(String sqlid) ;
    <T> T searchOne(String sqlid,Object paramObj) ;
    //--------------dao代理相关的api-----------------
    <T> T getDaoProxy(Class<T> interfaceType);
}
