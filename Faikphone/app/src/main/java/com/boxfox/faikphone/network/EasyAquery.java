package com.boxfox.faikphone.network;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by boxfox on 2017-07-13.
 */

public class EasyAquery {
    private AQuery aq;
    private Map<String, Object> params;

    private String url;

    public EasyAquery(Context context) {
        aq = new AQuery(context);
        params = new HashMap<>();
        params.put("token", FirebaseInstanceId.getInstance().getToken());
    }

    public EasyAquery setUrl(String url) {
        this.url = url;
        return this;
    }

    public EasyAquery addQuery(String key, String value) {
        url += (url.contains("?") ? "&" : "?") + key + "=" + value;
        return this;
    }

    public EasyAquery addParam(String key, Object data) {
        params.put(key, data);
        return this;
    }

    public EasyAquery post(Class clazz, AjaxCallback callback) {
        aq.ajax(url, params, clazz, callback);
        return this;
    }

    public EasyAquery post() {
        aq.ajax(url, params, String.class, new AjaxCallback<String>());
        return this;
    }

    public EasyAquery get(Class clazz, AjaxCallback callback) {
        aq.ajax(url, clazz, callback);
        return this;
    }

    public EasyAquery get() {
        aq.ajax(url, String.class, new AjaxCallback<String>());
        return this;
    }

}
