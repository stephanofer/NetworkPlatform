package com.stephanofer.networkplatform.database;

import java.sql.Connection;

@FunctionalInterface
public interface SqlTask {

    void execute(Connection connection) throws Exception;
}
