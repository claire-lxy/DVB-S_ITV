package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.DateModel;
import com.sw.dvblib.SWBooking;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;


public class EpgChannelListAdapter extends BaseListViewAdapter<EpgEvent_t> {

    public EpgChannelListAdapter(Context context, List<EpgEvent_t> datas) {
        super(context, datas, R.layout.epg_channel_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, EpgEvent_t item) {
        SysTime_t startTime = SWTimerManager.getInstance().getStartTime(item);
        SysTime_t endTime = SWTimerManager.getInstance().getEndTime(item);

        holder.setText(R.id.epg_title, item.memEventName)
                .setText(R.id.epg_time, new DateModel(startTime, endTime).getFormatHourAndMinute())
                .setVisibility(R.id.epg_book_type, item.schtype == SWBooking.BookSchType.PLAY.ordinal()
                        || item.schtype == SWBooking.BookSchType.RECORD.ordinal() ? View.VISIBLE : View.INVISIBLE)
                .setImageResource(R.id.epg_book_type, item.schtype == SWBooking.BookSchType.PLAY.ordinal() ? R.mipmap.ic_book_play : R.mipmap.ic_book_record);
        if (SWTimerManager.getInstance().isProgramPlaying(item)) {
            holder.setText(R.id.epg_status, mContext.getString(R.string.epg_event_playing));
        } else {
            holder.setText(R.id.epg_status, "");
        }
    }
}
