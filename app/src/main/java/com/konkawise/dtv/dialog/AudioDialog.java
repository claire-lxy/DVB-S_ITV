package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWDJAPVRManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.sw.dvblib.SWFta;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HKKAV_StreamType_t;

public class AudioDialog extends BaseDialogFragment {

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

        audioTrackPosition = SWFtaManager.getInstance().getCurrProgParam(SWFta.OSDFTA_TRACK);
        mTvAudioTrack.setText(mAudioTrackArray[audioTrackPosition]);

        if (where == WHERE_TOPMOST) {
            audioLanguagePosition = SWFtaManager.getInstance().getCurrProgParam(SWFta.OSDFTA_AUDIO);
            ArrayList<String> audioNameList = SWPDBaseManager.getInstance().getCurrProgInfo().audioDB.audioName;
            audioTypeList = SWPDBaseManager.getInstance().getCurrProgInfo().audioDB.ucAudStrType;
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
            audioLanguagePosition = SWDJAPVRManager.getInstance().getCurrAudioIndex();
            ArrayList<String> audioNameList = SWDJAPVRManager.getInstance().getAudioList().audioName;
            audioTypeList = SWDJAPVRManager.getInstance().getAudioList().ucAudStrType;
            audioPidList = SWDJAPVRManager.getInstance().getAudioList().sAudPid;
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
            case HKKAV_StreamType_t.KKAV_PRIVATE_SECTIONS:
            case HKKAV_StreamType_t.KKAV_PRIVATE_PES:
                return getResources().getString(R.string.audio_language_type_private);

            case HKKAV_StreamType_t.KKAV_AUDIO_MPEG1:
                return getResources().getString(R.string.audio_language_type_mpeg1);

            case HKKAV_StreamType_t.KKAV_AUDIO_MPEG2:
                return getResources().getString(R.string.audio_language_type_mpeg2);

            case HKKAV_StreamType_t.KKAV_AUDIO_AAC_ADTS:
            case HKKAV_StreamType_t.KKAV_AUDIO_AAC_LATM:
            case HKKAV_StreamType_t.KKAV_AUDIO_AAC_RAW:
                return getResources().getString(R.string.audio_language_type_aac);

            case HKKAV_StreamType_t.KKAV_AUDIO_AC3:
                return getResources().getString(R.string.audio_language_type_ac3);

            case HKKAV_StreamType_t.KKAV_AUDIO_AC3_PLUS:
                return getResources().getString(R.string.audio_language_type_ac3_plus);

            case HKKAV_StreamType_t.KKAV_AUDIO_DTS:
                return getResources().getString(R.string.audio_language_type_dts);

            case HKKAV_StreamType_t.KKAV_AUDIO_LPCM:
                return getResources().getString(R.string.audio_language_type_lpcm);

            case HKKAV_StreamType_t.KKAV_AUDIO_WM9:
                return getResources().getString(R.string.audio_language_type_wm9);


            case HKKAV_StreamType_t.KKAV_MHEG:
                return getResources().getString(R.string.audio_language_type_mheg);

            case HKKAV_StreamType_t.KKAV_DSM_CC:
                return getResources().getString(R.string.audio_language_type_dsm_cc);

            case HKKAV_StreamType_t.KKAV_TYPE_H2221:
                return getResources().getString(R.string.audio_language_type_h2221);

            case HKKAV_StreamType_t.KKAV_TYPE_A:
                return getResources().getString(R.string.audio_language_type_a);

            case HKKAV_StreamType_t.KKAV_TYPE_B:
                return getResources().getString(R.string.audio_language_type_b);

            case HKKAV_StreamType_t.KKAV_TYPE_C:
                return getResources().getString(R.string.audio_language_type_c);

            case HKKAV_StreamType_t.KKAV_TYPE_D:
                return getResources().getString(R.string.audio_language_type_d);

            case HKKAV_StreamType_t.KKAV_TYPE_AUX:
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
                        SWFtaManager.getInstance().setCurrProgParam(SWFta.OSDFTA_TRACK, audioTrackPosition);
                        break;

                    case ITEM_AUDIO_LANGUAGE:
                        if (mAudioLanguageArray.length == 0)
                            break;
                        if (--audioLanguagePosition < 0)
                            audioLanguagePosition = mAudioLanguageArray.length - 1;
                        mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
                        if (where == WHERE_TOPMOST) {
                            SWFtaManager.getInstance().setCurrProgParam(SWFta.OSDFTA_AUDIO, audioLanguagePosition);
                        } else {
                            SWDJAPVRManager.getInstance().setAudioPid(audioPidList.get(audioLanguagePosition), audioTypeList.get(audioLanguagePosition));
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
                        SWFtaManager.getInstance().setCurrProgParam(SWFta.OSDFTA_TRACK, audioTrackPosition);
                        break;

                    case ITEM_AUDIO_LANGUAGE:
                        if (mAudioLanguageArray.length == 0)
                            break;
                        if (++audioLanguagePosition > mAudioLanguageArray.length - 1)
                            audioLanguagePosition = 0;
                        mTvAudioLanguage.setText(mAudioLanguageArray[audioLanguagePosition]);
                        SWFtaManager.getInstance().setCurrProgParam(SWFta.OSDFTA_AUDIO, audioLanguagePosition);
                        break;
                }
            }
        }

        return super.onKeyListener(dialog, keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(ITEM_AUDIO_TRACK, mIvAudioTrackLeft, mIvAudioTrackRight, mTvAudioTrack);
        itemChange(ITEM_AUDIO_LANGUAGE, mIvAudioLanguageLeft, mIvAudioLanguageRight, mTvAudioLanguage);
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(mCurrentSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
    }
}
