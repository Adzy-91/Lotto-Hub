package ie.adampurser.lottohub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.select.Elements;

import java.util.HashMap;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


public class MainFragment extends Fragment {
    private static final String LOG_TAG_INFO = "MainFragment.MESSAGE";
    private static final String LOG_TAG_DEBUG = "MainFragment.DEBUG";
    private static final String LOG_TAG_ERROR = "MainFragment.ERROR";

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFabCamera;
    private TextView mNoResultsView;
    private ActionBar mToolbar;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mProgressBarContainer;

    // a map containing all results with the key being the primary drawType e.g. EuroMillions
    public static HashMap<String, Result> sAllResults;

    private Result[] mResultsToDisplay;
    private ResultBlockAdapter mAdapter;
    private ResultJSONSerializer mLottoDrawSerializer;
    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPreferences;

    // used to check if we've just opened the app
    // not to be mistaken for first ever app run
    private boolean mFirstRun = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mLottoDrawSerializer = new ResultJSONSerializer(getActivity());

        mFragmentManager = getActivity().getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (mFragmentManager.getBackStackEntryCount() == 0) {
                    mToolbar.setTitle(R.string.toolbar_title_main);
                }
            }
        });

        // check if this is the first ever app run
        boolean firstEverRun = mSharedPreferences.getBoolean(
                getResources().getString(R.string.settings_key_first_run),
                true
        );

        // set default prefs if this is the first app run
        if(firstEverRun) {
            Log.i(LOG_TAG_INFO, "Running app for the first time");

            // set plus pref
            mSharedPreferences.edit()
                    .putBoolean(getString(R.string.settings_key_plus), true)
                    .apply();

            // set first run bool to false
            mSharedPreferences.edit()
                    .putBoolean(getResources().getString(R.string.settings_key_first_run), false)
                    .apply();
        }

        sAllResults = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, parent, false);

        mToolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(mToolbar != null) {
            mToolbar.setTitle(R.string.toolbar_title_main);
        }

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayoutMain);
        mRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(AppUtils.hasInternetConnection(getContext())) {
                    new UpdateResults().execute();
                }
                else {
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(),
                            R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
                }
            }
        });

        mProgressBarContainer = (LinearLayout) view.findViewById(R.id.progressBarContainerMain);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.resultBlockContainerMain);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFabCamera = (FloatingActionButton)
                view.findViewById(R.id.fabCheckNumbers);

        mFabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] primaryDrawTypes = DrawType.getPrimaryDrawTypes();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(primaryDrawTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getContext(), OcrCaptureActivity.class);
                        intent.putExtra(OcrCaptureActivity.DrawType, primaryDrawTypes[i]);
                        getActivity().startActivityForResult(intent, MainActivity.RC_OCR_CAPTURE);
                    }
                });
                builder.show();
            }
        });

        mNoResultsView = (TextView) view.findViewById(R.id.textViewNoConnectionMain);

        if(AppUtils.hasInternetConnection(getContext())) {
            new UpdateResults().execute();
        }
        else {
            Toast.makeText(getContext(),
                    R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
            loadAllResults();
            mProgressBarContainer.setVisibility(View.GONE);
            mFirstRun = false;
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemRefresh:
                if(AppUtils.hasInternetConnection(getContext())) {
                    new UpdateResults().execute();
                }
                else {
                    Toast.makeText(getContext(),
                            R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return false;
        }
    }

    private void setupAdapter() {
        mResultsToDisplay = ResultHelper.getResults(DrawType.getPrimaryDrawTypes(), sAllResults);

        mAdapter = new ResultBlockAdapter(
                mResultsToDisplay,
                new RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        String clickedDrawType = mResultsToDisplay[position].getDrawType();

                        mFragmentManager.beginTransaction()
                                .add(R.id.fragmentContainer,
                                        ResultFragmentPager.newInstance(ResultHelper.getCompleteResult(clickedDrawType, sAllResults)),
                                        clickedDrawType)
                                .addToBackStack(null)
                                .commit();
                        mToolbar.setTitle("");
                    }
                },
                ResultBlockAdapter.ADAPTER_LOCATION_MAIN
        );

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setDuration(100);

        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(scaleInAnimationAdapter);
        alphaInAnimationAdapter.setDuration(100);

        mRecyclerView.setAdapter(alphaInAnimationAdapter);
    }

    /* download the latest results */
    private class UpdateResults extends AsyncTask<Void,Void,Boolean> {

        DrawerLayout mDrawerLayout = (DrawerLayout)
                getActivity().findViewById(R.id.drawerLayoutNavDrawer);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Use accent progress bar if first run
            if(mFirstRun) {
                mProgressBarContainer.setVisibility(View.VISIBLE);
                mFirstRun = false;
            }
            else {
                mRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(true);
                    }
                });
            }

            // Disable views while updating
            mRecyclerView.setVisibility(View.GONE);
            mFabCamera.hide();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean resultsUpdated = false;

            if(sAllResults != null) {
                sAllResults.clear();
            }

            try {
                Elements resultElements = ResultDownloader.getLatestResult(ResultDownloader.ALL_DRAWS, 1);

                if(resultElements != null) {

                    ResultParser parser = new ResultParser();

                    // get all main results
                    sAllResults = parser.getResultsMap(resultElements);

                    // get raffle result
                    Elements raffleElements = ResultDownloader.getLatestResult(DrawType.LOTTO_PLUS_RAFFLE, 1);
                    sAllResults.put(DrawType.LOTTO_PLUS_RAFFLE, parser.getResult(raffleElements));

                    // save results
                    String [] primaryDrawTypes = DrawType.getPrimaryDrawTypes();
                    String [] associatedDrawTypes;

                    for(String drawType: primaryDrawTypes) {
                        associatedDrawTypes = DrawType.getAssociatedDrawTypes(drawType);
                        mLottoDrawSerializer.saveResult(ResultHelper.getResults(associatedDrawTypes, sAllResults));
                    }

                    resultsUpdated = true;
                }

            } catch (Exception e) {
                Log.e(LOG_TAG_ERROR, e.getMessage());
            }

            return resultsUpdated;
        }

        @Override
        protected void onPostExecute(Boolean resultsUpdated) {

            // update failed
            if(!resultsUpdated ) {
                Toast.makeText(getContext(),
                        R.string.toast_update_failed, Toast.LENGTH_LONG).show();

                // if there are no results already displaying then load
                if(sAllResults.size() == 0) {
                    loadAllResults();
                }
            }

            // load failed
            if(sAllResults.size() == 0) {
                mNoResultsView.setVisibility(View.VISIBLE);
                mFabCamera.hide();
            } else {
                setupAdapter();
                mFabCamera.show();
                mNoResultsView.setVisibility(View.GONE);
            }

            mProgressBarContainer.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);
            mRecyclerView.setVisibility(View.VISIBLE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mRefreshLayout.setEnabled(true);
        }
    }

    private void loadAllResults() {

        loadCompleteResult(DrawType.LOTTO);
        loadCompleteResult(DrawType.DAILY_MILLION);
        loadCompleteResult(DrawType.EURO_MILLIONS);

        if(sAllResults.size() == 0) {
            mNoResultsView.setVisibility(View.VISIBLE);
            mFabCamera.hide();
        }
        else {
            if(mAdapter == null) {
                setupAdapter();
            }
            mFabCamera.show();
        }
    }

    private void loadCompleteResult(String drawType) {
        try {
            Result[] completeResult = mLottoDrawSerializer.loadCompleteResult(drawType);

            for(Result result : completeResult) {
                sAllResults.put(result.getDrawType(), result);
            }
        }catch (Exception e) {
            Log.e(LOG_TAG_DEBUG, e.getMessage());
        }
    }

    public static MainFragment newInstance() {
        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);

        return fragment;
    }
}

