package com.boxfox.router.real;

import com.boxfox.dao.ChangeDAO;
import com.boxfox.support.handler.AbstractDAHandler;
import com.boxfox.support.routing.Route;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by boxfox on 2017-07-13.
 */
@Route(uri="/real.do/checkconn", method = HttpMethod.POST)
public class CheckConnRouter extends AbstractDAHandler<ChangeDAO> {

    public CheckConnRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        String fakeToken, token = request.getParam("token");
        if((fakeToken = dao.getFakeToken(token))!= null){
            response.setStatusCode(200);
            response.end(fakeToken);
        }else
            response.setStatusCode(400);
        if(!response.ended())
            response.end();
        response.close();
    }
}
