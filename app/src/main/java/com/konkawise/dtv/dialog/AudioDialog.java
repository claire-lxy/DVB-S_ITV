package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.DTVPlayerManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.DTVPVRManager;
import com.konkawise.dtv.base.BaseItemFocusChangeDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Enum_StreamType;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Enum_PlayParam;

public class AudioDialog extends BaseItemFocusChangeDialogFragment {

    public static final String TAG = "AudioDialog";
    private static final int ITEM_AUDIO_TRACK = 1;
    private static final int ITEM_AUDIO_LANGUAGE = 2;

    public static final int WHERE_TOPMOST = 0;
    public static final int WHERE_RECORDPLAYER = 1;

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_audio_track_left)
    ImageView mIvAudioTrackLeft;

    @BindView(R.id.tv_audio_track)
    TextView mTvAudioTrack;

    @BindView(R.id.iv_audio_track_right)
    ImageView mIvAudioTrackRight;

    @BindView(R.id.iv_audio_language_left)
    ImageView mIvAudioLanguageLeft;

    @BindView(R.id.tv_audio_language)
    TextView mTvAudioLanguage;

    @BindView(R.id.iv_audio_language_right)
    ImageView mIvAudioLanguageRight;

    @BindArray(R.array.audio_track)
    String[] mAudioTrackArray;

    private String mTitle;
    private int where;
    private String[] mAudioLanguageArray;
    private List<Integer> audioTypeList = new ArrayList<>();
    private List<Integer> audioPidList = new ArrayList<>();
    private int mCurrentSelectItem = ITEM_AUDIO_TRACK;
    private int audioTrackPosition;
    private int audioLanguagePosition;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_audio_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);


        audioTrackPosition = DTVPlayerManager.getInstance().getCurrProgParam(HPlayer_Enum_PlayParam.TRACK);
        mTvAudioTrack.setText(mAudioTrackArray[audioTrackPosition]);

        if (where == WHERE_TOPMOST) {
            audioLanguagePosition = DTVPlayerManager.getInstance().getCurrProgParam(HPlayer_Enum_PlayParam.AUDIO);
            ArrayList<String> audioNameList = DTVProgramManager.getInstance().getCurrProgInfo().audioDB.audioName;
            audioTypeList = DTVProgramManager.getInstance().getCurrProgInfo().audioDB.ucAudStrType;
            mAudioLanguageArray = new String[audioNameList.size()];
            for (int i = 0, j = 1; i < audioNameList.size(); i++) {
                Log.i(TAG, "audioNameList[" + i + "]: " + audioNameList.get(i) + " AudioTypeList[" + i + "]: " + audioTypeList.get(i));
                mAudioLanguageArray[i] = audioNameList.get(i);
                if (mAudioLanguageArray[i].equals("Audio"))
                    mAudioLanguageArray[i] = "Audio" + j++;
                mAudioLanguageArray[i] = mAudioLanguageArray[i] + "(" + getAudioNameByType(audioTypeList.get(i)) + ")";
            }
            mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
        } else {
            audioLanguagePosition = DTVPVRManager.getInstance().getCurrAudioIndex();
            ArrayList<String> audioNameList = DTVPVRManager.getInstance().getAudioList().audioName;
            audioTypeList = DTVPVRManager.getInstance().getAudioList().ucAudStrType;
            audioPidList = DTVPVRManager.getInstance().getAudioList().sAudPid;
            mAudioLanguageArray = new String[audioNameList.size()];
            for (int i = 0, j = 1; i < audioNameList.size(); i++) {
                Log.i(TAG, "audioNameList2[" + i + "] : " + audioNameList.get(i) + " AudioTypeList2[" + i + "] : " + audioTypeList.get(i));
                mAudioLanguageArray[i] = audioNameList.get(i);
                if (mAudioLanguageArray[i].equals("Audio"))
                    mAudioLanguageArray[i] = "Audio" + j++;
                mAudioLanguageArray[i] = mAudioLanguageArray[i] + "(" + getAudioNameByType(audioTypeList.get(i)) + ")";
            }
            mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
        }


        Log.i(TAG, "position = " + audioLanguagePosition);
        Log.i(TAG, "length = " + mAudioLanguageArray.length);
    }

    private String getAudioNameByType(int streamType) {
        switch (streamType) {
            case HPlayer_Enum_StreamType.PRIVATE_SECTIONS:
            case HPlayer_Enum_StreamType.PRIVATE_PES:
                return getResources().getString(R.string.audio_language_type_private);

            case HPlayer_Enum_StreamType.AUDIO_MPEG1:
                return getResources().getString(R.string.audio_language_type_mpeg1);

            case HPlayer_Enum_StreamType.AUDIO_MPEG2:
                return getResources().getString(R.string.audio_language_type_mpeg2);

            case HPlayer_Enum_StreamType.AUDIO_AAC_ADTS:
            case HPlayer_Enum_StreamType.AUDIO_AAC_LATM:
            case HPlayer_Enum_StreamType.AUDIO_AAC_RAW:
                return getResources().getString(R.string.audio_language_type_aac);

            case HPlayer_Enum_StreamType.AUDIO_AC3:
                return getResources().getString(R.string.audio_language_type_ac3);

            case HPlayer_Enum_StreamType.AUDIO_AC3_PLUS:
                return getResources().getString(R.string.audio_language_type_ac3_plus);

            case HPlayer_Enum_StreamType.AUDIO_DTS:
                return getResources().getString(R.string.audio_language_type_dts);

            case HPlayer_Enum_StreamType.AUDIO_LPCM:
                return getResources().getString(R.string.audio_language_type_lpcm);

            case HPlayer_Enum_StreamType.AUDIO_WM9:
                return getResources().getString(R.string.audio_language_type_wm9);


            case HPlayer_Enum_StreamType.MHEG:
                return getResources().getString(R.string.audio_language_type_mheg);

            case HPlayer_Enum_StreamType.DSM_CC:
                return getResources().getString(R.string.audio_language_type_dsm_cc);

            case HPlayer_Enum_StreamType.TYPE_H2221:
                return getResources().getString(R.string.audio_language_type_h2221);

            case HPlayer_Enum_StreamType.TYPE_A:
                return getResources().getString(R.string.audio_language_type_a);

            case HPlayer_Enum_StreamType.TYPE_B:
                return getResources().getString(R.string.audio_language_type_b);

            case HPlayer_Enum_StreamType.TYPE_C:
                return getResources().getString(R.string.audio_language_type_c);

            case HPlayer_Enum_StreamType.TYPE_D:
                return getResources().getString(R.string.audio_language_type_d);

            case HPlayer_Enum_StreamType.TYPE_AUX:
                return getResources().getString(R.string.audio_language_type_aux);

        }

        return getResources().getString(R.string.audio_language_type_unknown);
    }

    public AudioDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public AudioDialog where(int where) {
        this.where = where;
        return this;
    }

    public AudioDialog content(String[] content) {
        this.mAudioLanguageArray = content == null ? new String[0] : content;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (mCurrentSelectItem == ITEM_AUDIO_LANGUAGE) {
                    mCurrentSelectItem--;
                    itemFocusChange();
                }
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mCurrentSelectItem == ITEM_AUDIO_TRACK) {
                    mCurrentSelectItem++;
                    itemFocusChange();
                }
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                switch (mCurrentSelectItem) {
                    case ITEM_AUDIO_TRACK:
                        if (--audioTrackPosition < 0)
                            audioTrackPosition = mAudioTrackArray.length - 1;
                        mTvAudioTrack.setText(mAudioTrackArray[audioTrackPosition]);
                        DTVPlayerManager.getInstance().setCurrProgParam(HPlayer_Enum_PlayParam.TRACK, audioTrackPosition);
                        break;

                    case ITEM_AUDIO_LANGUAGE:
                        if (mAudioLanguageArray.length == 0)
                            break;
                        if (--audioLanguagePosition < 0)
                            audioLanguagePosition = mAudioLanguageArray.length - 1;
                        mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
                        if (where == WHERE_TOPMOST) {
                            DTVPlayerManager.getInstance().setCurrProgParam(HPlayer_Enum_PlayParam.AUDIO, audioLanguagePosition);
                        } else {
                            DTVPVRManager.getInstance().setAudioPid(audioPidList.get(audioLanguagePosition), audioTypeList.get(audioLanguagePosition));
                        }

                        break;
                }
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                switch (mCurrentSelectItem) {
                    case ITEM_AUDIO_TRACK:
                        if (++audioTrackPosition > mAudioTrackArray.length - 1)
                            audioTrackPosition = 0;
                        mTvAudioTrack.setText(mAudioTrackArray[audioTrackPosition]);
                        DTVPlayerManager.getInstance().setCurrProgParam(HPlayer_Enum_PlayParam.TRACK, audioTrackPosition);
                        break;

                    case ITEM_AUDIO_LANGUAGE:
                        if (mAudioLanguageArray.length == 0)
                            break;
                        if (++audioLanguagePosition > mAudioLanguageArray.length - 1)
                            audioLanguagePosition = 0;
                        mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
                        DTVPlayerManager.getInstance().setCurrProgParam(HPlayer_Enum_PlayParam.AUDIO, audioLanguagePosition);
                        break;
                }
            }
        }

        return super.onKeyListener(dialog, keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(mCurrentSelectItem, ITEM_AUDIO_TRACK, mIvAudioTrackLeft, mIvAudioTrackRight, mTvAudioTrack);
        itemChange(mCurrentSelectItem, ITEM_AUDIO_LANGUAGE, mIvAudioLanguageLeft, mIvAudioLanguageRight, mTvAudioLanguage);
    }
}
