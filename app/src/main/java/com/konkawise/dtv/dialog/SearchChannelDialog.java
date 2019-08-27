package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.OnClick;

public class SearchChannelDialog extends BaseDialogFragment {
    public static final String TAG = "SearchChannelDialog";

    @OnClick(R.id.btn_start_search)
    void search() {
        if (getDialog() != null) getDialog().dismiss();
        if (mOnSearchListener != null) {
            mOnSearchListener.onStartSearch();
        }
    }

    @OnClick(R.id.btn_exit_search)
    void exit() {
        if (getDialog() != null) getDialog().dismiss();
        if (mOnSearchListener != null) {
            mOnSearchListener.onExitSearch();
        }
    }

    // 存在进入app时会调用onKeyListener响应ok键，规避响应处理
    private boolean mInit = false;

    private OnSearchListener mOnSearchListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_search_channel_remind_layout;
    }

    @Override
    protected void setup(View view) {

    }

    public void resetInit() {
        if (!mInit) {
            mInit = true;
        }
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // 解决直接调用dismiss存在失效问题
            if (getDialog() != null) {
                getDialog().dismiss();
                if (mOnSearchListener != null) {
                    mOnSearchListener.onBackSearch();
                }
            }
            return true;
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    public SearchChannelDialog setOnSearchChannelListener(OnSearchListener listener) {
        this.mOnSearchListener = listener;
        return this;
    }

    public interface OnSearchListener {
        void onBackSearch();

        void onStartSearch();

        void onExitSearch();
    }
}
