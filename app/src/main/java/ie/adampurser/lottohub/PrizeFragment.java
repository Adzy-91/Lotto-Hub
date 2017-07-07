package ie.adampurser.lottohub;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class PrizeFragment extends Fragment {
    private static final String LOG_TAG_DEBUG = "PrizeFragment";
    private static final String KEY_EXTRA_TICKET = "checkerResultKeyTicket";
    private static final String KEY_USED_SAVED_TICKET = "CheckerKeyUsedSavedTicket";
    private static final String KEY_PREVIOUS_FRAGMENT = "previousFragment";
    public static final int PREVIOUS_FRAGMENT_MAIN = 20;
    public static final int PREVIOUS_FRAGMENT_CHECKER = 21;

    private ActionBar mToolbar;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mProgressBarText;

    private PrizeAdapter mPrizeAdapter;
    private Ticket mTicket;
    private LinkedList<Prize> mPrizes = null;
    private ArrayList<Result> mResultsToCheck;
    private String mDrawType;
    private boolean mClickedSavedTicket = false;
    private boolean mUsedSavedTicket;
    private int mPreviousFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mTicket = (Ticket) getArguments().getSerializable(KEY_EXTRA_TICKET);
        mDrawType = mTicket.getDrawType();

        for(int i = 0; i < mTicket.size(); i++) {
            Log.d(LOG_TAG_DEBUG, "Line " + String.valueOf(mTicket.size()));
            for(int j = 0; j < mTicket.getLine(i).getCurrentSize(); j++) {
                Log.d(LOG_TAG_DEBUG, mTicket.getLine(i).getNum(j));
            }
        }

        mUsedSavedTicket = getArguments().getBoolean(KEY_USED_SAVED_TICKET);
        mPreviousFragment = getArguments().getInt(KEY_PREVIOUS_FRAGMENT);

        mPrizes = new LinkedList<>();
        mPrizeAdapter = new PrizeAdapter(getContext(), mPrizes, mTicket);

        mResultsToCheck = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prize, container, false);

        mToolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(mToolbar != null) {
            mToolbar.setTitle(mDrawType);
        }

        // toggle drawer to hamburger icon and hide the spinner
        ((MainActivity)getActivity()).hideSpinner();
        ((MainActivity)getActivity()).toggleDrawer();

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBarPrizeFragment);
        mProgressBarText = (TextView) v.findViewById(R.id.progressBarTextPrizeFragment);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewPrizes);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new ScaleInAnimator());

        new DownloadResults().execute();

        return v;
    }

    /* adds prizes to list and updates RecyclerView accordingly */
    private void checkTicket() {
        Checker checker = null;
        switch (mDrawType) {
            case DrawType.LOTTO:
                checker = new LottoChecker();
                break;
            case DrawType.DAILY_MILLION:
                checker = new DailyMillionChecker();
                break;
            case DrawType.EURO_MILLIONS:
                checker = new EuroMillionsChecker();
        }

        if(checker != null) {
            for (int i = 0; i < mResultsToCheck.size(); i++) {
                Log.i(LOG_TAG_DEBUG, "Checking result on " + mResultsToCheck.get(i).getDate().toString());
                ArrayList<Prize> prizes;
                for (int j = 0; j < mTicket.size(); j++) {
                    prizes = checker.getPrizes(mTicket.getLine(j), mResultsToCheck.get(i));
                    if (prizes != null) {
                        for(Prize p: prizes) {
                            mPrizes.addFirst(p);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_prize, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder;
        switch (item.getItemId()) {
            case R.id.menuItemSaveTicket:
                builder = new AlertDialog.Builder(getActivity(), R.style.DialogAccent);
                builder.setTitle(R.string.dialog_title_save_ticket);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean saved = saveTicket();
                        if(saved) {
                            Snackbar snackbar = Snackbar
                                    .make(mRecyclerView, "Saved ticket", Snackbar.LENGTH_SHORT)
                                    .setActionTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                            View view = snackbar.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.snack_bar_text));
                            tv.setGravity(Gravity.CENTER);
                            snackbar.show();
                        }
                    }
                });
                builder.setNegativeButton("Don't save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                final AlertDialog dialog = builder.create();

                // change text color of dialog
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(getContext(), R.color.accent));

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                    }
                });

                dialog.show();

                return true;
            case android.R.id.home:
                if(!mClickedSavedTicket && !mUsedSavedTicket) {
                    builder = new AlertDialog.Builder(getActivity(), R.style.DialogAccent);
                    builder.setTitle(R.string.dialog_title_save_ticket);
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean ticketSaved = saveTicket();
                            removePrizeFragment();
                            if (ticketSaved) {
                                Toast.makeText(getActivity().getApplicationContext(), "Saved ticket",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity().getApplicationContext(), "Failed to save ticket",
                                        Toast.LENGTH_SHORT).show();
                            }
                            mClickedSavedTicket = true;
                        }
                    });
                    builder.setNegativeButton("Don't save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removePrizeFragment();
                        }
                    });
                    builder.show();
                }
                else {
                    removePrizeFragment();
                }
                return true;
            default:
                return false;
        }
    }

    public boolean saveTicket() {
        try {
            TicketJSONSerializer ticketSerializer = new TicketJSONSerializer(getActivity());
            ticketSerializer.saveTicket(mTicket, mDrawType);
            Log.i("CheckerResultInfo", "saved ticket");
            return true;
        } catch (Exception e) {
            Log.d(LOG_TAG_DEBUG, "Error saving ticket");
            return false;
        }
    }

    public void removePrizeFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if(mPreviousFragment == PREVIOUS_FRAGMENT_MAIN) {
            fm.beginTransaction().remove(this).commit();
            fm.popBackStack();
        }
        else {
            mToolbar.setTitle("");
            fm.beginTransaction()
                    .remove(PrizeFragment.this)
                    .add(R.id.fragmentContainer, CheckerFragment.newInstance(mDrawType))
                    .commit();
        }

        ((MainActivity) getActivity()).toggleDrawer();
    }

    public boolean savedTicket() {
        return mClickedSavedTicket;
    }

    public boolean usedSavedTicket() {
        return mUsedSavedTicket;
    }

    public static PrizeFragment newInstance(Ticket ticket, boolean usedSavedTicket, int previousFragment) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_EXTRA_TICKET, ticket);
        args.putBoolean(KEY_USED_SAVED_TICKET, usedSavedTicket);
        args.putInt(KEY_PREVIOUS_FRAGMENT, previousFragment);

        PrizeFragment fragment = new PrizeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private class DownloadResults extends AsyncTask<Void, Void, Boolean> {

        public DownloadResults() {}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mRecyclerView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBarText.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean downloadsSuccessful = true;
            boolean hasPlus = AppUtils.hasPlus(getContext());

            // draw types to download
            String[] drawTypes;

            // only check primary draw if we don't have plus
            if(!hasPlus) {
                drawTypes = new String[]{mTicket.getDrawType()};
            }
            // we have plus so check primary and plus draws
            else {
                drawTypes = DrawType.getAssociatedDrawTypes(mDrawType);

                if(mDrawType.equals(DrawType.LOTTO)) {
                    drawTypes = Arrays.copyOf(drawTypes, 3);
                }
            }

            ResultParser resultParser = new ResultParser();
            Elements resultElements;
            Date latestDrawDate = Result.getLatestDrawDate(mDrawType);
            // checking specific draw
            if(mTicket.getDates().size() == 1) {

                for(String drawType: drawTypes) {
                    try {
                        resultElements = ResultDownloader.getResultForDate(
                                drawType,
                                mTicket.getDates().get(0),
                                latestDrawDate
                        );
                        mResultsToCheck.add(resultParser.getResult(resultElements));
                    } catch (IOException e) {
                        e.printStackTrace();
                        downloadsSuccessful = false;
                    }
                }
            }
            // checking multiple draw dates
            else {
                for(String drawType: drawTypes) {
                    try {
                        for(Date drawDate: mTicket.getDates()) {
                            resultElements = ResultDownloader.getResultForDate(
                                    drawType,
                                    drawDate,
                                    latestDrawDate
                            );
                            mResultsToCheck.add(resultParser.getResult(resultElements));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        downloadsSuccessful = false;
                    }
                }
            }

            return downloadsSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean downloadSuccessful) {
            super.onPostExecute(downloadSuccessful);

            if(downloadSuccessful) {
                checkTicket();
            }
            else {
                Toast.makeText(getContext(),
                        "Failed to check ticket! Please check your internet connection",
                        Toast.LENGTH_LONG).show();
            }
            mRecyclerView.setAdapter(mPrizeAdapter);
            mProgressBar.setVisibility(View.GONE);
            mProgressBarText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
