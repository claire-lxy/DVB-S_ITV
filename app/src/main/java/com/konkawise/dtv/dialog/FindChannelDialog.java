package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.FindChannelAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

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

    private List<HProg_Struct_ProgInfo> mAllChannelList;
    private OnFindChannelCallback mOnFindChannelCallback;
    private FindChannelAdapter mAdapter;
    private Disposable mFindChannelDisposable;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_find_channel_layout;
    }

    @Override
    protected void setup(View view) {
        mEtFindChannel.addTextChangedListener(this);
        mEtFindChannel.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                mEtFindChannel.setText(EditUtils.getEditSubstring(mEtFindChannel));
                return true;
            }
            return false;
        });

        mAdapter = new FindChannelAdapter(getContext(), new ArrayList<>());
        mListView.setAdapter(mAdapter);
    }

    public FindChannelDialog channels(List<HProg_Struct_ProgInfo> channelList) {
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
            if (mFindChannelDisposable != null) {
                mFindChannelDisposable.dispose();
            }
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

        if (mFindChannelDisposable != null) {
            mFindChannelDisposable.dispose();
        }

        final String findKeyword = s.toString();
        mFindChannelDisposable = Observable.create((ObservableOnSubscribe<List<HProg_Struct_ProgInfo>>) emitter -> {
            List<HProg_Struct_ProgInfo> findChannels = new ArrayList<>();
            for (HProg_Struct_ProgInfo channel : mAllChannelList) {
                if (channel.Name.toUpperCase().contains(findKeyword.toUpperCase())) {
                    findChannels.add(channel);
                }
            }
            emitter.onNext(findChannels);
            emitter.onComplete();
        }).compose(RxTransformer.threadTransformer()).subscribe(findChannels -> {
            mAdapter.updateHighLightKeywords(findKeyword);
            mAdapter.updateData(findChannels);
        });
    }

    public interface OnFindChannelCallback {
        void onFindChannels(HProg_Struct_ProgInfo findChannel);
    }
}
