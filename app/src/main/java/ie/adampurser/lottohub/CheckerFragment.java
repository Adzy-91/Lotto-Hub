package ie.adampurser.lottohub;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class CheckerFragment extends Fragment implements OnDrawDateChangedListener {
    private static final String LOG_TAG_DEBUG = "checkerFragment.DEBUG";
    private static final String KEY_DRAW_TITLE = "checkerFragmentKey";

    private static final int TABLE_SIZE_LOTTO = 47;
    private static final int TABLE_SIZE_DAILY = 39;
    private static final int TABLE_SIZE_EURO = 50;
    private static final int NUM_OF_ROWS_LOTTO = 6;
    private static final int NUM_OF_ROWS_DAILY = 5;
    private static final int NUM_OF_ROWS_EURO = 6;
    private static final int NUM_OF_ITEMS_PER_ROW = 9;
    private static final int NUM_OF_ITEMS_IN_LAST_ROW_LOTTO = 2;
    private static final int NUM_OF_ITEMS_IN_LAST_ROW_DAILY = 3;
    private static final int NUM_OF_ITEMS_IN_LAST_ROW_EURO = 5;
    private static final int MAX_NUM_OF_LINES = 6;

    private ActionBar mToolbar;
    private LinearLayout mTable;
    private FloatingActionButton mFabAddLine;
    private CardView mButtonCheck;
    private RecyclerView mTicketLinesRecyclerView;
    private TicketAdapter mTicketAdapter;
    private TextView mEmptyListView;

    private String mDrawType;
    private Ticket mTicket;
    private ArrayList<String> mTableNums;
    private int mTableSize;
    private int mNumOfCompletedLines = 0;
    private TicketLine mRemovedLine;
    private ArrayList<Ticket> mSavedTickets;
    private OnNavSelectionChangedListener mOnNavSelectionChangedListener;
    private Date mCurrentDrawDate;
    private Date mLatestDrawDate;
    private boolean mUsedSavedTicket = false;
    private int mDrawsToCheckCurrentIndex = 0;
    private int mNumOfDrawsToCheck = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDrawType = getArguments().getString(KEY_DRAW_TITLE);
        ResultJSONSerializer serializer = new ResultJSONSerializer(getContext());
        try {
            mCurrentDrawDate = serializer.loadCompleteResult(mDrawType)[0].getDate();
            mLatestDrawDate = mCurrentDrawDate;
        }catch(Exception e) {
            Log.e(LOG_TAG_DEBUG, e.getMessage());
        }


        if(mLatestDrawDate == null) {
            mLatestDrawDate = mCurrentDrawDate;
        }

        switch (mDrawType) {
            case DrawType.LOTTO:
                mTableSize = TABLE_SIZE_LOTTO;
                setTableNums();
                break;
            case DrawType.DAILY_MILLION:
                mTableSize = TABLE_SIZE_DAILY;
                setTableNums();
                break;
            case DrawType.EURO_MILLIONS:
                mTableSize = TABLE_SIZE_EURO;
                setTableNums();
        }

        TicketJSONSerializer mTicketJSONSerializer = new TicketJSONSerializer(getActivity());

        // Load saved tickets
        try {
            mSavedTickets = mTicketJSONSerializer.loadTickets(mDrawType);
            Log.i(LOG_TAG_DEBUG, "Loaded tickets");
        } catch (Exception e) {
            Log.e(LOG_TAG_DEBUG, "Error loading tickets");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_checker, container, false);

        mToolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(mToolbar != null) {
            mToolbar.setTitle("");
        }
        ((MainActivity)getActivity()).showSpinner();

        getActivity().findViewById(R.id.toolbarSpinner).setVisibility(View.VISIBLE);
        mOnNavSelectionChangedListener.setSelection(getResources().getInteger(R.integer.nav_index_checker));

        // start PrizeFragment to view prizes
        mButtonCheck = (CardView)view.findViewById(R.id.buttonCheckNumbers);
        mButtonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTicket == null) {
                    Toast.makeText(getContext(), R.string.toast_checker_no_results,
                            Toast.LENGTH_LONG).show();
                }
                else {
                    if(mNumOfCompletedLines < mTicket.size()) {
                        mTicket.removeLastLine();
                    }
                    if (mNumOfCompletedLines == 0) {
                        Toast.makeText(getActivity(), R.string.toast_checker_no_lines,
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // check if we need to add any extra dates to the ticket
                        if(mNumOfDrawsToCheck > 1) {
                            mTicket.getDates().clear();
                            mTicket.addDate(mLatestDrawDate);
                            Date previousDate = mLatestDrawDate;
                            for(int i = 1; i < mNumOfDrawsToCheck; i++) {
                                previousDate = Result.getPreviousDrawDate(mDrawType, previousDate);
                                mTicket.addDate(previousDate);
                            }
                        }
                        getFragmentManager().beginTransaction()
                                .remove(CheckerFragment.this)
                                .add(R.id.fragmentContainer, PrizeFragment.newInstance(
                                        mTicket,
                                        mUsedSavedTicket,
                                        PrizeFragment.PREVIOUS_FRAGMENT_CHECKER), null)
                                .commit();
                    }
                }
            }
        });

        mTable = (LinearLayout)view.findViewById(R.id.checkerTable);
        setTable();
        disableTableNums();

        mTicketLinesRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerViewLineContainer);
        mTicketLinesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mTicket = new Ticket(mDrawType, mCurrentDrawDate);
        mTicketAdapter = new TicketAdapter(mTicket,
                TicketAdapter.LOCATION_CHECKER,
                new RecyclerViewOnLongItemClickListener() {
                    @Override
                    public void onLongItemClick(final int position, View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(
                                getContext(),
                                R.style.DialogAccent
                        );
                        builder.setTitle("Delete line?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        removeLine(position);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                });
        mTicketLinesRecyclerView.setAdapter(mTicketAdapter);

        mEmptyListView = (TextView)view.findViewById(R.id.checkerEmptyListText);

        // remove ticket line by swiping
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                removeLine(position);
                resetTable();
                disableTableNums();

                // SnackBar
                View.OnClickListener snackBarListener = new View.OnClickListener() {
                    @Override
                    // undo removeFirstOccurrence line
                    public void onClick(View v) {
                        undoRemoveLine();
                    }
                };
                Snackbar snackbar = Snackbar
                        .make(mButtonCheck, R.string.snack_bar_remove_line_text, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snack_bar_remove_line_undo, snackBarListener)
                        .setActionTextColor(getResources().getColor(R.color.accent));
                View view = snackbar.getView();
                TextView tv = (TextView)
                        view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(getResources().getColor(R.color.snack_bar_text));
                snackbar.show();

                mUsedSavedTicket = false;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mTicketLinesRecyclerView);

        // click listener to add a new ticket line
        mFabAddLine = (FloatingActionButton)view.findViewById(R.id.fabButtonAddLine);
        mFabAddLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // line already added
                if (mTicket.size() > mNumOfCompletedLines) {
                    Toast.makeText(getActivity(), R.string.toast_checker_line_already_added, Toast.LENGTH_SHORT).show();
                }
                // max number of line reached
                else if(mNumOfCompletedLines == MAX_NUM_OF_LINES) {
                    Toast.makeText(getActivity(), R.string.toast_checker_max_num_of_lines, Toast.LENGTH_LONG).show();
                }
                // add a new line
                else {
                    if (mEmptyListView.getVisibility() == View.VISIBLE) {
                        mEmptyListView.setVisibility(View.GONE);
                        mTicketLinesRecyclerView.setVisibility(View.VISIBLE);
                    }

                    TicketLine line = new TicketLine(mDrawType);
                    mTicket.addLine(line);
                    mTicketAdapter.notifyItemInserted(mTicket.size()-1);
                    mTicketLinesRecyclerView.scrollToPosition(mTicketAdapter.getItemCount() - 1);
                    enableTableNums();
                    mUsedSavedTicket = false;
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_checker, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final AlertDialog.Builder builder;
        final AlertDialog dialog;
        switch (item.getItemId()) {
            case R.id.menuItemCalendar:
                if(AppUtils.hasInternetConnection(getContext())) {
                    CalendarFragment calendarFragment = CalendarFragment
                            .newInstance(mCurrentDrawDate, mLatestDrawDate, mDrawType);
                    calendarFragment.show(getChildFragmentManager(), CalendarFragment.FRAGMENT_ID);
                }
                else {
                    AppUtils.displayNoConnectionToast(getContext());
                }
                return true;

            case R.id.menuItemCheckerSettings:
                if(!AppUtils.hasInternetConnection(getContext())) {
                    AppUtils.displayNoConnectionToast(getContext());
                }
                else {
                    String[] itemTitles = getResources().getStringArray(R.array.checker_settings_items);
                    String[] items = new String[itemTitles.length + 1];
                    items[0] = AppUtils.getFormatDate(mCurrentDrawDate);
                    for (int i = 1; i < items.length; i++) {
                        items[i] = itemTitles[i - 1];
                    }

                    builder = new AlertDialog.Builder(getContext(), R.style.DialogAccent);
                    builder.setTitle("Draws to check");
                    builder.setSingleChoiceItems(items, mDrawsToCheckCurrentIndex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mDrawsToCheckCurrentIndex = i;
                            switch (i) {
                                case 0:
                                    mNumOfDrawsToCheck = 1;
                                    mTicket.getDates().clear();
                                    mTicket.addDate(mLatestDrawDate);
                                    break;
                                case 1:
                                    mNumOfDrawsToCheck = 2;
                                    break;
                                case 2:
                                    mNumOfDrawsToCheck = 4;
                                    break;
                                case 3:
                                    mNumOfDrawsToCheck = 8;
                                    break;
                            }
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }

                return true;

            case R.id.menuItemShowSavedTickets:
                RelativeLayout listViewContainer = (RelativeLayout) LayoutInflater.from(getActivity())
                        .inflate(R.layout.dialog_saved_tickets, null, false);
                final RecyclerView savedTicketList = (RecyclerView)
                        listViewContainer.findViewById(R.id.recyclerViewSavedTickets);
                final TextView emptyView = (TextView)
                        listViewContainer.findViewById(R.id.savedTicketEmptyView);
                if(mSavedTickets.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                    savedTicketList.setVisibility(View.VISIBLE);
                }

                builder = new AlertDialog.Builder(getActivity(), R.style.DialogDarkTextSecondary);
                builder.setView(listViewContainer);
                builder.setTitle("Saved Tickets");
                dialog = builder.create();
                savedTicketList.setLayoutManager(new LinearLayoutManager(
                        getContext(), LinearLayoutManager.VERTICAL, false
                ));

                RecyclerViewOnItemClickListener onItemClickListener =
                        new RecyclerViewOnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                mTicket.clearLines();
                                mTicket.setLines(mSavedTickets.get(position).getCopyOfLines());

                                mEmptyListView.setVisibility(View.GONE);
                                mTicketLinesRecyclerView.setVisibility(View.VISIBLE);

                                mTicketAdapter.notifyDataSetChanged();

                                mNumOfCompletedLines = mTicket.size();
                                disableTableNums();

                                mUsedSavedTicket = true;

                                dialog.dismiss();

                            }
                        };
                savedTicketList.setAdapter(new SavedTicketAdapter(
                            mSavedTickets, onItemClickListener,
                            null, SavedTicketAdapter.LOCATION_SAVED_TICKET_DIALOG)
                );

                dialog.show();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mOnNavSelectionChangedListener = (OnNavSelectionChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    private void setTableNums() {
        mTableNums = new ArrayList<>(mTableSize);
        for(int i = 0; i < mTableSize; i++) {
            mTableNums.add(String.valueOf(i + 1));
        }
    }

    private void undoRemoveLine() {
        if(mRemovedLine.isCompleted()) {
            mNumOfCompletedLines++;
        }

        int position = mRemovedLine.getLinePosition();

        if(mTicket.size() == 0) {
            mEmptyListView.setVisibility(View.GONE);
        }

        mTicket.addLine(position, mRemovedLine);
        mTicketAdapter.notifyItemInserted(position);

        // Check if first line was removed
        if(position == 0 && !mTicket.getCurrentLine().isCompleted()) {
            enableTableNums();
            LinkedList<String> completedLineNums = mTicket.getCurrentLine().getNums();
            setPressedTableNums(completedLineNums);
        }

        mTicketAdapter.notifyDataSetChanged();
    }

    private void removeLine(int position) {
        if (mTicket.getLine(position).isCompleted()) {
            mNumOfCompletedLines--;
        }
        // backup line
        mRemovedLine = mTicket.getLine(position);
        mTicket.removeLine(position);
        mTicketAdapter.notifyItemRemoved(position);

        // Update table
        if (position == 0) {
            resetTable();
            disableTableNums();
        }

        if (mTicket.size() == 0) {
            mEmptyListView.setVisibility(View.VISIBLE);
        }
    }

    private void setTable() {
        switch(mTableSize) {
            case TABLE_SIZE_LOTTO:
                for(int i = 0; i < NUM_OF_ROWS_LOTTO - 1 ; i++) {
                    mTable.addView(getTableRow(i+1));
                }
                mTable.addView(getLastTableRow(NUM_OF_ROWS_LOTTO));
                break;
            case TABLE_SIZE_DAILY:
                for(int i = 0; i < NUM_OF_ROWS_DAILY - 1; i++) {
                    mTable.addView(getTableRow(i+1));
                }
                mTable.addView(getLastTableRow(NUM_OF_ROWS_DAILY));
                break;
            case TABLE_SIZE_EURO:
                for(int i = 0; i < NUM_OF_ROWS_EURO - 1; i++) {
                    mTable.addView(getTableRow(i+1));
                }
                mTable.addView(getLastTableRow(NUM_OF_ROWS_EURO));
                break;
        }
    }


    /* removes all views from the table and repopulates the table */
    private void resetTable() {
        mTable.removeAllViews();
        setTable();
    }

    /* sets a click listener on a table number */
    private void setTableNumListener(final CardView container) {
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView tableNumTextView = (TextView)
                        container.findViewById(R.id.textViewCheckerTableNum);
                final String tableNumValue = (String) tableNumTextView.getText();

                // Remove number from line
                if (getCurrentLine().contains(tableNumValue) && !onEuroBonusNums()) {
                    container.setSelected(false);
                    tableNumTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                    getCurrentLine().removeFirstOccurrence(tableNumValue);
                }

                // Remove number euro millions bonus
                else if (onEuroBonusNums() && container.isSelected()) {
                    for (int i = getCurrentLineSize() - 1; i >= 5; i--) {
                        if (getCurrentLine().getNum(i).equals(tableNumValue)) {
                            getCurrentLine().remove(i);
                            container.setSelected(false);
                            tableNumTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                        }
                    }
                }

                // Add number to current line. Update colors
                else {
                    getCurrentLine().add(tableNumValue);
                    container.setSelected(true);
                    tableNumTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

                    // Euro millions bonus num check
                    if (mTableSize == TABLE_SIZE_EURO && getCurrentLineSize() == 5) {
                        resetTable();
                        disableTableNums(12, mTableSize - 1);
                    }

                }

                // Line full
                if (getCurrentLineSize() == getCurrentLine().getCapacity()) {
                    Toast.makeText(getActivity(), R.string.toast_checker_line_added, Toast.LENGTH_SHORT).show();
                    resetTable();
                    disableTableNums();
                    mNumOfCompletedLines++;
                }

                mTicketAdapter.notifyDataSetChanged();
            }
        });
    }

    /* returns whether user is on Euro Millions bonus numbers for the current line */
    private boolean onEuroBonusNums() {
        return mTableSize == TABLE_SIZE_EURO && (getCurrentLine().getCurrentSize() == 5
                || getCurrentLine().getCurrentSize() == 6);
    }

    private TicketLine getCurrentLine() {
        return mTicket.getCurrentLine();
    }

    private int getCurrentLineSize() {
        return mTicket.getCurrentLine().getCurrentSize();
    }

    /* enable table numbers */
    private void enableTableNums() {
        LinearLayout tableRow;
        CardView container;
        TextView num;
        int tableItemIndex;
        int tableRowIndex = 0;

        for (int i = 0; i < mTableSize; i++) {
            tableItemIndex = i % NUM_OF_ITEMS_PER_ROW;
            tableRow = (LinearLayout) mTable.getChildAt(tableRowIndex);

            container = (CardView) tableRow.getChildAt(tableItemIndex);
            container.setClickable(true);
            container.setEnabled(true);

            num = (TextView) container.findViewById(R.id.textViewCheckerTableNum);
            num.setEnabled(true);

            if (tableItemIndex == NUM_OF_ITEMS_PER_ROW - 1) {
                tableRowIndex++;
            }
        }
    }

    /* disable table numbers */
    private void disableTableNums() {
        LinearLayout tableRow;
        CardView container;
        TextView num;
        int tableItemIndex;
        int tableRowIndex = 0;

        for(int i = 0; i < mTableSize; i++) {
            tableItemIndex = i%NUM_OF_ITEMS_PER_ROW;
            tableRow = (LinearLayout)mTable.getChildAt(tableRowIndex);

            container = (CardView)tableRow.getChildAt(tableItemIndex);
            container.setClickable(false);
            container.setEnabled(false);

            num = (TextView) container.findViewById(R.id.textViewCheckerTableNum);
            num.setEnabled(false);

            if(tableItemIndex == NUM_OF_ITEMS_PER_ROW - 1) {
                tableRowIndex++;
            }
        }
    }

    /* disable table numbers between two given indexes */
    private void disableTableNums(int indexFrom, int indexTo) {
        LinearLayout tableRow;
        CardView container;
        TextView tableNum;
        int tableItemIndex;
        int tableRowIndex = 0;

        // Euro Millions Plus
        if(indexFrom == 12) {
            tableRowIndex = 1;
        }
        for(int i = indexFrom; i <= indexTo; i++) {
            tableItemIndex = i % NUM_OF_ITEMS_PER_ROW;
            tableRow = (LinearLayout) mTable.getChildAt(tableRowIndex);

            container = (CardView)tableRow.getChildAt(tableItemIndex);
            container.setClickable(false);
            tableNum = (TextView) container.findViewById(R.id.textViewCheckerTableNum);
            tableNum.setTextColor(getResources()
                    .getColor(R.color.checker_table_num_text_disabled));

            if(tableItemIndex == NUM_OF_ITEMS_PER_ROW - 1) {
                tableRowIndex++;
            }
        }
    }

    private void setPressedTableNums(LinkedList<String> pressedNums) {
        LinearLayout tableRow;
        TextView tableNum;
        int tableItemIndex;
        int tableRowIndex = 0;

        for(int i = 0; i < mTableSize; i++) {
            tableItemIndex = i%NUM_OF_ITEMS_PER_ROW;
            tableRow = (LinearLayout)mTable.getChildAt(tableRowIndex);

            tableNum = (TextView)tableRow.getChildAt(tableItemIndex);
            CharSequence num = tableNum.getText();
            if(pressedNums.contains(num.toString())) {
                tableNum.setTextColor(getResources()
                        .getColor(R.color.checker_table_num_text_pressed));
                tableNum.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            }

            if(tableItemIndex == NUM_OF_ITEMS_PER_ROW - 1) {
                tableRowIndex++;
            }
        }
    }

    /* returns a table row */
    private LinearLayout getTableRow(int rowNum) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        LinearLayout tableRow = (LinearLayout)layoutInflater
                .inflate(R.layout.checker_table_row, mTable, false);

        setTableRow(tableRow, NUM_OF_ITEMS_PER_ROW, rowNum);

        return tableRow;
    }

    /* returns the last table row */
    private LinearLayout getLastTableRow(int rowNum) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        LinearLayout tableRow = (LinearLayout)layoutInflater
                .inflate(R.layout.checker_table_row, mTable, false);

        if(mTableSize == TABLE_SIZE_LOTTO) {
            setTableRow(tableRow, NUM_OF_ITEMS_IN_LAST_ROW_LOTTO, rowNum);
        }
        else if(mTableSize == TABLE_SIZE_DAILY) {
            setTableRow(tableRow, NUM_OF_ITEMS_IN_LAST_ROW_DAILY, rowNum);
        }
        else {
            setTableRow(tableRow, NUM_OF_ITEMS_IN_LAST_ROW_EURO, rowNum);
        }

        return tableRow;
    }

    /*
     * set numbers on a given table row
     * if the given row is the last row in the table it sets the remaining TextViews invisible
     */
    private void setTableRow(LinearLayout tableRow, int visibleNums, int rowNum) {
        CardView container;
        TextView numText;

        if(mTableNums.size() == 0) {
            setTableNums();
        }

        for(int i = 0; i < visibleNums; i++) {
            container = (CardView)tableRow.getChildAt(i);
            numText = (TextView) container.findViewById(R.id.textViewCheckerTableNum);
            numText.setText(mTableNums.get(
                            ((rowNum - 1) * NUM_OF_ITEMS_PER_ROW) + i)
            );
            setTableNumListener(container);
        }

        if(visibleNums < NUM_OF_ITEMS_PER_ROW) {
            for(int i = visibleNums; i < NUM_OF_ITEMS_PER_ROW; i++) {
                tableRow.getChildAt(i).setVisibility(View.INVISIBLE);
            }
        }
    }

    public static CheckerFragment newInstance(String drawType) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_DRAW_TITLE, drawType);

        CheckerFragment fragment = new CheckerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onDrawDateChanged(Date newDate) {
        mCurrentDrawDate = newDate;
        mTicket.getDates().clear();
        mTicket.addDate(newDate);
    }
}
