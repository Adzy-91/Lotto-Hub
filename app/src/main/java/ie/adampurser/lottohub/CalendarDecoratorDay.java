package ie.adampurser.lottohub;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class CalendarDecoratorDay implements DayViewDecorator {
    private Context mContext;
    private String mDrawTitle;

    public CalendarDecoratorDay(Context context, String drawTitle) {
        mContext = context;
        mDrawTitle = drawTitle;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return !Result.isDrawDay(mDrawTitle, calendarDay.getDate());
    }

    @Override
    public void decorate(DayViewFacade dayViewFacade) {
        dayViewFacade.addSpan(new ForegroundColorSpan(
                mContext.getResources().getColor(R.color.dark_text_hints)
        ));
        dayViewFacade.setDaysDisabled(true);
    }
}
