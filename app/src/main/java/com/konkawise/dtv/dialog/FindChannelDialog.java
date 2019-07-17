package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.adapter.FindChannelAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class FindChannelDialog extends BaseDialogFragment implements TextWatcher, WeakToolInterface {
    public static final String TAG = "FindChannelDialog";

    @BindView(R.id.et_find_channel)
    LastInputEditText mEtFindChannel;

    @BindView(R.id.lv_find_channel_list)
    ListView mListView;

    @OnItemClick(R.id.lv_find_channel_list)
    void onItemClick(int position) {
        dismiss();
        if (mOnFindChannelCallback != null) {
            mOnFindChannelCallback.onFindChannels(mAdapter.getItem(position));
        }
    }

    private List<PDPMInfo_t> mAllChannelList;
    private FindChannelRunnable mFindChannelRunnable;
    private OnFindChannelCallback mOnFindChannelCallback;
    private FindChannelAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_find_channel_layout;
    }

    @Override
    protected void setup(View view) {
        mEtFindChannel.addTextChangedListener(this);
        mEtFindChannel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mEtFindChannel.setText(EditUtils.getEditSubstring(mEtFindChannel));
                    return true;
                }
                return false;
            }
        });

        mFindChannelRunnable = new FindChannelRunnable(this);
        mAdapter = new FindChannelAdapter(getContext(), new ArrayList<>());
        mListView.setAdapter(mAdapter);
    }

    public FindChannelDialog channels(List<PDPMInfo_t> channelList) {
        this.mAllChannelList = channelList == null ? new ArrayList<>() : channelList;
        return this;
    }

    public FindChannelDialog setOnFindChannelCallback(OnFindChannelCallback callback) {
        this.mOnFindChannelCallback = callback;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            mEtFindChannel.removeTextChangedListener(this);
            ThreadPoolManager.getInstance().remove(mFindChannelRunnable);
            WeakToolManager.getInstance().removeWeakTool(this);
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mAllChannelList == null || mAllChannelList.isEmpty()) return;

        ThreadPoolManager.getInstance().remove(mFindChannelRunnable);
        mFindChannelRunnable.updateKeyword(s.toString());
        ThreadPoolManager.getInstance().execute(mFindChannelRunnable);
    }

    private static class FindChannelRunnable extends WeakRunnable<FindChannelDialog> {
        String findKeyword;

        FindChannelRunnable(FindChannelDialog view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            List<PDPMInfo_t> findChannels = new ArrayList<>();
            if (!TextUtils.isEmpty(findKeyword)) {
                for (PDPMInfo_t channel : mWeakReference.get().mAllChannelList) {
                    if (channel.Name.toUpperCase().contains(findKeyword.toUpperCase())) {
                        findChannels.add(channel);
                    }
                }
            }

            FindChannelDialog context = mWeakReference.get();
            if (context.getActivity() != null) {
                context.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.mAdapter.updateHighLightKeywords(findKeyword);
                        context.mAdapter.updateData(findChannels);
                    }
                });
            }
        }

        void updateKeyword(String keyword) {
            findKeyword = keyword;
        }
    }

    public interface OnFindChannelCallback {
        void onFindChannels(PDPMInfo_t findChannel);
    }
}
