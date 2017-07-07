package ie.adampurser.lottohub;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Date;

public class CalendarDecoratorSelectedDraw implements DayViewDecorator {
    private Context mContext;
    private Date mDate;

    public CalendarDecoratorSelectedDraw(Context context, Date date) {
        mContext = context;
        mDate = date;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
       return calendarDay.getDate().equals(mDate);
    }

    @Override
    public void decorate(DayViewFacade dayViewFacade) {
        dayViewFacade.addSpan(new ForegroundColorSpan(
                ContextCompat.getColor(mContext, R.color.white))
        );
    }
}
