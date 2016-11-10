package com.lt.tiebabiketeam.frame.network;

import com.android.volley.VolleyError;

public class ApiError extends VolleyError {
    private ApiRequest apiRequest;
    private int errcode;
    private String errmsg;


    public ApiError(ApiRequest apiRequest, ApiMsg apiMsg) {
        this.apiRequest = apiRequest;
        this.errcode = apiMsg.getErrcode();
        this.errmsg = apiMsg.getErrmsg();
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

}
