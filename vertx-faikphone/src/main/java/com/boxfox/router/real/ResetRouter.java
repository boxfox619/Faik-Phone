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

/**
 * Created by boxfox on 2017-07-13.
 */
@Route(uri="/real.do/reset", method = HttpMethod.POST)
public class ResetRouter extends AbstractDAHandler<ChangeDAO> {
    public ResetRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        String type = request.getParam("type").toString();
        String token = request.getParam("token");
        if (type != null) {
            switch (type) {
                case "conn":
                    response.setStatusCode(dao.resetConnection(token, true)?200:400);
                    break;
                case "code":
                    response.setStatusCode(dao.resetCode(token)?200:400);
                    System.out.println("testas");
                    break;
                case "all":
                    response.setStatusCode(dao.resetAll(token, true)?200:400);
                    break;
                default:
                    System.out.println("Invalid Request");
                    response.end("Invalid Request");
            }
        } else {
            System.out.println("Invalied Request");
            response.end("Invalid Request");
        }
        response.close();
    }
}
