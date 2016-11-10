package com.lt.tiebabiketeam.frame.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lt.tiebabiketeam.ApiURL;
import com.lt.tiebabiketeam.MainApplication;
import com.lt.tiebabiketeam.R;
import com.lt.tiebabiketeam.utils.DpiUtils;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyVolley {
    private static MyVolley mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private MyVolley(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(50);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static synchronized MyVolley getInstance() {
        if (mInstance == null) {
            mInstance = new MyVolley(MainApplication.getAppContext());
        }
        return mInstance;
    }

    public static <T> Request<T> addRequest(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(10 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return getInstance().getRequestQueue().add(req);
    }

    public static void asyncImage(String picUrl, ImageView imageView) {
        int maxWidth = imageView.getWidth();
        int maxHeight = imageView.getHeight();
        maxWidth = maxWidth > 0 ? maxWidth : DpiUtils.getWidth();
        maxHeight = maxHeight > 0 ? maxHeight : DpiUtils.getHeight();
        asyncImage(picUrl, imageView, maxWidth, maxHeight);
    }

    public static void asyncImage(String picUrl, ImageView imageView, int maxWidth, int maxHeight) {
        if (TextUtils.isEmpty(picUrl)) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        picUrl = appendPicUrl(picUrl);
        MyVolley.getInstance().getImageLoader().get(picUrl, ImageLoader.getImageListener(imageView, R.mipmap.ic_launcher, R.mipmap.ic_launcher), maxWidth, maxHeight);
    }

    public static void asyncImage(String picUrl, ImageLoader.ImageListener imageListener, int maxWidth, int maxHeight) {
        if (TextUtils.isEmpty(picUrl)) {
            return;
        }
        picUrl = appendPicUrl(picUrl);
        MyVolley.getInstance().getImageLoader().get(picUrl, imageListener, maxWidth, maxHeight);
    }

    public static void asyncImage(String picUrl, NetworkImageView imageView) {
        if (TextUtils.isEmpty(picUrl)) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        picUrl = appendPicUrl(picUrl);
        imageView.setDefaultImageResId(R.mipmap.ic_launcher);
        imageView.setErrorImageResId(R.mipmap.ic_launcher);
        imageView.setImageUrl(picUrl, mInstance.getImageLoader());
    }

    /**
     * 非网络图片自动补全url (存储于服务端本地的图片)
     */
    public static String appendPicUrl(String picUrl) {
        if (picUrl.contains("http")) {
            return picUrl;
        } else {
            return ApiURL.API_FILE_IMAGE + picUrl;
        }
    }

    /**
     * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
     * Cache-control headers are ignored. SoftTtl == 1 mins, ttl == 24 hours.
     *
     * @param response The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is not cacheable.
     */
    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverETag;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }

        serverETag = headers.get("ETag");

        final long cacheHitButRefreshed = 30 * 1000; // in 1 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverETag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 将实体类的属性转成键值对
     *
     * @param object 实体类
     * @return 键值对
     */
    public static Map<String, Object> objectToMap(Object object) {
        Type typeOfT = new TypeToken<Map<String, Object>>() {
        }.getType();
        return new Gson().fromJson(new Gson().toJson(object), typeOfT);
    }

    public static Map<String, Object> queryStrToMap(String queryStr) {
        Map<String, Object> params = new LinkedHashMap<>();
        String[] keyValues = queryStr.split("&");
        for (String s : keyValues) {
            String[] keyValue = s.split("=");
            params.put(keyValue[0], keyValue.length < 2 ? "" : keyValue[1]);
        }
        return params;
    }

    /**
     * 将键值对转成请求参数格式的String类型
     *
     * @param params 键值对
     * @return queryString
     */
    public static String mapToQueryStr(Map<String, Object> params) {
        String retStr = "";
        for (String key : params.keySet()) {
            Object value = params.get(key);
            retStr += key + '=' + (value == null ? "" : value) + "&";
        }
        if (!retStr.isEmpty()) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
        return retStr;
    }
}