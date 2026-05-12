package com.stephanofer.networkplatform.database;

import java.sql.Connection;

@FunctionalInterface
public interface SqlExecutor<T> {

    T execute(Connection connection) throws Exception;
}
