package ie.adampurser.lottohub;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SavedTicketFragment extends Fragment {
    private static final String KEY_EXTRA = "savedTicketExtra";
    private static final String LOG_TAG_DEBUG = "savedTicketFragment";

    private RecyclerView mRecyclerView;
    private TextView mEmptyListView;

    private String mDrawType;
    private SavedTicketAdapter mAdapter;
    private ArrayList<Ticket> mTickets;
    private TicketJSONSerializer mSerializer;
    private Ticket mRemovedTicket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawType = getArguments().getString(KEY_EXTRA);

        mSerializer = new TicketJSONSerializer(getContext());

        try {
            mTickets = mSerializer.loadTickets(mDrawType);
        }catch (Exception e) {
            Log.e(LOG_TAG_DEBUG, e.getMessage());
            mTickets = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_saved_tickets, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewSavedTicketFragment);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        // Show ticket
        RecyclerViewOnItemClickListener onItemClickListener =
                new RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TicketAdapter lineAdapter = new TicketAdapter(
                                mTickets.get(position),
                                TicketAdapter.LOCATION_SAVED_TICKET_FRAGMENT
                        );
                        CardView ticketView = (CardView) inflater
                                .inflate(R.layout.ticket, null, false);
                        RecyclerView ticketRecyclerView = (RecyclerView)
                                ticketView.findViewById(R.id.recyclerViewTicket);
                        ticketRecyclerView.setLayoutManager(new LinearLayoutManager(
                                getContext(), LinearLayoutManager.VERTICAL, false
                        ));
                        ticketRecyclerView.setAdapter(lineAdapter);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(ticketView);
                        builder.show();

                    }
                };

        // Show delete ticket dialog
        RecyclerViewOnLongItemClickListener onLongItemClickListener =
                new RecyclerViewOnLongItemClickListener() {
                    @Override
                    public void onLongItemClick(final int position, View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogAccent);
                        builder.setTitle("Delete ticket?");
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removeTicket(position);
                                mAdapter.notifyItemRemoved(position);
                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        builder.show();
                    }
                };

        mAdapter = new SavedTicketAdapter(mTickets, onItemClickListener,
                onLongItemClickListener, SavedTicketAdapter.LOCATION_SAVED_TICKET_FRAGMENT);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        removeTicket(viewHolder.getAdapterPosition());
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mEmptyListView = (TextView) v.findViewById(R.id.savedTicketFragmentEmptyView);
        if(mTickets.size() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
        }

        return v;
    }

    public void removeTicket(final int position) {
        mRemovedTicket = mTickets.get(position);
        mTickets.remove(position);
        mAdapter.notifyItemRemoved(position);
        try {
            mSerializer.removeTicket(position, mDrawType);
        } catch (Exception e) {
            Log.d(LOG_TAG_DEBUG, e.getMessage());
        }

        // undo removed ticket
        View.OnClickListener snackBarListener = new View.OnClickListener() {
            @Override
            // undo removeFirstOccurrence ticket
            public void onClick(View v) {
                mTickets.add(position, mRemovedTicket);
                if(mTickets.size() == 1) {
                    mEmptyListView.setVisibility(View.GONE);
                }
                try {
                    mSerializer.saveTickets(mTickets.toArray(new Ticket[mTickets.size()]), mDrawType);
                }catch (Exception e) {
                    Log.d(LOG_TAG_DEBUG, e.getMessage());
                }

                mAdapter.notifyItemInserted(position);
            }
        };
        Snackbar snackbar = Snackbar
                .make(mRecyclerView,
                        R.string.snack_bar_remove_ticket_text,
                        Snackbar.LENGTH_LONG
                )
                .setAction(R.string.snack_bar_remove_line_undo, snackBarListener)
                .setActionTextColor(getResources().getColor(R.color.accent));
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(getResources().getColor(R.color.snack_bar_text));
        snackbar.show();
        if(mTickets.size() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
        }
    }

    public static SavedTicketFragment newInstance(String drawTitle) {
        Bundle args = new Bundle();
        args.putString(KEY_EXTRA, drawTitle);

        SavedTicketFragment fragment = new SavedTicketFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
