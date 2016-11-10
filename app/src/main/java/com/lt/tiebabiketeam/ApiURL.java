package com.lt.tiebabiketeam;

public class ApiURL {
    public static final String SERVER_HOST = "http://120.76.127.6";
    public static final String API_FILE_IMAGE = "/image/";

    public static final String API_RENT_RECORD_GET_REMAIN_COUNT = "/rentRecord/getRemainCount"; // 获取已出租及剩余车辆数
    public static final String API_HOME_URL = "http://2228080.net/chargeQuery1.html"; // 获取已出租及剩余车辆数


    public static String getUrl(String url) {
        if (url.startsWith("/")) {
            return ApiURL.SERVER_HOST + url;
        }
        return url;
    }
}