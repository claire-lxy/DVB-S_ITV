package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.ChannelEditAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.FavoriteDialog;
import com.konkawise.dtv.dialog.OnCheckGroupCallback;
import com.konkawise.dtv.dialog.PIDDialog;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.rx.RxTransformer;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Group;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgEditInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class ChannelEditActivity extends BaseActivity implements LifecycleObserver {
    private static final int EDIT_TYPE_LOCK = 1 << 1;
    private static final int EDIT_TYPE_HIDE = 1 << 2;

    private static final int EDIT_SAVE_FAVORITE = 1 << 3;
    private static final int EDIT_SAVE_CHANNEL = 1 << 4;
    private static final int EDIT_SAVE_PID = 1 << 5;
    private static final int EDIT_SAVE_SORT = 1 << 6;

    @IntDef(flag = true, value = {
            EDIT_TYPE_HIDE, EDIT_TYPE_LOCK})
    private @interface EditType {
    }

    @IntDef(flag = true, value = {
            EDIT_SAVE_FAVORITE, EDIT_SAVE_CHANNEL,
            EDIT_SAVE_PID, EDIT_SAVE_SORT
    })
    private @interface EditSaveType {
    }

    @BindView(R.id.tv_satellite_name)
    TextView mTvSatelliteName;

    @BindView(R.id.lv_channel_list)
    ListView mLvChannelList;

    @BindView(R.id.pb_loading_channel)
    ProgressBar mPbLoadingChannel;

    @BindView(R.id.ll_channel_fav_move)
    LinearLayout mLlChannelFavMove;

    @BindView(R.id.tv_channel_fav_move)
    TextView mTvChannelFavMove;

    @BindView(R.id.tv_channel_lock_rename)
    TextView mTvChannelLockRename;

    @BindView(R.id.tv_channel_more_sort)
    TextView mTvChannelMoreSort;

    @BindView(R.id.tv_channel_skip_delete)
    TextView mTvChannelSkipDelete;

    @BindView(R.id.ll_channel_pid)
    LinearLayout mLlChannelPid;

    @BindArray(R.array.sort_content)
    String[] mSortArray;

    @OnItemSelected(R.id.lv_channel_list)
    void onItemSelect(int position) {
        mCurrSelectPosition = position;
    }

    @OnItemClick(R.id.lv_channel_list)
    void onItemClick(int position) {
        mAdapter.setSelect(position);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void notifyTopmostUpdateProgramWhenChannelEdit() {
        if (isFinishing() && mDataSaved) {
            EventBus.getDefault().post(new ProgramUpdateEvent(true));
        }
    }

    // 存储原始或保存后的喜爱分组
    private SparseArray<List<HProg_Struct_ProgInfo>> mFavChannelsMap = new SparseArray<>();
    // 存储编辑操作的喜爱分组
    private SparseArray<List<HProg_Struct_ProgInfo>> mEditFavChannelsMap;
    // 存储编辑操作的pid
    private SparseArray<int[]> mEditPidMap = new SparseArray<>(); // key:progNo
    // 存储编辑操作的sortType
    private int mEditSortType = DTVProgramManager.getInstance().getSortType();
    // 存储编辑操作的rename
    private SparseArray<String> mEditRenameMap = new SparseArray<>(); // key:progNo

    private List<HProg_Struct_SatInfo> mSatList;
    private int mCurrSatPosition;

    private List<HProg_Struct_ProgInfo> ltTotalProgList = new ArrayList<>();
    private boolean loadFlag = true;
    private boolean analyFavFlag = true;

    private ChannelEditAdapter mAdapter;
    private int mCurrSelectPosition;

    private boolean mSaveFavorite;
    private boolean mSaveEditChannel;
    private boolean mSavePid;
    private boolean mSaveSortType;
    private boolean mSaveData;
    private boolean mShowMore;
    private boolean mFinish;
    private boolean mDataSaved;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_channel_edit;
    }

    @Override
    protected void setup() {
        DTVProgramManager.getInstance().setCurrGroup(HProg_Enum_Group.TOTAL_GROUP, 1);
        initChannelList();
        mTvSatelliteName.setText(R.string.all);
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initChannelList() {
        mAdapter = new ChannelEditAdapter(this, new ArrayList<>());
        mLvChannelList.setAdapter(mAdapter);

        int scrollToProgIndex = 0;
        HProg_Struct_ProgInfo currProgInfo = DTVProgramManager.getInstance().getCurrProgInfo();
        if (currProgInfo != null) scrollToProgIndex = currProgInfo.ProgIndex;
        loadChannelList(scrollToProgIndex);
    }

    private int scrollToPosition(int targetProgIndex) {
        if (mAdapter.getCount() <= 0) return 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (targetProgIndex == mAdapter.getItem(i).ProgIndex) {
                return i;
            }
        }
        return 0;
    }

    private void updateChannelList() {
        int scrollToProgIndex = 0;
        if (mAdapter.getCount() > 0) {
            if (mCurrSelectPosition > mAdapter.getData().size() - 1) mCurrSelectPosition = 0;
            scrollToProgIndex = mAdapter.getItem(mCurrSelectPosition).ProgIndex;
        }
        loadChannelList(scrollToProgIndex);
    }

    private void loadChannelList(int scrollToProgIndex) {
        addObservable(Observable.just(getChannelList())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(channelList -> mPbLoadingChannel.setVisibility(View.VISIBLE))
                .observeOn(Schedulers.io())
                .doOnNext(channelList -> {
                    if (analyFavFlag) {
                        mFavChannelsMap = DTVProgramManager.getInstance().getFavChannelMap(ltTotalProgList);
                        mEditFavChannelsMap = mFavChannelsMap.clone();
                    }
                })
                .compose(RxTransformer.threadTransformer())
                .subscribe(channelList -> {
                    mPbLoadingChannel.setVisibility(View.GONE);

                    if (channelList != null && !channelList.isEmpty()) {
                        mAdapter.updateData(channelList);
                        mLvChannelList.setSelection(scrollToPosition(scrollToProgIndex));
                    } else {
                        mAdapter.updateData(new ArrayList<>());
                    }
                }));
    }

    private List<HProg_Struct_SatInfo> getSateList() {
        if (mSatList == null) {
            mSatList = new ArrayList<>();
            List<HProg_Struct_SatInfo> allSatList = DTVProgramManager.getInstance().getAllSatList(this);
            if (allSatList != null && !allSatList.isEmpty()) {
                mSatList.addAll(allSatList);
            }
        }
        return mSatList;
    }

    private void showSaveDataDialog() {
        new CommTipsDialog().title(getString(R.string.channel_edit_save_title))
                .content(getString(R.string.channel_edit_save_content))
                .setOnPositiveListener("", () -> {
                    if (mSaveFavorite) saveFavorite();
                    if (mSaveEditChannel) saveEditChannel();
                    if (mSavePid) savePid();
                    if (mSaveSortType) saveSortType();

                    loadFlag = false;

                    // 对数据编辑过一次，退出界面时通知topmost刷新频道列表
                    if (!mDataSaved && (mSaveFavorite || mSaveEditChannel || mSavePid || mSaveSortType)) {
                        mDataSaved = true;
                    }
                    if (mFinish) {
                        finish();
                    } else {
                        resetEdit();
                    }
                })
                .setOnNegativeListener("", () -> {
                    loadFlag = true;

                    resetSortType();
                    resetEdit();

                    if (mFinish) finish();
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void resetEdit() {
        mSaveFavorite = false;
        mSaveEditChannel = false;
        mSavePid = false;
        mSaveSortType = false;
        mSaveData = false;
        mEditFavChannelsMap = mFavChannelsMap.clone();

        mEditPidMap.clear();
        mEditRenameMap.clear();

        mAdapter.clearSelect();
        mAdapter.clearDelete();

        mTvSatelliteName.setText(getSateList().get(mCurrSatPosition).sat_name);
        updateMore();
        updateChannelList();
    }

    private void resetSortType() {
        DTVProgramManager.getInstance().setSortType(mEditSortType);
    }

    /**
     * 保存频道编辑，喜爱分组
     */
    private void saveFavorite() {
        for (int i = 0; i < mEditFavChannelsMap.size(); i++) {
            List<HProg_Struct_ProgInfo> editFavoriteList = mEditFavChannelsMap.get(i);
            if (editFavoriteList == null) continue;

            mFavChannelsMap.put(i, editFavoriteList);

            DTVProgramManager.getInstance().editFavProgList(i, getFavoriteProgIndexs(mFavChannelsMap.get(i)), 1);
        }
    }

    /**
     * 保存频道编辑，lock、skip、rename、delete
     */
    private void saveEditChannel() {
        ArrayList<HProg_Struct_ProgEditInfo> editList = getEditList();
        if (editList != null && !editList.isEmpty()) {
            DTVProgramManager.getInstance().editGroupProgList(editList);
        }
    }

    /**
     * 保存频道编辑，pid
     */
    private void savePid() {
        if (mAdapter.getCount() > 0) {
            List<HProg_Struct_ProgInfo> channelList = mAdapter.getData();
            if (channelList != null && !channelList.isEmpty() && mEditPidMap != null && mEditPidMap.size() > 0) {
                for (int i = 0; i < channelList.size(); i++) {
                    int[] pids = mEditPidMap.get(channelList.get(i).ProgNo);
                    if (pids != null && pids.length == 3) {
                        DTVProgramManager.getInstance().setServicePID(i, pids[0], pids[1], pids[2]);
                    }
                }
            }
        }
    }

    /**
     * 保存频道编辑，sortType
     */
    private void saveSortType() {
        mEditSortType = DTVProgramManager.getInstance().getSortType();
    }

    private void showFavDialog() {
        if (mAdapter.getCount() <= 0) return;

        new FavoriteDialog()
                .setData(mAdapter.getItem(mCurrSelectPosition))
                .title(getString(R.string.dialog_favorite_title))
                .multi(isMulti())
                .setOnCheckGroupCallback(new OnCheckGroupCallback() {
                    @Override
                    public void callback(SparseBooleanArray checkMap) {
                        if (isMulti()) {
                            for (int i = 0; i < mAdapter.getData().size(); i++) {
                                if (mAdapter.getSelectMap().get(i)) {
                                    editFavorite(i, checkMap);
                                }
                            }
                        } else {
                            editFavorite(mCurrSelectPosition, checkMap);
                        }

                        mAdapter.clearSelect();

                        recordSaveData(EDIT_SAVE_FAVORITE);
                    }

                    @Override
                    public void cancel() {
                        mAdapter.clearSelect();
                    }
                }).show(getSupportFragmentManager(), FavoriteDialog.TAG);
    }

    /**
     * 频道编辑，Fav
     */
    private void editFavorite(int position, SparseBooleanArray checkMap) {
        StringBuilder sb = new StringBuilder(mEditFavChannelsMap.size());
        for (int i = mEditFavChannelsMap.size() - 1; i >= 0; i--) {
            sb.append(checkMap.get(i) ? "1" : "0"); // 拼接喜爱分组二进制
        }
        int binaryFavFlag = Integer.valueOf(sb.toString(), 2);
        int hexFavFlag = Integer.valueOf(Integer.toHexString(binaryFavFlag), 16);
        HProg_Struct_ProgInfo favChannelInfo = mAdapter.getItem(position);
        favChannelInfo.FavFlag = hexFavFlag;

        for (int favIndex = mEditFavChannelsMap.size() - 1; favIndex >= 0; favIndex--) {
            List<HProg_Struct_ProgInfo> favChannelList = mEditFavChannelsMap.get(favIndex);
            if (favChannelList == null) {
                favChannelList = new ArrayList<>();
            }

            int favChannelInFavGroupPosition = isChannelInFavGroup(favChannelList, favChannelInfo);
            if (favChannelInFavGroupPosition == -1) {
                if (checkMap.get(favIndex)) {
                    favChannelList.add(favChannelInfo);
                }
            } else {
                if (checkMap.get(favIndex)) {
                    favChannelList.set(favChannelInFavGroupPosition, favChannelInfo);
                } else {
                    favChannelList.remove(favChannelInFavGroupPosition);
                }
            }

            mEditFavChannelsMap.put(favIndex, favChannelList);
        }

        // 同步修改Channel显示喜爱图标
        mAdapter.updateData(position, favChannelInfo);
    }

    /**
     * 喜爱分组是否包含该频道
     *
     * @return 返回在该喜爱分组中的索引值position，不存在返回-1
     */
    private int isChannelInFavGroup(List<HProg_Struct_ProgInfo> favoriteChannelList, HProg_Struct_ProgInfo favChannelInfo) {
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            if (favoriteChannelList.get(i).ProgIndex == favChannelInfo.ProgIndex) return i;
        }
        return -1;
    }

    private int[] getFavoriteProgIndexs(List<HProg_Struct_ProgInfo> favoriteChannelList) {
        int[] favoriteProgIndexs = new int[favoriteChannelList.size()];
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            favoriteProgIndexs[i] = favoriteChannelList.get(i).ProgIndex;
        }
        return favoriteProgIndexs;
    }

    /**
     * 频道编辑，move
     */
    private void moveChannels() {
        HProg_Struct_ProgInfo item = mAdapter.getItem(mCurrSelectPosition); // 移动前选中的频道信息

        List<HProg_Struct_ProgInfo> moveChannels = mAdapter.moveChannels();
        if (moveChannels != null && !moveChannels.isEmpty()) {
            mAdapter.addData(getChannelMoveAfterPosition(item), moveChannels);
            mAdapter.clearSelect();
            mAdapter.clearDelete();

            recordSaveData(EDIT_SAVE_CHANNEL);
        }
    }

    /**
     * 获取频道移动后勾选的频道插入列表位置
     */
    private int getChannelMoveAfterPosition(HProg_Struct_ProgInfo moveBeforeChannelInfo) {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (moveBeforeChannelInfo.ProgIndex == mAdapter.getItem(i).ProgIndex) {
                return i;
            }
        }
        return 0;
    }

    private void toggleMoreTag() {
        mShowMore = !mShowMore;
        updateMore();
    }

    private void updateMore() {
        if (mShowMore) {
            mLlChannelFavMove.setVisibility(mCurrSatPosition == 0 ? View.VISIBLE : View.GONE);
        } else {
            mLlChannelFavMove.setVisibility(View.VISIBLE);
        }
        mTvChannelFavMove.setText(mShowMore ? R.string.channel_move : R.string.channel_fav);
        mTvChannelLockRename.setText(mShowMore ? R.string.rename : R.string.lock);
        mTvChannelMoreSort.setText(mShowMore ? R.string.channel_sort : R.string.channel_more);
        mTvChannelSkipDelete.setText(mShowMore ? R.string.delete : R.string.skip);
        mLlChannelPid.setVisibility(mShowMore ? View.VISIBLE : View.GONE);
    }

    /**
     * 频道编辑，lock、skip
     */
    private void editChannel(@EditType int editType) {
        List<HProg_Struct_ProgInfo> channelList = mAdapter.getData();
        if (channelList == null || channelList.isEmpty()) return;

        if (isMulti()) {
            for (int i = 0; i < channelList.size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    editLockOrSkip(channelList, editType, i);
                }
            }
        } else {
            editLockOrSkip(channelList, editType, mCurrSelectPosition);
        }

        mAdapter.clearSelect();
    }

    private void editLockOrSkip(List<HProg_Struct_ProgInfo> channelList, @EditType int editType, int position) {
        recordSaveData(EDIT_SAVE_CHANNEL);

        HProg_Struct_ProgInfo newChannelInfo = channelList.get(position);
        switch (editType) {
            case EDIT_TYPE_HIDE:
                newChannelInfo.HideFlag = newChannelInfo.HideFlag == 1 ? 0 : 1;
                break;
            case EDIT_TYPE_LOCK:
                newChannelInfo.LockFlag = newChannelInfo.LockFlag == 1 ? 0 : 1;
                break;
        }
        mAdapter.updateData(position, newChannelInfo);
    }

    /**
     * 频道编辑，rename
     */
    public void renameChannel(String newName) {
        recordSaveData(EDIT_SAVE_CHANNEL);

        HProg_Struct_ProgInfo channelInfo = mAdapter.getItem(mCurrSelectPosition);
        channelInfo.Name = newName;
        mAdapter.updateData(mCurrSelectPosition, channelInfo);
        mAdapter.notifyDataSetChanged();

        mEditRenameMap.put(channelInfo.ProgNo, newName);
    }

    /**
     * 频道编辑，delete
     */
    private void deleteChannels() {
        recordSaveData(EDIT_SAVE_CHANNEL);

        List<HProg_Struct_ProgInfo> channelList = mAdapter.getData();
        if (channelList != null && !channelList.isEmpty()) {
            for (int i = 0; i < channelList.size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    mAdapter.setDelete(i);
                }
            }
        }
        if (!isMulti()) {
            mAdapter.setDelete(mCurrSelectPosition);
        }
        mAdapter.clearSelect();
    }

    private ArrayList<HProg_Struct_ProgEditInfo> getEditList() {
        List<HProg_Struct_ProgInfo> channelList = mAdapter.getData();
        if (channelList == null || channelList.isEmpty()) return null;

        ArrayList<HProg_Struct_ProgEditInfo> editList = new ArrayList<>();
        List<HProg_Struct_ProgInfo> ltRmProgList = new ArrayList<>();
        for (int i = 0; i < channelList.size(); i++) {
            HProg_Struct_ProgEditInfo editInfo = new HProg_Struct_ProgEditInfo();
            editInfo.used = 1;
            editInfo.progindex = channelList.get(i).ProgIndex;
            editInfo.delFlag = mAdapter.getDeleteMap().get(i) ? 1 : 0;
            if (mAdapter.getDeleteMap().get(i)) {
                editInfo.delFlag = 1;
                ltRmProgList.add(channelList.get(i));
            } else {
                editInfo.delFlag = 0;
            }
            editInfo.hideFlag = channelList.get(i).HideFlag;
            editInfo.lockFlag = channelList.get(i).LockFlag;

            String channelName = mEditRenameMap.get(channelList.get(i).ProgNo);
            boolean isRename = !TextUtils.isEmpty(channelName);
            editInfo.nameFlag = isRename ? 1 : 0;
            editInfo.newprogname = isRename ? channelName : "";
            editList.add(editInfo);
        }
        ltTotalProgList.removeAll(ltRmProgList);
        channelList.removeAll(ltRmProgList);
        return editList;
    }

    private List<HProg_Struct_ProgInfo> getChannelList() {
        if (loadFlag) {
            ltTotalProgList = DTVProgramManager.getInstance().getCurrGroupProgInfoList(new int[1]);
        }
        return DTVProgramManager.getInstance().getTotalGroupSatProgList(ltTotalProgList, getSateList().get(mCurrSatPosition).SatIndex);
    }


    private boolean isMulti() {
        return mAdapter.getSelectMap().indexOfValue(true) != -1;
    }

    private void recordSaveData(@EditSaveType int saveType) {
        switch (saveType) {
            case EDIT_SAVE_FAVORITE:
                mSaveFavorite = true;
                break;
            case EDIT_SAVE_CHANNEL:
                mSaveEditChannel = true;
                break;
            case EDIT_SAVE_PID:
                mSavePid = true;
                break;
            case EDIT_SAVE_SORT:
                mSaveSortType = true;
                break;
        }
        mSaveData = true;
    }

    private void showRenameDialog() {
        if (mAdapter.getCount() <= 0 || mCurrSelectPosition >= mAdapter.getCount()) return;
        new RenameDialog()
                .setProgNo(mAdapter.getItem(mCurrSelectPosition).PShowNo)
                .setName(mAdapter.getItem(mCurrSelectPosition).Name)
                .setOnRenameEditListener(newName -> {
                    if (TextUtils.isEmpty(newName)) return;

                    renameChannel(newName);
                }).show(getSupportFragmentManager(), RenameDialog.TAG);
    }

    private void showSortDialog() {
        if (mAdapter.getCount() <= 0) return;

        int currSortType = DTVProgramManager.getInstance().getSortType();
        new CommCheckItemDialog()
                .title(getString(R.string.channel_sort))
                .content(Arrays.asList(mSortArray))
                .position(currSortType - 1)
                .setOnDismissListener((dialog, position, checkContent) -> {
                    if (currSortType != (position + 1)) {
                        DTVProgramManager.getInstance().setSortType(position + 1);
                        loadFlag = true;
                        analyFavFlag = true;
                        updateChannelList();
                        recordSaveData(EDIT_SAVE_SORT);
                    }

                    mAdapter.clearSelect();
                    mAdapter.clearDelete();
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void showPidDialog() {
        if (mAdapter.getCount() <= 0 || mCurrSelectPosition >= mAdapter.getCount()) return;

        int[] currPids;
        int progNo = mAdapter.getItem(mCurrSelectPosition).ProgNo;
        if (mEditPidMap.get(progNo) != null && mEditPidMap.get(progNo).length == 3) {
            currPids = mEditPidMap.get(progNo);
        } else {
            currPids = DTVProgramManager.getInstance().getServicePID(progNo);
        }
        if (currPids == null) return;

        new PIDDialog()
                .setPids(currPids)
                .setOnEditPidListener(pids -> {
                    if (pids != null && pids.length == 3) {
                        if (currPids.length == 3) {
                            if (isPidEdit(currPids, pids)) {
                                mEditPidMap.put(progNo, new int[]{pids[0], pids[1], pids[2]});
                            }
                        } else {
                            mEditPidMap.put(progNo, new int[]{pids[0], pids[1], pids[2]});
                        }
                        recordSaveData(EDIT_SAVE_PID);
                    }
                }).show(getSupportFragmentManager(), PIDDialog.TAG);
    }

    private boolean isPidEdit(int[] currPids, int[] editPids) {
        if (currPids != null && currPids.length == 3 && editPids != null && editPids.length == 3) {
            for (int i = 0; i < 3; i++) {
                if (currPids[i] != editPids[i]) return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (--mCurrSatPosition < 0) mCurrSatPosition = getSateList().size() - 1;
            if (mSaveData) {
                analyFavFlag = true;
                showSaveDataDialog();
            } else {
                analyFavFlag = false;
                loadFlag = false;
                resetSortType();
                resetEdit();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (++mCurrSatPosition >= getSateList().size()) mCurrSatPosition = 0;
            if (mSaveData) {
                analyFavFlag = true;
                showSaveDataDialog();
            } else {
                analyFavFlag = false;
                loadFlag = false;
                resetSortType();
                resetEdit();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            if (mShowMore && mCurrSatPosition == 0) {
                moveChannels();
            } else {
                showFavDialog();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            if (mShowMore) {
                showRenameDialog();
            } else {
                editChannel(EDIT_TYPE_LOCK);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            if (mShowMore) {
                showSortDialog();
            } else {
                toggleMoreTag();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mShowMore) {
                showPidDialog();
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mShowMore) {
                toggleMoreTag();
                return true;
            } else {
                if (mSaveData) {
                    mFinish = true;
                    showSaveDataDialog();
                    return true;
                }
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mCurrSelectPosition >= mAdapter.getCount() - 1) {
                mLvChannelList.setSelection(0);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mCurrSelectPosition <= 0) {
                mLvChannelList.setSelection(mAdapter.getCount() - 1);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            if (mShowMore) {
                deleteChannels();
            } else {
                editChannel(EDIT_TYPE_HIDE);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
