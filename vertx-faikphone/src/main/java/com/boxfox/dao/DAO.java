package com.boxfox.dao;


import com.boxfox.support.utilities.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by boxfox on 2017-07-13.
 */
public abstract class DAO {
    public abstract void bind(Connection connection);

    private synchronized static PreparedStatement buildQuery(Connection connection, String sql, Object... args) {
        Log.query(sql);
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int placeholderCount = 1;
            for (Object o : args) {
                statement.setObject(placeholderCount++, o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;
    }

    public synchronized static ResultSet executeQuery(Connection connection, String sql, Object... args) {
        try {
            return buildQuery(connection, sql, args).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static int executeUpdate(Connection connection, String sql, Object... args) {
        try {
            return buildQuery(connection, sql, args).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
