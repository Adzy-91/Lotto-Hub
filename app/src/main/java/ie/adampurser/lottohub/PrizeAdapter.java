package ie.adampurser.lottohub;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

public class PrizeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG_DEBUG = "PrizeAdapter.DEBUG";
    private static final int VIEW_TYPE_EURO = 10;
    private static final int VIEW_TYPE_EURO_PLUS = 11;
    private static final int VIEW_TYPE_LOTTO = 12;
    private static final int VIEW_TYPE_TICKET = 13;
    private static final int VIEW_TYPE_MESSAGE = 14;
    private static final int VIEW_TYPE_DIVIDER = 15;
    // +3 for ticket, section header and message
    private static final int NUM_OF_LIST_EXTRAS = 3;
    private static final String EURO_SYMBOL = "\\u20ac";

    private Context mContext;
    private LinkedList<Prize> mPrizes;
    private Ticket mTicket;

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView title;
        TextView footer;

        public TicketViewHolder(View v) {
            super(v);

            recyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewTicket);
            title = (TextView) v.findViewById(R.id.ticketTitle);
            footer = (TextView) v.findViewById(R.id.ticketFooter);
        }

        public void bindTicket(Ticket ticket, Context context) {
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false
            ));

            TicketAdapter ticketAdapter = new TicketAdapter(
                    ticket, TicketAdapter.LOCATION_SAVED_TICKET_FRAGMENT);
            recyclerView.setAdapter(ticketAdapter);

            title.setText(context.getResources().getString(R.string.ticket_title));

            footer.setVisibility(View.VISIBLE);
            footer.setText(context.getResources().getString(R.string.ticket_footer_text)
                    + " " + getNumOfResultsText(ticket));
        }

        /* get ticket footer which displays which draws are being checked */
        private String getNumOfResultsText(Ticket ticket){
            String footerText;
            switch (ticket.getDates().size()) {
                case 1:
                    footerText = AppUtils.getFormatDate(ticket.getDates().get(0));
                    break;
                default:
                    int numOfDates = ticket.getDates().size();
                    footerText = AppUtils.getFormatDate(ticket.getDates().get(numOfDates - 1)) +
                            " - " +
                            AppUtils.getFormatDate(ticket.getDates().get(0));
            }

            return footerText;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView title;
        TextView message;
        Context context;

        public MessageViewHolder(View v, Context context) {
            super(v);

            cardView = (CardView) v.findViewById(R.id.cardViewPrizeMessage);
            title = (TextView) v.findViewById(R.id.prizeMessageTitle);
            message = (TextView) v.findViewById(R.id.prizeMessageMessage);

            this.context = context;
        }

        public void setMessage(boolean isWinner, int numOfPrizes) {
            if(isWinner) {
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.ticket));

                title.setTextColor(context.getResources().getColor(R.color.white));
                title.setText(context.getResources().getString(R.string.prize_winner_message_title));

                String messageString = "Congratulations you've won ";
                if(numOfPrizes == 1) {
                    messageString += "a prize!";
                }
                else {
                    messageString += numOfPrizes + " prizes!";
                }
                message.setText(messageString);
                message.setTextColor(context.getResources().getColor(R.color.light_text_secondary));
            }
        }
    }

    public static class DividerViewHolder extends RecyclerView.ViewHolder{
        public DividerViewHolder(View v) {
            super(v);
        }
    }

    public static class PrizeViewHolder extends RecyclerView.ViewHolder {
        Context context;
        CardView container;
        TextView drawInfo;
        LinearLayout numContainer;
        TextView prize;
        boolean firstRun = true;

        PrizeViewHolder(View v, Context context) {
            super(v);

            container = (CardView) v.findViewById(R.id.cardViewPrizeContainer);
            drawInfo = (TextView) v.findViewById(R.id.textViewPrizeDrawInfo);
            numContainer = (LinearLayout) v.findViewById(R.id.linLayoutPrizeWinningNums);
            prize = (TextView) v.findViewById(R.id.textViewPrize);
            this.context = context;
        }

        public void bindPrize(Prize p) {
            if (!firstRun) {
                container.setVisibility(View.VISIBLE);
            }

            drawInfo.setText(" (" + p.getLineLetter() + ") " + p.getDrawTitle()
                    + " | " + AppUtils.getFormatDate(p.getDate()
            ));

            String[] winningNums = p.getWinningNums();
            ArrayList<String> matchingLineNums = p.getMatchingNums();
            TextView num;

            for (int i = 0; i < winningNums.length; i++) {
                num = (TextView) numContainer.getChildAt(i);
                num.setText(winningNums[i]);
                if (matchingLineNums.contains(winningNums[i])) {
                    num.setBackgroundResource(R.drawable.shape_prize_winning_num_selected);
                    num.setTextColor(context.getResources().getColor(R.color.white));
                }
                else {
                    num.setBackgroundResource(R.drawable.shape_prize_winning_num);
                    num.setTextColor(context.getResources().getColor(R.color.prize_winning_ball_text));
                }
            }

            String[] bonusNums = p.getBonusNums();
            if (bonusNums != null) {
                ArrayList<String> matchingBonusNums = p.getMatchingBonusNums();
                int count = 0;
                for (int i = winningNums.length; i < numContainer.getChildCount(); i++) {
                    num = (TextView) numContainer.getChildAt(i);
                    num.setText(bonusNums[count]);
                    if (matchingBonusNums.contains(bonusNums[count])) {
                        num.setBackgroundResource(R.drawable.shape_prize_bonus_num_selected);
                        num.setTextColor(context.getResources().getColor(R.color.primary));
                    }
                    else {
                        num.setBackgroundResource(R.drawable.shape_prize_bonus_num);
                        num.setTextColor(context.getResources().getColor(R.color.prize_winning_ball_background));
                    }
                    count++;
                }
            }

            prize.setText(context.getString(R.string.prize, p.getPrize()));
        }
    }

    public PrizeAdapter(Context context, LinkedList<Prize> prizes, Ticket ticket) {
        mContext = context;
        mPrizes = prizes;
        mTicket = ticket;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_TICKET) {
            return new TicketViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.ticket, parent, false));
        }

        if(viewType == VIEW_TYPE_DIVIDER) {
            return new DividerViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.recycler_view_divider, parent, false));
        }

        if(viewType == VIEW_TYPE_MESSAGE) {
            return new MessageViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.prize_message, parent, false), mContext);
        }

        CardView c = (CardView) LayoutInflater.from(mContext)
                .inflate(R.layout.prize, parent, false);
        LinearLayout numContainer = (LinearLayout) c.findViewById(R.id.linLayoutPrizeWinningNums);

        switch (viewType) {
            case VIEW_TYPE_EURO:
                numContainer.removeView(numContainer.findViewById(R.id.prizeWinningNumSix));
                break;
            case VIEW_TYPE_EURO_PLUS:
                numContainer.removeView(numContainer.findViewById(R.id.prizeWinningNumSix));
                numContainer.removeView(numContainer.findViewById(R.id.prizeBonusNumOne));
                numContainer.removeView(numContainer.findViewById(R.id.prizeBonusNumTwo));
                break;
            case VIEW_TYPE_LOTTO:
                numContainer.removeView(numContainer.findViewById(R.id.prizeBonusNumTwo));
        }

        return new PrizeViewHolder(c, parent.getContext());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TicketViewHolder) {
            TicketViewHolder ticketViewHolder = (TicketViewHolder) holder;
            ticketViewHolder.bindTicket(mTicket, mContext);
        }
        else if(holder instanceof MessageViewHolder) {
            MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            messageViewHolder.setMessage(mPrizes.size() > 0, mPrizes.size());
        }
        else if(holder instanceof DividerViewHolder) {
            // do nothing
        }
        else {
            PrizeViewHolder prizeViewHolder = (PrizeViewHolder) holder;
            prizeViewHolder.bindPrize(mPrizes.get(position - NUM_OF_LIST_EXTRAS));
        }
    }

    @Override
    public int getItemCount() {
        return mPrizes.size() + NUM_OF_LIST_EXTRAS;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_TICKET;
        }

        if(position == 1) {
            return VIEW_TYPE_DIVIDER;
        }

        if(position == 2) {
            return VIEW_TYPE_MESSAGE;
        }

        String drawTitle = mPrizes.get(position - NUM_OF_LIST_EXTRAS).getDrawTitle();
        switch (drawTitle) {
            case DrawType.EURO_MILLIONS:
                return VIEW_TYPE_EURO;
            case DrawType.EURO_MILLIONS_PLUS:
                return VIEW_TYPE_EURO_PLUS;
            default:
                return VIEW_TYPE_LOTTO;
        }
    }
}

