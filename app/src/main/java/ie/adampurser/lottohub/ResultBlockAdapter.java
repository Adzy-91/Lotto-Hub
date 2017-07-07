package ie.adampurser.lottohub;

import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ResultBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ADAPTER_LOCATION_MAIN = 20;
    public static final int ADAPTER_LOCATION_RESULTS = 21;

    private static final int VIEW_TYPE_LOTTO = 11;
    private static final int VIEW_TYPE_EURO = 12;
    private static final int VIEW_TYPE_EURO_PLUS = 13;
    private static final int VIEW_TYPE_RAFFLE_NUMBER = 14;
    private static final int VIEW_TYPE_DATE = 15;
    private static final int VIEW_TYPE_FOOTER = 16;

    private Result[] mResults;
    private RecyclerViewOnItemClickListener mRecyclerClickListener;
    private int mAdapterLocation;

    public static class ResultBlockViewHolder extends RecyclerView.ViewHolder {
        CardView container;
        TextView title;
        LinearLayout numContainer;
        ImageView arrow;
        TextView drawInfo;

        public ResultBlockViewHolder(View v) {
            super(v);
            container = (CardView) v.findViewById(R.id.cardViewResultBlock);
            title = (TextView) v.findViewById(R.id.resultBlockTitle);
            numContainer = (LinearLayout) v.findViewById(R.id.resultBlockNumsContainer);
            arrow = (ImageView) v.findViewById(R.id.resultBlockArrow);
            drawInfo = (TextView) v.findViewById(R.id.resultBlockDrawInfo);
        }

        public void bindResultBlock(Result result, int adapterLocation) {
            title.setText(result.getDrawType());

            if(result.getWinningNums() == null) {
                return;
            }

            TextView num;
            for(int i = 0; i < result.getWinningNums().length; i++) {
                num = (TextView)numContainer.getChildAt(i);
                num.setText(result.getWinningNums()[i]);
            }

            if(result.getBonusNums() != null) {
                for (int i = 0; i < result.getBonusNums().length; i++) {
                    num = (TextView) numContainer.getChildAt(result.getWinningNums().length + i);
                    num.setText(result.getBonusNumber(i));
                }
            }
            switch (adapterLocation) {
                case ADAPTER_LOCATION_MAIN:
                    drawInfo.setText(AppUtils.getFormatDate(result.getDate())
                            + " | " + result.getFormatJackpot());
                    break;
                case ADAPTER_LOCATION_RESULTS:
                    arrow.setVisibility(View.GONE);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    drawInfo.setVisibility(View.GONE);
            }
        }
    }

    public ResultBlockAdapter(Result[] results,
                              RecyclerViewOnItemClickListener listener,
                              int adapterLocation) {
        mResults = results;
        mRecyclerClickListener = listener;
        mAdapterLocation = adapterLocation;
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        public DateViewHolder(View v) {
            super(v);

            date = (TextView) v.findViewById(R.id.dateResultFragment);
        }

        public void bindDate(String dateString) {
            date.setText(dateString);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout footer;

        public FooterViewHolder(View v) {
            super(v);

            footer = (LinearLayout) v.findViewById(R.id.recyclerViewFooter);
        }

        public void bindFooter() {
            footer.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_DATE) {
            return new DateViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.date_result_fragment, parent, false));
        }

        if(viewType == VIEW_TYPE_FOOTER) {
            return new FooterViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_dummy, parent, false));
        }

        CardView container = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_block, parent, false);
        LinearLayout numContainer = (LinearLayout)
                container.findViewById(R.id.resultBlockNumsContainer);
        switch (viewType) {
            case VIEW_TYPE_EURO:
                numContainer.removeViewAt(5);
                break;
            case VIEW_TYPE_RAFFLE_NUMBER:
                numContainer.removeViewAt(7);
                numContainer.removeViewAt(6);
                numContainer.removeViewAt(5);
                numContainer.removeViewAt(4);
                break;
            case VIEW_TYPE_EURO_PLUS:
                numContainer.removeViewAt(7);
                numContainer.removeViewAt(6);
                numContainer.removeViewAt(5);
                break;
            case VIEW_TYPE_LOTTO:
                numContainer.removeViewAt(7);
                break;
        }

        return new ResultBlockViewHolder(container);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof ResultBlockViewHolder) {

            ResultBlockViewHolder resultBlockViewHolder = (ResultBlockViewHolder) holder;
            resultBlockViewHolder.bindResultBlock(mResults[getCorrectPosition(position)], mAdapterLocation);

            resultBlockViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerClickListener.onItemClick(v, getCorrectPosition(position));
                        }
                    }, 200);
                }
            });
        }
        else if(holder instanceof DateViewHolder) {

            DateViewHolder dateViewHolder = (DateViewHolder) holder;

            // set the date info for the draw
            String dateInfo = AppUtils.getFormatDate(mResults[0].getDate());

            // addLine time of draw in case of Daily Million which has two draws per day
            if(mResults[0].getDrawType().equals(DrawType.DAILY_MILLION)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(ResultParser.DATE_FORMAT_TIME, Locale.ENGLISH);
                dateInfo += " at " + dateFormat.format(mResults[0].getDate());
            }

            dateViewHolder.bindDate(dateInfo);
        }
        else if(holder instanceof FooterViewHolder) {

            FooterViewHolder footerViewHolder = (FooterViewHolder)holder;
            footerViewHolder.bindFooter();
        }
    }

    @Override
    public int getItemCount() {

        if(mAdapterLocation == ADAPTER_LOCATION_RESULTS) {

            // +2 for date and footer
            return mResults.length+2;
        }

        // +1 for footer
        return mResults.length+1;
    }

    @Override
    public int getItemViewType(int position) {

        // Handle footer
        if(mAdapterLocation == ADAPTER_LOCATION_MAIN && position == mResults.length) {
            return VIEW_TYPE_FOOTER;
        }

        if(mAdapterLocation == ADAPTER_LOCATION_RESULTS && position == 0) {
            return VIEW_TYPE_DATE;
        }

        if(mAdapterLocation == ADAPTER_LOCATION_RESULTS && position == mResults.length + 1) {
            return VIEW_TYPE_FOOTER;
        }

        String drawType = mResults[getCorrectPosition(position)].getDrawType();

        switch (drawType) {
            case DrawType.EURO_MILLIONS:
                return VIEW_TYPE_EURO;
            case DrawType.EURO_MILLIONS_PLUS:
                return VIEW_TYPE_EURO_PLUS;
            case DrawType.LOTTO_PLUS_RAFFLE:
                return VIEW_TYPE_RAFFLE_NUMBER;
            default:
                return VIEW_TYPE_LOTTO;
        }
    }

    private int getCorrectPosition(int position) {
        if(mAdapterLocation == ADAPTER_LOCATION_RESULTS) {
            return position - 1;
        }
        else {
            return position;
        }
    }
}
