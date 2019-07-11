package com.konkawise.dtv.ui;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.Toast;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.adapter.BookListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookParameterModel;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.dialog.BookDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.view.TVListView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class BookListActivity extends BaseActivity {
    @BindView(R.id.lv_book_list)
    TVListView mLvBookList;

    @OnItemSelected(R.id.lv_book_list)
    void onItemSelect(int position) {
        mCurrSelectPosition = position;
    }

    private BookListAdapter mAdapter;

    private int mCurrSelectPosition;
    private boolean mBook;

    @Override
    public int getLayoutId() {
        return R.layout.activity_book_list;
    }

    @Override
    protected void setup() {
        mAdapter = new BookListAdapter(this, SWBookingManager.getInstance().getBookingModelList());
        mLvBookList.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing() && mBook) {
            SWBookingManager.getInstance().updateDBase(0);
        }
    }

    private void showBookDialog(String title, final int bookingType) {
        List<PDPInfo_t> progList = getProgList(bookingType);
        if (bookingType == Constants.BOOK_TYPE_EDIT && (progList == null || progList.isEmpty()))
            return;

        new BookDialog()
                .title(title)
                .bookModel(bookingType == Constants.BOOK_TYPE_ADD ? null : mAdapter.getItem(mCurrSelectPosition))
                .progList(progList)
                .channelNamePosition(getChannelNamePosition(bookingType))
                .setOnBookCallbackListener(new BookDialog.OnBookCallbackListener() {
                    @Override
                    public void onBookCallback(@NonNull BookParameterModel pm) {
                        if (pm.bookingModel == null) return;

                        switch (pm.bookConflict) {
                            case Constants.BOOK_CONFLICT_NONE: // 当前参数的book没有冲突，正常添加
                                SWBookingManager.getInstance().addProg(pm.bookingModel.bookInfo);
                                if (bookingType == Constants.BOOK_TYPE_ADD) {
                                    mAdapter.addData(mAdapter.getCount(), pm.bookingModel);
                                } else {
                                    mAdapter.updateData(mCurrSelectPosition, pm.bookingModel);
                                }
                                break;
                            case Constants.BOOK_CONFLICT_LIMIT:
                                Toast.makeText(BookListActivity.this, getString(R.string.toast_book_limit), Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.BOOK_CONFLICT_ADD: // 当前参数的book有冲突，如果是添加需要先删除后再添加
                                SWBookingManager.getInstance().addProg(pm.bookConflict, pm.conflictBookProg, pm.bookingModel.bookInfo);
                                int conflictPosition = findConflictBookProgPosition(pm.conflictBookProg);
                                if (conflictPosition != -1) {
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
                    }
                }).show(getSupportFragmentManager(), BookDialog.TAG);
    }

    private void showReplaceBookDialog(BookingModel conflictBookModel, BookingModel newBookModel) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_book_conflict_title))
                .content(MessageFormat.format(getString(R.string.dialog_book_conflict_content),
                        conflictBookModel.getBookDate(this, BookingModel.BOOK_TIME_SEPARATOR_EMPTY), conflictBookModel.progInfo.Name, conflictBookModel.getBookType(this)))
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
        PDPInfo_t conflictProgInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(conflictBookProg.servid, conflictBookProg.tsid, conflictBookProg.sat);
        if (conflictProgInfo != null && mAdapter.getCount() > 0) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                if (mAdapter.getData().get(i).progInfo.Name.equals(conflictProgInfo.Name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getChannelNamePosition(int bookingType) {
        switch (bookingType) {
            case Constants.BOOK_TYPE_ADD:
                return 0;
            case Constants.BOOK_TYPE_EDIT:
                BookingModel bookingModel = mAdapter.getItem(mCurrSelectPosition);
                if (bookingModel != null) {
                    List<PDPInfo_t> progList = getProgList(bookingType);
                    if (progList != null && !progList.isEmpty()) {
                        for (int i = 0; i < progList.size(); i++) {
                            if (progList.get(i).Name.equals(bookingModel.progInfo.Name)) {
                                return i;
                            }
                        }
                    }
                }
                break;
        }
        return 0;
    }

    private List<PDPInfo_t> getProgList(int bookingType) {
        switch (bookingType) {
            case Constants.BOOK_TYPE_ADD:
                return SWPDBaseManager.getInstance().getCurrGroupProgInfoList();
            case Constants.BOOK_TYPE_EDIT:
                List<BookingModel> bookingModels = mAdapter.getData();
                List<PDPInfo_t> progList = new ArrayList<>();
                if (bookingModels != null && !bookingModels.isEmpty()) {
                    for (BookingModel bookingModel : bookingModels) {
                        progList.add(bookingModel.progInfo);
                    }
                }
                return progList;
        }
        return null;
    }

    private void showDeleteBookDialog() {
        new CommTipsDialog().title(getString(R.string.delete_book_title))
                .content(getString(R.string.delete_book_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWBookingManager.getInstance().deleteProg(mAdapter.getItem(mCurrSelectPosition).bookInfo);
                        mAdapter.notifyDataSetChanged();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            showBookDialog(getString(R.string.add), Constants.BOOK_TYPE_ADD);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            showBookDialog(getString(R.string.edit), Constants.BOOK_TYPE_EDIT);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            showDeleteBookDialog();
            return true;
        }

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

        return super.onKeyDown(keyCode, event);
    }
}
