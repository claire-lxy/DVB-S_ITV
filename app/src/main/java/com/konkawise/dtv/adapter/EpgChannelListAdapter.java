package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;

import com.konkawise.dtv.DTVCommonManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.DateModel;
import com.sw.dvblib.DTVCommon;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Task;
import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;

public class EpgChannelListAdapter extends BaseListViewAdapter<HEPG_Struct_Event> {

    public EpgChannelListAdapter(Context context, List<HEPG_Struct_Event> datas) {
        super(context, datas, R.layout.epg_channel_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, HEPG_Struct_Event item) {
        DTVCommon.TimeModel startTime = DTVCommonManager.getInstance().getStartTime(item);
        DTVCommon.TimeModel endTime = DTVCommonManager.getInstance().getEndTime(item);

        holder.setText(R.id.epg_title, item.memEventName)
                .setText(R.id.epg_time, new DateModel(startTime, endTime).getFormatHourAndMinute())
                .setVisibility(R.id.epg_book_type, item.schtype == HBooking_Enum_Task.PLAY
                        || item.schtype == HBooking_Enum_Task.RECORD ? View.VISIBLE : View.INVISIBLE)
                .setImageResource(R.id.epg_book_type, item.schtype == HBooking_Enum_Task.PLAY ? R.mipmap.ic_book_play : R.mipmap.ic_book_record);
        if (DTVCommonManager.getInstance().isProgramPlaying(item)) {
            holder.setText(R.id.epg_status, mContext.getString(R.string.epg_event_playing));
        } else {
            holder.setText(R.id.epg_status, "");
        }
    }
}
