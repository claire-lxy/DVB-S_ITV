package com.konkawise.dtv.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment implements WeakToolInterface {
    private BaseActivity mActivity;

    private Unbinder mUnBinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) context;
            this.mActivity = activity;
            activity.onFragmentAttach();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        mUnBinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setup(view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        WeakToolManager.getInstance().removeWeakTool(this);
        if (mUnBinder != null) mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        if (mActivity != null) mActivity.onFragmentDetach(getTag());
        mActivity = null;
        super.onDetach();
    }

    protected abstract int getLayoutId();

    protected abstract void setup(View view);

    public interface FragmentCallback {
        void onFragmentAttach();

        void onFragmentDetach(String tag);
    }
}
