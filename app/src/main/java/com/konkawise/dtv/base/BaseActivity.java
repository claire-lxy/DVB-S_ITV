package com.konkawise.dtv.base;

import android.arch.lifecycle.LifecycleObserver;
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
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends FragmentActivity
        implements WeakToolInterface, BaseFragment.FragmentCallback, HomeReceiver.OnReceiveHomeHandleListener {
    private HomeReceiver mHomeReceiver;
    private Unbinder mUnBinder;
    private LifecycleObserver mLifecycleObserver;
    private DisposableDelegate mDisposableDelegate = new DisposableDelegate();

    // 标志位主要处理界面跳转到当前界面，当前界面会响应到onKeyUp同样按键事件
    protected boolean mDispatchKeyUpReady;

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

        mLifecycleObserver = provideLifecycleObserver();
        registerLifecycleObserver(mLifecycleObserver);

        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeReceiver();
        getWindow().getDecorView().postDelayed(() -> mDispatchKeyUpReady = true, 300);
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
        mDisposableDelegate.clearObservables();
        unregisterLifecycleObserver(mLifecycleObserver);
        super.onDestroy();
    }

    public void registerLifecycleObserver(LifecycleObserver observer) {
        if (observer != null) {
            getLifecycle().addObserver(observer);
        }
    }

    public void unregisterLifecycleObserver(LifecycleObserver observer) {
        if (observer != null) {
            getLifecycle().removeObserver(observer);
        }
    }

    protected void addObservable(Disposable disposable) {
        mDisposableDelegate.addObservable(disposable);
    }

    protected void removeObservable(Disposable disposable) {
        mDisposableDelegate.removeObservable(disposable);
    }

    protected LifecycleObserver provideLifecycleObserver() {
        return null;
    }

    @Override
    public boolean onHomeHandleCallback() {
        return true;
    }

    protected abstract int getLayoutId();

    protected abstract void setup();
}
