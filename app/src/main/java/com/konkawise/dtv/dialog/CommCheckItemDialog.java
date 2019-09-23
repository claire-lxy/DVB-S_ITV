package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.CommCheckItemAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;

public class CommCheckItemDialog extends BaseDialogFragment {
    public static final String TAG = "CommCheckItemDialog";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.lv_content)
    ListView mLvContent;

    @OnItemClick(R.id.lv_content)
    void onItemClick(int position) {
        dismiss();
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this, position, mAdapter.getItem(position));
        }
    }

    @OnItemSelected(R.id.lv_content)
    void onItemSelect(int position) {
        mSelectPosition = position;
    }

    private String mTitle;
    private List<String> mContent;
    private CommCheckItemAdapter mAdapter;
    private int mSelectPosition;
    private OnDismissListener mOnDismissListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_comm_check_item_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        mAdapter = new CommCheckItemAdapter(getContext(), mContent);
        mAdapter.setSelectItem(mSelectPosition);
        mLvContent.setAdapter(mAdapter);
        mLvContent.setSelection(mSelectPosition);
        setListViewHeightBasedOnChildren(mLvContent);
    }

    public CommCheckItemDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public CommCheckItemDialog content(List<String> content) {
        this.mContent = content == null ? new ArrayList<>() : content;
        return this;
    }

    public CommCheckItemDialog position(int selectPosition) {
        this.mSelectPosition = selectPosition;
        return this;
    }

    public void updateContent(List<String> content) {
        String lastCheckContent = mAdapter.getItem(mSelectPosition);
        Log.i(TAG, "lastCheckContent:" + lastCheckContent + "------content:" + content);
        mSelectPosition = content.indexOf(lastCheckContent);
        if (mSelectPosition < 0)
            mSelectPosition = 0;

        mAdapter.updateData(content);
        mAdapter.setSelectItem(mSelectPosition);
        mLvContent.setSelection(mSelectPosition);
        setListViewHeightBasedOnChildren(mLvContent);
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int lvLayoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, dm);
        if (lvLayoutHeight > params.height) {
            params.height = lvLayoutHeight;
        } else {
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 196, dm);
        }
        listView.setLayoutParams(params);
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mAdapter.getCount() > 0 && mSelectPosition <= 0) {
                mLvContent.setSelection(mAdapter.getCount() - 1);
                return true;
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mAdapter.getCount() > 0 && mSelectPosition >= mAdapter.getCount() - 1) {
                mLvContent.setSelection(0);
                return true;
            }
        }
        return super.onKeyListener(dialog, keyCode, event);
    }

    public CommCheckItemDialog setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
        return this;
    }

    public interface OnDismissListener {
        void onDismiss(CommCheckItemDialog dialog, int position, String checkContent);
    }
}
