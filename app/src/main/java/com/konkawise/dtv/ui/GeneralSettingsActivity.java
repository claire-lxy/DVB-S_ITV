package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.EditTimeDialog;

import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class GeneralSettingsActivity extends BaseItemFocusChangeActivity {

    private static final String TAG = "GeneralSettingsActivity";
    private static final int ITEM_SCART = 1;
    private static final int ITEM_PFBAR_TIMEOUT = 2;
    private static final int ITEM_RATIO_MODE = 3;
    private static final int ITEM_ASPECT_MODE = 4;
    private static final int ITEM_SWITCH_CHANNEL = 5;
    private static final int ITEM_FIRST_AUDIO_LANGUAGE = 6;
    private static final int ITEM_SECOND_AUDIO_LANGUAGE = 7;
    private static final int ITEM_SUBTITLE_DISPLAY = 8;
    private static final int ITEM_SUBTITLE_LANGUAGE = 9;
    private static final int ITEM_CHANNEL_SCAN = 10;
    private static final int ITEM_CHANNEL_SCAN_TIME = 11;

    @BindView(R.id.item_scart)
    RelativeLayout rlItemScart;

    @BindView(R.id.item_subtitle_language)
    RelativeLayout rlItemSubtitleLanguage;

    @BindView(R.id.item_channel_scan)
    RelativeLayout rlItemChannelScan;

    @BindView(R.id.item_channel_scan_time)
    RelativeLayout rlItemChannelScanTime;

    @BindView(R.id.iv_scart_left)
    ImageView mIvScartLeft;

    @BindView(R.id.tv_scart)
    TextView mTvScart;

    @BindView(R.id.iv_scart_right)
    ImageView mIvScartRight;

    @BindView(R.id.iv_subtitle_display_left)
    ImageView mIvSubtitleDisplayLeft;

    @BindView(R.id.tv_subtitle_display)
    TextView mTvSubtitleDisplay;

    @BindView(R.id.iv_subtitle_display_right)
    ImageView mIvSubtitleDisplayRight;

    @BindView(R.id.iv_pf_timeout_left)
    ImageView mIvPfTimeoutLeft;

    @BindView(R.id.tv_pf_timeout)
    TextView mTvPfTimeout;

    @BindView(R.id.iv_pf_tiemout_right)
    ImageView mIvPfTimeoutRight;

    @BindView(R.id.iv_ratio_left)
    ImageView mIvRatioLeft;

    @BindView(R.id.tv_ratio)
    TextView mTvRatio;

    @BindView(R.id.iv_ratio_right)
    ImageView mIvRatioRight;

    @BindView(R.id.iv_aspect_left)
    ImageView mIvAspectLeft;

    @BindView(R.id.tv_aspect)
    TextView mTvAspect;

    @BindView(R.id.iv_aspect_right)
    ImageView mIvAspectRight;

    @BindView(R.id.iv_switch_channel_left)
    ImageView mIvSwitchChannelLeft;

    @BindView(R.id.tv_switch_channel)
    TextView mTvSwitchChannel;

    @BindView(R.id.iv_switch_channel_right)
    ImageView mIvSwitchChannelRight;

    @BindView(R.id.iv_first_audio_language_left)
    ImageView mIvFirstAudioLanguageLeft;

    @BindView(R.id.tv_first_audio_language)
    TextView mTvFirstAudioLanguage;

    @BindView(R.id.iv_first_audio_language_right)
    ImageView mIvFirstAudioLanguageRight;

    @BindView(R.id.iv_second_audio_language_left)
    ImageView mIvSecondAudioLanguageLeft;

    @BindView(R.id.tv_second_audio_language)
    TextView mTvSecondAudioLanguage;

    @BindView(R.id.iv_second_audio_language_right)
    ImageView mIvSecondAudioLanguageRight;

    @BindView(R.id.iv_subtitle_language_left)
    ImageView mIvSubtitleLanguageLeft;

    @BindView(R.id.tv_subtitle_language)
    TextView mTvSubtitleLanguage;

    @BindView(R.id.iv_subtitle_language_right)
    ImageView mIvSubtitleLanguageRight;

    @BindView(R.id.iv_channel_scan_left)
    ImageView mIvChannelScanLeft;

    @BindView(R.id.tv_channel_scan)
    TextView mTvChannelScan;

    @BindView(R.id.iv_channel_scan_right)
    ImageView mIvChannelScanRight;

    @BindView(R.id.tv_channel_scan_time)
    TextView mTvChannelScanTime;

    @BindArray(R.array.scart)
    String[] mScartArray;

    @BindArray(R.array.pfbar_time)
    String[] mPfTimeoutArray;

    @BindArray(R.array.ratio_mode)
    String[] mRatioModeArray;

    @BindArray(R.array.aspect_mode)
    String[] mAspectModeArray;

    @BindArray(R.array.switch_channel_mode)
    String[] mSwitchChannelModeArray;

    @BindArray(R.array.general_switch)
    String[] mGeneralSwitchArray;

    @BindArray(R.array.language)
    String[] mLanguageArray;

    @OnClick(R.id.item_scart)
    void scart() {
        showGeneralSettingDialog(getString(R.string.scart), Arrays.asList(mScartArray), scartPosition);
    }

    @OnClick(R.id.item_subtitle_display)
    void subtitleDisplay() {
        showGeneralSettingDialog(getString(R.string.subtitles_setting), Arrays.asList(mGeneralSwitchArray), subtitleDisplayPosition);
    }

    @OnClick(R.id.item_pf_timeout)
    void pfTimeout() {
        showGeneralSettingDialog(getString(R.string.timeout), Arrays.asList(mPfTimeoutArray), pfTimeoutPosition);
    }

    @OnClick(R.id.item_ratio_mode)
    void ratioMode() {
        showGeneralSettingDialog(getString(R.string.ratio_mode), Arrays.asList(mRatioModeArray), ratioModePosition);
    }

    @OnClick(R.id.item_aspect_mode)
    void aspectMode() {
        showGeneralSettingDialog(getString(R.string.aspect_mode), Arrays.asList(mAspectModeArray), aspectModePosition);
    }

    @OnClick(R.id.item_switch_channel)
    void switchChannel() {
        showGeneralSettingDialog(getString(R.string.switch_channel_mode), Arrays.asList(mSwitchChannelModeArray), switchChannelPosition);
    }

    @OnClick(R.id.item_first_audio_language)
    void firstAudioLanguage() {
        showGeneralSettingDialog(getString(R.string.first_audio_language), Arrays.asList(mLanguageArray), firstAudioLanguagePosition);
    }

    @OnClick(R.id.item_second_audio_language)
    void secondAudioLanguage() {
        showGeneralSettingDialog(getString(R.string.second_audio_language), Arrays.asList(mLanguageArray), secondAudioLanguagePosition);
    }

    @OnClick(R.id.item_subtitle_language)
    void subtitleLanguage() {
        showGeneralSettingDialog(getString(R.string.subtitle_language), Arrays.asList(mLanguageArray), subtitleLanguagePosition);
    }

    @OnClick(R.id.item_channel_scan)
    void channelScan() {
        showGeneralSettingDialog(getString(R.string.channel_scan), Arrays.asList(mGeneralSwitchArray), channelScanPosition);
    }

    @OnClick(R.id.item_channel_scan_time)
    void channelScanTime() {
        new EditTimeDialog().setCurrTime(SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AutoScanTime) * 1000)
                .setTimeLimit(23 * 60 * 60 * 1000 + 59 * 60 * 1000)
                .from(EditTimeDialog.FROM_CHANNEL_SCAN_TIEM)
                .setTimeListener(new EditTimeDialog.OnTimeListener() {
                    @Override
                    public void time(int hour, int minute, int second) {
                        SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AutoScanTime, hour * 60 * 60 + minute * 60);
                        mTvChannelScanTime.setText(hour + ":" + minute);
                    }
                }).show(getSupportFragmentManager(), EditTimeDialog.TAG);
    }

    private int mCurrentSelectItem = ITEM_SCART;
    private int scartPosition;
    private int subtitleDisplayPosition;
    private int pfTimeoutPosition;
    private int ratioModePosition;
    private int aspectModePosition;
    private int switchChannelPosition;
    private int firstAudioLanguagePosition;
    private int secondAudioLanguagePosition;
    private int subtitleLanguagePosition;
    private int channelScanPosition;

    private int[] arrayPfBarTime = new int[]{5, 8, 10};

    @Override
    public int getLayoutId() {
        return R.layout.activity_general_settings;
    }

    @Override
    protected void setup() {
        initData();

        showOrDismissChannelScanTime();
        mTvScart.setText(mScartArray[scartPosition]);
        mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
        mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
        mTvRatio.setText(mRatioModeArray[ratioModePosition]);
        mTvAspect.setText(mAspectModeArray[aspectModePosition]);
        mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
        mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
        mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
        mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
        mTvChannelScan.setText(mGeneralSwitchArray[channelScanPosition]);
        mTvChannelScanTime.setText(transTimeToStr(SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AutoScanTime)));
    }

    private void initData() {
        scartPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.TV_SCART));
        subtitleDisplayPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.SubtitleDisplay));
        pfTimeoutPosition = getSelectPosition(new int[]{5, 8, 10}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.PD_dispalytime));
        ratioModePosition = getSelectPosition(new int[]{0, 1, 2}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.ScreenRatio));
        aspectModePosition = getSelectPosition(new int[]{0, 1, 2, 3}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AspectRatio));
        switchChannelPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.PD_SwitchMode));
        firstAudioLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AudioLanguage0));
        secondAudioLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AudioLanguage1));
        subtitleLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.SubtitleLanguage));
        channelScanPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.AutoScanEnabled));
    }

    private String transTimeToStr(int second) {
        int hour = second / (60 * 60);
        int minute = (second - hour * 60 * 60) / 60;
        String hStr = hour < 10 ? "0" + hour : "" + hour;
        String mStr = minute < 10 ? "0" + minute : "" + minute;
        return hStr + ":" + mStr;
    }

    private int getSelectPosition(int[] datas, int value) {
        if (datas == null || datas.length <= 0) return 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == value) return i;
        }
        return 0;
    }

    private void showGeneralSettingDialog(String title, List<String> content, int selectPosition) {
        new CommCheckItemDialog()
                .title(title)
                .content(content)
                .position(selectPosition)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        switch (mCurrentSelectItem) {
                            case ITEM_SCART:
                                mTvScart.setText(checkContent);
                                scartPosition = Arrays.asList(mScartArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.TV_SCART, scartPosition);
                                break;
                            case ITEM_SUBTITLE_DISPLAY:
                                mTvSubtitleDisplay.setText(checkContent);
                                subtitleDisplayPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleDisplay, subtitleDisplayPosition);
                                break;
                            case ITEM_PFBAR_TIMEOUT:
                                mTvPfTimeout.setText(checkContent);
                                pfTimeoutPosition = Arrays.asList(mPfTimeoutArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_dispalytime, arrayPfBarTime[pfTimeoutPosition]);
                                break;
                            case ITEM_RATIO_MODE:
                                mTvRatio.setText(checkContent);
                                ratioModePosition = Arrays.asList(mRatioModeArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScreenRatio, ratioModePosition);
                                break;
                            case ITEM_ASPECT_MODE:
                                mTvAspect.setText(checkContent);
                                aspectModePosition = Arrays.asList(mAspectModeArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AspectRatio, aspectModePosition);
                                break;
                            case ITEM_SWITCH_CHANNEL:
                                mTvSwitchChannel.setText(checkContent);
                                switchChannelPosition = Arrays.asList(mSwitchChannelModeArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_SwitchMode, switchChannelPosition);
                                break;
                            case ITEM_FIRST_AUDIO_LANGUAGE:
                                mTvFirstAudioLanguage.setText(checkContent);
                                firstAudioLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage0, firstAudioLanguagePosition);
                                break;
                            case ITEM_SECOND_AUDIO_LANGUAGE:
                                mTvSecondAudioLanguage.setText(checkContent);
                                secondAudioLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage1, secondAudioLanguagePosition);
                                break;
                            case ITEM_SUBTITLE_LANGUAGE:
                                mTvSubtitleLanguage.setText(checkContent);
                                subtitleLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleLanguage, subtitleLanguagePosition);
                                break;
                            case ITEM_CHANNEL_SCAN:
                                mTvChannelScan.setText(checkContent);
                                channelScanPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AutoScanEnabled, channelScanPosition);
                                showOrDismissChannelScanTime();
                                break;
                            default:
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void showOrDismissChannelScanTime() {
        if (channelScanPosition == 0) {
            rlItemChannelScanTime.setVisibility(View.GONE);
        } else {
            rlItemChannelScanTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrentSelectItem) {
                case ITEM_PFBAR_TIMEOUT:
                case ITEM_RATIO_MODE:
                case ITEM_ASPECT_MODE:
                case ITEM_SWITCH_CHANNEL:
                case ITEM_FIRST_AUDIO_LANGUAGE:
                case ITEM_SECOND_AUDIO_LANGUAGE:
                case ITEM_SUBTITLE_DISPLAY:
                case ITEM_SUBTITLE_LANGUAGE:
                case ITEM_CHANNEL_SCAN:
                case ITEM_CHANNEL_SCAN_TIME:
                    mCurrentSelectItem--;
                    break;

                case ITEM_SCART:
                    if (rlItemChannelScanTime.getVisibility() == View.VISIBLE) {
                        mCurrentSelectItem = ITEM_CHANNEL_SCAN_TIME;
                        rlItemChannelScanTime.requestFocus();
                        itemFocusChange();
                    } else {
                        mCurrentSelectItem = ITEM_CHANNEL_SCAN;
                        rlItemChannelScan.requestFocus();
                        itemFocusChange();
                    }
                    return true;
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_SCART:
                case ITEM_PFBAR_TIMEOUT:
                case ITEM_RATIO_MODE:
                case ITEM_ASPECT_MODE:
                case ITEM_SWITCH_CHANNEL:
                case ITEM_FIRST_AUDIO_LANGUAGE:
                case ITEM_SECOND_AUDIO_LANGUAGE:
                case ITEM_SUBTITLE_DISPLAY:
                case ITEM_SUBTITLE_LANGUAGE:
                    mCurrentSelectItem++;
                    break;

                case ITEM_CHANNEL_SCAN:
                    if (rlItemChannelScanTime.getVisibility() != View.VISIBLE) {
                        mCurrentSelectItem = ITEM_SCART;
                        rlItemScart.requestFocus();
                        itemFocusChange();
                        return true;
                    }
                    mCurrentSelectItem++;
                    break;

                case ITEM_CHANNEL_SCAN_TIME:
                    mCurrentSelectItem = ITEM_SCART;
                    rlItemScart.requestFocus();
                    itemFocusChange();
                    return true;
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {
                case ITEM_SCART:
                    if (--scartPosition < 0)
                        scartPosition = mScartArray.length - 1;
                    mTvScart.setText(mScartArray[scartPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.TV_SCART, scartPosition);
                    break;

                case ITEM_SUBTITLE_DISPLAY:
                    if (--subtitleDisplayPosition < 0)
                        subtitleDisplayPosition = mGeneralSwitchArray.length - 1;
                    mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleDisplay, subtitleDisplayPosition);
                    break;

                case ITEM_PFBAR_TIMEOUT:
                    if (--pfTimeoutPosition < 0) pfTimeoutPosition = mPfTimeoutArray.length - 1;
                    mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_dispalytime, arrayPfBarTime[pfTimeoutPosition]);
                    break;

                case ITEM_RATIO_MODE:
                    if (--ratioModePosition < 0)
                        ratioModePosition = mRatioModeArray.length - 1;
                    mTvRatio.setText(mRatioModeArray[ratioModePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScreenRatio, ratioModePosition);
                    break;

                case ITEM_ASPECT_MODE:
                    if (--aspectModePosition < 0)
                        aspectModePosition = mAspectModeArray.length - 1;
                    mTvAspect.setText(mAspectModeArray[aspectModePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AspectRatio, aspectModePosition);
                    break;

                case ITEM_SWITCH_CHANNEL:
                    if (--switchChannelPosition < 0)
                        switchChannelPosition = mSwitchChannelModeArray.length - 1;
                    mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_SwitchMode, switchChannelPosition);
                    break;

                case ITEM_FIRST_AUDIO_LANGUAGE:
                    if (--firstAudioLanguagePosition < 0)
                        firstAudioLanguagePosition = mLanguageArray.length - 1;
                    mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage0, firstAudioLanguagePosition);
                    break;

                case ITEM_SECOND_AUDIO_LANGUAGE:
                    if (--secondAudioLanguagePosition < 0)
                        secondAudioLanguagePosition = mLanguageArray.length - 1;
                    mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage1, secondAudioLanguagePosition);
                    break;

                case ITEM_SUBTITLE_LANGUAGE:
                    if (--subtitleLanguagePosition < 0)
                        subtitleLanguagePosition = mLanguageArray.length - 1;
                    mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleLanguage, subtitleLanguagePosition);
                    break;

                case ITEM_CHANNEL_SCAN:
                    if (--channelScanPosition < 0)
                        channelScanPosition = mGeneralSwitchArray.length - 1;
                    mTvChannelScan.setText(mGeneralSwitchArray[channelScanPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AutoScanEnabled, channelScanPosition);
                    showOrDismissChannelScanTime();
                    break;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {
                case ITEM_SCART:
                    if (++scartPosition > mScartArray.length - 1)
                        scartPosition = 0;
                    mTvScart.setText(mScartArray[scartPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.TV_SCART, scartPosition);
                    break;

                case ITEM_SUBTITLE_DISPLAY:
                    if (++subtitleDisplayPosition > mGeneralSwitchArray.length - 1)
                        subtitleDisplayPosition = 0;
                    mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleDisplay, subtitleDisplayPosition);
                    break;

                case ITEM_PFBAR_TIMEOUT:
                    if (++pfTimeoutPosition > mPfTimeoutArray.length - 1) pfTimeoutPosition = 0;
                    mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_dispalytime, arrayPfBarTime[pfTimeoutPosition]);
                    break;

                case ITEM_RATIO_MODE:
                    if (++ratioModePosition > mRatioModeArray.length - 1)
                        ratioModePosition = 0;
                    mTvRatio.setText(mRatioModeArray[ratioModePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScreenRatio, ratioModePosition);
                    break;

                case ITEM_ASPECT_MODE:
                    if (++aspectModePosition > mAspectModeArray.length - 1)
                        aspectModePosition = 0;
                    mTvAspect.setText(mAspectModeArray[aspectModePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AspectRatio, aspectModePosition);
                    break;

                case ITEM_SWITCH_CHANNEL:
                    if (++switchChannelPosition > mSwitchChannelModeArray.length - 1)
                        switchChannelPosition = 0;
                    mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.PD_SwitchMode, switchChannelPosition);
                    break;

                case ITEM_FIRST_AUDIO_LANGUAGE:
                    if (++firstAudioLanguagePosition > mLanguageArray.length - 1)
                        firstAudioLanguagePosition = 0;
                    mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage0, firstAudioLanguagePosition);
                    break;

                case ITEM_SECOND_AUDIO_LANGUAGE:
                    if (++secondAudioLanguagePosition > mLanguageArray.length - 1)
                        secondAudioLanguagePosition = 0;
                    mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AudioLanguage1, secondAudioLanguagePosition);
                    break;

                case ITEM_SUBTITLE_LANGUAGE:
                    if (++subtitleLanguagePosition > mLanguageArray.length - 1)
                        subtitleLanguagePosition = 0;
                    mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.SubtitleLanguage, subtitleLanguagePosition);
                    break;

                case ITEM_CHANNEL_SCAN:
                    if (++channelScanPosition > mGeneralSwitchArray.length - 1)
                        channelScanPosition = 0;
                    mTvChannelScan.setText(mGeneralSwitchArray[channelScanPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.AutoScanEnabled, channelScanPosition);
                    showOrDismissChannelScanTime();
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(mCurrentSelectItem, ITEM_SCART, mIvScartLeft, mIvScartRight, mTvScart);
        itemChange(mCurrentSelectItem, ITEM_SUBTITLE_DISPLAY, mIvSubtitleDisplayLeft, mIvSubtitleDisplayRight, mTvSubtitleDisplay);
        itemChange(mCurrentSelectItem, ITEM_PFBAR_TIMEOUT, mIvPfTimeoutLeft, mIvPfTimeoutRight, mTvPfTimeout);
        itemChange(mCurrentSelectItem, ITEM_RATIO_MODE, mIvRatioLeft, mIvRatioRight, mTvRatio);
        itemChange(mCurrentSelectItem, ITEM_ASPECT_MODE, mIvAspectLeft, mIvAspectRight, mTvAspect);
        itemChange(mCurrentSelectItem, ITEM_SWITCH_CHANNEL, mIvSwitchChannelLeft, mIvSwitchChannelRight, mTvSwitchChannel);
        itemChange(mCurrentSelectItem, ITEM_FIRST_AUDIO_LANGUAGE, mIvFirstAudioLanguageLeft, mIvFirstAudioLanguageRight, mTvFirstAudioLanguage);
        itemChange(mCurrentSelectItem, ITEM_SECOND_AUDIO_LANGUAGE, mIvSecondAudioLanguageLeft, mIvSecondAudioLanguageRight, mTvSecondAudioLanguage);
        itemChange(mCurrentSelectItem, ITEM_SUBTITLE_LANGUAGE, mIvSubtitleLanguageLeft, mIvSubtitleLanguageRight, mTvSubtitleLanguage);
        itemChange(mCurrentSelectItem, ITEM_CHANNEL_SCAN, mIvChannelScanLeft, mIvChannelScanRight, mTvChannelScan);
        itemChange(mCurrentSelectItem, ITEM_CHANNEL_SCAN_TIME, null, null, mTvChannelScanTime);
    }
}
