package com.lt.tiebabiketeam.frame.network;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RedirectError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.lt.tiebabiketeam.ApiURL;
import com.lt.tiebabiketeam.MainApplication;
import com.lt.tiebabiketeam.utils.Logger;
import com.lt.tiebabiketeam.utils.NetworkUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ApiRequest<T> extends GsonRequest<T> {
    public static final String TAG = "ApiRequest";

    /**
     * 错误类型
     */
    protected ErrorType mErrorType;

    /**
     * 错误消息
     */
    protected String mErrMsg;

    /**
     * 用于显示错误信息的Toast的上下文
     */
    protected Context mToastContext;//优先级别 toast<textView
    /**
     * 通过textView来显示错误信息
     */
    protected TextView mErrTextView;

    /**
     * 错误类型
     */
    public enum ErrorType {
        NETWORK_DISCONNECTED,//用户的Wi-Fi和移动数据已断开
        BAD_REQUEST,//用户的网络正常开启,但请求发送失败或服务端未正常响应.
        USER_IS_NOT_LOGIN,//用户未登录,但调用了必须登录才能访问的接口
        API_ERROR, //业务逻辑错误.
        REPAIRABLE_ERROR //可内部自行解决的错误. 目前只有一种,即用户未登录,但又保存了账号密码的这种情况.
    }


    public ApiRequest(String url) {
        super(ApiURL.getUrl(url));
    }

    public ApiRequest(String url, Map<String, Object> params) {
        super(ApiURL.getUrl(url), params);
    }

    protected Context getContext() {
        return mToastContext == null ? MainApplication.getAppContext() : mToastContext;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        String cookie = MainApplication.getApp().getLoginCookie();
        if (cookie == null) {
            return super.getHeaders();
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        return headers;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Response<T> parsedResponse = super.parseNetworkResponse(response);
        if (!parsedResponse.isSuccess()
                && parsedResponse.error != null
                && parsedResponse.error instanceof ParseError) {
            return parsedResponse;
        }
        String json = null;
        try {
            json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (ApiMsg.isApiMsg(json)) {
            ApiMsg apiMsg = new Gson().fromJson(json, ApiMsg.class);
            if (apiMsg.getErrcode() != 0) {
                return Response.error(new ApiError(this, apiMsg));
            }
        }
        return parsedResponse;
    }

    /**
     * <p>一般情况下,不应该直接覆盖此方法,除非想屏蔽所有的默认错误处理实现,</p>
     * <p>否则,应该重载{@link #onError(VolleyError, ErrorType, String)}或更为具体的方法</p>
     *
     * @param volleyError 错误信息
     * @see #onNetworkDisconnected()
     * @see #onBadRequest()
     * @see #onUserIsNotLogin()
     * @see #onApiError(int, String)
     * @see #onError(VolleyError, ErrorType, String)
     * @see
     */
    @Override
    protected void onErrorResponse(VolleyError volleyError) {
        if (volleyError instanceof ApiError) {
            apiErrorDefaultHandler((ApiError) volleyError);
        } else if (volleyError instanceof AuthFailureError) {
            Logger.i(TAG, "授权失败");
            onBadRequest();
        } else if (volleyError instanceof NetworkError) {
            Logger.i(TAG, "网络连接错误");
            if (volleyError instanceof NoConnectionError) {
                Logger.i(TAG, "无可用网络连接");
            }
            if (volleyError.getMessage() != null) {
                Logger.i(TAG, volleyError.getMessage());
            }
            onNetworkConnectedError();
        } else if (volleyError instanceof ParseError) {
            volleyError.printStackTrace();
            Logger.i(TAG, "数据解析出错");
            onBadRequest();
        } else if (volleyError instanceof RedirectError) {
            Logger.i(TAG, "地址重定向");
            onBadRequest();
        } else if (volleyError instanceof ServerError) {
            if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode != 200) {
                onHttpStatusError(volleyError);
            } else {
                Logger.i(TAG, "服务端内部出错,请确保请求参数合法或联系服务端开发人员!");
            }
            onBadRequest();
        } else if (volleyError instanceof TimeoutError) {
            Logger.i(TAG, "请求超时");
            onNetworkConnectedError();
        } else {
            NetworkResponse response = volleyError.networkResponse;
            if (response != null) {
                Logger.i(TAG, "response.statusCode=" + response.statusCode);
                if (response.statusCode != 200) {
                    onHttpStatusError(volleyError);
                } else {
                    Logger.i(TAG, "服务端返回数据异常,请联系服务端开发人员!");
                    try {
                        Logger.i(TAG, new String(response.data, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                onBadRequest();
            } else {
                onNetworkConnectedError();
            }
        }
    }

    /**
     * HTTP状态错误
     *
     * @param volleyError 错误信息
     */
    private void onHttpStatusError(VolleyError volleyError) {
        switch (volleyError.networkResponse.statusCode) {
            case 400:
                Logger.i(TAG, "错误的请求，请确保请求参数合法");
                break;
            case 401:
                Logger.i(TAG, "未授权，需要授权认证");
                break;
            case 403:
                Logger.i(TAG, "禁止访问，请联系服务端开发人员");
                break;
            case 404:
                Logger.i(TAG, "URL地址不正确或服务端未启动!");
                break;
            case 500:
                Logger.i(TAG, "服务端出错，请确保请求参数合法或联系服务端开发人员!");
            default:
                Logger.i(TAG, "Http StatusCode = " + volleyError.networkResponse.statusCode);
                break;
        }
    }

    /**
     * 网络连接错误
     */
    private void onNetworkConnectedError() {
        if (!NetworkUtils.isNetConnected()) {
            onNetworkDisconnected();
        } else {
            onBadRequest();
        }
    }

    /**
     * 业务逻辑错误默认处理方法
     *
     * @param apiError Api错误信息
     */
    private void apiErrorDefaultHandler(ApiError apiError) {
        Logger.i(TAG, "errcode=" + apiError.getErrcode());
        Logger.i(TAG, "errmsg=" + apiError.getErrmsg());
        switch (apiError.getErrcode()) {
            case ErrMsg.USER_IS_NOT_LOGIN:
                MainApplication.getApp().setLoginCookie(null);
//                if (LoginFragment.hasLoginInfo()) {
//                    autoLogin(apiError.getApiRequest());
//                    mErrorType = ErrorType.REPAIRABLE_ERROR;
//                } else {
//                    onUserIsNotLogin();
//                }
                break;
            default:
                onApiError(apiError.getErrcode(), apiError.getErrmsg());
                break;
        }
    }

    /**
     * 自动登录
     *
     * @param apiRequest 未完成的请求
     */
    private void autoLogin(final ApiRequest apiRequest) {
//        LoginFragment.login(new LoginFragment.LoginCallbackImpl() {
//            @Override
//            public void onSuccess(UserInfo userInfo) {
//                super.onSuccess(userInfo);
//                String cookie = MainApplication.getApp().getLoginCookie();
//                Logger.i("ApiRequest", "cookie=" + cookie);
//                Logger.i("ApiRequest", "userInfo=" + userInfo);
//                if (cookie != null && !cookie.isEmpty() && apiRequest != null) {
//                    Logger.i("ApiRequest", "自动登录成功，正在重新发送未完成的请求");
//                    MyVolley.addRequest(apiRequest);
//                }
//            }
//        });
    }


    /**
     * 网络未连接.
     */
    protected void onNetworkDisconnected() {
        mErrorType = ErrorType.NETWORK_DISCONNECTED;
        mErrMsg = "当前网络不可用，请检查网络设置。";
    }

    /**
     * 用户的网络正常,但数据加载失败或请求失败.
     */
    protected void onBadRequest() {
        mErrorType = ErrorType.BAD_REQUEST;
        mErrMsg = getMethod() == Method.GET ? "数据加载失败,请重试" : "请求超时,请重试";
    }

    /**
     * 用户未登录,且本地未缓存账号密码.
     */
    protected void onUserIsNotLogin() {
        mErrorType = ErrorType.USER_IS_NOT_LOGIN;
        mErrMsg = "您还未登录哦";
    }

    /**
     * 业务逻辑错误.
     *
     * @param code 错误码
     * @param msg  错误消息
     */
    protected void onApiError(int code, String msg) {
        mErrorType = ErrorType.API_ERROR;
        mErrMsg = msg;
    }

    /**
     * 屏蔽了可自动修复的错误, 如:用户未登录但保存了账号密码的这种情况,此时程序可以自动登录
     * <p>一般情况下,子类不应该直接覆盖该方法,而应该使用 {@link #onError(VolleyError, ErrorType, String)} <p>
     * <p>该方法在 {@link #onErrorResponse(VolleyError)}之后执行. <p>
     *
     * @param volleyError 错误信息
     */
    @Override
    protected void onError(VolleyError volleyError) {
        if (mErrorType != ErrorType.REPAIRABLE_ERROR) {
            onError(volleyError, mErrorType, mErrMsg);
        }
    }

    /**
     * <p>默认通过Toast来显示错误消息</p>
     * <p>如果设置了{@link #showErrByTextView(TextView)}则通过TextView来显示错误消息.</p>
     * <p>如果希望通过其他方式来错误信息,则可以覆盖此方法</p>
     * <p>如果只是想监听错误事件,而不干扰默认错误处理方式,则子类应该调用{@code super.onError(volleyError,errorType,errMsg)} </p>
     *
     * @param volleyError 错误信息
     * @param errorType   错误类型
     * @param errMsg      错误消息,可能为空
     * @see #onError(VolleyError)
     */
    protected void onError(VolleyError volleyError, ErrorType errorType, String errMsg) {
        if (errorType != null && errMsg != null) {
            if (mErrTextView != null && mErrorType != ErrorType.API_ERROR) {
                mErrTextView.setText(mErrMsg);
            } else {
                Logger.showToast(getContext(), mErrMsg);
            }
        }
    }

    /**
     * 请求成功.
     *
     * @param result 如果T不是 {@link ApiMsg}类型,则T为期望的数据. 否则T固定是错误码为0的一个{@link ApiMsg}对象.
     */
    @Override
    protected void onSuccess(T result) {
        super.onSuccess(result);
    }

    /**
     * 可重载此方法以隐藏Dialog、ProgressBar或停止下拉刷新等
     * <p>此方法在{@link #onSuccess(T)}和{@link #onError(VolleyError)}之前执行</p>
     */
    @Override
    protected void onFinish() {
        super.onFinish();
    }

    /**
     * 通过Toast显示错误信息,如果不调用此方法,则默认使用ApplicationContext.
     * 使用ApplicationContext的缺点是,当程序退出桌面后,Toast还会继续显示.
     *
     * @param context 用于显示错误信息的Toast的上下文
     */
    public ApiRequest<T> showErrByToast(Context context) {
        mToastContext = context;
        return this;
    }

    /**
     * 通过textView来显示错误信息, 非常适合结合ListView的EmptyView来使用
     *
     * @param textView 用于显示错误信息的textView
     */
    public ApiRequest<T> showErrByTextView(TextView textView) {
        mErrTextView = textView;
        return this;
    }

}