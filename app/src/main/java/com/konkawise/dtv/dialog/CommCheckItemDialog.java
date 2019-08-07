package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.util.DisplayMetrics;
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

public class CommCheckItemDialog extends BaseDialogFragment {
    public static final String TAG = "CommCheckItemDialog";

    @BindView(R.id.tv_title)
    TextView mTv_title;

    @BindView(R.id.lv_content)
    ListView mLv_content;

    @OnItemClick(R.id.lv_content)
    void onItemClick(int position) {
        dismiss();
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this, position, mAdapter.getItem(position));
        }
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
        mTv_title.setText(mTitle);

        mAdapter = new CommCheckItemAdapter(getContext(), mContent);
        mAdapter.setSelectItem(mSelectPosition);
        mLv_content.setAdapter(mAdapter);
        mLv_content.setSelection(mSelectPosition);
        setListViewHeightBasedOnChildren(mLv_content);
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
        mAdapter.updateData(content);
        mSelectPosition = content.indexOf(lastCheckContent);
        mLv_content.setSelection(mSelectPosition);
        setListViewHeightBasedOnChildren(mLv_content);
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

    public CommCheckItemDialog setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
        return this;
    }

    public interface OnDismissListener {
        void onDismiss(CommCheckItemDialog dialog, int position, String checkContent);
    }
}
