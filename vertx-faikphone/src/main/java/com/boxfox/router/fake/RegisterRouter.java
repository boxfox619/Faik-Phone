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
@Route(uri="/fake.do/register", method = HttpMethod.POST)
public class RegisterRouter extends AbstractDAHandler<ChangeDAO> {

    public RegisterRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        if(dao.insertFaikPhoneToken(request.getParam("token"), request.getParam("code"))){
            response.setStatusCode(200);
            response.end(dao.getPhoneNum(request.getParam("token")));
        }else{
            response.setStatusCode(400);
        }
        response.close();
    }
}
