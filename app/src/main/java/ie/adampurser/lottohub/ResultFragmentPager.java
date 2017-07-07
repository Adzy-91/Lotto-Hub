package ie.adampurser.lottohub;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ResultFragmentPager extends Fragment implements OnDrawDateChangedListener {
    private static final String TAG_DEBUG="ResultsManager.DEBUG";
    private static String KEY_COMPLETE_RESULT = "CompleteResultPagerFragment.EXTRA";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Spinner mSpinner;
    private RelativeLayout mProgressBarContainer;

    private ResultsPagerAdapter mAdapter;
    private FragmentManager mChildFragmentManager;
    private String mDrawType;
    private Result[] mCompleteResult;
    private Date mLatestDrawDate;
    private OnNavSelectionChangedListener mOnNavSelectionChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mCompleteResult = (Result[]) getArguments().get(KEY_COMPLETE_RESULT);

        if(mCompleteResult != null) {
            mDrawType = mCompleteResult[0].getDrawType();
            mLatestDrawDate = mCompleteResult[0].getDate();
        }

        mOnNavSelectionChangedListener.setSelection(getResources().getInteger(R.integer.nav_index_results));

        mChildFragmentManager = getChildFragmentManager();
        CharSequence tabTitles[]= {
                getResources().getString(R.string.toolbar_tabs_results),
                getResources().getString(R.string.toolbar_tabs_winnings)
        };
        mAdapter =  new ResultsPagerAdapter(mChildFragmentManager, tabTitles);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_pager, container, false);

        mTabLayout = (TabLayout) view.findViewById(R.id.resultsFragmentTabs);

        mViewPager = (ViewPager) view.findViewById(R.id.viewPagerResultsFragment);

        mProgressBarContainer = (RelativeLayout) view.findViewById(R.id.progressContainerFragmentResults);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(toolbar != null) {
            toolbar.setTitle("");
        }

        mSpinner = (Spinner) getActivity().findViewById(R.id.toolbarSpinner);
        switch (mDrawType) {
            case DrawType.LOTTO:
                mSpinner.setSelection(0);
                break;
            case DrawType.DAILY_MILLION:
                mSpinner.setSelection(1);
                break;
            case DrawType.EURO_MILLIONS:
                mSpinner.setSelection(2);
        }
        mSpinner.setVisibility(View.VISIBLE);
        mSpinner.setEnabled(true);

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_result, menu);
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

    public static ResultFragmentPager newInstance(Result[] completeResult) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_COMPLETE_RESULT, completeResult);

        ResultFragmentPager fragment = new ResultFragmentPager();
        fragment.setArguments(args);

        return fragment;
    }

    private class ResultsPagerAdapter extends FragmentPagerAdapter {
        CharSequence mTabTitles[];

        public ResultsPagerAdapter(FragmentManager fm, CharSequence tabTitles[]) {
            super(fm);
            mTabTitles = tabTitles;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return ResultFragment.newInstance(mCompleteResult);
                case 1:
                    return ResultBreakdownFragment.newInstance(mCompleteResult);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }

        @Override
        public int getCount() {
            return mTabTitles.length;
        }
    }

    @Override
    public void onDrawDateChanged(Date newDate) {
        new UpdateResults(newDate).execute();
    }

    /* update the result to display with the given date */
    private class UpdateResults extends AsyncTask<Void, Void, Boolean> {

        Date newDate;

        UpdateResults(Date newDate) {
            this.newDate = newDate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mViewPager.setVisibility(View.GONE);
            mProgressBarContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Boolean downloadSuccessful = false;
            try {
                ResultParser parser = new ResultParser();
                Elements resultElements;

                Result[] completeResultTemp = new Result[mCompleteResult.length];

                // loop through results to be downloaded
                for(int i = 0; i < mCompleteResult.length; i++) {

                    // download draw at position i for specified date
                    resultElements = ResultDownloader.getResultForDate(
                            mCompleteResult[i].getDrawType(),
                            newDate,
                            mLatestDrawDate
                    );

                    // download has failed
                    if(resultElements.size() == 2) {
                        Log.i(TAG_DEBUG, "Received null back from lotto api");
                        return false;
                    }

                    // obtain the new result
                    completeResultTemp[i] = parser.getResult(resultElements);
                }

                // only insert new results when we know all downloads have been successful
                for(int i = 0; i < completeResultTemp.length; i++) {
                    mCompleteResult[i] = completeResultTemp[i];
                }

                downloadSuccessful = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return downloadSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean downloadSuccessful) {
            super.onPostExecute(downloadSuccessful);

            if(downloadSuccessful) {
                mAdapter.notifyDataSetChanged();
                List<Fragment> fragments = mChildFragmentManager.getFragments();
                ((ResultFragment) fragments.get(0)).notifyAdapter();
                ((ResultBreakdownFragment) fragments.get(1)).notifyAdapter();
            }
            else {
                Toast.makeText(getContext(),
                        R.string.toast_update_failed, Toast.LENGTH_LONG).show();
            }

            mViewPager.setVisibility(View.VISIBLE);
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }
}
