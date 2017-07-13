package com.boxfox.router.real;

import com.boxfox.additional.MessageManager;
import com.boxfox.dao.ChangeDAO;
import com.boxfox.support.handler.AbstractDAHandler;
import com.boxfox.support.routing.Route;
import com.boxfox.support.utilities.response.ResponseJsonUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.json.JSONObject;

import javax.xml.ws.Response;
import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Created by boxfox on 2017-07-13.
 */
@Route(uri = "/real.do/send", method = HttpMethod.POST)
public class MessageRouter extends AbstractDAHandler<ChangeDAO> {
    public MessageRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request = ctx.request();
        HttpServerResponse response = ctx.response();

        JSONObject object = new JSONObject(request.getParam("message"));
        response.setStatusCode(sendMessage(dao, object, request.getParam("token")));
        response.close();
    }

    private int sendMessage(ChangeDAO dao, JSONObject json, String token) {
        MessageManager manager = new MessageManager(dao.getConnFromRealToken(token).getFakeToken());
        boolean response = manager.sendMessage(json.toString());
        return response == true ? 200 : 400;
    }

}
