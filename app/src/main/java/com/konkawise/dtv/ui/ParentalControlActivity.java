package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.SetPasswordDialog;
import com.sw.dvblib.SWFta;

import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class ParentalControlActivity extends BaseActivity {
    private static final String TAG = "KKDVB_" + ParentalControlActivity.class.getSimpleName();
    private static final int ITEM_MENU_LOCK = 1;
    private static final int ITEM_CHANNEL_LOCK = 2;
    private static final int ITEM_CONTROL_AGE = 3;
    private static final int ITEM_SET_PASSWORD = 4;

    @BindView(R.id.iv_menu_lock_left)
    ImageView mIvMenuLockLeft;

    @BindView(R.id.tv_menu_lock)
    TextView mTvMenuLock;

    @BindView(R.id.iv_menu_lock_right)
    ImageView mIvMenuLockRight;

    @BindView(R.id.iv_channel_lock_left)
    ImageView mIvChannelLockLeft;

    @BindView(R.id.tv_channel_lock)
    TextView mTvChannelLock;

    @BindView(R.id.iv_channel_lock_right)
    ImageView mIvChannelLockRight;

    @BindView(R.id.iv_control_age_left)
    ImageView mTvControlAgeLeft;

    @BindView(R.id.tv_control_age)
    TextView mTvControlAge;

    @BindView(R.id.iv_control_age_right)
    ImageView mIvControlAgeRight;

    @BindArray(R.array.general_switch)
    String[] mGeneralSwitch;

    @BindArray(R.array.parental_control_age)
    String[] mControlAge;

    @OnClick(R.id.item_menu_lock)
    void menuLock() {
        showCheckItemDialog(getResources().getString(R.string.menu_lock), Arrays.asList(mGeneralSwitch), menuLockPosition);
    }

    @OnClick(R.id.item_channel_lock)
    void channelLock() {
        showCheckItemDialog(getResources().getString(R.string.channel_lock2), Arrays.asList(mGeneralSwitch), channelLockPosition);
    }

    @OnClick(R.id.item_control_age)
    void controlAge() {
        showCheckItemDialog(getResources().getString(R.string.control_age), Arrays.asList(mControlAge), controlAgePosition);
    }

    @OnClick(R.id.rl_set_password)
    void setPassword() {
        showPasswordDialog();
    }

    private int position = ITEM_MENU_LOCK;
    private int menuLockPosition;
    private int channelLockPosition;
    private int controlAgePosition;

    @Override
    public int getLayoutId() {
        return R.layout.activity_parental_control;
    }

    @Override
    protected void setup() {
        initData();
        mTvMenuLock.setText(mGeneralSwitch[menuLockPosition]);
        mTvChannelLock.setText(mGeneralSwitch[channelLockPosition]);
        mTvControlAge.setText(mControlAge[controlAgePosition]);
    }

    private void initData() {
        menuLockPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_cMenuLock.ordinal()));
        channelLockPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_cParentLock.ordinal()));
        int ctrlAge = SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_PC_AGE.ordinal());
        if (ctrlAge > 0 && ctrlAge < 19) {
            controlAgePosition = ctrlAge - 1;
        } else {
            controlAgePosition = 0;
        }
    }

    private int getSelectPosition(int[] datas, int value) {
        if (datas == null || datas.length <= 0) return 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == value) return i;
        }
        return 0;
    }

    private void showCheckItemDialog(String title, List<String> content, int selectPosition) {
        new CommCheckItemDialog()
                .title(title)
                .content(content)
                .position(selectPosition)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        switch (position) {
                            case ITEM_MENU_LOCK:
                                mTvMenuLock.setText(checkContent);
                                menuLockPosition = Arrays.asList(mGeneralSwitch).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cMenuLock.ordinal(), menuLockPosition);
                                break;
                            case ITEM_CHANNEL_LOCK:
                                mTvChannelLock.setText(checkContent);
                                channelLockPosition = Arrays.asList(mGeneralSwitch).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cParentLock.ordinal(), channelLockPosition);
                                break;
                            case ITEM_CONTROL_AGE:
                                mTvControlAge.setText(checkContent);
                                controlAgePosition = Arrays.asList(mControlAge).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PC_AGE.ordinal(), controlAgePosition + 1);
                                break;
                            default:
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void showPasswordDialog() {
        new SetPasswordDialog()
                .setOnSavePasswordListener(new SetPasswordDialog.OnSavePasswordListener() {
                    @Override
                    public void onSavePassword(String currentPassword, String newPassword) {
                        SWFtaManager.getInstance().setCommPWDInfo(SWFta.E_E2PP.E2P_Password.ordinal(), newPassword);
                    }

                    @Override
                    public void onCancel() {

                    }
                }).show(getSupportFragmentManager(), SetPasswordDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            switch (position) {
                case ITEM_CHANNEL_LOCK:
                case ITEM_CONTROL_AGE:
                case ITEM_SET_PASSWORD:
                    position--;
                    break;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (position) {
                case ITEM_MENU_LOCK:
                case ITEM_CHANNEL_LOCK:
                case ITEM_CONTROL_AGE:
                    position++;
                    break;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (position) {
                case ITEM_MENU_LOCK:
                    if (--menuLockPosition < 0) menuLockPosition = mGeneralSwitch.length - 1;
                    mTvMenuLock.setText(mGeneralSwitch[menuLockPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cMenuLock.ordinal(), menuLockPosition);
                    break;

                case ITEM_CHANNEL_LOCK:
                    if (--channelLockPosition < 0) channelLockPosition = mGeneralSwitch.length - 1;
                    mTvChannelLock.setText(mGeneralSwitch[channelLockPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cParentLock.ordinal(), channelLockPosition);
                    break;

                case ITEM_CONTROL_AGE:
                    if (--controlAgePosition < 0) controlAgePosition = mControlAge.length - 1;
                    mTvControlAge.setText(mControlAge[controlAgePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PC_AGE.ordinal(), controlAgePosition + 1);
                    break;
            }

        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (position) {
                case ITEM_MENU_LOCK:
                    if (++menuLockPosition > mGeneralSwitch.length - 1) menuLockPosition = 0;
                    mTvMenuLock.setText(mGeneralSwitch[menuLockPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cMenuLock.ordinal(), menuLockPosition);
                    break;

                case ITEM_CHANNEL_LOCK:
                    if (++channelLockPosition > mGeneralSwitch.length - 1) channelLockPosition = 0;
                    mTvChannelLock.setText(mGeneralSwitch[channelLockPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cParentLock.ordinal(), channelLockPosition);
                    break;

                case ITEM_CONTROL_AGE:
                    if (++controlAgePosition > mControlAge.length - 1) controlAgePosition = 0;
                    mTvControlAge.setText(mControlAge[controlAgePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PC_AGE.ordinal(), controlAgePosition + 1);
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(ITEM_MENU_LOCK, mIvMenuLockLeft, mIvMenuLockRight, mTvMenuLock);
        itemChange(ITEM_CHANNEL_LOCK, mIvChannelLockLeft, mIvChannelLockRight, mTvChannelLock);
        itemChange(ITEM_CONTROL_AGE, mTvControlAgeLeft, mIvControlAgeRight, mTvControlAge);
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(position == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(position == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(position == selectItem ? View.VISIBLE : View.INVISIBLE);
    }
}
