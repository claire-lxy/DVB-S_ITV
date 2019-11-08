package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.PropertiesManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.DTVBookingManager;
import com.konkawise.dtv.adapter.BookListAdapter;
import com.konkawise.dtv.annotation.BookType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.dialog.BookDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.rx.RxBus;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Repeat;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;

public class BookListActivity extends BaseActivity implements LifecycleObserver, RealTimeManager.OnReceiveTimeListener {
    @BindView(R.id.tv_system_time)
    TextView mTvSystemTime;

    @BindView(R.id.lv_book_list)
    TVListView mLvBookList;

    @BindView(R.id.ll_bottom_bar_ok)
    ViewGroup mBottomBarOk;

    @BindView(R.id.ll_bottom_bar_blue)
    ViewGroup mBottomBarBlue;

    @BindView(R.id.tv_bottom_bar_red)
    TextView mTvBottomBarAddBook;

    @BindView(R.id.tv_bottom_bar_green)
    TextView mTvBottomBarEditBook;

    @BindView(R.id.tv_bottom_bar_yellow)
    TextView mTvBottomBarDeleteBook;

    @OnItemSelected(R.id.lv_book_list)
    void onItemSelect(int position) {
        mCurrSelectPosition = position;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void registerBookUpdate() {
        addObservable(RxBus.getInstance().toObservable(BookUpdateEvent.class)
                .subscribe(event -> {
                    if (event.bookInfo != null) {
                        int position = findConflictBookProgPosition(event.bookInfo);
                        if (event.bookInfo.repeatway == HBooking_Enum_Repeat.ONCE && position >= 0) {
                            mAdapter.removeData(position);
                        }
                    }
                }));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void registerRealTimeUpdate() {
        RealTimeManager.getInstance().register(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void unregisterRealTimeUpdate() {
        RealTimeManager.getInstance().unregister(this);
    }

    private boolean mBook;

    private BookListAdapter mAdapter;
    private int mCurrSelectPosition;
    private List<HProg_Struct_ProgBasicInfo> mCurrTypeProgList;
    private List<HProg_Struct_ProgBasicInfo> mAnotherTypeProgList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_book_list;
    }

    @Override
    protected void setup() {
        mBottomBarOk.setVisibility(View.GONE);
        mBottomBarBlue.setVisibility(View.GONE);
        mTvBottomBarAddBook.setText(R.string.add);
        mTvBottomBarEditBook.setText(R.string.edit);
        mTvBottomBarDeleteBook.setText(R.string.delete);

        mAdapter = new BookListAdapter(this, new ArrayList<>());
        mLvBookList.setAdapter(mAdapter);

        loadBookList();
        preloadProgList();
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    @Override
    public void onReceiveTimeCallback(String time) {
        if (!TextUtils.isEmpty(time)) {
            mTvSystemTime.setText(time);
        }
    }

    private void loadBookList() {
        addObservable(Observable.just(DTVBookingManager.getInstance().getBookingModelList())
                .compose(RxTransformer.threadTransformer())
                .subscribe(bookingModels -> {
                    if (bookingModels != null && !bookingModels.isEmpty()) {
                        mAdapter.addData(bookingModels);
                    }
                }));
    }

    private void preloadProgList() {
        loadCurrTypeProgList();
        loadAnotherTypeProgList();
    }

    private void loadCurrTypeProgList() {
        addObservable(Observable.just(DTVProgramManager.getInstance().getCurrGroupProgInfoList())
                .subscribeOn(Schedulers.io())
                .subscribe(progInfoList -> mCurrTypeProgList = progInfoList));
    }

    private void loadAnotherTypeProgList() {
        addObservable(Observable.just(DTVProgramManager.getInstance().getAnotherTypeProgInfoList())
                .subscribeOn(Schedulers.io())
                .subscribe(progInfoList -> mAnotherTypeProgList = progInfoList));
    }

    private void showPowerSavingOffDialog(String bookTitle, int bookingType) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_title_tips))
                .content(getString(R.string.dialog_power_saving_off_cotnent))
                .resizeDialogWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.7))
                .setOnNegativeListener(getString(R.string.cancel), () -> showBookDialog(bookTitle, bookingType))
                .setOnPositiveListener(getString(R.string.dialog_power_saving_positive), () -> {
                    // 设置为浅待机
                    PropertiesManager.getInstance().setProperty(Constants.STANDBY_PROPERTY, Constants.StandbyProperty.SMART_SUSPEND);
                    showBookDialog(bookTitle, bookingType);
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showBookDialog(String title, @BookType final int bookingType) {
        if (bookingType == Constants.BookType.EDIT && mAdapter.getCount() <= 0) return;

        List<HProg_Struct_ProgBasicInfo> progList = getCurrTypeProgList();
        if (progList == null || progList.isEmpty()) return;

        new BookDialog()
                .title(title)
                .bookModel(bookingType == Constants.BookType.ADD ? null : mAdapter.getItem(mCurrSelectPosition))
                .bookType(bookingType)
                .currTypeProgList(progList)
                .anotherTypeProgList(getAnotherTypeProgList(bookingType)) // if book type is edit, it will be null
                .channelNamePosition(getChannelNamePosition(bookingType))
                .setOnBookCallbackListener(pm -> {
                    if (pm.bookingModel == null) return;

                    switch (pm.bookConflict) {
                        case Constants.BookConflictType.NONE: // 当前参数的book没有冲突，正常添加
                            if (bookingType == Constants.BookType.ADD) {
                                DTVBookingManager.getInstance().addTimer(pm.bookingModel.bookInfo);
                                mAdapter.addData(mAdapter.getCount(), pm.bookingModel);
                            } else {
                                DTVBookingManager.getInstance().replaceTimer(mAdapter.getItem(mCurrSelectPosition).bookInfo, pm.bookingModel.bookInfo);
                                mAdapter.updateData(mCurrSelectPosition, pm.bookingModel);
                            }
                            break;
                        case Constants.BookConflictType.LIMIT:
                            ToastUtils.showToast(R.string.toast_book_limit);
                            break;
                        case Constants.BookConflictType.ADD: // 当前参数的book有冲突，如果是添加需要先删除后再添加
                            int conflictPosition = findConflictBookProgPosition(pm.conflictBookProg);
                            if (conflictPosition != -1) {
                                DTVBookingManager.getInstance().addTimer(pm.bookConflict, pm.conflictBookProg, pm.bookingModel.bookInfo);
                                mAdapter.removeData(conflictPosition);
                                mAdapter.addData(mAdapter.getCount(), pm.bookingModel);
                            }
                            break;
                        case Constants.BookConflictType.REPLACE: // 当前参数的book有冲突，需要询问替换
                            BookingModel conflictBookModel = new BookingModel();
                            conflictBookModel.bookInfo = pm.conflictBookProg;
                            conflictBookModel.progInfo = DTVProgramManager.getInstance().getProgInfoByServiceId(pm.conflictBookProg.servid, pm.conflictBookProg.tsid, pm.conflictBookProg.sat);
                            showReplaceBookDialog(conflictBookModel, pm.bookingModel);
                            break;
                    }

                    mBook = true;
                    DTVBookingManager.getInstance().updateDBase(0);
                }).show(getSupportFragmentManager(), BookDialog.TAG);
    }

    private void showReplaceBookDialog(BookingModel conflictBookModel, BookingModel newBookModel) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_book_conflict_title))
                .content(MessageFormat.format(getString(R.string.dialog_book_conflict_content),
                        conflictBookModel.getBookDate(this, BookingModel.BOOK_TIME_SEPARATOR_EMPTY), conflictBookModel.progInfo.Name, conflictBookModel.getBookType(this)))
                .lineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
                .setOnPositiveListener(getString(R.string.dialog_book_conflict_positive), () -> {
                    DTVBookingManager.getInstance().replaceTimer(conflictBookModel.bookInfo, newBookModel.bookInfo);
                    int conflictPosition = findConflictBookProgPosition(conflictBookModel.bookInfo);
                    if (conflictPosition != -1) {
                        mAdapter.updateData(conflictPosition, newBookModel);
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private int findConflictBookProgPosition(@NonNull HBooking_Struct_Timer conflictBookProg) {
        if (mAdapter.getCount() > 0) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                HBooking_Struct_Timer bookInfo = mAdapter.getData().get(i).bookInfo;
                if (bookInfo == null) continue;
                if (bookInfo.servid == conflictBookProg.servid && bookInfo.tsid == conflictBookProg.tsid && bookInfo.sat == conflictBookProg.sat) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getChannelNamePosition(int bookingType) {
        List<HProg_Struct_ProgBasicInfo> progList = getCurrTypeProgList();
        if (progList != null && !progList.isEmpty()) {
            String progName = "";
            if (bookingType == Constants.BookType.ADD) {
                progName = DTVProgramManager.getInstance().getCurrProgInfo().Name;
            } else if (bookingType == Constants.BookType.EDIT) {
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

    private List<HProg_Struct_ProgBasicInfo> getCurrTypeProgList() {
        if (mCurrTypeProgList != null && !mCurrTypeProgList.isEmpty()) {
            return mCurrTypeProgList;
        }
        return DTVProgramManager.getInstance().getCurrGroupProgInfoList();
    }

    private List<HProg_Struct_ProgBasicInfo> getAnotherTypeProgList(int bookingType) {
        switch (bookingType) {
            case Constants.BookType.ADD:
                if (mAnotherTypeProgList != null && !mAnotherTypeProgList.isEmpty()) {
                    return mAnotherTypeProgList;
                }
                return DTVProgramManager.getInstance().getAnotherTypeProgInfoList();
            case Constants.BookType.EDIT:
                return null;
        }
        return null;
    }

    private void showDeleteBookDialog() {
        if (mAdapter.getCount() <= 0) return;

        new CommTipsDialog().title(getString(R.string.delete_book_title))
                .content(getString(R.string.delete_book_content))
                .setOnPositiveListener(getString(R.string.ok), () -> {
                    DTVBookingManager.getInstance().deleteTimer(mAdapter.getItem(mCurrSelectPosition).bookInfo);
                    mAdapter.removeData(mCurrSelectPosition);

                    mBook = true;
                    DTVBookingManager.getInstance().updateDBase(0);
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private boolean isPowerSavingOff() {
        return Constants.StandbyProperty.SMART_SUSPEND.equals(PropertiesManager.getInstance().getProperty(Constants.STANDBY_PROPERTY, ""));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            if (isPowerSavingOff()) {
                showBookDialog(getString(R.string.add), Constants.BookType.ADD);
            } else {
                showPowerSavingOffDialog(getString(R.string.add), Constants.BookType.ADD);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            if (isPowerSavingOff()) {
                showBookDialog(getString(R.string.edit), Constants.BookType.EDIT);
            } else {
                showPowerSavingOffDialog(getString(R.string.edit), Constants.BookType.EDIT);
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
}
