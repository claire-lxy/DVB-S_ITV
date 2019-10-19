package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.DTVPVRManager;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class EditTimeDialog extends BaseDialogFragment {
    public static final String TAG = "SeekTimeDialog";

    public static final int FROM_CHANNEL_SCAN_TIEM = 0;

    @BindView(R.id.tv_tip)
    TextView tvTip;

    @BindView(R.id.eidt_hour)
    EditText etHour;

    @BindView(R.id.eidt_minute)
    EditText etMinute;

    @BindView(R.id.eidt_second)
    EditText etSecond;

    @BindView(R.id.colon2)
    TextView tvColon2;

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
    private int from = -1;

    private int totalDuration;
    private int currHour, currMinute, currSecond;
    private int h, m, s;
    private OnTimeListener listener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_seek_time_layout;
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
        if(from == FROM_CHANNEL_SCAN_TIEM){
            tvTip.setText(R.string.channel_scan_time_tip);
            etSecond.setVisibility(View.GONE);
            tvColon2.setVisibility(View.GONE);
        }
        etHour.setText(currHour + "");
        etMinute.setText(currMinute + "");
        etSecond.setText(currSecond + "");
        etHour.setFocusable(totalDuration > 60 * 60 * 1000);
        etMinute.setFocusable(totalDuration > 60 * 1000);
        etHour.setTextColor(getResources().getColor(totalDuration < 60 * 60 * 1000 ? R.color.epg_text_normal : R.color.epg_text_select));
        etMinute.setTextColor(getResources().getColor(totalDuration < 60 * 1000 ? R.color.epg_text_normal : R.color.epg_text_select));
    }

    private boolean inputTimeCheck() {
        StringBuilder sbHour = new StringBuilder(etHour.getText().toString());
        StringBuilder sbMinute = new StringBuilder(etMinute.getText().toString());
        StringBuilder sbSecond = new StringBuilder(etSecond.getText().toString());
        h = Integer.valueOf((sbHour.length() == 2 && sbHour.charAt(0) == '0') ? sbHour.deleteCharAt(0).toString() : sbHour.toString());
        m = Integer.valueOf((sbMinute.length() == 2 && sbMinute.charAt(0) == '0') ? sbMinute.deleteCharAt(0).toString() : sbMinute.toString());
        s = Integer.valueOf((sbSecond.length() == 2 && sbSecond.charAt(0) == '0') ? sbSecond.deleteCharAt(0).toString() : sbSecond.toString());
        if(from == FROM_CHANNEL_SCAN_TIEM){
            return (h * 60 * 60 + m * 60 + s)*1000 <= totalDuration;
        }
        return (h * 60 * 60 + m * 60 + s)*1000 <= DTVPVRManager.getInstance().getPlayProgress().endMs;
    }

    public EditTimeDialog setTimeLimit(int totalDuration) {
        this.totalDuration = totalDuration;
        return this;
    }

    public EditTimeDialog setCurrTime(int currMS) {
        int seconds = currMS / 1000;
        currHour = seconds / (60 * 60);
        currMinute = (seconds - currHour * 60 * 60) / 60;
        currSecond = (seconds - currHour * 60 * 60) % 60;
        return this;
    }

    public EditTimeDialog from(int from){
        this.from = from;
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
                    maxTime = totalDuration / (60 * 60 * 1000);
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

    public EditTimeDialog setTimeListener(OnTimeListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnTimeListener {
        void time(int hour, int minute, int second);
    }
}
