package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.SubtitleAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;

public class SubtitleDialog extends BaseDialogFragment {

    public static final String TAG = "SubtitleDialog";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.lv_content)
    ListView mLvContent;

    @OnItemClick(R.id.lv_content)
    void onItemClick(int position) {
        dismiss();
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this, position, null);
        }
    }

    private String mTitle;
    private List mContent;
    private SubtitleAdapter mAdapter;
    private int mSelectPosition;
    private OnDismissListener mOnDismissListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_comm_check_item_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        mAdapter = new SubtitleAdapter(getContext(), mContent);
        mAdapter.setSelectItem(mSelectPosition);
        mLvContent.setAdapter(mAdapter);
        mLvContent.setSelection(mSelectPosition);
        setListViewHeightBasedOnChildren(mLvContent);
    }

	@Override
	public void onStart() {
		super.onStart();
		Log.i("SubtitleDialog", "onStart");
		Window window = getDialog().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		lp.x = 20;
		lp.y = 20;
		window.setAttributes(lp);
		window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.25), ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("SubtitleDialog", "onResume");
	}

	public SubtitleDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public SubtitleDialog content(List content) {
        this.mContent = content == null ? new ArrayList<>() : content;
		Log.i(TAG, "mContent size = " + mContent.size());
		return this;
    }

    public SubtitleDialog position(int selectPosition) {
        this.mSelectPosition = selectPosition;
        return this;
    }

    public void updateContent(List content) {
        Object lastCheckContent = mAdapter.getItem(mSelectPosition);
        mAdapter.updateData(content);
        mSelectPosition = content.indexOf(lastCheckContent);
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
        int lvLayoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
        if (lvLayoutHeight > params.height) {
            params.height = lvLayoutHeight;
        }
        listView.setLayoutParams(params);
    }

    public SubtitleDialog setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
        return this;
    }

    public interface OnDismissListener {
        void onDismiss(SubtitleDialog dialog, int position, String checkContent);
    }
}
