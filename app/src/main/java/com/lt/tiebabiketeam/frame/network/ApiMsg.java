package com.lt.tiebabiketeam.frame.network;

public class ApiMsg {
    private int errcode;
    private String errmsg;

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

    @Override
    public String toString() {
        return "errcode:" + errcode + ",errmsg:" + errmsg;
    }

    public static boolean isApiMsg(String json) {
        String msgRegExp = "\\{\"errcode\":-?\\d*,\"errmsg\":\"[\\s\\S]*\"\\}";
        return json != null && json.matches(msgRegExp);
    }
}