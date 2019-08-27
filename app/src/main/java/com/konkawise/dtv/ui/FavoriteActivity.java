package com.konkawise.dtv.ui;

import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.FavoriteChannelAdapter;
import com.konkawise.dtv.adapter.FavoriteGroupAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.event.ReloadSatEvent;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.sw.dvblib.SWPDBase;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

/**
 * 创建者      lj DELL
 * 创建时间    2018/12/9 15:24
 * 描述       喜好列表编辑界面
 *
 * <p>
 * 更新者      $Author$
 * <p>
 * 更新时间    $Date$
 * 更新描述    ${TODO}
 */
public class FavoriteActivity extends BaseActivity {
    private static final String TAG = "FavoriteActivity";

    @BindView(R.id.lv_favorite_group)
    TVListView mLvFavoriteGroup;

    @BindView(R.id.lv_favorite_channel)
    TVListView mLvFavoriteChannel;

    @BindView(R.id.pb_loading_favorite)
    ProgressBar mPbLoadingFaovrite;

    @BindView(R.id.ll_fav_rename)
    LinearLayout mBottomFavRename;

    @BindView(R.id.ll_fav_edit_channel)
    LinearLayout mBottomFavEditChannel;

    @OnItemSelected(R.id.lv_favorite_group)
    void selectGroupItem(int position) {
        mFavoriteGroupIndex = position + 1;
        mFavoriteChannelAdapter.clearSelect();
        mFavoriteChannelAdapter.setFocus(mFavoriteChannelFocus);
        loadFavoriteChannels(position);
    }

    @OnFocusChange(R.id.lv_favorite_channel)
    void favoriteChannelFocusChange(boolean isFocus) {
        Log.i(TAG, "channel focus change = " + isFocus);
        mFavoriteChannelFocus = isFocus;

        //设置左侧分组焦点失去时背景色
        mFavoriteGroupAdapter.setFocus(isFocus);
        mFavoriteGroupAdapter.setSelectPosition(mLvFavoriteGroup.getCurrentSelect());

        //设置右侧焦点失去时背景色
        mFavoriteChannelAdapter.setFocus(isFocus);
        mFavoriteChannelAdapter.setSelectPosition(mLvFavoriteChannel.getCurrentSelect());

        mBottomFavRename.setVisibility(isFocus ? View.GONE : View.VISIBLE);
        mBottomFavEditChannel.setVisibility(isFocus ? View.VISIBLE : View.GONE);

        mFavoriteChannelAdapter.notifyDataSetChanged();
        mFavoriteGroupAdapter.notifyDataSetChanged();
    }

    @OnItemSelected(R.id.lv_favorite_channel)
    void selectChannelItem(int position) {
        mFavoriteChannelIndex = position;
        mFavoriteChannelAdapter.setFocus(mFavoriteChannelFocus);
        mFavoriteChannelAdapter.setSelectPosition(position);
        mFavoriteChannelAdapter.notifyDataSetChanged();
    }

    @OnItemClick(R.id.lv_favorite_channel)
    void onItemClick(int position) {
        mFavoriteChannelAdapter.setSelect(position);
    }

    private SparseArray<List<PDPMInfo_t>> mFavoriteChannelsMap = new SparseArray<>();
    private FavoriteGroupAdapter mFavoriteGroupAdapter;
    private FavoriteChannelAdapter mFavoriteChannelAdapter;
    private LoadFavoriteRunnable mLoadFavoriteRunnable;

    // 注意索引是+1的，获取item信息要-1处理
    private int mFavoriteGroupIndex;

    private int mFavoriteChannelIndex;

    private boolean mFavoriteChannelFocus = false;
    private boolean mFavEdit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_favorite_channel;
    }

    @Override
    protected void setup() {
        SWPDBaseManager.getInstance().setCurrGroup(SWPDBase.SW_WHOLE_GROUP, 0);
        initFavoriteGroup();
        initFavoriteChannel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && mFavEdit) {
            EventBus.getDefault().post(new ReloadSatEvent());
        }
    }

    private void initFavoriteGroup() {
        int[] favIndexArray = SWPDBaseManager.getInstance().getFavIndexArray();
        mFavoriteGroupAdapter = new FavoriteGroupAdapter(this, SWPDBaseManager.getInstance().getFavoriteGroupNameList(favIndexArray.length));
        mLvFavoriteGroup.setAdapter(mFavoriteGroupAdapter);
        mLvFavoriteGroup.setSelection(0);
    }

    private void initFavoriteChannel() {
        mFavoriteChannelAdapter = new FavoriteChannelAdapter(this, new ArrayList<>());
        mLvFavoriteChannel.setAdapter(mFavoriteChannelAdapter);

        loadFavoriteChannels(0);
    }

    private void loadFavoriteChannels(int favIndex) {
        if (mLoadFavoriteRunnable == null) {
            mLoadFavoriteRunnable = new LoadFavoriteRunnable(this);
        }
        mPbLoadingFaovrite.setVisibility(View.VISIBLE);
        ThreadPoolManager.getInstance().remove(mLoadFavoriteRunnable);
        mLoadFavoriteRunnable.favIndex = favIndex;
        ThreadPoolManager.getInstance().execute(mLoadFavoriteRunnable);
    }

    private static class LoadFavoriteRunnable extends WeakRunnable<FavoriteActivity> {
        int favIndex;

        LoadFavoriteRunnable(FavoriteActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            FavoriteActivity context = mWeakReference.get();

            context.mFavoriteChannelsMap = SWPDBaseManager.getInstance().getFavChannelMap();
            List<PDPMInfo_t> favoriteChannels = context.mFavoriteChannelsMap.get(favIndex);
            if (favoriteChannels == null) {
                favoriteChannels = SWPDBaseManager.getInstance().getFavListByIndex(favIndex);
                context.mFavoriteChannelsMap.put(favIndex, favoriteChannels);
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.mPbLoadingFaovrite.setVisibility(View.GONE);
                    context.mFavoriteChannelAdapter.updateData(context.mFavoriteChannelsMap.get(favIndex));
                    context.mFavoriteGroupAdapter.setSelectPosition(0);
                }
            });
        }
    }

    private boolean isMulti() {
        return mFavoriteChannelAdapter.getSelectMap().indexOfValue(true) != -1;
    }

    private void showDeleteDataDialog() {
        new CommTipsDialog().title(getString(R.string.channel_edit_FAVList_delete_title))
                .content(getString(R.string.channel_edit_FAVList_delete_content))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        List<PDPMInfo_t> ltRemoves = new ArrayList<>();
                        if (isMulti())
                            ltRemoves = mFavoriteChannelAdapter.getSelectData();
                        else
                            ltRemoves.add(mFavoriteChannelAdapter.getData().get(mFavoriteChannelIndex));
                        removeFAVChannels(ltRemoves, mFavoriteGroupIndex - 1);
                        mFavoriteChannelAdapter.clearSelect();
                        mFavoriteChannelAdapter.updateData(mFavoriteChannelsMap.get(mFavoriteGroupIndex - 1));

                        mFavEdit = true;
                    }
                })
                .setOnNegativeListener("", new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {

                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SWPDBaseManager.getInstance().setFavChannelMap(mFavoriteChannelsMap);
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            if (mBottomFavRename.getVisibility() == View.VISIBLE) {
                new RenameDialog()
                        .setNameType(getString(R.string.edit_favorite_group_name))
                        .setNum(String.valueOf(mFavoriteGroupIndex))
                        .setName(mFavoriteGroupAdapter.getItem(mFavoriteGroupIndex - 1))
                        .setOnRenameEditListener(new RenameDialog.onRenameEditListener() {
                            @Override
                            public void onRenameEdit(String newName) {
                                PreferenceManager.getInstance().putString("fav" + (mFavoriteGroupIndex - 1), newName);
                                mFavoriteGroupAdapter.updateData(mFavoriteGroupIndex - 1, newName);

                                mFavEdit = true;
                            }
                        }).show(getSupportFragmentManager(), RenameDialog.TAG);

            }
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            if (mBottomFavEditChannel.getVisibility() == View.VISIBLE) {
                showDeleteDataDialog();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mFavoriteChannelFocus) {
                mLvFavoriteChannel.requestFocus();
                if (mFavoriteChannelAdapter.getCount() > 0 && mFavoriteChannelIndex >= mFavoriteChannelAdapter.getCount() - 1) {
                    mLvFavoriteChannel.setSelection(0);
                    return true;
                }
            } else {
                if (mFavoriteGroupAdapter.getCount() > 0 && --mFavoriteGroupIndex >= mFavoriteGroupAdapter.getCount() - 1) {
                    mLvFavoriteGroup.setSelection(0);
                    return true;
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mFavoriteChannelFocus) {
                if (mFavoriteChannelAdapter.getCount() > 0 && mFavoriteChannelIndex <= 0) {
                    mLvFavoriteChannel.setSelection(mFavoriteChannelAdapter.getCount() - 1);
                    return true;
                }
            } else {
                if (mFavoriteGroupAdapter.getCount() > 0 && --mFavoriteGroupIndex <= 0) {
                    mLvFavoriteGroup.setSelection(mFavoriteGroupAdapter.getCount() - 1);
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private int[] getFavList(List<PDPMInfo_t> favoriteChannelList) {
        int[] favList = new int[favoriteChannelList.size()];
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            favList[i] = favoriteChannelList.get(i).ProgIndex;
        }
        return favList;
    }

    private void removeFAVChannels(List<PDPMInfo_t> removeList, int position) {
        List<PDPMInfo_t> ltPDPMInfo_t = mFavoriteChannelsMap.get(position);
        ltPDPMInfo_t.removeAll(removeList);
        SWPDBaseManager.getInstance().saveFavorite(mFavoriteChannelsMap);
    }
}
