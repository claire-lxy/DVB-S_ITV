package com.konkawise.dtv.base;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.konkawise.dtv.LanguageManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.receiver.HomeReceiver;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseActivity extends FragmentActivity
        implements WeakToolInterface, BaseFragment.FragmentCallback, HomeReceiver.OnReceiveHomeHandleListener {
    private HomeReceiver mHomeReceiver;

    private Unbinder mUnBinder;

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.attachBaseContext(LanguageManager.getInstance().wrapLocaleContext(newBase));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterHomeReceiver();
    }

    private void registerHomeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mHomeReceiver = new HomeReceiver();
        registerReceiver(mHomeReceiver, intentFilter);

        mHomeReceiver.registerReceiveHomeHandlerListener(this);
    }

    private void unregisterHomeReceiver() {
        if (mHomeReceiver != null) {
            unregisterReceiver(mHomeReceiver);
            mHomeReceiver.unregisterReceiveHomeHandleListener();
        }
    }

    @Override
    public void onFragmentAttach() {

    }

    @Override
    public void onFragmentDetach(String tag) {

    }

    @Override
    protected void onDestroy() {
        WeakToolManager.getInstance().removeWeakTool(this);
        if (mUnBinder != null) mUnBinder.unbind();
        super.onDestroy();
    }

    @Override
    public boolean onHomeHandleCallback() {
        return true;
    }

    protected abstract int getLayoutId();

    protected abstract void setup();
}
