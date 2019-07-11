package com.konkawise.dtv.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 跟随Activity生命周期消失的dialog
 */
public abstract class BaseDialogFragment extends DialogFragment {
    private BaseActivity mActivity;

    private Unbinder mUnBinder;

    protected String getStrings(@StringRes int resId) {
        if (getContext() != null) {
            return getContext().getResources().getString(resId);
        }
        return "";
    }

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Dialog dialog = new Dialog(getContext());
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return onKeyListener(dialog, keyCode, event);
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setLayout(resizeDialogWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setup(view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null) {
            transaction.remove(fragment);
        }
        transaction.addToBackStack(null);
        show(transaction, tag);
    }

    @Override
    public void onDestroyView() {
        if (mUnBinder != null) mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        if (mActivity != null) mActivity.onFragmentDetach(getTag());
        mActivity = null;
        super.onDetach();
    }

    protected int resizeDialogWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.5);
    }

    /**
     * 监听响应事件
     */
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        return false;
    }

    protected abstract int getLayoutId();

    /**
     * 对话框创建后，在该方法修改对话框布局的某些view
     */
    protected abstract void setup(View view);
}
