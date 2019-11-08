package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;

public class AutoDiSEqCDialog extends BaseDialogFragment implements WeakToolInterface {
    public static final String TAG = "AutoDiSEqCDialog";
    private static final int MAX_TRY_COUNT = 3;
    private static final int MAX_PORT_INDEX = 3;

    @BindView(R.id.tv_auto_diseqc_content)
    TextView mTvAutoDiSEqCContent;

    @BindView(R.id.auto_diseqc_result_layout)
    ViewGroup mAutoDiSEqCResultLayout;

    @BindView(R.id.tv_auto_diseqc_result)
    TextView mTvAutoDiSEqCResult;

    @BindView(R.id.btn_auto_diseqc_confirm)
    TextView mBtnConfirm;

    @OnClick(R.id.btn_auto_diseqc_confirm)
    void autoDiSEqCConfirm() {
        dismiss();
        if (mOnAutoDiSEqCResultListener != null) {
            mOnAutoDiSEqCResultListener.onAutoDiSEqCResult(mLockDiSEqc);
        }
    }

    private int mSatIndex;
    private HProg_Struct_TP mTpData;
    private int mLockDiSEqc;
    private Disposable mAutoDiSEqCDisposable;
    private OnAutoDiSEqCResultListener mOnAutoDiSEqCResultListener;
    private int tryCount;
    private int portIndex;
    private boolean lockDiSEqC;
    private boolean foundDiSEqC;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_auto_diseqc_layout;
    }

    @Override
    protected void setup(View view) {
        updateAutoDiSEqcContent(0, 0);

        autoDiSEqC();
    }

    public AutoDiSEqCDialog satIndex(int satIndex) {
        this.mSatIndex = satIndex;
        return this;
    }

    public AutoDiSEqCDialog tpData(HProg_Struct_TP tpData) {
        this.mTpData = tpData;
        return this;
    }

    private void showAutoDiSEqcResult(int portIndex) {
        mLockDiSEqc = portIndex;

        mTvAutoDiSEqCContent.setVisibility(View.GONE);
        mAutoDiSEqCResultLayout.setVisibility(View.VISIBLE);
        mBtnConfirm.requestFocus();
        mTvAutoDiSEqCResult.setText(portIndex < 0 ? getStrings(R.string.dialog_no_found_diseqc)
                : MessageFormat.format(getStrings(R.string.dialog_found_diseqc), getFoundDiSEqC(portIndex)));
    }

    private String getFoundDiSEqC(int portIndex) {
        switch (portIndex) {
            case Constants.DiSEqCPortIndex.DISEQC_A:
                return "DiSEqC A";
            case Constants.DiSEqCPortIndex.DISEQC_B:
                return "DiSEqC B";
            case Constants.DiSEqCPortIndex.DISEQC_C:
                return "DiSEqC C";
            case Constants.DiSEqCPortIndex.DISEQC_D:
                return "DiSEqC D";
            default:
                return "";
        }
    }

    private void updateAutoDiSEqcContent(int portIndex, int tryCount) {
        mTvAutoDiSEqCContent.setText(MessageFormat.format(getStrings(R.string.dialog_try_auto_diseqc),
                String.valueOf(portIndex), String.valueOf(tryCount), String.valueOf(MAX_TRY_COUNT)));
    }

    private void autoDiSEqC() {
        if (mAutoDiSEqCDisposable != null) {
            mAutoDiSEqCDisposable.dispose();
        }

        mAutoDiSEqCDisposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            while (portIndex <= MAX_PORT_INDEX) {
                if (tryCount >= MAX_TRY_COUNT) {
                    tryCount = 0;
                    portIndex++;
                    lockDiSEqC = false;
                } else {
                    tryCount++;
                }

                if (!lockDiSEqC) {
                    lockDiSEqC = DTVSearchManager.getInstance().tunerLockFreqDiSEqC(mSatIndex, mTpData.Freq, mTpData.Symbol, mTpData.Qam, portIndex) == 1;
                }

                if (DTVSearchManager.getInstance().tunerIsLocked()) {
                    emitter.onNext(true);
                    emitter.onComplete();
                } else {
                    emitter.onNext(false);

                    Thread.sleep(2000);
                }
            }
        }).compose(RxTransformer.threadTransformer()).subscribe(foundDiSEqC -> {
            if (foundDiSEqC) {
                showAutoDiSEqcResult(portIndex);
            } else {
                if (portIndex <= MAX_PORT_INDEX) {
                    updateAutoDiSEqcContent(portIndex, tryCount);
                } else {
                    showAutoDiSEqcResult(-1);
                }
            }
        });
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mAutoDiSEqCDisposable != null) {
                mAutoDiSEqCDisposable.dispose();
            }
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    public AutoDiSEqCDialog setOnAutoDiSEqCResultListener(OnAutoDiSEqCResultListener listener) {
        this.mOnAutoDiSEqCResultListener = listener;
        return this;
    }

    public interface OnAutoDiSEqCResultListener {
        void onAutoDiSEqCResult(int portIndex);
    }
}
