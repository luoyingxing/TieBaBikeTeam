package com.lt.tiebabiketeam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.lt.tiebabiketeam.entity.RentNumber;
import com.lt.tiebabiketeam.frame.BaseActivity;
import com.lt.tiebabiketeam.frame.network.ApiRequest;
import com.lt.tiebabiketeam.utils.DpiUtils;
import com.lt.tiebabiketeam.utils.FileUtils;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private long mExitTime;
    private TextView mRemainCountTV;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLog.e("onCreate");
        setContentView(R.layout.activity_main);
        mBackView.setVisibility(View.GONE);
        mTitleView.setText(getResources().getString(R.string.app_name));
        init();
    }

    private void init() {
        findView();
        loadBikeNumber();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_home_phone:
                callPhone();
                break;
            case R.id.ll_home_count:
                loadBikeNumber();
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showPopupWindow();
            FileUtils.savePref("first_coming", "MainActivity");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (FileUtils.getPref("first_coming").isEmpty()) {
            Message msg = mHandler.obtainMessage();
            mHandler.sendMessageDelayed(msg, 2000);

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void findView() {
        LinearLayout mLayoutPhone = (LinearLayout) findViewById(R.id.ll_home_phone);
        LinearLayout mLayoutCount = (LinearLayout) findViewById(R.id.ll_home_count);
        mRemainCountTV = (TextView) findViewById(R.id.tv_home_count);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_focus_details);
        mWebView = (WebView) findViewById(R.id.wv_home_view);

        mLayoutPhone.setOnClickListener(this);
        mLayoutCount.setOnClickListener(this);

        mWebView.requestFocusFromTouch();
        mWebView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        mWebView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        mWebView.getSettings().setBuiltInZoomControls(true);
        // 扩大比例的缩放
//        mWebView.getSettings().setUseWideViewPort(true);
        // 自适应屏幕
//        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
                mLog.e(newProgress + "%");
            }
        });

        mWebView.loadUrl(ApiURL.API_HOME_URL);

    }

    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:2228080"));
        startActivity(intent);
    }

    private void loadBikeNumber() {
        new ApiRequest<RentNumber>(ApiURL.API_RENT_RECORD_GET_REMAIN_COUNT) {
            @Override
            protected void onSuccess(RentNumber rentNumber) {
                mRemainCountTV.setText(String.valueOf(rentNumber.getRemainCount()));
            }

            @Override
            protected void onFinish() {
                super.onFinish();
            }

        }.get();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                showToast("再按一次退出");
                mExitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void showPopupWindow() {
        View customView = getLayoutInflater().inflate(R.layout.refresh_popupwindow_content, null, false);
        popupWindow = new PopupWindow(customView, DpiUtils.getWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });
        popupWindow.showAsDropDown(getActionbarLayout());
    }

}