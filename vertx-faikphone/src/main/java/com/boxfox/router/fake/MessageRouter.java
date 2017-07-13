package com.boxfox.router.fake;

import com.boxfox.additional.MessageManager;
import com.boxfox.dao.ChangeDAO;
import com.boxfox.support.handler.AbstractDAHandler;
import com.boxfox.support.routing.Route;
import com.boxfox.support.utilities.response.ResponseJsonUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by boxfox on 2017-07-13.
 */
@Route(uri="/fake.do/send", method = HttpMethod.POST)
public class MessageRouter extends AbstractDAHandler<ChangeDAO> {

    public MessageRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        response.setStatusCode(sendMessage(request.getParam("token"), request.getParam("message")));
        response.close();
    }

    private int sendMessage(String token, String message){
        MessageManager manager = new MessageManager(token);
        boolean response = manager.doRequest(message);
        return response == true ?200:400;
    }

}
