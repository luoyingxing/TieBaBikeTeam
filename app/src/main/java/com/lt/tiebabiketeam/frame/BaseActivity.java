package com.lt.tiebabiketeam.frame;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lt.tiebabiketeam.Constant;
import com.lt.tiebabiketeam.R;
import com.lt.tiebabiketeam.utils.Logger;


public class BaseActivity extends ActionBarActivity implements View.OnClickListener, BackHandledInterface {
    private BackHandledFragment mBackHandedFragment;
    protected String mTag;
    protected Logger mLog;
    protected ViewGroup mActionbarLayout;
    protected ViewGroup mMiddleLayout;
    protected TextView mTitleView;
    protected ImageView mRightImage;
    protected TextView mRightText;
    protected View mBackView;
    protected CharSequence mTitle = "";
    View.OnClickListener mRightImageOnClickListener;
    View.OnClickListener mRightTextOnClickListener;

    protected int getLayout() {
        return R.layout.activity_base;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getClass().getSimpleName();
        mLog = new Logger(mTag, Log.VERBOSE);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintResource(R.color.theme_color);//通知栏所需颜色
//        }

        setContentView(getLayout());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_custom_view);
        Toolbar parent = (Toolbar) actionBar.getCustomView().getParent();
        parent.setContentInsetsAbsolute(0, 0);

        mActionbarLayout = (RelativeLayout) findViewById(R.id.actionbar_custom_view_container);

        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(R.string.app_name);

        mMiddleLayout = (RelativeLayout) findViewById(R.id.layout);

        mRightImage = (ImageView) findViewById(R.id.right_image);
        mRightImage.setVisibility(View.VISIBLE);
        mRightImage.setOnClickListener(this);

        mRightText = (TextView) findViewById(R.id.right_text);
        mRightText.setVisibility(View.VISIBLE);
        mRightText.setOnClickListener(this);

        mBackView = findViewById(R.id.back);
        mBackView.setVisibility(View.VISIBLE);
        mBackView.setOnClickListener(this);
        mTitleView.setText(mTitle);
        String fragmentName = getIntent().getStringExtra(Constant.ARGS_FRAGMENT_NAME);
        switchFragment(fragmentName);
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title;
        mTitleView.setText(mTitle);
    }

    @Override
    public void onBackPressed() {
        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            goBack();
        }
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        mBackHandedFragment = selectedFragment;
    }

    public void switchFragment(String fragmentName) {
        if (fragmentName != null) {
            Fragment fragment = Fragment.instantiate(this, fragmentName, getIntent().getExtras());
            switchFragment(fragment);
        }
    }

    public void switchFragment(Fragment fragment) {
        switchFragment(fragment, false);
    }

    public void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.fragment_right_in,
                R.anim.fragment_left_out,
                R.anim.fragment_left_in,
                R.anim.fragment_right_out
        );

        transaction.replace(R.id.container, fragment, fragment.getClass().getName());
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void goBack() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public ViewGroup getActionbarLayout() {
        return mActionbarLayout;
    }

    public TextView getmTitleView() {
        return mTitleView;
    }

    public ViewGroup getmMiddleLayout() {
        return mMiddleLayout;
    }

    public void showBackView(boolean visible) {
        mBackView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public View getBackView() {
        return mBackView;
    }

    public ImageView getRightImage() {
        return mRightImage;
    }

    public TextView getRightText() {
        return mRightText;
    }

    public void setOnRightImageClick(View.OnClickListener listener) {
        mRightImageOnClickListener = listener;
    }

    public void setOnRightTextClick(View.OnClickListener listener) {
        mRightTextOnClickListener = listener;
    }

    public void onRightImageClick() {
        if (mRightImageOnClickListener != null) {
            mRightImageOnClickListener.onClick(mRightImage);
        }
    }

    public void onRightTextClick() {
        if (mRightTextOnClickListener != null) {
            mRightTextOnClickListener.onClick(mRightText);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                goBack();
                break;
            case R.id.right_image:
                onRightImageClick();
                break;
            case R.id.right_text:
                onRightTextClick();
                break;
        }
    }

    protected void showToast(Object msg) {
        Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(Object msg) {
        Toast.makeText(this, "" + msg, Toast.LENGTH_LONG).show();
    }
}
