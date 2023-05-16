package com.sql;

import org.orm.annotations.Sql;

public interface UserSql {

    @Sql("insert into t_user values(#{uno},#{uname})")
    public void save();

}
