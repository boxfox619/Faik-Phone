package com.boxfox.router;

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
@Route(uri="/real.do", method = HttpMethod.GET)
public class RealPhoneRouter extends AbstractDAHandler<ChangeDAO> {
    public RealPhoneRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        String type = request.getParam("type").toString();
        System.out.println(type + " request from " + request.getParam("token") + "to RealPhone Controller");

        if (type != null) {
            switch (type) {
                case "register":
                    response.end(registerRealPhone(dao, request.getParam("token"), request.getParam("pnum")));
                    break;
                case "send_message":
                    JSONObject object = new JSONObject(request.getParam("message"));
                    response.end(sendMessage(dao, object, request.getParam("token")));
                    break;
                case "reset_conn":
                    response.end(resetConnection(dao, request.getParam("token")));
                    break;
                case "reset_code":
                    response.end(resetCode(dao, request.getParam("token")));
                    break;
                case "reset_all":
                    response.end(resetAll(dao, request.getParam("token")));
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

    private String registerRealPhone(ChangeDAO dao, String token, String phoneNum) {
        return dao.insertRealPhoneToken(token, phoneNum) == true ?
                ResponseJsonUtil.makeCodeResponse("register_response", dao.getAuthCode(token)) :
                ResponseJsonUtil.makeErrorResponse("register_response", "Device Already Registered");
    }

    private String sendMessage(ChangeDAO dao, JSONObject json, String token) {
        MessageManager manager = new MessageManager(dao.getConnFromRealToken(token).getFakeToken());
        boolean response = manager.sendMessage(json.toString());
        return response == true ?
                ResponseJsonUtil.makeSuccessResponse("send_call_response", "Message Request Success") :
                ResponseJsonUtil.makeErrorResponse("send_call_response", "Message Request Failed");
    }

    private String resetConnection(ChangeDAO dao, String token) {
        return dao.resetConnection(token, true) == true ?
                ResponseJsonUtil.makeSuccessResponse("reset_conn_response", "Reset Success") : ResponseJsonUtil.makeErrorResponse("reset_conn_response", "Reset Failed");
    }

    private String resetCode(ChangeDAO dao, String token) {
        return dao.resetCode(token) == true ?
                ResponseJsonUtil.makeCodeResponse("reset_code_response", dao.getAuthCode(token)) :
                ResponseJsonUtil.makeErrorResponse("reset_code_response", "Reset Failed");
    }

    private String resetAll(ChangeDAO dao, String token) {
        return dao.resetAll(token, true) == true ?
                ResponseJsonUtil.makeSuccessResponse("reset_all_response", "Reset Success") :
                ResponseJsonUtil.makeErrorResponse("reset_all_response", "Reset Failed");
    }
}
