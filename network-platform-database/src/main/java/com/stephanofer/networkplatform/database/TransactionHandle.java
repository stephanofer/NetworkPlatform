package com.stephanofer.networkplatform.database;

import java.sql.Connection;

public interface TransactionHandle {

    Connection connection();
}
