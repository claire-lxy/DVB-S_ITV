package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.sw.dvblib.SWFta;

import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class GeneralSettingsActivity extends BaseActivity {
    private static final String TAG = "KKDVB_" + GeneralSettingsActivity.class.getSimpleName();
    private static final int ITEM_ANTENNA_POWER = 1;
    private static final int ITEM_AREA_SETTING = 2;
    private static final int ITEM_LCN = 3;
    private static final int ITEM_SUBTITLE_DISPLAY = 4;
    private static final int ITEM_PFBAR_TIMEOUT = 5;
    private static final int ITEM_ASPECT_RATIO = 6;
    private static final int ITEM_SWITCH_CHANNEL = 7;
    private static final int ITEM_FIRST_AUDIO_LANGUAGE = 8;
    private static final int ITEM_SECOND_AUDIO_LANGUAGE = 9;
    private static final int ITEM_TELETEXT_LANGUAGE = 10;
    private static final int ITEM_SUBTITLE_LANGUAGE = 11;

    @BindView(R.id.item_antenna_power)
    RelativeLayout mItemAntennaPower;

    @BindView(R.id.iv_antenna_power_left)
    ImageView mIvAntennaPowerLeft;

    @BindView(R.id.tv_antenna_power)
    TextView mTvAntennaPower;

    @BindView(R.id.iv_antenna_power_right)
    ImageView mIvAntennaPowerRight;

    @BindView(R.id.item_area_setting)
    RelativeLayout mItemAreaSetting;

    @BindView(R.id.iv_area_setting_left)
    ImageView mIvAreaSettingLeft;

    @BindView(R.id.tv_area_setting)
    TextView mTvAreaSetting;

    @BindView(R.id.iv_area_setting_right)
    ImageView mIvAreaSettingRight;

    @BindView(R.id.item_lcn)
    RelativeLayout mItemLcn;

    @BindView(R.id.iv_lcn_left)
    ImageView mIvLcnLeft;

    @BindView(R.id.tv_lcn)
    TextView mTvLcn;

    @BindView(R.id.iv_lcn_right)
    ImageView mIvLcnRight;

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

    @BindView(R.id.iv_aspect_radio_left)
    ImageView mIvAspectRadioLeft;

    @BindView(R.id.tv_aspect_radio)
    TextView mTvAspectRadio;

    @BindView(R.id.iv_aspect_radio_right)
    ImageView mIvAspectRadioRight;

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

    @BindView(R.id.iv_teletext_language_left)
    ImageView mIvTeletextLanguageLeft;

    @BindView(R.id.tv_teletext_language)
    TextView mTvTeletextLanguage;

    @BindView(R.id.iv_teletext_language_right)
    ImageView mIvTeletextLanguageRight;

    @BindView(R.id.iv_subtitle_language_left)
    ImageView mIvSubtitleLanguageLeft;

    @BindView(R.id.tv_subtitle_language)
    TextView mTvSubtitleLanguage;

    @BindView(R.id.iv_subtitle_language_right)
    ImageView mIvSubtitleLanguageRight;

    @BindArray(R.array.antenna_power)
    String[] mAntennaPowerArray;

    @BindArray(R.array.area_setting)
    String[] mAreaSettingArray;

    @BindArray(R.array.pfbar_time)
    String[] mPfTimeoutArray;

    @BindArray(R.array.aspect_ratio_mode)
    String[] mAspectRatioModeArray;

    @BindArray(R.array.switch_channel_mode)
    String[] mSwitchChannelModeArray;

    @BindArray(R.array.general_switch)
    String[] mGeneralSwitchArray;

    @BindArray(R.array.language)
    String[] mLanguageArray;

    @OnClick(R.id.item_antenna_power)
    void antennaPower() {
        showGeneralSettingDialog(getString(R.string.antenna_power), Arrays.asList(mGeneralSwitchArray), antennaPowerPosition);
    }

    @OnClick(R.id.item_area_setting)
    void areaSetting() {
        showGeneralSettingDialog(getString(R.string.area_setting), Arrays.asList(mAreaSettingArray), areaSettingPosition);
    }

    @OnClick(R.id.item_lcn)
    void lcn() {
        showGeneralSettingDialog(getString(R.string.lcn), Arrays.asList(mGeneralSwitchArray), lcnPosition);
    }

    @OnClick(R.id.item_subtitle_display)
    void subtitleDisplay() {
        showGeneralSettingDialog(getString(R.string.subtitles_setting), Arrays.asList(mGeneralSwitchArray), subtitleDisplayPosition);
    }

    @OnClick(R.id.item_pf_timeout)
    void pfTimeout() {
        showGeneralSettingDialog(getString(R.string.timeout), Arrays.asList(mPfTimeoutArray), pfTimeoutPosition);
    }

    @OnClick(R.id.item_aspect_radio)
    void aspectRadio() {
        showGeneralSettingDialog(getString(R.string.aspect_ratio_mode), Arrays.asList(mAspectRatioModeArray), aspectRatioPosition);
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

    @OnClick(R.id.item_teletext_language)
    void teletextLanguage() {
        showGeneralSettingDialog(getString(R.string.teletext), Arrays.asList(mLanguageArray), teletextLanguagePosition);
    }

    @OnClick(R.id.item_subtitle_language)
    void subtitleLanguage() {
        showGeneralSettingDialog(getString(R.string.subtitle_language), Arrays.asList(mLanguageArray), subtitleLanguagePosition);
    }

    private int mCurrentSelectItem = ITEM_SUBTITLE_DISPLAY;
    private int antennaPowerPosition;
    private int areaSettingPosition;
    private int lcnPosition;
    private int subtitleDisplayPosition;
    private int pfTimeoutPosition;
    private int aspectRatioPosition;
    private int switchChannelPosition;
    private int firstAudioLanguagePosition;
    private int secondAudioLanguagePosition;
    private int teletextLanguagePosition;
    private int subtitleLanguagePosition;

    private int[] arrayPfBarTime = new int[]{5, 8, 10};

    @Override
    public int getLayoutId() {
        return R.layout.activity_general_settings;
    }

    @Override
    protected void setup() {
        initIntent();
        initData();

        if (isT2Setting()) {
            mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
            mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
            mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
        }
        mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
        mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
        mTvAspectRadio.setText(mAspectRatioModeArray[aspectRatioPosition]);
        mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
        mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
        mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
        mTvTeletextLanguage.setText(mLanguageArray[teletextLanguagePosition]);
        mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
    }

    private void initIntent() {
        if (isT2Setting()) {
            mItemAntennaPower.setVisibility(View.VISIBLE);
            mItemAreaSetting.setVisibility(View.VISIBLE);
            mItemLcn.setVisibility(View.VISIBLE);
        }
    }

    private boolean isT2Setting() {
        return getIntent().getBooleanExtra(Constants.IntentKey.INTENT_T2_SETTING, false);
    }

    private void initData() {
        antennaPowerPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal()));
        subtitleDisplayPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleDisplay.ordinal()));
        pfTimeoutPosition = getSelectPosition(new int[]{5, 8, 10},SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_PD_dispalytime.ordinal()));
        aspectRatioPosition = getSelectPosition(new int[]{0, 1, 2}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_ScreenRatio.ordinal()));
        switchChannelPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_PD_SwitchMode.ordinal()));
        firstAudioLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage0.ordinal()));
        secondAudioLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage1.ordinal()));
        teletextLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_cLanguage.ordinal()));
        subtitleLanguagePosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleLanguage.ordinal()));
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
                            case ITEM_ANTENNA_POWER:
                                mTvAntennaPower.setText(checkContent);
                                antennaPowerPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal(), antennaPowerPosition);
                                break;
                            case ITEM_AREA_SETTING:
                                mTvAreaSetting.setText(checkContent);
                                areaSettingPosition = Arrays.asList(mAreaSettingArray).indexOf(checkContent);
                                break;
                            case ITEM_LCN:
                                mTvLcn.setText(checkContent);
                                lcnPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                break;
                            case ITEM_SUBTITLE_DISPLAY:
                                mTvSubtitleDisplay.setText(checkContent);
                                subtitleDisplayPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleDisplay.ordinal(), subtitleDisplayPosition);
                                break;
                            case ITEM_PFBAR_TIMEOUT:
                                mTvPfTimeout.setText(checkContent);
                                pfTimeoutPosition = Arrays.asList(mPfTimeoutArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_dispalytime.ordinal(), arrayPfBarTime[pfTimeoutPosition]);
                                break;
                            case ITEM_ASPECT_RATIO:
                                mTvAspectRadio.setText(checkContent);
                                aspectRatioPosition = Arrays.asList(mAspectRatioModeArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_ScreenRatio.ordinal(), aspectRatioPosition);
                                break;
                            case ITEM_SWITCH_CHANNEL:
                                mTvSwitchChannel.setText(checkContent);
                                switchChannelPosition = Arrays.asList(mSwitchChannelModeArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_SwitchMode.ordinal(), switchChannelPosition);
                                break;
                            case ITEM_FIRST_AUDIO_LANGUAGE:
                                mTvFirstAudioLanguage.setText(checkContent);
                                firstAudioLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage0.ordinal(), firstAudioLanguagePosition);
                                break;
                            case ITEM_SECOND_AUDIO_LANGUAGE:
                                mTvSecondAudioLanguage.setText(checkContent);
                                secondAudioLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage1.ordinal(), secondAudioLanguagePosition);
                                break;
                            case ITEM_TELETEXT_LANGUAGE:
                                mTvTeletextLanguage.setText(checkContent);
                                teletextLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cLanguage.ordinal(), teletextLanguagePosition);
                                break;
                            case ITEM_SUBTITLE_LANGUAGE:
                                mTvSubtitleLanguage.setText(checkContent);
                                subtitleLanguagePosition = Arrays.asList(mLanguageArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleLanguage.ordinal(), subtitleLanguagePosition);
                                break;
                            default:
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (isT2Setting()) {
                switch (mCurrentSelectItem) {
                    case ITEM_AREA_SETTING:
                    case ITEM_LCN:
                    case ITEM_SUBTITLE_DISPLAY:
                    case ITEM_PFBAR_TIMEOUT:
                    case ITEM_ASPECT_RATIO:
                    case ITEM_SWITCH_CHANNEL:
                    case ITEM_FIRST_AUDIO_LANGUAGE:
                    case ITEM_SECOND_AUDIO_LANGUAGE:
                    case ITEM_TELETEXT_LANGUAGE:
                    case ITEM_SUBTITLE_LANGUAGE:
                        mCurrentSelectItem--;
                        break;
                }
            } else {
                switch (mCurrentSelectItem) {
                    case ITEM_PFBAR_TIMEOUT:
                    case ITEM_ASPECT_RATIO:
                    case ITEM_SWITCH_CHANNEL:
                    case ITEM_FIRST_AUDIO_LANGUAGE:
                    case ITEM_SECOND_AUDIO_LANGUAGE:
                    case ITEM_TELETEXT_LANGUAGE:
                    case ITEM_SUBTITLE_LANGUAGE:
                        mCurrentSelectItem--;
                        break;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (isT2Setting()) {
                switch (mCurrentSelectItem) {
                    case ITEM_ANTENNA_POWER:
                    case ITEM_AREA_SETTING:
                    case ITEM_LCN:
                    case ITEM_SUBTITLE_DISPLAY:
                    case ITEM_PFBAR_TIMEOUT:
                    case ITEM_ASPECT_RATIO:
                    case ITEM_SWITCH_CHANNEL:
                    case ITEM_FIRST_AUDIO_LANGUAGE:
                    case ITEM_SECOND_AUDIO_LANGUAGE:
                    case ITEM_TELETEXT_LANGUAGE:
                        mCurrentSelectItem++;
                        break;
                }
            } else {
                switch (mCurrentSelectItem) {
                    case ITEM_SUBTITLE_DISPLAY:
                    case ITEM_PFBAR_TIMEOUT:
                    case ITEM_ASPECT_RATIO:
                    case ITEM_SWITCH_CHANNEL:
                    case ITEM_FIRST_AUDIO_LANGUAGE:
                    case ITEM_SECOND_AUDIO_LANGUAGE:
                    case ITEM_TELETEXT_LANGUAGE:
                        mCurrentSelectItem++;
                        break;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {
                case ITEM_ANTENNA_POWER:
                    if (--antennaPowerPosition < 0)
                        antennaPowerPosition = mGeneralSwitchArray.length - 1;
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
                    break;

                case ITEM_AREA_SETTING:
                    if (--areaSettingPosition < 0)
                        areaSettingPosition = mAreaSettingArray.length - 1;
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    if (--lcnPosition < 0) lcnPosition = mGeneralSwitchArray.length - 1;
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    break;

                case ITEM_SUBTITLE_DISPLAY:
                    if (--subtitleDisplayPosition < 0)
                        subtitleDisplayPosition = mGeneralSwitchArray.length - 1;
                    mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleDisplay.ordinal(), subtitleDisplayPosition);
                    break;

                case ITEM_PFBAR_TIMEOUT:
                    if (--pfTimeoutPosition < 0) pfTimeoutPosition = mPfTimeoutArray.length - 1;
                    mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_dispalytime.ordinal(), arrayPfBarTime[pfTimeoutPosition]);
                    break;

                case ITEM_ASPECT_RATIO:
                    if (--aspectRatioPosition < 0)
                        aspectRatioPosition = mAspectRatioModeArray.length - 1;
                    mTvAspectRadio.setText(mAspectRatioModeArray[aspectRatioPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_ScreenRatio.ordinal(), aspectRatioPosition);
                    break;

                case ITEM_SWITCH_CHANNEL:
                    if (--switchChannelPosition < 0)
                        switchChannelPosition = mSwitchChannelModeArray.length - 1;
                    mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_SwitchMode.ordinal(), switchChannelPosition);
                    break;

                case ITEM_FIRST_AUDIO_LANGUAGE:
                    if (--firstAudioLanguagePosition < 0)
                        firstAudioLanguagePosition = mLanguageArray.length - 1;
                    mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage0.ordinal(), firstAudioLanguagePosition);
                    break;

                case ITEM_SECOND_AUDIO_LANGUAGE:
                    if (--secondAudioLanguagePosition < 0)
                        secondAudioLanguagePosition = mLanguageArray.length - 1;
                    mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage1.ordinal(), secondAudioLanguagePosition);
                    break;

                case ITEM_TELETEXT_LANGUAGE:
                    if (--teletextLanguagePosition < 0)
                        teletextLanguagePosition = mLanguageArray.length - 1;
                    mTvTeletextLanguage.setText(mLanguageArray[teletextLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cLanguage.ordinal(), teletextLanguagePosition);
                    break;

                case ITEM_SUBTITLE_LANGUAGE:
                    if (--subtitleLanguagePosition < 0)
                        subtitleLanguagePosition = mLanguageArray.length - 1;
                    mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleLanguage.ordinal(), subtitleLanguagePosition);
                    break;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {
                case ITEM_ANTENNA_POWER:
                    if (++antennaPowerPosition > mGeneralSwitchArray.length - 1)
                        antennaPowerPosition = 0;
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
                    break;

                case ITEM_AREA_SETTING:
                    if (++areaSettingPosition > mAreaSettingArray.length - 1)
                        areaSettingPosition = 0;
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    if (++lcnPosition > mSwitchChannelModeArray.length - 1) lcnPosition = 0;
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    break;

                case ITEM_SUBTITLE_DISPLAY:
                    if (++subtitleDisplayPosition > mGeneralSwitchArray.length - 1)
                        subtitleDisplayPosition = 0;
                    mTvSubtitleDisplay.setText(mGeneralSwitchArray[subtitleDisplayPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleDisplay.ordinal(), subtitleDisplayPosition);
                    break;

                case ITEM_PFBAR_TIMEOUT:
                    if (++pfTimeoutPosition > mPfTimeoutArray.length - 1) pfTimeoutPosition = 0;
                    mTvPfTimeout.setText(mPfTimeoutArray[pfTimeoutPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_dispalytime.ordinal(), arrayPfBarTime[pfTimeoutPosition]);
                    break;

                case ITEM_ASPECT_RATIO:
                    if (++aspectRatioPosition > mAspectRatioModeArray.length - 1)
                        aspectRatioPosition = 0;
                    mTvAspectRadio.setText(mAspectRatioModeArray[aspectRatioPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_ScreenRatio.ordinal(), aspectRatioPosition);
                    break;

                case ITEM_SWITCH_CHANNEL:
                    if (++switchChannelPosition > mSwitchChannelModeArray.length - 1)
                        switchChannelPosition = 0;
                    mTvSwitchChannel.setText(mSwitchChannelModeArray[switchChannelPosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_PD_SwitchMode.ordinal(), switchChannelPosition);
                    break;

                case ITEM_FIRST_AUDIO_LANGUAGE:
                    if (++firstAudioLanguagePosition > mLanguageArray.length - 1)
                        firstAudioLanguagePosition = 0;
                    mTvFirstAudioLanguage.setText(mLanguageArray[firstAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage0.ordinal(), firstAudioLanguagePosition);
                    break;

                case ITEM_SECOND_AUDIO_LANGUAGE:
                    if (++secondAudioLanguagePosition > mLanguageArray.length - 1)
                        secondAudioLanguagePosition = 0;
                    mTvSecondAudioLanguage.setText(mLanguageArray[secondAudioLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AudioLanguage1.ordinal(), secondAudioLanguagePosition);
                    break;

                case ITEM_TELETEXT_LANGUAGE:
                    if (++teletextLanguagePosition > mLanguageArray.length - 1)
                        teletextLanguagePosition = 0;
                    mTvTeletextLanguage.setText(mLanguageArray[teletextLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_cLanguage.ordinal(), teletextLanguagePosition);
                    break;

                case ITEM_SUBTITLE_LANGUAGE:
                    if (++subtitleLanguagePosition > mLanguageArray.length - 1)
                        subtitleLanguagePosition = 0;
                    mTvSubtitleLanguage.setText(mLanguageArray[subtitleLanguagePosition]);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SubtitleLanguage.ordinal(), subtitleLanguagePosition);
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
        antennaPowerItemFocusChange();
        areaSettingItemFocusChange();
        lcnItemFocusChange();
        itemChange(ITEM_SUBTITLE_DISPLAY, mIvSubtitleDisplayLeft, mIvSubtitleDisplayRight, mTvSubtitleDisplay);
        itemChange(ITEM_PFBAR_TIMEOUT, mIvPfTimeoutLeft, mIvPfTimeoutRight, mTvPfTimeout);
        itemChange(ITEM_ASPECT_RATIO, mIvAspectRadioLeft, mIvAspectRadioRight, mTvAspectRadio);
        itemChange(ITEM_SWITCH_CHANNEL, mIvSwitchChannelLeft, mIvSwitchChannelRight, mTvSwitchChannel);
        itemChange(ITEM_FIRST_AUDIO_LANGUAGE, mIvFirstAudioLanguageLeft, mIvFirstAudioLanguageRight, mTvFirstAudioLanguage);
        itemChange(ITEM_SECOND_AUDIO_LANGUAGE, mIvSecondAudioLanguageLeft, mIvSecondAudioLanguageRight, mTvSecondAudioLanguage);
        itemChange(ITEM_TELETEXT_LANGUAGE, mIvTeletextLanguageLeft, mIvTeletextLanguageRight, mTvTeletextLanguage);
        itemChange(ITEM_SUBTITLE_LANGUAGE, mIvSubtitleLanguageLeft, mIvSubtitleLanguageRight, mTvSubtitleLanguage);
    }

    private void antennaPowerItemFocusChange() {
        if (isT2Setting()) {
            itemChange(ITEM_ANTENNA_POWER, mIvAntennaPowerLeft, mIvAntennaPowerRight, mTvAntennaPower);
        }
    }

    private void areaSettingItemFocusChange() {
        if (isT2Setting()) {
            itemChange(ITEM_AREA_SETTING, mIvAreaSettingLeft, mIvAreaSettingRight, mTvAreaSetting);
        }
    }

    private void lcnItemFocusChange() {
        if (isT2Setting()) {
            itemChange(ITEM_LCN, mIvLcnLeft, mIvLcnRight, mTvLcn);
        }
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(mCurrentSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
    }
}
