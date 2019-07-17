package com.konkawise.dtv.ui;

import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.ChannelEditAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.FavoriteDialog;
import com.konkawise.dtv.dialog.OnCheckGroupCallback;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PIDDialog;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.sw.dvblib.SWPDBase;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.PDPEdit_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class ChannelEditActivity extends BaseActivity {
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

    // 存储原始或保存后的喜爱分组
    private SparseArray<List<PDPMInfo_t>> mFavChannelsMap = new SparseArray<>();
    // 存储编辑操作的喜爱分组
    private SparseArray<List<PDPMInfo_t>> mEditFavChannelsMap;
    // 存储编辑操作的pid
    private SparseArray<int[]> mEditPidMap = new SparseArray<>();
    // 存储编辑操作的sortType
    private int mEditSortType = SWPDBaseManager.getInstance().getSortType();

    private List<SatInfo_t> mSatList;
    private int mCurrSatPosition;

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

    private LoadFavoriteChannelsRunnable mLoadFavoriteChannelsRunnable;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_channel_edit;
    }

    @Override
    protected void setup() {
        initFavoriteChannels();
        initChannelList();
        mTvSatelliteName.setText(R.string.all);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            SWPDBaseManager.getInstance().setCurrProgType(SWPDBase.SW_WHOLE_GROUP, 0);
            if (mDataSaved) {
                EventBus.getDefault().post(new ProgramUpdateEvent(true));
            }
        }
    }

    @Override
    protected void onDestroy() {
        ThreadPoolManager.getInstance().remove(mLoadFavoriteChannelsRunnable);
        super.onDestroy();
    }

    private void initFavoriteChannels() {
        mLoadFavoriteChannelsRunnable = new LoadFavoriteChannelsRunnable(this);
        ThreadPoolManager.getInstance().execute(mLoadFavoriteChannelsRunnable);
    }

    private static class LoadFavoriteChannelsRunnable extends WeakRunnable<ChannelEditActivity> {

        LoadFavoriteChannelsRunnable(ChannelEditActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            SWPDBaseManager.getInstance().setCurrProgType(SWPDBase.SW_TOTAL_GROUP, 0);
            int[] favIndexArray = SWPDBaseManager.getInstance().getFavIndexArray();
            for (int i = 0; i < favIndexArray.length; i++) {
                mWeakReference.get().mFavChannelsMap.put(i, SWPDBaseManager.getInstance().getCurrGroupProgListByCond(2, favIndexArray[i]));
            }

            mWeakReference.get().mEditFavChannelsMap = mWeakReference.get().mFavChannelsMap.clone();
        }
    }

    private void initChannelList() {
        mAdapter = new ChannelEditAdapter(this, getChannelList());
        mLvChannelList.setAdapter(mAdapter);
        mLvChannelList.setSelection(getScrollToPosition());
    }

    private int getScrollToPosition() {
        List<PDPMInfo_t> progList = SWPDBaseManager.getInstance().getTotalGroupProgList();
        if (progList == null || progList.isEmpty()) {
            return 0;
        }

        PDPMInfo_t currProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        for (int i = 0; i < progList.size(); i++) {
            if (currProgInfo.ProgIndex == progList.get(i).ProgIndex) {
                return i;
            }
        }
        return 0;
    }

    private void updateChannelList() {
        List<PDPMInfo_t> channelList = getChannelList();
        if (channelList != null && !channelList.isEmpty()) {
            mAdapter.updateData(channelList);
        }
    }

    private List<SatInfo_t> getSateList() {
        if (mSatList == null) {
            mSatList = new ArrayList<>();
            List<SatInfo_t> allSatList = SWPDBaseManager.getInstance().getAllSatList(this);
            if (allSatList != null && !allSatList.isEmpty()) {
                mSatList.addAll(allSatList);
            }
        }
        return mSatList;
    }

    private void showSaveDataDialog() {
        new CommTipsDialog().title(getString(R.string.channel_edit_save_title))
                .content(getString(R.string.channel_edit_save_content))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        if (mSaveFavorite) saveFavorite();
                        if (mSaveEditChannel) saveEditChannel();
                        if (mSavePid) savePid();
                        if (mSaveSortType) saveSortType();

                        // 对数据编辑过一次，退出界面时通知topmost刷新频道列表
                        if (!mDataSaved && (mSaveFavorite || mSaveEditChannel || mSavePid || mSaveSortType)) {
                            mDataSaved = true;
                        }
                        resetEdit();

                        if (mFinish) finish();
                    }
                })
                .setOnNegativeListener("", new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        resetSortType();
                        resetEdit();

                        if (mFinish) finish();
                    }
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

        mAdapter.clearSelect();
        mAdapter.clearDelete();

        mTvSatelliteName.setText(getSateList().get(mCurrSatPosition).sat_name);
        updateMore();
        updateChannelList();
    }

    private void resetSortType() {
        SWPDBaseManager.getInstance().setSortType(mEditSortType);
    }

    /**
     * 保存频道编辑，喜爱分组
     */
    private void saveFavorite() {
        for (int i = 0; i < mEditFavChannelsMap.size(); i++) {
            List<PDPMInfo_t> editFavoriteList = mEditFavChannelsMap.get(i);
            if (editFavoriteList == null) continue;

            mFavChannelsMap.put(i, editFavoriteList);

            SWPDBaseManager.getInstance().editFavProgList(i,
                    getFavoriteProgIndexs(mFavChannelsMap.get(i)), mFavChannelsMap.get(i).size(), 1);
        }
    }

    /**
     * 保存频道编辑，lock、skip、rename、delete
     */
    private void saveEditChannel() {
        ArrayList<PDPEdit_t> editList = getEditList();
        if (editList != null && !editList.isEmpty()) {
            SWPDBaseManager.getInstance().editGroupProgList(editList);
        }
    }

    /**
     * 保存频道编辑，pid
     */
    private void savePid() {
        List<PDPMInfo_t> channelList = getChannelList();
        if (channelList != null && !channelList.isEmpty() && mEditPidMap != null && mEditPidMap.size() > 0) {
            for (int i = 0; i < channelList.size(); i++) {
                int[] pids = mEditPidMap.get(i);
                if (pids != null && pids.length == 3) {
                    SWPDBaseManager.getInstance().setServicePID(i, pids[0], pids[1], pids[2]);
                }
            }
        }
    }

    /**
     * 保存频道编辑，sortType
     */
    private void saveSortType() {
        mEditSortType = SWPDBaseManager.getInstance().getSortType();
    }

    private void showFavDialog() {
        new FavoriteDialog()
                .setData(mEditFavChannelsMap, mAdapter.getItem(mCurrSelectPosition))
                .title(getString(R.string.dialog_favorite_title))
                .multi(isMulti())
                .setOnCheckGroupCallback(new OnCheckGroupCallback() {
                    @Override
                    public void callback(SparseBooleanArray checkMap) {
                        for (int favIndex = 0; favIndex < mEditFavChannelsMap.size(); favIndex++) {
                            if (mEditFavChannelsMap.get(favIndex) == null) continue;

                            if (checkMap.get(favIndex)) {
                                addFav(favIndex);
                            } else {
                                removeFav(favIndex);
                            }
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

    private void addFav(int favIndex) {
        List<PDPMInfo_t> favChannelList = mEditFavChannelsMap.get(favIndex);
        if (mAdapter.getData() == null || mAdapter.getData().isEmpty()) return;

        if (isMulti()) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    addFav(favIndex, i, favChannelList);
                }
            }
        } else {
            addFav(favIndex, mCurrSelectPosition, favChannelList);
        }
    }

    private void removeFav(int favIndex) {
        List<PDPMInfo_t> favChannelList = mEditFavChannelsMap.get(favIndex);
        if (mAdapter.getData() == null || mAdapter.getData().isEmpty()) return;

        if (isMulti()) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    removeFav(favIndex, i, favChannelList);
                }
            }
        } else {
            removeFav(favIndex, mCurrSelectPosition, favChannelList);
        }
    }

    private void addFav(int favIndex, int position, List<PDPMInfo_t> favChannelList) {
        PDPMInfo_t favChannelInfo = mAdapter.getItem(position);
        if (isChannelInFavGroup(favChannelList, favChannelInfo) == -1) {
            favChannelInfo.FavFlag = 1;
            favChannelList.add(favChannelInfo);
            mEditFavChannelsMap.put(favIndex, favChannelList);
            // 同步修改Channel显示喜爱图标
            mAdapter.updateData(position, favChannelInfo);
        }
    }

    private void removeFav(int favIndex, int position, List<PDPMInfo_t> favChannelList) {
        PDPMInfo_t favChannelInfo = mAdapter.getItem(position);
        int removeIndex = isChannelInFavGroup(favChannelList, favChannelInfo);
        if (removeIndex != -1) {
            favChannelList.remove(removeIndex);
            mEditFavChannelsMap.put(favIndex, favChannelList);

            // 频道也不在其他喜爱分组中，同步修改Channel隐藏喜爱图标
            if (!isChannelInOtherFavGroup(favIndex, favChannelInfo)) {
                favChannelInfo.FavFlag = 0;
                mAdapter.updateData(position, favChannelInfo);
            }
        }
    }

    /**
     * @param exceptFavIndex 要排除查找的喜爱分组索引
     */
    private boolean isChannelInOtherFavGroup(int exceptFavIndex, PDPMInfo_t favChannelInfo) {
        for (int i = 0; i < mEditFavChannelsMap.size(); i++) {
            if (i == exceptFavIndex) continue;
            if (isChannelInFavGroup(mEditFavChannelsMap.get(i), favChannelInfo) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 喜爱分组是否包含该频道
     *
     * @return 返回在该喜爱分组中的索引值position，不存在返回-1
     */
    private int isChannelInFavGroup(List<PDPMInfo_t> favoriteChannelList, PDPMInfo_t favChannelInfo) {
        for (int i = 0; i < favoriteChannelList.size(); i++) {
            if (favoriteChannelList.get(i).ProgIndex == favChannelInfo.ProgIndex) return i;
        }
        return -1;
    }

    private int[] getFavoriteProgIndexs(List<PDPMInfo_t> favoriteChannelList) {
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
        PDPMInfo_t item = mAdapter.getItem(mCurrSelectPosition); // 移动前选中的频道信息

        List<PDPMInfo_t> moveChannels = mAdapter.moveChannels();
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
    private int getChannelMoveAfterPosition(PDPMInfo_t moveBeforeChannelInfo) {
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
     * 频道编辑 lock、skip
     */
    private void editChannel(@EditType int editType) {
        List<PDPMInfo_t> channelList = mAdapter.getData();
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

    private void editLockOrSkip(List<PDPMInfo_t> channelList, @EditType int editType, int position) {
        recordSaveData(EDIT_SAVE_CHANNEL);

        PDPMInfo_t newChannelInfo = channelList.get(position);
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

        mAdapter.getItem(mCurrSelectPosition).Name = newName;
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 频道编辑，delete
     */
    private void deleteChannels() {
        recordSaveData(EDIT_SAVE_CHANNEL);

        List<PDPMInfo_t> channelList = mAdapter.getData();
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

    private ArrayList<PDPEdit_t> getEditList() {
        List<PDPMInfo_t> originChannelList = getChannelList();
        List<PDPMInfo_t> channelList = mAdapter.getData();
        if (originChannelList == null || originChannelList.isEmpty()
                || channelList == null || channelList.isEmpty()) return null;

        ArrayList<PDPEdit_t> editList = new ArrayList<>();
        for (int i = 0; i < channelList.size(); i++) {
            PDPEdit_t editInfo = new PDPEdit_t();
            editInfo.used = 1;
            editInfo.progindex = channelList.get(i).ProgIndex;
            editInfo.delFlag = mAdapter.getDeleteMap().get(i) ? 1 : 0;
            editInfo.hideFlag = channelList.get(i).HideFlag;
            editInfo.lockFlag = channelList.get(i).LockFlag;

            boolean isRename = !TextUtils.equals(originChannelList.get(i).Name, channelList.get(i).Name);
            editInfo.nameFlag = isRename ? 1 : 0;
            editInfo.newprogname = isRename ? channelList.get(i).Name : "";
            editList.add(editInfo);
        }
        return editList;
    }

    private List<PDPMInfo_t> getChannelList() {
        if (mCurrSatPosition == 0) {
            return SWPDBaseManager.getInstance().getTotalGroupProgList();
        } else {
            return SWPDBaseManager.getInstance().getCurrGroupProgListByCond(1, getSateList().get(mCurrSatPosition).SatIndex);
        }
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
                .setOldName(mAdapter.getItem(mCurrSelectPosition).Name)
                .setEditLisener(new RenameDialog.EditTextLisener() {
                    @Override
                    public void setEdit(String newName) {
                        if (TextUtils.isEmpty(newName)) return;

                        renameChannel(newName);
                    }
                }).show(getSupportFragmentManager(), RenameDialog.TAG);
    }

    private void showSortDialog() {
        if (mAdapter.getCount() <= 0) return;

        int currSortType = SWPDBaseManager.getInstance().getSortType();
        new CommCheckItemDialog()
                .title(getString(R.string.channel_sort))
                .content(Arrays.asList(mSortArray))
                .position(currSortType - 1)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        if (currSortType != (position + 1)) {
                            SWPDBaseManager.getInstance().setSortType(position + 1);
                            updateChannelList();
                            recordSaveData(EDIT_SAVE_SORT);
                        }

                        mAdapter.clearSelect();
                        mAdapter.clearDelete();
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void showPidDialog() {
        if (mAdapter.getCount() <= 0 || mCurrSelectPosition >= mAdapter.getCount()) return;

        int[] currPids;
        if (mEditPidMap.get(mAdapter.getItem(mCurrSelectPosition).ProgNo) != null && mEditPidMap.get(mAdapter.getItem(mCurrSelectPosition).ProgNo).length == 3) {
            currPids = mEditPidMap.get(mCurrSelectPosition);
        } else {
            currPids = SWPDBaseManager.getInstance().getServicePID(mAdapter.getItem(mCurrSelectPosition).ProgNo);
        }
        if (currPids == null) return;

        new PIDDialog()
                .setPids(currPids)
                .setOnEditPidListener(new PIDDialog.OnEditPidListener() {
                    @Override
                    public void onPidEdit(int[] pids) {
                        if (pids != null && pids.length == 3) {
                            if (currPids.length == 3) {
                                if (isPidEdit(currPids, pids)) {
                                    mEditPidMap.put(mCurrSelectPosition, new int[]{pids[0], pids[1], pids[2]});
                                }
                            } else {
                                mEditPidMap.put(mCurrSelectPosition, new int[]{pids[0], pids[1], pids[2]});
                            }
                            recordSaveData(EDIT_SAVE_PID);
                        }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (--mCurrSatPosition < 0) mCurrSatPosition = getSateList().size() - 1;
            if (mSaveData) {
                showSaveDataDialog();
            } else {
                resetSortType();
                resetEdit();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (++mCurrSatPosition >= getSateList().size()) mCurrSatPosition = 0;
            if (mSaveData) {
                showSaveDataDialog();
            } else {
                resetSortType();
                resetEdit();
            }
            return true;
        }

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

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            if (mShowMore) {
                deleteChannels();
            } else {
                editChannel(EDIT_TYPE_HIDE);
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

        return super.onKeyDown(keyCode, event);
    }
}
