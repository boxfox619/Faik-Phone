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
@Route(uri="/fake.do/reset", method = HttpMethod.POST)
public class ResetRouter extends AbstractDAHandler<ChangeDAO> {

    public ResetRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        String type = request.getParam("type").toString();
        switch (request.getParam("type").toString()) {
            case "all":
                response.setStatusCode(resetAll(dao, request.getParam("token")));
                break;
            case "conn":
                response.setStatusCode(resetConnection(dao, request.getParam("token")));
                break;
            default:
                System.out.println("Invalid Request");
                response.end("Invalid Request");
        }
        if(!response.ended())
            response.end();
        response.close();
    }

    private int resetAll(ChangeDAO dao, String token){
        return dao.resetAll(token, false) == true ?200:400;
    }

    private int resetConnection(ChangeDAO dao, String token){
        return dao.resetConnection(token, false) == true ?200:400;
    }


}
