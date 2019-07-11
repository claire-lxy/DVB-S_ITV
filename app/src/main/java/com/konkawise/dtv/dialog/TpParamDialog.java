package com.konkawise.dtv.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.annotation.TpType;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;

public class TpParamDialog extends BaseDialogFragment {
    public static final String TAG = "TpParamDialog";

    public Context mContext;

    @BindView(R.id.et_tp_freq)
    public LastInputEditText mEtTpFreq;

    @BindView(R.id.et_tp_qam)
    public TextView mEtTpQam;

    @BindView(R.id.et_tp_symbol)
    public LastInputEditText mEtTpSymbol;

    private String mTpFreq;
    private String mTpSymbol;
    private String mTpQam;
    private Boolean mOff = false;

    @TpType
    private int mTpType;

    private OnTpParamListener mOnTpParamListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_tp_param_layout;
    }

    @Override
    protected void setup(View view) {
        mEtTpFreq.setText(TextUtils.isEmpty(mTpFreq) ? "" : mTpFreq);
        mEtTpSymbol.setText(TextUtils.isEmpty(mTpSymbol) ? "" : mTpSymbol);
        mEtTpQam.setText(TextUtils.isEmpty(mTpQam) ? "" : mTpQam);

        mEtTpFreq.setHint(TextUtils.isEmpty(mTpFreq) ? "0" : mTpFreq);
        mEtTpSymbol.setHint(TextUtils.isEmpty(mTpSymbol) ? "" : mTpSymbol);
        mEtTpQam.setHint(TextUtils.isEmpty(mTpQam) ? "" : mTpQam);

        mEtTpFreq.setSelection(mEtTpFreq.getText().toString().length());
        mEtTpSymbol.setSelection(mEtTpSymbol.getText().toString().length());

        if (mTpType == Constants.TP_TYPE_ADD) {
            mEtTpFreq.setInputType(InputType.TYPE_NULL);
            mEtTpSymbol.setInputType(InputType.TYPE_NULL);
        } else {
            mEtTpFreq.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            mEtTpSymbol.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            mEtTpFreq.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEtTpFreq.requestFocus();
                }
            }, 100);
        }

        mEtTpFreq.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtTpFreq.setText(EditUtils.getEditSubstring(mEtTpFreq));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEtTpSymbol.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtTpSymbol.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });

        mEtTpSymbol.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtTpSymbol.setText(EditUtils.getEditSubstring(mEtTpSymbol));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEtTpQam.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtTpQam.requestFocus();
                                }
                            }, 100);
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtTpFreq.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtTpFreq.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });

        mEtTpQam.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            setVorH();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            setVorH();
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtTpSymbol.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtTpSymbol.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected int resizeDialogWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
    }

    private void setVorH() {
        if (mOff) {
            mEtTpQam.setText(R.string.h);
            mOff = false;
        } else {
            mEtTpQam.setText(R.string.v);
            mOff = true;
        }
    }

    public TpParamDialog freq(String freq) {
        this.mTpFreq = TextUtils.isEmpty(freq) ? "0" : freq;
        return this;
    }

    public TpParamDialog symbol(String symbol) {
        this.mTpSymbol = TextUtils.isEmpty(symbol) ? "0" : symbol;
        return this;
    }

    public TpParamDialog qam(String qam) {
        this.mTpQam = TextUtils.isEmpty(qam) ? "H" : qam;
        return this;
    }

    public TpParamDialog tpType(@TpType int tpType) {
        this.mTpType = tpType;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN && mOnTpParamListener != null) {
            dismiss();
            mOnTpParamListener.onEditTp(mEtTpFreq.getText().toString(), mEtTpSymbol.getText().toString(), mEtTpQam.getText().toString());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
            return true;
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    public TpParamDialog setOnTpParamListener(OnTpParamListener listener) {
        this.mOnTpParamListener = listener;
        return this;
    }

    public interface OnTpParamListener {
        void onEditTp(String freq, String symbol, String qam);
    }
}
