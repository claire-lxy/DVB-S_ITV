package com.konkawise.dtv.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseRecyclerAdapter;
import com.konkawise.dtv.adapter.base.BaseRecyclerHolder;
import com.konkawise.dtv.bean.BlindTpModel;
import com.konkawise.dtv.utils.Utils;
import java.util.List;

public class BlindTpAdapter extends BaseRecyclerAdapter<BlindTpModel> {

    public BlindTpAdapter(Context context, List<BlindTpModel> datas) {
        super(context, datas, R.layout.recycleview_pro_num_item);
    }

    @NonNull
    @Override
    public BaseRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == BlindTpModel.VIEW_TYPE_TP) {
            return new BaseRecyclerHolder(mInflater.inflate(R.layout.recycleview_tp_item, parent, false));
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    protected void convert(BaseRecyclerHolder holder, int position, BlindTpModel item) {
        if (getItemViewType(position) == BlindTpModel.VIEW_TYPE_TP) {
            holder.setText(R.id.tv_tp_adapter_num, String.valueOf(position + 1))
                    .setText(R.id.tv_tp_left_text, item.pssParam_t.Freq + mContext.getResources().getString(R.string.mhz))
                    .setText(R.id.tv_tp_right_text, item.pssParam_t.Rate + mContext.getResources().getString(R.string.ks))
                    .setText(R.id.tv_tp_v_h, Utils.getVorH(mContext, item.pssParam_t.Qam));
        } else {
            holder.setText(R.id.tv_pro_adapter_num, String.valueOf(position + 1))
                    .setText(R.id.tv_pro_left_text, item.pdpInfo_t.Name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).type == BlindTpModel.VIEW_TYPE_TP) {
            return BlindTpModel.VIEW_TYPE_TP;
        } else {
            return BlindTpModel.VIEW_TYPE_PRO;
        }
    }

    @Override
    public void addData(int position, BlindTpModel item) {
        int count = getItemCount();
        mDatas.add(position > count ? count : position, item);
        notifyDataSetChanged();
    }

    @Override
    public void addData(List<BlindTpModel> datas) {
        if (datas == null) return;
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }
}
