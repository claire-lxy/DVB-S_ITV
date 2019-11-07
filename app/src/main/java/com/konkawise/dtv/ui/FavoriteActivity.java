package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.FavoriteChannelAdapter;
import com.konkawise.dtv.adapter.FavoriteGroupAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.event.ReloadSatEvent;
import com.konkawise.dtv.view.TVListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Group;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

public class FavoriteActivity extends BaseActivity implements LifecycleObserver {
    private static final String TAG = "FavoriteActivity";

    @BindView(R.id.lv_favorite_group)
    TVListView mLvFavoriteGroup;

    @BindView(R.id.lv_favorite_channel)
    TVListView mLvFavoriteChannel;

    @BindView(R.id.pb_loading_favorite)
    ProgressBar mPbLoadingFaovrite;

    @BindView(R.id.ll_bottom_bar_red)
    ViewGroup mBottomBarRed;

    @BindView(R.id.ll_bottom_bar_blue)
    ViewGroup mBottomBarBlue;

    @BindView(R.id.ll_bottom_bar_green)
    ViewGroup mBottomBarRename;

    @BindView(R.id.tv_bottom_bar_green)
    TextView mTvBottomBarRename;

    @BindView(R.id.ll_bottom_bar_ok)
    ViewGroup mBottomBarOk;

    @BindView(R.id.ll_bottom_bar_yellow)
    ViewGroup mBottomBarDelete;

    @BindView(R.id.tv_bottom_bar_yellow)
    TextView mTvBottomBarDelete;

    @OnItemSelected(R.id.lv_favorite_group)
    void selectGroupItem(int position) {
        mFavoriteGroupIndex = position + 1;
        mFavoriteChannelAdapter.clearSelect();
        mFavoriteChannelAdapter.setFocus(mFavoriteChannelFocus);
        loadFavoriteChannels(position);
    }

    @OnFocusChange(R.id.lv_favorite_channel)
    void favoriteChannelFocusChange(boolean isFocus) {
        mFavoriteChannelFocus = isFocus;

        //设置左侧分组焦点失去时背景色
        mFavoriteGroupAdapter.setFocus(isFocus);
        mFavoriteGroupAdapter.setSelectPosition(mLvFavoriteGroup.getCurrentSelect());

        //设置右侧焦点失去时背景色
        mFavoriteChannelAdapter.setFocus(isFocus);
        mFavoriteChannelAdapter.setSelectPosition(mLvFavoriteChannel.getCurrentSelect());

        mBottomBarRename.setVisibility(isFocus ? View.GONE : View.VISIBLE);
        mBottomBarOk.setVisibility(isFocus ? View.VISIBLE : View.GONE);
        mBottomBarDelete.setVisibility(isFocus ? View.VISIBLE : View.GONE);

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

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void notifyTopmostReloadSat() {
        if (isFinishing() && mFavEdit) {
            EventBus.getDefault().post(new ReloadSatEvent());
        }
    }

    private SparseArray<List<HProg_Struct_ProgInfo>> mFavoriteChannelsMap = new SparseArray<>();
    private FavoriteGroupAdapter mFavoriteGroupAdapter;
    private FavoriteChannelAdapter mFavoriteChannelAdapter;

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
        mBottomBarOk.setVisibility(View.GONE);
        mBottomBarRed.setVisibility(View.GONE);
        mBottomBarBlue.setVisibility(View.GONE);
        mBottomBarDelete.setVisibility(View.GONE);
        mTvBottomBarRename.setText(R.string.rename);
        mTvBottomBarDelete.setText(R.string.channel_del);

        DTVProgramManager.getInstance().setCurrGroup(HProg_Enum_Group.TOTAL_GROUP, 1);
        initFavoriteGroup();
        initFavoriteChannel();
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initFavoriteGroup() {
        int[] favIndexArray = DTVProgramManager.getInstance().getFavIndexArray();
        mFavoriteGroupAdapter = new FavoriteGroupAdapter(this, DTVProgramManager.getInstance().getFavoriteGroupNameList(favIndexArray.length));
        mLvFavoriteGroup.setAdapter(mFavoriteGroupAdapter);
        mLvFavoriteGroup.setSelection(0);
    }

    private void initFavoriteChannel() {
        mFavoriteChannelAdapter = new FavoriteChannelAdapter(this, new ArrayList<>());
        mLvFavoriteChannel.setAdapter(mFavoriteChannelAdapter);
    }

    private void loadFavoriteChannels(int favIndex) {
        if (mFavoriteChannelsMap == null || mFavoriteChannelsMap.size() == 0) {
            addObservable(Observable.just(DTVProgramManager.getInstance().getCurrGroupProgInfoList(new int[1]))
                    .subscribeOn(Schedulers.io())
                    .map(channels -> DTVProgramManager.getInstance().getFavChannelMap(channels))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(listSparseArray -> mPbLoadingFaovrite.setVisibility(View.VISIBLE))
                    .subscribe(map -> {
                        mPbLoadingFaovrite.setVisibility(View.GONE);
                        mFavoriteChannelsMap = map;
                        refreshFavoriteAdapter(favIndex);
                    }));
        } else {
            refreshFavoriteAdapter(favIndex);
        }
    }

    private void refreshFavoriteAdapter(int favIndex) {
        List<HProg_Struct_ProgInfo> showProgList = new ArrayList<>();
        for (HProg_Struct_ProgInfo pdpMInfo_t : mFavoriteChannelsMap.get(favIndex)) {
            if (pdpMInfo_t.HideFlag == 0) {
                showProgList.add(pdpMInfo_t);
            }
        }
        mFavoriteChannelAdapter.updateData(showProgList);
        mFavoriteGroupAdapter.setSelectPosition(0);
    }

    private boolean isMulti() {
        return mFavoriteChannelAdapter.getSelectMap().indexOfValue(true) != -1;
    }

    private void showRenameDialog() {
        new RenameDialog()
                .setNameType(getString(R.string.edit_favorite_group_name))
                .setNum(String.valueOf(mFavoriteGroupIndex))
                .setName(mFavoriteGroupAdapter.getItem(mFavoriteGroupIndex - 1))
                .setOnRenameEditListener(newName -> {
                    DTVProgramManager.getInstance().setFavGroupName(mFavoriteGroupIndex - 1, newName);
                    mFavoriteGroupAdapter.updateData(mFavoriteGroupIndex - 1, newName);

                    mFavEdit = true;
                }).show(getSupportFragmentManager(), RenameDialog.TAG);
    }

    private void showDeleteDataDialog() {
        new CommTipsDialog().title(getString(R.string.channel_edit_FAVList_delete_title))
                .content(getString(R.string.channel_edit_FAVList_delete_content))
                .setOnPositiveListener("", () -> {
                    List<HProg_Struct_ProgInfo> ltRemoves = new ArrayList<>();
                    if (isMulti())
                        ltRemoves = mFavoriteChannelAdapter.getSelectData();
                    else
                        ltRemoves.add(mFavoriteChannelAdapter.getData().get(mFavoriteChannelIndex));
                    removeFAVChannels(ltRemoves, mFavoriteGroupIndex - 1);
                    mFavoriteChannelAdapter.clearSelect();
                    List<HProg_Struct_ProgInfo> showProgList = new ArrayList<>();
                    for (HProg_Struct_ProgInfo pdpMInfo_t : mFavoriteChannelsMap.get(mFavoriteGroupIndex - 1)) {
                        if (pdpMInfo_t.HideFlag == 0) {
                            showProgList.add(pdpMInfo_t);
                        }
                    }
                    mFavoriteChannelAdapter.updateData(showProgList);

                    mFavEdit = true;
                })
                .setOnNegativeListener("", () -> {

                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            if (mBottomBarRename.getVisibility() == View.VISIBLE) {
                showRenameDialog();
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            if (mBottomBarDelete.getVisibility() == View.VISIBLE) {
                showDeleteDataDialog();
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

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

    private void removeFAVChannels(List<HProg_Struct_ProgInfo> removeList, int position) {
        List<HProg_Struct_ProgInfo> ltPDPMInfo_t = mFavoriteChannelsMap.get(position);
        ltPDPMInfo_t.removeAll(removeList);
        DTVProgramManager.getInstance().saveFavorite(mFavoriteChannelsMap);
    }
}
