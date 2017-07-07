package ie.adampurser.lottohub;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.LineViewHolder> {
    private static final String LOG_TAG_DEBUG = "Ticket.DEBUG";

    public static final int LOCATION_CHECKER = 10;
    public static final int LOCATION_SAVED_TICKET_FRAGMENT = 11;

    private static final int VIEW_TYPE_LOTTO = 20;
    private static final int VIEW_TYPE_EURO = 21;

    private Ticket mTicket;
    private String mDrawTitle;
    private int mLocation;
    private RecyclerViewOnLongItemClickListener mLongItemClickListener;

    public static class LineViewHolder extends RecyclerView.ViewHolder {
        CardView container;
        TextView mLetter;
        LinearLayout mNumContainer;

        LineViewHolder(View v) {
            super(v);
            container = (CardView) v.findViewById(R.id.ticketLineChecker);
            mLetter = (TextView) v.findViewById(R.id.ticketLineLetter);
            mNumContainer = (LinearLayout)v.findViewById(R.id.ticketLineNumContainer);
        }

        public void bindLine(TicketLine line) {
            mLetter.setText(String.valueOf(line.getLetter()) + ".");

            LinkedList<String> lineNums = line.getNums();

            TextView num;
            for(int i =0; i < line.getCapacity(); i++) {
                num = (TextView)mNumContainer.getChildAt(i);
                num.setText("");
            }
            String lineNum;
            for(int i = 0; i < lineNums.size(); i++) {
                num = (TextView)mNumContainer.getChildAt(i);
                lineNum = lineNums.get(i);
                if(lineNum.length() == 1) {
                    lineNum = "0" + lineNum;
                }
                num.setText(lineNum);
            }
        }
    }

    public TicketAdapter(Ticket ticket, int location) {
        mTicket = ticket;
        mDrawTitle = ticket.getDrawType();
        mLocation = location;
    }

    public TicketAdapter(Ticket ticket, int location, RecyclerViewOnLongItemClickListener listener) {
        mTicket = ticket;
        mDrawTitle = ticket.getDrawType();
        mLocation = location;
        mLongItemClickListener = listener;
    }

    @Override
    public LineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(mLocation == LOCATION_SAVED_TICKET_FRAGMENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ticket_line_prize_fragment, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ticket_line_checker_fragment, parent, false);
        }
        LinearLayout numContainer = (LinearLayout)view.findViewById(R.id.ticketLineNumContainer);
        if(viewType == VIEW_TYPE_EURO) {
            numContainer.removeView(numContainer.findViewById(R.id.ticketLineNumSix));
        }
        else {
            numContainer.removeView(numContainer.findViewById(R.id.ticketLineBonusOne));
            numContainer.removeView(numContainer.findViewById(R.id.ticketLineBonusTwo));
        }

        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineViewHolder holder, final int position) {
        holder.bindLine(mTicket.getLine(position));
        if(mLongItemClickListener != null) {
            holder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mLongItemClickListener.onLongItemClick(position, view);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTicket.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mDrawTitle) {
            case DrawType.EURO_MILLIONS:
                return VIEW_TYPE_EURO;
            default:
                return VIEW_TYPE_LOTTO;
        }
    }
}

