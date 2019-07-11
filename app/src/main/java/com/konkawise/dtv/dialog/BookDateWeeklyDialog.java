package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.CheckGroupAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.util.Arrays;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class BookDateWeeklyDialog extends BaseDialogFragment {
    public static final String TAG = "BookDateWeeklyDialog";

    @BindView(R.id.tv_check_group_title)
    TextView mTvTitle;

    @BindView(R.id.lv_check_group)
    ListView mListView;

    @OnClick(R.id.tv_sure)
    void saveWeekly() {
        dismiss();
        if (mOnCheckGroupCallback != null) {
            mOnCheckGroupCallback.callback(mAdapter.getCheckMap());
        }
    }

    @OnClick(R.id.tv_canncle)
    void cancels() {
        dismiss();
    }

    @OnItemClick(R.id.lv_check_group)
    void onItemClick(int position) {
        mAdapter.setCheck(position);
    }

    @BindArray(R.array.book_date_weekly)
    String[] mBookDateWeeklyArray;

    private CheckGroupAdapter mAdapter;
    private OnCheckGroupCallback mOnCheckGroupCallback;

    private String mTitle;
    private String mCurrCheckDay;

    public BookDateWeeklyDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public BookDateWeeklyDialog check(String currCheckDay) {
        this.mCurrCheckDay = currCheckDay;
        return this;
    }

    public BookDateWeeklyDialog setOnCheckGroupCallback(OnCheckGroupCallback callback) {
        this.mOnCheckGroupCallback = callback;
        return this;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_check_group_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        mAdapter = new CheckGroupAdapter(getContext(), Arrays.asList(mBookDateWeeklyArray));
        mListView.setAdapter(mAdapter);

        if (TextUtils.equals(mCurrCheckDay, getStrings(R.string.book_date_week_everyday))) {
            mAdapter.checkAll();
        } else {
            String[] weekDays = mCurrCheckDay.split(" ");
            if (weekDays.length > 0) {
                for (String weekDay : weekDays) {
                    for (int j = 0; j < mBookDateWeeklyArray.length; j++) {
                        if (TextUtils.equals(weekDay, mBookDateWeeklyArray[j])) {
                            mAdapter.setCheck(j);
                        }
                    }
                }
            }
        }
    }
}
