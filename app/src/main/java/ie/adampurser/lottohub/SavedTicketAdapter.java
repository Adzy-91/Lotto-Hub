package ie.adampurser.lottohub;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SavedTicketAdapter extends RecyclerView.Adapter<SavedTicketAdapter.SavedTicketViewHolder> {
    private static final String LOG_TAG_DEBUG = "SavedTicketAdapter";
    public static final String LOCATION_SAVED_TICKET_FRAGMENT = "locationFragment";
    public static final String LOCATION_SAVED_TICKET_DIALOG = "locationDialog";

    private ArrayList<Ticket> mTickets;
    private RecyclerViewOnItemClickListener mItemClickListener;
    private RecyclerViewOnLongItemClickListener mLongItemClickListener;
    private String mLocation;

    public static class SavedTicketViewHolder extends RecyclerView.ViewHolder {
        CardView container;
        TextView lineInfo;

        SavedTicketViewHolder(View v) {
            super(v);

            container = (CardView) v.findViewById(R.id.cardViewSavedTicket);
            lineInfo = (TextView) v.findViewById(R.id.savedTicketLineInfo);
        }

        public void bindSavedTicket(Ticket ticket) {
            int ticketSize = ticket.size();
            String s;
            switch (ticketSize) {
                case 1:
                    s = " Line";
                    break;
                default:
                    s = " Lines";
            }
            lineInfo.setText(String.valueOf(ticketSize) + s);
        }
    }

    public SavedTicketAdapter(ArrayList<Ticket> tickets,
                              RecyclerViewOnItemClickListener itemClickListener,
                              RecyclerViewOnLongItemClickListener longItemClickListener,
                              String location) {
        mTickets = tickets;
        mItemClickListener = itemClickListener;
        mLongItemClickListener = longItemClickListener;
        mLocation = location;
    }

    @Override
    public SavedTicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SavedTicketViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.saved_ticket_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(SavedTicketViewHolder holder, final int position) {
        if(mLocation.equals(LOCATION_SAVED_TICKET_FRAGMENT)) {
            holder.lineInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        holder.bindSavedTicket(mTickets.get(position));

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v, position);
            }
        });

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
        return mTickets.size();
    }
}
