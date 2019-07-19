package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class SeekTimeDialog extends BaseDialogFragment {
    public static final String TAG = "SeekTimeDialog";

    @BindView(R.id.tv_tip)
    TextView tvTip;

    @BindView(R.id.eidt_hour)
    EditText etHour;

    @BindView(R.id.eidt_minute)
    EditText etMinute;

    @BindView(R.id.eidt_second)
    EditText etSecond;

    @OnClick(R.id.eidt_hour)
    void hourClick() {
        centerClick();
    }

    @OnClick(R.id.eidt_minute)
    void minuteClick() {
        centerClick();
    }

    @OnClick(R.id.eidt_second)
    void secondClick() {
        centerClick();
    }

    private void centerClick() {
        if (!inputTimeCheck()) {
            tvTip.setText(R.string.input_invalid);
            tvTip.setTextColor(getResources().getColor(R.color.holo_red_light));
        } else {
            if (listener != null)
                listener.time(h, m, s);
            dismiss();
        }
    }

    private int maxHour, maxMinute, maxSecond;
    private int currHour, currMinute, currSecond;
    private int h, m, s;
    private OnTimeListener listener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_seek_time;
    }

    @Override
    protected void setup(View view) {
        etHour.setInputType(InputType.TYPE_NULL);
        etMinute.setInputType(InputType.TYPE_NULL);
        etSecond.setInputType(InputType.TYPE_NULL);
        etHour.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            Log.i(TAG, "etHour KEYCODE_DPAD_RIGHT");
                            etMinute.requestFocus();
                            return true;

                    }
                }
                return false;
            }
        });
        etMinute.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            Log.i(TAG, "etMinute KEYCODE_DPAD_LEFT");
                            if (etHour.hasFocusable())
                                etHour.requestFocus();
                            return true;

                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            Log.i(TAG, "etMinute KEYCODE_DPAD_RIGHT");
                            if (etSecond.hasFocusable())
                                etSecond.requestFocus();
                            return true;

                    }
                }
                return false;
            }
        });
        etSecond.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            Log.i(TAG, "etSecond KEYCODE_DPAD_LEFT");
                            if (etMinute.hasFocusable())
                                etMinute.requestFocus();
                            return true;

                    }
                }
                return false;
            }
        });
        etHour.addTextChangedListener(new MyTextWatcher(MyTextWatcher.HANDLER_HOUR));
        etMinute.addTextChangedListener(new MyTextWatcher(MyTextWatcher.HANDLER_MINUTE));
        etSecond.addTextChangedListener(new MyTextWatcher(MyTextWatcher.HANDLER_SECOND));
        initUIContent();
    }

    private void initUIContent() {
        etHour.setText(currHour+"");
        etMinute.setText(currMinute+"");
        etSecond.setText(currSecond+"");
        etHour.setFocusable(maxHour != 0);
        etMinute.setFocusable(maxMinute != 0);
        etHour.setTextColor(getResources().getColor(maxHour == 0 ? R.color.epg_text_normal : R.color.epg_text_select));
        etMinute.setTextColor(getResources().getColor(maxMinute == 0 ? R.color.epg_text_normal : R.color.epg_text_select));
    }

    private boolean inputTimeCheck() {
        StringBuilder sbHour = new StringBuilder(etHour.getText().toString());
        StringBuilder sbMinute = new StringBuilder(etMinute.getText().toString());
        StringBuilder sbSecond = new StringBuilder(etSecond.getText().toString());
        h = Integer.valueOf((sbHour.length() == 2 && sbHour.charAt(0) == '0') ? sbHour.deleteCharAt(0).toString() : sbHour.toString());
        m = Integer.valueOf((sbMinute.length() == 2 && sbMinute.charAt(0) == '0') ? sbMinute.deleteCharAt(0).toString() : sbMinute.toString());
        s = Integer.valueOf((sbSecond.length() == 2 && sbSecond.charAt(0) == '0') ? sbSecond.deleteCharAt(0).toString() : sbSecond.toString());
        return h * 60 * 60 + m * 60 + s < maxHour * 60 * 60 + maxMinute * 60 + maxSecond;
    }

    public SeekTimeDialog setTimeLimit(int hour, int minute, int second) {
        this.maxHour = hour;
        this.maxMinute = minute;
        this.maxSecond = second;
        return this;
    }

    public SeekTimeDialog setCurrTime(int currHour, int currMinute, int currSecond) {
        this.currHour = currHour;
        this.currMinute = currMinute;
        this.currSecond = currSecond;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null)
                listener.time(currHour, currMinute, currSecond);
            dismiss();
            return true;
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    class MyTextWatcher implements TextWatcher {

        private static final int HANDLER_HOUR = 0;
        private static final int HANDLER_MINUTE = 1;
        private static final int HANDLER_SECOND = 2;

        private int currHandler;

        public MyTextWatcher(int currHandler) {
            this.currHandler = currHandler;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged: s = " + s + ", start = " + start +
                    ", count = " + count + ", after = " + after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged: s = " + s + ", start = " + start +
                    ", before = " + before + ", count = " + count);
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: " + s);
            int maxTime = 0;
            EditText currView = etHour;
            switch (currHandler) {
                case HANDLER_HOUR:
                    maxTime = maxHour;
                    currView = etHour;
                    break;
                case HANDLER_MINUTE:
                    maxTime = 59;
                    currView = etMinute;
                    break;
                case HANDLER_SECOND:
                    maxTime = 59;
                    currView = etSecond;
                    break;
            }
            StringBuilder sbMinute = new StringBuilder(s.toString());
            if (sbMinute.length() == 3) {
                currView.setText(sbMinute.substring(2).toString());
            } else if (sbMinute.length() == 2 && sbMinute.charAt(0) == '0') {
                sbMinute.deleteCharAt(0);
                currView.setText(Integer.valueOf(sbMinute.toString()) > maxTime ? "0" : sbMinute.toString());
            } else if (Integer.valueOf(sbMinute.toString()) > maxTime)
                currView.setText("0");

            currView.setSelection(currView.getText().toString().length());
        }
    }

    public SeekTimeDialog setTimeListener(OnTimeListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnTimeListener {
        void time(int hour, int minute, int second);
    }
}