package com.boxfox.support.handler;

import com.boxfox.dao.DAO;
import com.boxfox.support.utilities.Config;
import com.boxfox.support.utilities.Log;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.sql.*;

/**
 * Created by boxfox on 2017-07-13.
 */
public abstract class AbstractDAHandler<T extends DAO> implements Handler<RoutingContext> {
    private T dao;
    private static Connection connection;

    private static String url;
    private static String user = Config.getValue("dbUserName");
    private static String password = Config.getValue("dbPassword");

    static {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:mysql://localhost:");
        urlBuilder.append(Config.getValue("dbPort")).append("/");
        urlBuilder.append(Config.getValue("dbName")).append("?");
        urlBuilder.append("allowMultiQueries=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false");

        url = urlBuilder.toString();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public AbstractDAHandler(Class<T> clazz){
        try {
            dao = clazz.newInstance();
            dao.bind(connection);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(RoutingContext routingContext) {
        handle(routingContext, dao);
    }

    public abstract void handle(RoutingContext routingContext, T db);

}
