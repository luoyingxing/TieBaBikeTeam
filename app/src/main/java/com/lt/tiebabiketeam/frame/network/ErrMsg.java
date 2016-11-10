package com.lt.tiebabiketeam.frame.network;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrMsg {
    public static final int OK = 0;

    public static final int NEW_VERSION_IS_AVAILABLE = 1011;
    public static final int INCORRECT_USERNAME_OR_PASSWORD = 1021;
    public static final int ACCOUNT_HAS_BEEN_REGISTERED = 1061;
    public static final int INCORRECT_OLD_PASSWORD = 1091;
    public static final int INCORRECT_USERNAME_OR_EMAIL = 1051;
    public static final int RETRIEVE_PASSWORD_FAILED = 1052;
    public static final int EMAIL_SEND_FAILED = 1053;
    public static final int IMAGE_UPLOAD_FAILED = 1331;
    public static final int IMAGE_UPLOAD_SIZE_EXCEED = 1332;
    public static final int AVATAR_UPLOAD_FAILED = 1341;
    public static final int USER_IS_NOT_LOGIN = 40005;
    public static final int SYSTEM_IS_BUSY = 1501;
    public static final int INSUFFICIENT_BALANCE = 1601;
    public static final int ILLEGAL_ARGUMENT = 1701;
    public static final int ACCOUNT_UNBOUND = 40008;
    public static final int ACCOUNT_IS_EXIST = 1703;
    public static final int ACCOUNT_BINDING_FAILED = 1704;

    public static JSONObject errJson(int errcode) {
        JSONObject json = new JSONObject();
        try {
            json.put("errcode", errcode);
            json.put("errmsg", getMessage(errcode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static String getMessage(int code) {
        switch (code) {
            case OK:
                return "ok";
            case NEW_VERSION_IS_AVAILABLE:
                return "有新版本可用";
            case INCORRECT_USERNAME_OR_PASSWORD:
                return "用户名或密码错误";
            case ACCOUNT_HAS_BEEN_REGISTERED:
                return "该用户名已经被注册";
            case INCORRECT_OLD_PASSWORD:
                return "旧密码错误";
            case INCORRECT_USERNAME_OR_EMAIL:
                return "用户名不存在";
            case RETRIEVE_PASSWORD_FAILED:
                return "找回密码失败";
            case EMAIL_SEND_FAILED:
                return "邮件发送失败";
            case IMAGE_UPLOAD_FAILED:
                return "图片上传失败";
            case IMAGE_UPLOAD_SIZE_EXCEED:
                return "图片尺寸超过规定大小:3M";
            case AVATAR_UPLOAD_FAILED:
                return "头像上传失败";
            case USER_IS_NOT_LOGIN:
                return "用户未登录";
            case ILLEGAL_ARGUMENT:
                return "参数非法";
            case ACCOUNT_UNBOUND:
                return "此Id未绑定天仁英语帐号";
            case ACCOUNT_IS_EXIST:
                return "此Id已绑定天仁英语帐号";
            case ACCOUNT_BINDING_FAILED:
                return "绑定失败";
            case INSUFFICIENT_BALANCE:
                return "账户余额不足";
            case SYSTEM_IS_BUSY:
                return "系统繁忙";
            default:
                return null; // cannot be
        }
    }

    public static JSONObject okJson() {
        return errJson(OK);
    }
}
