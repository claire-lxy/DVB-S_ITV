package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.BookingModel;
import com.sw.dvblib.SWBooking;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;

public class BookListAdapter extends BaseListViewAdapter<BookingModel> {

    public BookListAdapter(Context context, List<BookingModel> datas) {
        super(context, datas, R.layout.booking_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, BookingModel item) {
        holder.setText(R.id.tv_book_num, String.valueOf(position + 1))
                .setText(R.id.tv_book_channel_name, item.getBookProgName())
                .setText(R.id.tv_book_date_monthly, item.getBookDate(mContext, BookingModel.BOOK_TIME_SEPARATOR_NEWLINE))
                .setTextSize(R.id.tv_book_date_monthly, getDateTextSize(item.bookInfo))
                .setText(R.id.tv_book_mode, item.getBookMode(mContext))
                .setText(R.id.tv_book_type, item.getBookType(mContext));
    }

    private float getDateTextSize(HSubforProg_t bookInfo) {
        if (bookInfo == null) return 15;

        float textSize = 15;
        if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                textSize = 19;
            }
        }

        if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()
                || bookInfo.schtype == SWBooking.BookSchType.NONE.ordinal()) {
            if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()
                    || bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                textSize = 19;
            }
        }
        return textSize;
    }
}
