package com.boxfox.support.utilities.response;

import org.json.JSONObject;

/**
 * Created by dsm_025 on 2017-04-06.
 */
public class ResponseJsonUtil {
    public static String makeSuccessResponse(String type, String message){
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("state", true);
        object.put("message", message);
        return object.toString();
    }
    public static String makeErrorResponse(String type, String message){
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("state", false);
        object.put("message", message);
        return object.toString();
    }
    public static String makeCodeResponse(String type, String code){
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("state", true);
        object.put("message", code);
        return object.toString();
    }
}
