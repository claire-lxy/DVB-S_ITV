package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.BookingModel;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Repeat;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Task;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;

public class BookListAdapter extends BaseListViewAdapter<BookingModel> {

    public BookListAdapter(Context context, List<BookingModel> datas) {
        super(context, datas, R.layout.booking_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, BookingModel item) {
        holder.setText(R.id.tv_book_num, String.valueOf(position + 1))
                .setText(R.id.tv_book_channel_name, item.getBookProgName())
                .setText(R.id.tv_book_date, item.getBookDate(mContext, BookingModel.BOOK_TIME_SEPARATOR_NEWLINE))
                .setTextSize(R.id.tv_book_date, getDateTextSize(item.bookInfo))
                .setText(R.id.tv_book_mode, item.getBookMode(mContext))
                .setText(R.id.tv_book_type, item.getBookType(mContext));
    }

    private float getDateTextSize(HBooking_Struct_Timer bookInfo) {
        if (bookInfo == null) return 15;

        float textSize = 15;
        if (bookInfo.schtype == HBooking_Enum_Task.RECORD) {
            if (bookInfo.repeatway == HBooking_Enum_Repeat.DAILY) {
                textSize = 19;
            }
        }

        if (bookInfo.schtype == HBooking_Enum_Task.PLAY
                || bookInfo.schtype == HBooking_Enum_Task.NONE) {
            if (bookInfo.repeatway == HBooking_Enum_Repeat.ONCE
                    || bookInfo.repeatway == HBooking_Enum_Repeat.DAILY) {
                textSize = 19;
            }
        }
        return textSize;
    }
}
