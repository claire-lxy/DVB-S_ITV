package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.bean.BookParameterModel;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.TimeUtils;
import com.sw.dvblib.SWBooking;

import java.text.MessageFormat;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class BookDialog extends BaseDialogFragment {
    public static final String TAG = "BookDialog";

    private static final int ITEM_BUTTON_FOCUS = -1;
    private static final int ITEM_TYPE = 1;
    private static final int ITEM_MODE = 2;
    private static final int ITEM_CHANNEL_TYPE = 3;
    private static final int ITEM_CHANNEL_NAME = 4;
    private static final int ITEM_DATE = 5;
    private static final int ITEM_START_TIME = 6;
    private static final int ITEM_END_TIME = 7;

    // book type
    private static final int BOOK_TYPE_STANDBY = 0;
    private static final int BOOK_TYPE_PLAY = 1;
    private static final int BOOK_TYPE_RECORD = 2;

    // book mode
    private static final int BOOK_MODE_ONCE = 0;
    private static final int BOOK_MODE_DAILY = 1;
    private static final int BOOK_MODE_WEEKLY = 2;
    private static final int BOOK_MODE_MONTHLY = 3;

    // book channel type
    private static final int BOOK_CHANNEL_TYPE_TV = 0;
    private static final int BOOK_CHANNEL_TYPE_RADIO = 1;

    @BindView(R.id.tv_book_dialog_title)
    TextView mTvTitle;

    @BindView(R.id.item_book_type)
    LinearLayout mItemBookType;

    @BindView(R.id.iv_book_type_left)
    ImageView mIvBookTypeLeft;

    @BindView(R.id.tv_book_type)
    TextView mTvBookType;

    @BindView(R.id.iv_book_type_right)
    ImageView mIvBookTypeRight;

    @BindView(R.id.item_book_mode)
    LinearLayout mItemBookMode;

    @BindView(R.id.iv_book_mode_left)
    ImageView mIvBookModeLeft;

    @BindView(R.id.tv_book_mode)
    TextView mTvBookMode;

    @BindView(R.id.iv_book_mode_right)
    ImageView mIvBookModeRight;

    @BindView(R.id.item_book_channel_type)
    LinearLayout mItemBookChannelType;

    @BindView(R.id.iv_book_channel_type_left)
    ImageView mIvBookChannelTypeLeft;

    @BindView(R.id.tv_book_channel_type)
    TextView mTvBookChannelType;

    @BindView(R.id.iv_book_channel_type_right)
    ImageView mIvBookChannelTypeRight;

    @BindView(R.id.item_book_channel_name)
    LinearLayout mItemBookChannelName;

    @BindView(R.id.iv_book_channel_name_left)
    ImageView mIvBookChannelNameLeft;

    @BindView(R.id.tv_book_channel_name)
    TextView mTvBookChannelName;

    @BindView(R.id.iv_book_channel_name_right)
    ImageView mIvBookChannelNameRight;

    @BindView(R.id.item_book_date)
    LinearLayout mItemBookDate;

    @BindView(R.id.tv_book_date_title)
    TextView mTvBookDateTitle;

//    @BindView(R.id.tv_book_date_weekly)
//    TextView mTvBookDateWeekly;

    @BindView(R.id.rl_book_date_weekly)
    ViewGroup mItemBookDateWeekly;

    @BindView(R.id.iv_book_date_weekly_left)
    ImageView mIvBookDateWeeklyLeft;

    @BindView(R.id.tv_book_date_weekly)
    TextView mTvBookDateWeekly;

    @BindView(R.id.iv_book_date_weekly_right)
    ImageView mIvBookDateWeeklyRight;

    @BindView(R.id.rl_book_date_monthly)
    ViewGroup mItemBookDateMonthly;

    @BindView(R.id.iv_book_date_monthly_left)
    ImageView mIvBookDateMonthlyLeft;

    @BindView(R.id.tv_book_date_monthly)
    TextView mTvBookDateMonthly;

    @BindView(R.id.iv_book_date_monthly_right)
    ImageView mIvBookDateMonthlyRight;

    @BindView(R.id.ll_book_date_edit)
    ViewGroup mItemBookDateEdit;

    @BindView(R.id.et_book_date_year)
    EditText mEtBookDateYear;

    @BindView(R.id.et_book_date_month)
    EditText mEtBookDateMonth;

    @BindView(R.id.et_book_date_day)
    EditText mEtBookDateDay;

    @BindView(R.id.item_book_start_time)
    LinearLayout mItemBookStartTime;

    @BindView(R.id.et_book_start_time_hour)
    EditText mEtBookStartTimeHour;

    @BindView(R.id.et_book_start_time_minute)
    EditText mEtBookStartTimeMinute;

    @BindView(R.id.item_book_end_time)
    LinearLayout mItemBookEndTime;

    @BindView(R.id.et_book_end_time_hour)
    EditText mEtBookEndTimeHour;

    @BindView(R.id.et_book_end_time_minute)
    EditText mEtBookEndTimeMinute;

    @BindView(R.id.btn_book)
    TextView mBtnBook;

    @BindView(R.id.btn_cancel_book)
    TextView mBtnCancelBook;

    @BindArray(R.array.book_type)
    String[] mBookTypeArray;

    @BindArray(R.array.book_mode)
    String[] mBookModeArray;

    @BindArray(R.array.book_channel_type)
    String[] mBookChannelTypeArray;

    @BindArray(R.array.book_date_weekly)
    String[] mBookDateWeeklyArray;

    /**
     * Booking逻辑：
     * 在Type是Record时，无论Mode是哪种，只要确保startTime和endTime的年月日相同，startTime和endTime的时分不同能计算出所跨的秒数即可
     * 在Type不是Record时，无论Mode是哪种，只要确保startTime和endTime的年月日相同，startTime的时分不为空即可
     */
    @OnClick(R.id.btn_book)
    void book() {
        if (!isInputValid()) {
            Toast.makeText(getContext(), getStrings(R.string.toast_book_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mOnBookCallbackListener != null && mProgList != null && !mProgList.isEmpty()) {
            dismiss();

            updateBookInfo();

            SysTime_t startTime = new SysTime_t();
            SysTime_t endTime = new SysTime_t();
            startTime.Year = endTime.Year = TimeUtils.getYear(mEtBookDateYear.getText().toString());
            startTime.Month = endTime.Month = TimeUtils.getMonth(mEtBookDateMonth.getText().toString());
            startTime.Day = endTime.Day = TimeUtils.getDay(mEtBookDateDay.getText().toString());
            startTime.Hour = TimeUtils.getHour(mEtBookStartTimeHour.getText().toString());
            startTime.Minute = TimeUtils.getMinute(mEtBookStartTimeMinute.getText().toString());

            // 只有在record时计算定时器秒数时使用
            endTime.Hour = TimeUtils.getHour(mEtBookEndTimeHour.getText().toString());
            endTime.Minute = TimeUtils.getMinute(mEtBookEndTimeMinute.getText().toString());

            // 没有输入，默认获取到当前日期，如2019-6-25 17:00
            mBookModel.bookInfo.year = startTime.Year;
            mBookModel.bookInfo.month = startTime.Month;
            mBookModel.bookInfo.day = startTime.Day;
            mBookModel.bookInfo.hour = startTime.Hour;
            mBookModel.bookInfo.minute = startTime.Minute;

            switch (mCurrModePosition) {
                case BOOK_MODE_WEEKLY:
                    // daily mode
                    // TextUtils.equals(mTvBookDateWeekly.getText().toString(), getStrings(R.string.book_date_week_everyday))
                    int dayOfWeek = TimeUtils.getWeekByStr(getContext(), mTvBookDateWeekly.getText().toString());
                    List<Integer> dayOfMonths = TimeUtils.getDayOfMonthsByDayOfWeek(startTime.Year, startTime.Month, dayOfWeek);
                    if (dayOfMonths != null && !dayOfMonths.isEmpty()) {
                        boolean isFound = true;
                        int currDayOfMonth = TimeUtils.getDay();
                        for (int i = 0; i < dayOfMonths.size(); i++) {
                            int dayOfMonth = dayOfMonths.get(i);
                            // 最后一个日期和当前日期相同，那要从下个月或下一年开始找开始找同一个星期的日期
                            if (currDayOfMonth >= dayOfMonth && i == dayOfMonths.size() - 1) {
                                isFound = false;
                                break;
                            } else {
                                // 如果有日期大于当前日期，开始日期就是这一天
                                if (dayOfMonth > currDayOfMonth) {
                                    startTime.Day = endTime.Day = dayOfMonth;
                                    mBookModel.bookInfo.day = startTime.Day;
                                    break;
                                }
                            }
                        }

                        if (!isFound) {
                            startTime.Month += 1;
                            if (startTime.Month > 12) {
                                startTime.Month = 1;
                                startTime.Year += 1;
                            }
                            mBookModel.bookInfo.year = endTime.Year = startTime.Year;
                            mBookModel.bookInfo.month = endTime.Month = startTime.Month;
                            dayOfMonths = TimeUtils.getDayOfMonthsByDayOfWeek(startTime.Year, startTime.Month, dayOfWeek);
                            if (dayOfMonths != null && !dayOfMonths.isEmpty()) {
                                // 找到最近的一个日期
                                startTime.Day = endTime.Day = dayOfMonths.get(0);
                                mBookModel.bookInfo.day = startTime.Day;
                            }
                        }
                    }
                    break;

                case BOOK_MODE_MONTHLY:
                    startTime.Day = mCurrBookDayOfMonthPosition;
                    int currDayOfMonth = TimeUtils.getDay();
                    if (startTime.Day < currDayOfMonth) {
                        startTime.Month += 1;
                        if (startTime.Month > 12) {
                            startTime.Month = 1;
                            startTime.Year += 1;
                        }
                    }
                    mBookModel.bookInfo.year = endTime.Year = startTime.Year;
                    mBookModel.bookInfo.month = endTime.Month = startTime.Month;
                    mBookModel.bookInfo.day = endTime.Day = startTime.Day;
                    break;
            }

            computeBookTotalSeconds(startTime, endTime);

            BookParameterModel bpm = new BookParameterModel();
            bpm.bookingModel = mBookModel;
            HSubforProg_t conflictBookProg = SWBookingManager.getInstance().conflictCheck(bpm.bookingModel.bookInfo, 0);
            bpm.bookConflict = SWBookingManager.getInstance().getConflictType(conflictBookProg);
            if (bpm.bookConflict == Constants.BOOK_CONFLICT_ADD || bpm.bookConflict == Constants.BOOK_CONFLICT_REPLACE) {
                bpm.conflictBookProg = conflictBookProg;
            }
            Log.i(TAG, "parameter model = " + bpm);
            mOnBookCallbackListener.onBookCallback(bpm);
        }
    }

    private void computeBookTotalSeconds(SysTime_t startTime, SysTime_t endTime) {
        if (mCurrTypePosition == BOOK_TYPE_RECORD) {
            mBookModel.bookInfo.lasttime = TimeUtils.getTotalSeconds(startTime, endTime);
        }
    }

    private boolean isInputValid() {
        if (mBookModel == null) return false;

        if (mBookModel.bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
            return isDateValid() && isHourAndMinuteValid();
        }

        return isHourAndMinuteValid();
    }

    private boolean isDateValid() {
        if (mBookModel == null) return false;

        String year = mEtBookDateYear.getText().toString();
        String month = mEtBookDateMonth.getText().toString();
        String day = mEtBookDateDay.getText().toString();
        if (TextUtils.isEmpty(year) || Integer.valueOf(year) < TimeUtils.getYear()) {
            dateInvalidFocus(mEtBookDateYear);
            return false;
        }

        if (TextUtils.isEmpty(month) || Integer.valueOf(month) < TimeUtils.getMonth() || !TimeUtils.isMonthValid(Integer.valueOf(month))) {
            dateInvalidFocus(mEtBookDateMonth);
            return false;
        }

        if (TextUtils.isEmpty(day) || Integer.valueOf(day) < TimeUtils.getDay() || Integer.valueOf(day) > TimeUtils.getDayOfMonthByYearAndMonth(Integer.valueOf(year), Integer.valueOf(month))) {
            dateInvalidFocus(mEtBookDateDay);
            return false;
        }

        return true;
    }

//    private boolean isMonthlyValid() {
//        if (mBookModel == null) return false;
//
//        return !TextUtils.isEmpty(mTvBookDateMonthly.getText().toString());
//    }

//    private boolean isWeeklyValid() {
//        if (mBookModel == null) return false;
//
//        return TextUtils.isEmpty(mTvBookDateWeekly.getText().toString());
//    }

    private boolean isHourAndMinuteValid() {
        if (mBookModel == null) return false;

        String startHour = mEtBookStartTimeHour.getText().toString();
        String startMinute = mEtBookStartTimeMinute.getText().toString();

        if (TextUtils.isEmpty(startHour) || !TimeUtils.isHourValid(Integer.valueOf(startHour)) || Integer.valueOf(startHour) < TimeUtils.getHour()) {
            timeInvalidFocus(ITEM_START_TIME, mEtBookStartTimeHour);
            return false;
        }

        if (TextUtils.isEmpty(startMinute) || !TimeUtils.isMinuteValid(Integer.valueOf(startMinute))) {
            timeInvalidFocus(ITEM_START_TIME, mEtBookStartTimeMinute);
            return false;
        }

        if (mBookModel.bookInfo.schtype != SWBooking.BookSchType.RECORD.ordinal()) {
            return true;
        }

        String endHour = mEtBookEndTimeHour.getText().toString();
        String endMinute = mEtBookEndTimeMinute.getText().toString();

        if (TextUtils.isEmpty(endHour) || !TimeUtils.isHourValid(Integer.valueOf(endHour)) && Integer.valueOf(endHour) < Integer.valueOf(startHour)) {
            timeInvalidFocus(ITEM_END_TIME, mEtBookEndTimeHour);
            return false;
        }

        if (TextUtils.isEmpty(endMinute) || !TimeUtils.isMinuteValid(Integer.valueOf(endMinute)) ||
                !TimeUtils.isBookSecondsValid(Integer.valueOf(startHour), Integer.valueOf(startMinute), Integer.valueOf(endHour), Integer.valueOf(endMinute))) {
            timeInvalidFocus(ITEM_END_TIME, mEtBookEndTimeMinute);
            return false;
        }

        return true;
    }

    @OnClick(R.id.btn_cancel_book)
    void cancelBook() {
        dismiss();
    }

    @OnFocusChange(R.id.et_book_start_time_hour)
    void onEditStartHourFocusChange(boolean hasFocus) {
        if (!hasFocus) {
            String startHour = mEtBookStartTimeHour.getText().toString();
            if (TextUtils.isEmpty(startHour)) {
                mEtBookStartTimeHour.setText("0");
            }
        }
    }

    @OnFocusChange(R.id.et_book_start_time_minute)
    void onEditStartMinuteFocusChange(boolean hasFocus) {
        if (!hasFocus) {
            String startMinute = mEtBookStartTimeMinute.getText().toString();
            if (TextUtils.isEmpty(startMinute)) {
                mEtBookStartTimeMinute.setText("0");
            }
        }
    }

    @OnFocusChange(R.id.et_book_end_time_hour)
    void onEditHourFocusChange(boolean hasFocus) {
        if (!hasFocus) {
            String endHour = mEtBookEndTimeHour.getText().toString();
            if (TextUtils.isEmpty(endHour)) {
                mEtBookEndTimeHour.setText("0");
            }
        }
    }

    @OnFocusChange(R.id.et_book_end_time_minute)
    void onEditMinuteFocusChange(boolean hasFocus) {
        if (!hasFocus) {
            String endMinute = mEtBookEndTimeMinute.getText().toString();
            if (TextUtils.isEmpty(endMinute)) {
                mEtBookEndTimeMinute.setText("0");
            }
        }
    }

    // 按钮获取焦点时，EditText取消焦点，否则相反
    @OnFocusChange({R.id.btn_book, R.id.btn_cancel_book})
    void onButtonFocusChange(boolean hasFocus) {
        mEtBookDateYear.setFocusable(!hasFocus);
        mEtBookDateMonth.setFocusable(!hasFocus);
        mEtBookDateDay.setFocusable(!hasFocus);
        mEtBookStartTimeHour.setFocusable(!hasFocus);
        mEtBookStartTimeMinute.setFocusable(!hasFocus);
        mEtBookEndTimeHour.setFocusable(!hasFocus);
        mEtBookEndTimeMinute.setFocusable(!hasFocus);
    }

    private int mCurrentSelectItem = ITEM_TYPE;

    private int mCurrTypePosition = BOOK_TYPE_STANDBY;
    private int mCurrModePosition;
    private int mCurrChannelTypePosition = BOOK_CHANNEL_TYPE_TV;
    private int mCurrBookDayOfMonthPosition = 1;
    private int mMaxBookDayOfMonth = TimeUtils.getDayOfMonthByYearAndMonth(TimeUtils.getYear(), TimeUtils.getMonth());
    private int mCurrBookDayOfWeekPosition;

    private OnBookCallbackListener mOnBookCallbackListener;

    private String mTitle;
    private BookingModel mBookModel;
    private List<PDPInfo_t> mProgList;
    private int mChannelNamePosition;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_booking_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        mTvBookType.setText(getBookTypeText());
        mCurrTypePosition = getPosition(mTvBookType.getText().toString(), mBookTypeArray);

        mTvBookMode.setText(getBookModelText());
        mCurrModePosition = getPosition(mTvBookMode.getText().toString(), mBookModeArray);

        mTvBookChannelType.setText(getBookChannelTypeText());
        mCurrChannelTypePosition = getPosition(mTvBookChannelType.getText().toString(), mBookChannelTypeArray);

        mTvBookChannelName.setText(getBookChannelNameText());

        mEtBookDateYear.setText(getBookYearText());
        mEtBookDateMonth.setText(getBookMonthText());
        mEtBookDateDay.setText(getBookDayText());

        mTvBookDateWeekly.setText(getBookDayOfWeekText());
        mCurrBookDayOfWeekPosition = getPosition(mTvBookDateWeekly.getText().toString(), mBookDateWeeklyArray);

        mTvBookDateMonthly.setText(getBookDayOfMonthText());
        mCurrBookDayOfMonthPosition = mBookModel == null ? 1 : mBookModel.bookInfo.day;

        mEtBookStartTimeHour.setText(getBookStartHourText());
        mEtBookStartTimeMinute.setText(getBookStartMinuteText());
        mEtBookEndTimeHour.setText(getBookEndHourText());
        mEtBookEndTimeMinute.setText(getBookEndMinuteText());

        mEtBookDateYear.setOnKeyListener(new EditKeyListener(mEtBookDateYear, mEtBookDateDay, mEtBookDateMonth));
        mEtBookDateMonth.setOnKeyListener(new EditKeyListener(mEtBookDateMonth, mEtBookDateYear, mEtBookDateDay));
        mEtBookDateDay.setOnKeyListener(new EditKeyListener(mEtBookDateDay, mEtBookDateMonth, mEtBookDateYear));

        mEtBookStartTimeHour.setOnKeyListener(new EditKeyListener(mEtBookStartTimeHour, mEtBookStartTimeMinute, mEtBookStartTimeMinute));
        mEtBookStartTimeMinute.setOnKeyListener(new EditKeyListener(mEtBookStartTimeMinute, mEtBookStartTimeHour, mEtBookStartTimeHour));

        mEtBookEndTimeHour.setOnKeyListener(new EditKeyListener(mEtBookEndTimeHour, mEtBookEndTimeMinute, mEtBookEndTimeMinute));
        mEtBookEndTimeMinute.setOnKeyListener(new EditKeyListener(mEtBookEndTimeMinute, mEtBookEndTimeHour, mEtBookEndTimeHour));

        initBookEdit();

        notifyItemFocusChange();
        notifyBookDateItemChange();
        itemFocusChange();
    }

    @Override
    protected int resizeDialogWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
    }

    private String getBookTypeText() {
        if (mBookModel == null) {
            return mBookTypeArray[BOOK_TYPE_RECORD];
        }

        if (mBookModel.bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
            return mBookTypeArray[BOOK_TYPE_PLAY];
        } else if (mBookModel.bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            return mBookTypeArray[BOOK_TYPE_RECORD];
        }
        return mBookTypeArray[BOOK_TYPE_STANDBY];
    }

    private String getBookModelText() {
        if (mBookModel == null) {
            return mBookModeArray[0];
        }

        if (mBookModel.bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
            return mBookModeArray[0];
        } else if (mBookModel.bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
            return mBookModeArray[1];
        } else if (mBookModel.bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
            return mBookModeArray[2];
        } else if (mBookModel.bookInfo.repeatway == SWBooking.BookRepeatWay.MONTHLY.ordinal()) {
            return mBookModeArray[3];
        }
        return mBookModeArray[0];
    }

    private String getBookChannelTypeText() {
        if (mBookModel == null) {
            return mBookChannelTypeArray[0];
        }

        if (mBookModel.bookInfo.type == SWBooking.BookType.TV.ordinal()) {
            return mBookChannelTypeArray[0];
        } else if (mBookModel.bookInfo.type == SWBooking.BookType.RADIO.ordinal()) {
            return mBookChannelTypeArray[1];
        }
        return mBookChannelTypeArray[0];
    }

    private String getBookChannelNameText() {
        if (mProgList != null && !mProgList.isEmpty()) {
            return mBookModel == null ? mProgList.get(0).Name : mProgList.get(mChannelNamePosition).Name;
        }
        return "";
    }

    private String getBookYearText() {
        // weekly和monthly可能调整后会更改，在确定时获取最新的年份
        return mBookModel == null || mCurrModePosition == BOOK_MODE_WEEKLY || mCurrModePosition == BOOK_MODE_MONTHLY ? "" : String.valueOf(mBookModel.bookInfo.year);
    }

    private String getBookMonthText() {
        // weekly和monthly可能调整后会更改，在确定时获取最新的月份
        return mBookModel == null || mCurrModePosition == BOOK_MODE_WEEKLY || mCurrModePosition == BOOK_MODE_MONTHLY ? "" : String.valueOf(mBookModel.bookInfo.month);
    }

    private String getBookDayText() {
        // weekly和monthly可能调整后会更改，在确定时获取最新的日期
        return mBookModel == null || mCurrModePosition == BOOK_MODE_WEEKLY || mCurrModePosition == BOOK_MODE_MONTHLY ? "" : String.valueOf(mBookModel.bookInfo.day);
    }

    private String getBookDayOfWeekText() {
        if (mBookModel == null) return mBookDateWeeklyArray[0];

        int dayOfWeek = TimeUtils.getDayOfWeek(mBookModel.bookInfo.year, mBookModel.bookInfo.month, mBookModel.bookInfo.day);
        return mBookDateWeeklyArray[dayOfWeek - 1];
    }

    private String getBookDayOfMonthText() {
        if (mBookModel == null)
            return MessageFormat.format(getStrings(R.string.book_date_day), String.valueOf(mCurrBookDayOfMonthPosition));

        return MessageFormat.format(getString(R.string.book_date_day), String.valueOf(mBookModel.bookInfo.day));
    }

    private String getBookStartHourText() {
        return mBookModel == null ? "" : String.valueOf(mBookModel.bookInfo.hour);
    }

    private String getBookStartMinuteText() {
        return mBookModel == null ? "" : String.valueOf(mBookModel.bookInfo.minute);
    }

    private String getBookEndHourText() {
        if (mBookModel == null) return "";
        int[] endDateArr = TimeUtils.getEndDate(mBookModel.bookInfo.year, mBookModel.bookInfo.month, mBookModel.bookInfo.day,
                mBookModel.bookInfo.hour, mBookModel.bookInfo.minute, mBookModel.bookInfo.lasttime);
        if (endDateArr == null) return "";
        return String.valueOf(endDateArr[TimeUtils.HOUR]);
    }

    private String getBookEndMinuteText() {
        if (mBookModel == null) return "";
        int[] endDateArr = TimeUtils.getEndDate(mBookModel.bookInfo.year, mBookModel.bookInfo.month, mBookModel.bookInfo.day,
                mBookModel.bookInfo.hour, mBookModel.bookInfo.minute, mBookModel.bookInfo.lasttime);
        if (endDateArr == null) return "";
        return String.valueOf(endDateArr[TimeUtils.MINUTE]);
    }

    private int getPosition(String text, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (TextUtils.equals(text, array[i])) {
                return i;
            }
        }
        return 0;
    }

    private void initBookEdit() {
        if (mBookModel == null) {
            mEtBookDateYear.setText(String.valueOf(TimeUtils.getYear()));
            mEtBookDateMonth.setText(String.valueOf(TimeUtils.getMonth()));
            mEtBookDateDay.setText(String.valueOf(TimeUtils.getDay()));
            mEtBookStartTimeHour.setText(String.valueOf(TimeUtils.getHour()));
            mEtBookStartTimeMinute.setText(String.valueOf(TimeUtils.getMinute()));

            if (mCurrTypePosition == BOOK_TYPE_RECORD) {
                mEtBookEndTimeHour.setText(String.valueOf(TimeUtils.getHour()));
                mEtBookEndTimeMinute.setText(String.valueOf(TimeUtils.getMinute()));
            }
        }
    }

    private class EditKeyListener implements View.OnKeyListener {
        EditText editText;
        EditText leftEditText;
        EditText rightEditText;

        EditKeyListener(EditText editText, EditText leftEditText, EditText rightEditText) {
            this.editText = editText;
            this.leftEditText = leftEditText;
            this.rightEditText = rightEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (TextUtils.isEmpty(editText.getText().toString())) {
                            viewDelayFocus(leftEditText);
                        } else {
                            editText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    editText.setText(EditUtils.getEditSubstring(editText));
                                    editText.setSelection(editText.getText().length());
                                }
                            }, 10);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        viewDelayFocus(rightEditText);
                        break;
                }
            }
            return false;
        }
    }

    private void viewDelayFocus(TextView textView) {
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.requestFocus();
                if (textView instanceof EditText) {
                    EditText editText = (EditText) textView;
                    editText.setSelection(editText.getText().length());
                }
            }
        }, 10);
    }

    public BookDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public BookDialog bookModel(BookingModel bookModel) {
        this.mBookModel = bookModel;
        return this;
    }

    public BookDialog progList(List<PDPInfo_t> progList) {
        this.mProgList = progList;
        return this;
    }

    public BookDialog channelNamePosition(int position) {
        this.mChannelNamePosition = position;
        return this;
    }

//    private void showBookDateWeeklyDialog() {
//        new BookDateWeeklyDialog()
//                .title(getStrings(R.string.book_weekly_dialog_title))
//                .check(mTvBookDateWeekly.getText().toString())
//                .setOnCheckGroupCallback(new OnCheckGroupCallback() {
//                    @Override
//                    public void callback(SparseBooleanArray checkMap) {
//                        if (isCheckAll(checkMap)) {
//                            mTvBookDateWeekly.setText(getStrings(R.string.book_date_week_everyday));
//                            return;
//                        }
//
//                        mTvBookDateWeekly.setText(""); // clear
//                        for (int i = 0; i < mBookDateWeeklyArray.length; i++) {
//                            if (checkMap.get(i)) {
//                                mTvBookDateWeekly.append(mBookDateWeeklyArray[i]);
//                                mTvBookDateWeekly.append(" ");
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void cancel() {
//
//                    }
//                }).show(getActivity().getSupportFragmentManager(), BookDateWeeklyDialog.TAG);
//    }

//    private boolean isCheckAll(SparseBooleanArray checkMap) {
//        for (int i = 0; i < mBookDateWeeklyArray.length; i++) {
//            if (!checkMap.get(i)) {
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_MODE:
                case ITEM_CHANNEL_TYPE:
                case ITEM_CHANNEL_NAME:
                case ITEM_END_TIME:
                    --mCurrentSelectItem;
                    break;
                case ITEM_START_TIME:
                    if (mCurrTypePosition != BOOK_TYPE_STANDBY && mCurrModePosition == BOOK_MODE_DAILY) {
                        mCurrentSelectItem -= 2; // select channel name
                    } else if (mCurrTypePosition == BOOK_TYPE_STANDBY && mCurrModePosition == BOOK_MODE_DAILY) {
                        mCurrentSelectItem -= 4; // select mode
                    } else {
                        --mCurrentSelectItem;
                    }
                    break;
                case ITEM_DATE:
                    if (mCurrTypePosition == BOOK_TYPE_STANDBY) {
                        mCurrentSelectItem -= 3; // select mode
                    } else {
                        --mCurrentSelectItem;
                    }
                    break;
                case ITEM_BUTTON_FOCUS:
                    // 按钮取消焦点
                    mBtnBook.setFocusable(false);
                    mBtnCancelBook.setFocusable(false);
                    if (mCurrTypePosition == BOOK_TYPE_RECORD) {
                        mCurrentSelectItem = ITEM_END_TIME;
                    } else if (mCurrTypePosition == BOOK_TYPE_STANDBY || mCurrTypePosition == BOOK_TYPE_PLAY) {
                        mCurrentSelectItem = ITEM_START_TIME;
                    }
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_TYPE:
                case ITEM_CHANNEL_TYPE:
                case ITEM_DATE:
                    ++mCurrentSelectItem;
                    break;
                case ITEM_CHANNEL_NAME:
                    if (mCurrModePosition == BOOK_MODE_DAILY) {
                        mCurrentSelectItem += 2; // select mode
                    } else {
                        ++mCurrentSelectItem;
                    }
                    break;
                case ITEM_MODE:
                    if (mCurrTypePosition == BOOK_TYPE_STANDBY) {
                        if (mCurrModePosition == BOOK_MODE_DAILY) {
                            mCurrentSelectItem += 4; // select start time
                        } else {
                            mCurrentSelectItem += 3; // select date
                        }
                    } else {
                        ++mCurrentSelectItem;
                    }
                    break;
                case ITEM_START_TIME:
                    if (mCurrTypePosition == BOOK_TYPE_RECORD) {
                        ++mCurrentSelectItem;
                    } else {
                        // 按钮获取焦点，取消EditText焦点
                        mCurrentSelectItem = ITEM_BUTTON_FOCUS;
                        mBtnBook.setFocusable(true);
                        mBtnCancelBook.setFocusable(true);
                        viewDelayFocus(mBtnBook);
                    }
                    break;
                case ITEM_END_TIME:
                    // 按钮获取焦点，取消EditText焦点
                    mCurrentSelectItem = ITEM_BUTTON_FOCUS;
                    mBtnBook.setFocusable(true);
                    mBtnCancelBook.setFocusable(true);
                    viewDelayFocus(mBtnBook);
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_TYPE:
                    if (--mCurrTypePosition < 0) mCurrTypePosition = mBookTypeArray.length - 1;
                    mTvBookType.setText(mBookTypeArray[mCurrTypePosition]);
                    notifyItemFocusChange();
                    break;

                case ITEM_MODE:
                    if (--mCurrModePosition < 0) mCurrModePosition = mBookModeArray.length - 1;
                    mTvBookMode.setText(mBookModeArray[mCurrModePosition]);
                    notifyBookDateItemChange();
                    break;

                case ITEM_CHANNEL_TYPE:
                    if (--mCurrChannelTypePosition < 0)
                        mCurrChannelTypePosition = mBookChannelTypeArray.length - 1;
                    mTvBookChannelType.setText(mBookChannelTypeArray[mCurrChannelTypePosition]);
                    break;

                case ITEM_CHANNEL_NAME:
                    if (mProgList == null || mProgList.isEmpty()) return false;

                    if (--mChannelNamePosition < 0) mChannelNamePosition = mProgList.size() - 1;
                    mTvBookChannelName.setText(mProgList.get(mChannelNamePosition).Name);
                    break;

                case ITEM_DATE:
                    if (mCurrModePosition == BOOK_MODE_MONTHLY) {
                        if (--mCurrBookDayOfMonthPosition <= 0)
                            mCurrBookDayOfMonthPosition = mMaxBookDayOfMonth;
                        mTvBookDateMonthly.setText(MessageFormat.format(getStrings(R.string.book_date_day), String.valueOf(mCurrBookDayOfMonthPosition)));
                    }

                    if (mCurrModePosition == BOOK_MODE_WEEKLY) {
                        if (--mCurrBookDayOfWeekPosition < 0)
                            mCurrBookDayOfWeekPosition = mBookDateWeeklyArray.length - 1;
                        mTvBookDateWeekly.setText(mBookDateWeeklyArray[mCurrBookDayOfWeekPosition]);
                    }
                    break;
            }
            updateBookInfo();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_TYPE:
                    if (++mCurrTypePosition >= mBookTypeArray.length) mCurrTypePosition = 0;
                    mTvBookType.setText(mBookTypeArray[mCurrTypePosition]);
                    notifyItemFocusChange();
                    break;

                case ITEM_MODE:
                    if (++mCurrModePosition >= mBookModeArray.length) mCurrModePosition = 0;
                    mTvBookMode.setText(mBookModeArray[mCurrModePosition]);
                    notifyBookDateItemChange();
                    break;

                case ITEM_CHANNEL_TYPE:
                    if (++mCurrChannelTypePosition >= mBookChannelTypeArray.length)
                        mCurrChannelTypePosition = 0;
                    mTvBookChannelType.setText(mBookChannelTypeArray[mCurrChannelTypePosition]);
                    break;

                case ITEM_CHANNEL_NAME:
                    if (mProgList == null || mProgList.isEmpty()) return false;

                    if (++mChannelNamePosition > mProgList.size() - 1) mChannelNamePosition = 0;
                    mTvBookChannelName.setText(mProgList.get(mChannelNamePosition).Name);
                    break;

                case ITEM_DATE:
                    if (mCurrModePosition == BOOK_MODE_MONTHLY) {
                        if (++mCurrBookDayOfMonthPosition > mMaxBookDayOfMonth)
                            mCurrBookDayOfMonthPosition = 1;
                        mTvBookDateMonthly.setText(MessageFormat.format(getStrings(R.string.book_date_day), String.valueOf(mCurrBookDayOfMonthPosition)));
                    }

                    if (mCurrModePosition == BOOK_MODE_WEEKLY) {
                        if (++mCurrBookDayOfWeekPosition >= mBookDateWeeklyArray.length)
                            mCurrBookDayOfWeekPosition = 0;
                        mTvBookDateWeekly.setText(mBookDateWeeklyArray[mCurrBookDayOfWeekPosition]);
                    }
                    break;
            }
            updateBookInfo();
        }

//        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
//            if (mCurrModePosition == BOOK_MODE_WEEKLY && mCurrentSelectItem == ITEM_DATE) {
//                showBookDateWeeklyDialog();
//                return true;
//            }
//        }

        return super.onKeyListener(dialog, keyCode, event);
    }

    private void updateBookInfo() {
        if (mBookModel == null) {
            mBookModel = new BookingModel();
        }
        if (mBookModel.bookInfo == null) {
            mBookModel.bookInfo = new HSubforProg_t();
        }

        mBookModel.bookInfo.schtype = getBookType();
        mBookModel.bookInfo.repeatway = getBookMode();
        mBookModel.bookInfo.type = getBookChannelType();
        mBookModel.bookInfo.schway = SWBooking.BookWay.MANUAL.ordinal();
        if (mProgList != null && !mProgList.isEmpty()) {
            mBookModel.progInfo = mProgList.get(mChannelNamePosition);
        }
    }

    private int getBookType() {
        switch (mCurrTypePosition) {
            case BOOK_TYPE_STANDBY:
                return SWBooking.BookSchType.NONE.ordinal();
            case BOOK_TYPE_PLAY:
                return SWBooking.BookSchType.PLAY.ordinal();
            case BOOK_TYPE_RECORD:
                return SWBooking.BookSchType.RECORD.ordinal();
        }
        return SWBooking.BookSchType.RECORD.ordinal();
    }

    private int getBookMode() {
        switch (mCurrModePosition) {
            case BOOK_MODE_ONCE:
                return SWBooking.BookRepeatWay.ONCE.ordinal();
            case BOOK_MODE_DAILY:
                return SWBooking.BookRepeatWay.DAILY.ordinal();
            case BOOK_MODE_WEEKLY:
                return SWBooking.BookRepeatWay.WEEKLY.ordinal();
            case BOOK_MODE_MONTHLY:
                return SWBooking.BookRepeatWay.MONTHLY.ordinal();
        }
        return SWBooking.BookRepeatWay.ONCE.ordinal();
    }

    private int getBookChannelType() {
        switch (mCurrChannelTypePosition) {
            case BOOK_CHANNEL_TYPE_TV:
                return SWBooking.BookType.TV.ordinal();
            case BOOK_CHANNEL_TYPE_RADIO:
                return SWBooking.BookType.RADIO.ordinal();
        }
        return SWBooking.BookType.TV.ordinal();
    }

    /**
     * if BookType is record, ChannelType、ChannelName、BookEndTime focusable
     * if BookType is standby, ChannelType、ChannelName、BookEndTime no focusable
     * if BookType is play, ChannelType、ChannelName focusable, BookEndTime no focusable
     */
    private void notifyItemFocusChange() {
        if (mCurrTypePosition == BOOK_TYPE_RECORD) {
            mItemBookChannelType.setBackgroundResource(0);

            mItemBookChannelName.setBackgroundResource(0);

            mItemBookEndTime.setBackgroundResource(0);
        }

        if (mCurrTypePosition == BOOK_TYPE_STANDBY) {
            mItemBookChannelType.setBackgroundResource(R.color.color_4D4D4D);

            mItemBookChannelName.setBackgroundResource(R.color.color_4D4D4D);

            mItemBookEndTime.setBackgroundResource(R.color.color_4D4D4D);
        }

        if (mCurrTypePosition == BOOK_TYPE_PLAY) {
            mItemBookChannelType.setBackgroundResource(0);

            mItemBookChannelName.setBackgroundResource(0);

            mItemBookEndTime.setBackgroundResource(R.color.color_4D4D4D);
        }
    }

    private void notifyBookDateItemChange() {
        if (mCurrModePosition == BOOK_MODE_ONCE) {
            mItemBookDate.setVisibility(View.VISIBLE);
            mItemBookDateEdit.setVisibility(View.VISIBLE);
            mItemBookDateMonthly.setVisibility(View.GONE);
//            mTvBookDateWeekly.setVisibility(View.GONE);
            mItemBookDateWeekly.setVisibility(View.GONE);
            mTvBookDateTitle.setText(getStrings(R.string.book_date));
        }

        if (mCurrModePosition == BOOK_MODE_DAILY) {
            mItemBookDate.setVisibility(View.GONE);
        }

        if (mCurrModePosition == BOOK_MODE_WEEKLY) {
            mItemBookDate.setVisibility(View.VISIBLE);
            mItemBookDateEdit.setVisibility(View.GONE);
            mItemBookDateMonthly.setVisibility(View.GONE);
//            mTvBookDateWeekly.setVisibility(View.VISIBLE);
            mItemBookDateWeekly.setVisibility(View.VISIBLE);
            mTvBookDateTitle.setText(getStrings(R.string.book_week));
        }

        if (mCurrModePosition == BOOK_MODE_MONTHLY) {
            mItemBookDate.setVisibility(View.VISIBLE);
            mItemBookDateEdit.setVisibility(View.GONE);
//            mTvBookDateWeekly.setVisibility(View.GONE);
            mItemBookDateWeekly.setVisibility(View.GONE);
            mItemBookDateMonthly.setVisibility(View.VISIBLE);
            mTvBookDateTitle.setText(getStrings(R.string.book_day));
            mTvBookDateMonthly.setText(MessageFormat.format(getStrings(R.string.book_date_day), String.valueOf(mCurrBookDayOfMonthPosition)));
        }
    }

    private void itemFocusChange() {
        itemChange(ITEM_TYPE, mItemBookType, mIvBookTypeLeft, mIvBookTypeRight, mTvBookType);
        itemChange(ITEM_MODE, mItemBookMode, mIvBookModeLeft, mIvBookModeRight, mTvBookMode);
        channelTypeItemFocusChange();
        channelNameItemFocusChange();
        bookDateItemFocusChange();
        itemChange(ITEM_START_TIME, mItemBookStartTime, null, null, null);
        bookEndTimeItemFocusChange();

        notifyEditFocusChange();
    }

    private void channelTypeItemFocusChange() {
        if (mCurrTypePosition != BOOK_TYPE_STANDBY) {
            itemChange(ITEM_CHANNEL_TYPE, mItemBookChannelType, mIvBookChannelTypeLeft, mIvBookChannelTypeRight, mTvBookChannelType);
        }
    }

    private void channelNameItemFocusChange() {
        if (mCurrTypePosition != BOOK_TYPE_STANDBY) {
            itemChange(ITEM_CHANNEL_NAME, mItemBookChannelName, mIvBookChannelNameLeft, mIvBookChannelNameRight, mTvBookChannelName);
        }
    }

    private void bookDateItemFocusChange() {
        itemChange(ITEM_DATE, mItemBookDate, null, null, null);
        if (mCurrModePosition == BOOK_MODE_MONTHLY) {
            itemChange(ITEM_DATE, null, mIvBookDateMonthlyLeft, mIvBookDateMonthlyRight, mTvBookDateMonthly);
        }

        if (mCurrModePosition == BOOK_MODE_WEEKLY) {
            itemChange(ITEM_DATE, null, mIvBookDateWeeklyLeft, mIvBookDateWeeklyRight, mTvBookDateWeekly);
        }
    }

    private void bookEndTimeItemFocusChange() {
        if (mCurrTypePosition == BOOK_TYPE_RECORD) {
            itemChange(ITEM_END_TIME, mItemBookEndTime, null, null, null);
        }
    }

    private void notifyEditFocusChange() {
        dateEditFocus(mEtBookDateYear);
        startTimeEditFocus(mEtBookStartTimeHour);
        endTimeEditFocus(mEtBookEndTimeHour);
    }

    private void dateEditFocus(EditText focusEdit) {
        updateEditFocusable();

        // 获取焦点延时，防止概率出现requestFocus没有获取到焦点不显示光标问题
        if (mCurrentSelectItem == ITEM_DATE && mCurrModePosition != BOOK_MODE_WEEKLY) {
            viewDelayFocus(focusEdit);
        }
    }

    private void startTimeEditFocus(EditText focusEdit) {
        updateEditFocusable();

        if (mCurrentSelectItem == ITEM_START_TIME) {
            viewDelayFocus(focusEdit);
        }
    }

    private void endTimeEditFocus(EditText focusEdit) {
        updateEditFocusable();

        if (mCurrentSelectItem == ITEM_END_TIME) {
            viewDelayFocus(focusEdit);
        }
    }

    private void updateEditFocusable() {
        if (mCurrModePosition != BOOK_MODE_WEEKLY) {
            mEtBookDateYear.setFocusable(mCurrentSelectItem == ITEM_DATE);
            mEtBookDateMonth.setFocusable(mCurrentSelectItem == ITEM_DATE);
            mEtBookDateDay.setFocusable(mCurrentSelectItem == ITEM_DATE);
        }

        mEtBookStartTimeHour.setFocusable(mCurrentSelectItem == ITEM_START_TIME);
        mEtBookStartTimeMinute.setFocusable(mCurrentSelectItem == ITEM_START_TIME);

        mEtBookEndTimeHour.setFocusable(mCurrentSelectItem == ITEM_END_TIME);
        mEtBookEndTimeMinute.setFocusable(mCurrentSelectItem == ITEM_END_TIME);
    }

    private void dateInvalidFocus(EditText focusEdit) {
        mCurrentSelectItem = ITEM_DATE;
        mBtnBook.setFocusable(false);
        mBtnCancelBook.setFocusable(false);
        bookDateItemFocusChange();
        dateEditFocus(focusEdit);
    }

    private void timeInvalidFocus(int focusItemPosition, EditText focusEdit) {
        mCurrentSelectItem = focusItemPosition;
        mBtnBook.setFocusable(false);
        mBtnCancelBook.setFocusable(false);

        if (mCurrentSelectItem == ITEM_START_TIME) {
            itemChange(ITEM_START_TIME, mItemBookStartTime, null, null, null);
            startTimeEditFocus(focusEdit);
        }

        if (mCurrentSelectItem == ITEM_END_TIME) {
            itemChange(ITEM_END_TIME, mItemBookEndTime, null, null, null);
            endTimeEditFocus(focusEdit);
        }
    }

    private void itemChange(int selectItem, ViewGroup itemGroup, ImageView ivLeft, ImageView ivRight, TextView textView) {
        if (itemGroup != null) {
            itemGroup.setBackgroundResource(mCurrentSelectItem == selectItem ? R.drawable.btn_translate_bg_select_shape : 0);
        }
        if (ivLeft != null) {
            ivLeft.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (ivRight != null) {
            ivRight.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (textView != null) {
            textView.setBackgroundResource(mCurrentSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        }
    }

    public BookDialog setOnBookCallbackListener(OnBookCallbackListener listener) {
        this.mOnBookCallbackListener = listener;
        return this;
    }

    public interface OnBookCallbackListener {
        void onBookCallback(BookParameterModel bookParameterModel);
    }
}
