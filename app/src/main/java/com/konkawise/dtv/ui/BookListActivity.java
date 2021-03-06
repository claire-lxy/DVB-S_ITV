package com.konkawise.dtv.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PropertiesManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.adapter.BookListAdapter;
import com.konkawise.dtv.annotation.BookType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookParameterModel;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.dialog.BookDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakAsyncTask;
import com.sw.dvblib.SWBooking;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class BookListActivity extends BaseActivity implements RealTimeManager.OnReceiveTimeListener {
    @BindView(R.id.tv_system_time)
    TextView mTvSystemTime;

    @BindView(R.id.lv_book_list)
    TVListView mLvBookList;

    @OnItemSelected(R.id.lv_book_list)
    void onItemSelect(int position) {
        mCurrSelectPosition = position;
    }

    private boolean mBook;

    private BookListAdapter mAdapter;
    private int mCurrSelectPosition;
    private LoadBookingTask mLoadBookingTask;
    private List<PDPInfo_t> mCurrTypeProgList;
    private List<PDPInfo_t> mAnotherTypeProgList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_book_list;
    }

    @Override
    protected void setup() {
        EventBus.getDefault().register(this);

        mAdapter = new BookListAdapter(this, new ArrayList<>());
        mLvBookList.setAdapter(mAdapter);

        mLoadBookingTask = new LoadBookingTask(this);
        mLoadBookingTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RealTimeManager.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RealTimeManager.getInstance().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            if (mLoadBookingTask != null) {
                mLoadBookingTask.release();
                mLoadBookingTask = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onReceiveTimeCallback(String time) {
        if (!TextUtils.isEmpty(time)) {
            mTvSystemTime.setText(time);
        }
    }

    private static class LoadBookingTask extends WeakAsyncTask<BookListActivity, Void, List<BookingModel>> {

        LoadBookingTask(BookListActivity view) {
            super(view);
        }

        @Override
        protected List<BookingModel> backgroundExecute(Void... param) {
            BookListActivity context = mWeakReference.get();

            context.mCurrTypeProgList = SWPDBaseManager.getInstance().getCurrGroupProgInfoList();
            context.mAnotherTypeProgList = SWPDBaseManager.getInstance().getAnotherTypeProgInfoList();

            return SWBookingManager.getInstance().getBookingModelList();
        }

        @Override
        protected void postExecute(List<BookingModel> bookingModels) {
            if (bookingModels != null && !bookingModels.isEmpty()) {
                mWeakReference.get().mAdapter.addData(bookingModels);
            }
        }
    }

    private void showPowerSavingOffDialog(String bookTitle, int bookingType) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_title_tips))
                .content(getString(R.string.dialog_power_saving_off_cotnent))
                .resizeDialogWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.7))
                .setOnNegativeListener(getString(R.string.cancel), new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        showBookDialog(bookTitle, bookingType);
                    }
                })
                .setOnPositiveListener(getString(R.string.dialog_power_saving_positive), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        // 设置为浅待机
                        PropertiesManager.getInstance().setProperty(Constants.STANDBY_PROPERTY, Constants.STANDBY_SMART_SUSPEND);
                        showBookDialog(bookTitle, bookingType);
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showBookDialog(String title, @BookType final int bookingType) {
        if (bookingType == Constants.BOOK_TYPE_EDIT && mAdapter.getCount() <= 0) return;

        List<PDPInfo_t> progList = getCurrTypeProgList();
        if (progList == null || progList.isEmpty()) return;

        new BookDialog()
                .title(title)
                .bookModel(bookingType == Constants.BOOK_TYPE_ADD ? null : mAdapter.getItem(mCurrSelectPosition))
                .bookType(bookingType)
                .currTypeProgList(progList)
                .anotherTypeProgList(getAnotherTypeProgList(bookingType)) // if book type is edit, it will be null
                .channelNamePosition(getChannelNamePosition(bookingType))
                .setOnBookCallbackListener(new BookDialog.OnBookCallbackListener() {
                    @Override
                    public void onBookCallback(@NonNull BookParameterModel pm) {
                        if (pm.bookingModel == null) return;

                        switch (pm.bookConflict) {
                            case Constants.BOOK_CONFLICT_NONE: // 当前参数的book没有冲突，正常添加
                                if (bookingType == Constants.BOOK_TYPE_ADD) {
                                    SWBookingManager.getInstance().addProg(pm.bookingModel.bookInfo);
                                    mAdapter.addData(mAdapter.getCount(), pm.bookingModel);
                                } else {
                                    SWBookingManager.getInstance().replaceProg(mAdapter.getItem(mCurrSelectPosition).bookInfo, pm.bookingModel.bookInfo);
                                    mAdapter.updateData(mCurrSelectPosition, pm.bookingModel);
                                }
                                break;
                            case Constants.BOOK_CONFLICT_LIMIT:
                                ToastUtils.showToast(R.string.toast_book_limit);
                                break;
                            case Constants.BOOK_CONFLICT_ADD: // 当前参数的book有冲突，如果是添加需要先删除后再添加
                                int conflictPosition = findConflictBookProgPosition(pm.conflictBookProg);
                                if (conflictPosition != -1) {
                                    SWBookingManager.getInstance().addProg(pm.bookConflict, pm.conflictBookProg, pm.bookingModel.bookInfo);
                                    mAdapter.removeData(conflictPosition);
                                    mAdapter.addData(mAdapter.getCount(), pm.bookingModel);
                                }
                                break;
                            case Constants.BOOK_CONFLICT_REPLACE: // 当前参数的book有冲突，需要询问替换
                                BookingModel conflictBookModel = new BookingModel();
                                conflictBookModel.bookInfo = pm.conflictBookProg;
                                conflictBookModel.progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(pm.conflictBookProg.servid, pm.conflictBookProg.tsid, pm.conflictBookProg.sat);
                                showReplaceBookDialog(conflictBookModel, pm.bookingModel);
                                break;
                        }

                        mBook = true;
                        SWBookingManager.getInstance().updateDBase(0);
                    }
                }).show(getSupportFragmentManager(), BookDialog.TAG);
    }

    private void showReplaceBookDialog(BookingModel conflictBookModel, BookingModel newBookModel) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_book_conflict_title))
                .content(MessageFormat.format(getString(R.string.dialog_book_conflict_content),
                        conflictBookModel.getBookDate(this, BookingModel.BOOK_TIME_SEPARATOR_EMPTY), conflictBookModel.progInfo.Name, conflictBookModel.getBookType(this)))
                .lineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
                .setOnPositiveListener(getString(R.string.dialog_book_conflict_positive), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWBookingManager.getInstance().replaceProg(conflictBookModel.bookInfo, newBookModel.bookInfo);
                        int conflictPosition = findConflictBookProgPosition(conflictBookModel.bookInfo);
                        if (conflictPosition != -1) {
                            mAdapter.updateData(conflictPosition, newBookModel);
                        }
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private int findConflictBookProgPosition(@NonNull HSubforProg_t conflictBookProg) {
        if (mAdapter.getCount() > 0) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                HSubforProg_t bookInfo = mAdapter.getData().get(i).bookInfo;
                if (bookInfo == null) continue;
                if (bookInfo.servid == conflictBookProg.servid && bookInfo.tsid == conflictBookProg.tsid && bookInfo.sat == conflictBookProg.sat) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getChannelNamePosition(int bookingType) {
        List<PDPInfo_t> progList = getCurrTypeProgList();
        if (progList != null && !progList.isEmpty()) {
            String progName = "";
            if (bookingType == Constants.BOOK_TYPE_ADD) {
                progName = SWPDBaseManager.getInstance().getCurrProgInfo().Name;
            } else if (bookingType == Constants.BOOK_TYPE_EDIT) {
                BookingModel bookingModel = mAdapter.getItem(mCurrSelectPosition);
                if (bookingModel != null) {
                    progName = bookingModel.progInfo.Name;
                }
            }

            if (!TextUtils.isEmpty(progName)) {
                for (int i = 0; i < progList.size(); i++) {
                    if (progList.get(i).Name.equals(progName)) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    private List<PDPInfo_t> getCurrTypeProgList() {
        if (mCurrTypeProgList != null && !mCurrTypeProgList.isEmpty()) {
            return mCurrTypeProgList;
        }
        return SWPDBaseManager.getInstance().getCurrGroupProgInfoList();
    }

    private List<PDPInfo_t> getAnotherTypeProgList(int bookingType) {
        switch (bookingType) {
            case Constants.BOOK_TYPE_ADD:
                if (mAnotherTypeProgList != null && !mAnotherTypeProgList.isEmpty()) {
                    return mAnotherTypeProgList;
                }
                return SWPDBaseManager.getInstance().getAnotherTypeProgInfoList();
            case Constants.BOOK_TYPE_EDIT:
                return null;
        }
        return null;
    }

    private void showDeleteBookDialog() {
        if (mAdapter.getCount() <= 0) return;

        new CommTipsDialog().title(getString(R.string.delete_book_title))
                .content(getString(R.string.delete_book_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWBookingManager.getInstance().deleteProg(mAdapter.getItem(mCurrSelectPosition).bookInfo);
                        mAdapter.removeData(mCurrSelectPosition);

                        mBook = true;
                        SWBookingManager.getInstance().updateDBase(0);
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private boolean isPowerSavingOff() {
        return Constants.STANDBY_SMART_SUSPEND.equals(PropertiesManager.getInstance().getProperty(Constants.STANDBY_PROPERTY, ""));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            if (isPowerSavingOff()) {
                showBookDialog(getString(R.string.add), Constants.BOOK_TYPE_ADD);
            } else {
                showPowerSavingOffDialog(getString(R.string.add), Constants.BOOK_TYPE_ADD);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            if (isPowerSavingOff()) {
                showBookDialog(getString(R.string.edit), Constants.BOOK_TYPE_EDIT);
            } else {
                showPowerSavingOffDialog(getString(R.string.edit), Constants.BOOK_TYPE_EDIT);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            showDeleteBookDialog();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mAdapter.getCount() > 0 && mCurrSelectPosition >= mAdapter.getCount() - 1) {
                mLvBookList.setSelection(0);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mAdapter.getCount() > 0 && mCurrSelectPosition <= 0) {
                mLvBookList.setSelection(mAdapter.getCount() - 1);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra(Constants.IntentKey.INTENT_BOOK_UPDATE, mBook);
            setResult(Constants.RequestCode.REQUEST_CODE_EPG_BOOK, intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookUpdate(BookUpdateEvent event) {
        if (event.bookInfo != null) {
            int position = findConflictBookProgPosition(event.bookInfo);
            if (event.bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal() && position > 0) {
                mAdapter.removeData(position);
            }
        }
    }
}
