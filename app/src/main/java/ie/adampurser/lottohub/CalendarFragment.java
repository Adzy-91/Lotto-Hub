package ie.adampurser.lottohub;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarFragment extends DialogFragment {

    public static String FRAGMENT_ID = "Calendar";
    public static String EXTRA_CURRENT_DATE = "ExtraCurrent";
    public static String EXTRA_LATEST_DATE = "ExtraLatest";
    public static String EXTRA_DRAW_TYPE = "ExtraDrawType";

    private Date mCurrentDate;
    private Date mLatestDrawDate;
    private String mDrawType;
    private OnDrawDateChangedListener mOnDrawDateChangedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentDate = (Date) getArguments().get(EXTRA_CURRENT_DATE);
        mLatestDrawDate = (Date) getArguments().get(EXTRA_LATEST_DATE);
        mDrawType = getArguments().getString(EXTRA_DRAW_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calander, container, false);

        TextView date = (TextView) view.findViewById(R.id.calenderFragmentDate);
        date.setText(AppUtils.getFormatDate(mCurrentDate));

        MaterialCalendarView calendarView = (MaterialCalendarView) view.findViewById(R.id.calendarFragmentCalendar);
        calendarView.addDecorators(
                new CalendarDecoratorDay(getContext(), mDrawType),
                new CalendarDecoratorSelectedDraw(getContext(), mCurrentDate)
        );
        calendarView.setCurrentDate(mCurrentDate);
        calendarView.setSelectedDate(mCurrentDate);
        calendarView.setMaximumDate(mLatestDrawDate);
        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(@NonNull MaterialCalendarView widget, @Nullable CalendarDay date) {
                if (date != null) {
                    mOnDrawDateChangedListener.onDrawDateChanged(date.getDate());
                    dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Fragment fragment = getParentFragment();

        if(fragment instanceof ResultFragment) {
            mOnDrawDateChangedListener = (OnDrawDateChangedListener) fragment.getParentFragment();
        }
        // called from checker fragment
        else {
            mOnDrawDateChangedListener = (OnDrawDateChangedListener) fragment;
        }
    }

    public static CalendarFragment newInstance(Date current, Date latest, String drawType) {
        Bundle args = new Bundle();

        args.putSerializable(EXTRA_CURRENT_DATE, current);
        args.putSerializable(EXTRA_LATEST_DATE, latest);
        args.putSerializable(EXTRA_DRAW_TYPE, drawType);

        CalendarFragment fragment = new CalendarFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
