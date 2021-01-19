package com.jessepreiner.scheduling.readside;

import akka.japi.function.Function;
import akka.projection.jdbc.JdbcSession;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class PlainJdbcSession implements JdbcSession {

    private final Connection connection;

    PlainJdbcSession() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/akka?user=root");
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Result> Result withConnection(Function<Connection, Result> func) throws Exception {
        return func.apply(connection);
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }


}
