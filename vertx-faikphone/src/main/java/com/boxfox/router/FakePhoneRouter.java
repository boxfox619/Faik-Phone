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

/**
 * Created by boxfox on 2017-07-13.
 */
@Route(uri="/fake.do", method = HttpMethod.POST)
public class FakePhoneRouter extends AbstractDAHandler<ChangeDAO> {

    public FakePhoneRouter() {
        super(ChangeDAO.class);
    }

    @Override
    public void handle(RoutingContext ctx, ChangeDAO dao) {
        HttpServerRequest request  = ctx.request();
        HttpServerResponse response = ctx.response();
        String type = request.getParam("type").toString();
        System.out.println(type + " request from " + request.getParam("token") + " to FakePhone Controller");
        switch (request.getParam("type").toString()) {
            case "register":
                response.end(registerFakePhone(dao, request.getParam("token"), request.getParam("code")));
                break;
            case "send_message":
                response.end(sendMessage(request.getParam("token"), request.getParam("message")));
                break;
            case "reset_all":
                response.end(resetAll(dao, request.getParam("token")));
                break;
            case "reset_conn":
                response.end(resetConnection(dao, request.getParam("token")));
                break;
            case "check_conn":
                response.end(checkConnection(dao, request.getParam("token")));
                break;
            default:
                System.out.println("Invalid Request");
                response.end("Invalid Request");
        }
        response.close();
    }

    private String sendMessage(String token, String message){
        MessageManager manager = new MessageManager(token);
        boolean response = manager.doRequest(message);
        return response == true ?
                ResponseJsonUtil.makeSuccessResponse("send_message_response", "Message Request Success") :
                ResponseJsonUtil.makeErrorResponse("send_message_response", "Message Request Failed");
    }

    private String registerFakePhone(ChangeDAO dao, String token, String code){
        return dao.insertFaikPhoneToken(token, code) == true ?
                ResponseJsonUtil.makeSuccessResponse("register_response", dao.getPhoneNum(token)) :
                ResponseJsonUtil.makeErrorResponse("register_response", "Certification Failed");
    }

    private String resetAll(ChangeDAO dao, String token){
        return dao.resetAll(token, false) == true ?
                ResponseJsonUtil.makeSuccessResponse("reset_all_response", "Reset Success") :
                ResponseJsonUtil.makeErrorResponse("reset_all_response", "Reset Failed");
    }

    private String resetConnection(ChangeDAO dao, String token){
        return dao.resetConnection(token, false) == true ?
                ResponseJsonUtil.makeSuccessResponse("reset_conn_response", "Reset Success") :
                ResponseJsonUtil.makeErrorResponse("reset_conn_response", "Reset Failed");
    }

    private String checkConnection(ChangeDAO dao, String token){
        String phoneNum;
        return (phoneNum = dao.getPhoneNum(token)) != null ?
                ResponseJsonUtil.makeCodeResponse("check_connection_response", phoneNum) :
                ResponseJsonUtil.makeErrorResponse("check_connection_response", "Not Registered");
    }

}
