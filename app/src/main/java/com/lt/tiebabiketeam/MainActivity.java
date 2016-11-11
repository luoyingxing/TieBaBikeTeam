package com.lt.tiebabiketeam;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.lt.tiebabiketeam.entity.RentNumber;
import com.lt.tiebabiketeam.frame.BaseActivity;
import com.lt.tiebabiketeam.frame.network.ApiRequest;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private long mExitTime;
    private TextView mRemainCountTV;
    private WebView mWebView;

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

    private ProgressDialog mDialog;

    @SuppressLint("SetJavaScriptEnabled")
    private void findView() {
        LinearLayout mLayoutPhone = (LinearLayout) findViewById(R.id.ll_home_phone);
        LinearLayout mLayoutCount = (LinearLayout) findViewById(R.id.ll_home_count);
        mRemainCountTV = (TextView) findViewById(R.id.tv_home_count);
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
                mLog.e(newProgress + "%");
                if (newProgress == 100) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }
        });

        mWebView.loadUrl(ApiURL.API_HOME_URL);

        mDialog = ProgressDialog.show(MainActivity.this, "", "正在加载...");
        mDialog.setCanceledOnTouchOutside(true);
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

}