package com.lt.tiebabiketeam.frame.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lt.tiebabiketeam.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;


/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 * <p/>
 *
 * @param <T> JSON type of response expected
 */
public class GsonRequest<T> extends Request<T> {
    /**
     * Request method of this request.  Currently supports GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    private int mMethod;

    /**
     * URL of this request.
     */
    private String mUrl;

    /**
     * The redirect url to use for 3xx http responses
     */
    private String mRedirectUrl;

    /**
     * The unique identifier of the request
     */
    private String mIdentifier;


    /**
     * 请求参数
     */
    protected Map<String, Object> mParams;

    public GsonRequest(String url) {
        this(url, null);
    }

    public GsonRequest(String url, Map<String, Object> params) {
        super(Method.GET, url, null);
        mUrl = url;
        mParams = params;
    }

    @Override
    public int getMethod() {
        return mMethod;
    }

    @Override
    public String getUrl() {
        return (mRedirectUrl != null) ? mRedirectUrl : mUrl;
    }

    @Override
    public String getOriginUrl() {
        return mUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    /**
     * Returns the identifier of the request.
     */
    public String getIdentifier() {
        return mIdentifier;
    }

    @SuppressWarnings("unchecked")
    protected Type getType() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType genericSuperclassType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = genericSuperclassType.getActualTypeArguments();
        return actualTypeArguments[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        if (response.data == null || response.data.length == 0) {
            return Response.error(new ParseError());
        }
        String json = null;
        try {
            json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (json == null) {
            return Response.error(new ParseError());
        }
        Logger.i("GsonRequest", "Response=" + json);
        try {
            T obj = new Gson().fromJson(json, getType());
            Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
            return Response.success(obj, entry);
        } catch (JsonSyntaxException e) {
            return Response.error(new VolleyError(response));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        onFinish();
        onSuccess(response);
    }

    public void deliverError(VolleyError error) {
        onFinish();
        onErrorResponse(error);
        onError(error);
    }

    protected void onErrorResponse(VolleyError volleyError) {

    }

    /**
     * @see #getParameters()
     */
    @Override
    @Deprecated
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request.
     */
    protected Map<String, Object> getParameters() {
        return mParams;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Map<String, Object> params = getParameters();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    private byte[] encodeParameters(Map<String, Object> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                Object value = entry.getValue();
                if (value != null) {
                    encodedParams.append(URLEncoder.encode(value.toString(), paramsEncoding));
                }
                encodedParams.append('&');
            }
            if (encodedParams.length() > 0) {
                encodedParams = encodedParams.deleteCharAt(encodedParams.length() - 1);
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    private String getQueryString() {
        try {
            byte[] body = getBody();
            if (body != null) {
                return new String(body, getParamsEncoding());
            }
        } catch (AuthFailureError | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String paramsToString(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        StringBuilder encodedParams = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            encodedParams.append(entry.getKey());
            encodedParams.append('=');
            encodedParams.append(entry.getValue());
            encodedParams.append('&');
        }
        if (encodedParams.length() > 0) {
            encodedParams = encodedParams.deleteCharAt(encodedParams.length() - 1);
        }
        return encodedParams.toString();
    }

    public GsonRequest<T> addParam(String key, Object value) {
        if (mParams == null) {
            mParams = new TreeMap<>();
        }
        mParams.put(key, value);
        return this;
    }

    public GsonRequest<T> get() {
        mMethod = Method.GET;
        String queryString = getQueryString();
        if (queryString != null) {
            mUrl += (mUrl.contains("?") ? "&" : "?") + getQueryString();
        }
        Logger.i("GsonRequest", "Get: " + mUrl);
        return send();
    }

    public GsonRequest<T> post() {
        mMethod = Method.POST;
        Logger.i("GsonRequest", "Post: " + mUrl);
        Logger.i("GsonRequest", "Body: " + paramsToString(mParams));
        return send();
    }

    private GsonRequest<T> send() {
        mIdentifier = createIdentifier(mMethod, mUrl);
        MyVolley.addRequest(this);
        return this;
    }

    protected void onSuccess(T result) {
    }

    protected void onError(VolleyError volleyError) {
    }

    protected void onFinish() {
    }

    private static long sCounter;

    /**
     * sha1(Request:method:url:timestamp:counter)
     *
     * @param method http method
     * @param url    http request url
     * @return sha1 hash string
     */
    private static String createIdentifier(final int method, final String url) {
        return InternalUtils.sha1Hash("Request:" + method + ":" + url +
                ":" + System.currentTimeMillis() + ":" + (sCounter++));
    }

    static class InternalUtils {

        // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
        private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

        private static String convertToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_CHARS[v >>> 4];
                hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
            }
            return new String(hexChars);
        }

        public static String sha1Hash(String text) {
            String hash = null;
            try {
                final MessageDigest digest = MessageDigest.getInstance("SHA-1");
                final byte[] bytes = text.getBytes("UTF-8");
                digest.update(bytes, 0, bytes.length);
                hash = convertToHex(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return hash;
        }


    }
}