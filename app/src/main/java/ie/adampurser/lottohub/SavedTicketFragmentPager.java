package ie.adampurser.lottohub;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

public class SavedTicketFragmentPager extends Fragment {
    private static final String LOG_TAG_DEBUG = "SavedTicketPager";

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private SavedTicketPagerAdapter mPagerAdapter;
    private String[] mPrimaryDrawTypes;
    private FragmentManager mChildFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPrimaryDrawTypes = DrawType.getPrimaryDrawTypes();

        mChildFragmentManager = getChildFragmentManager();

        mPagerAdapter = new SavedTicketPagerAdapter(mChildFragmentManager, mPrimaryDrawTypes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_tickets_pager, container, false);

        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(toolbar != null) {
            toolbar.setTitle(R.string.toolbar_title_saved_tickets);
        }

        Spinner spinner = (Spinner) getActivity().findViewById(R.id.toolbarSpinner);
        spinner.setVisibility(View.GONE);

        mViewPager = (ViewPager) view.findViewById(R.id.viewPagerSavedTickets);
        mViewPager.setAdapter(mPagerAdapter);

        mTabLayout = (TabLayout) view.findViewById(R.id.tabsSavedTickets);
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_saved_tickets, menu);
    }

    private class SavedTicketPagerAdapter extends FragmentPagerAdapter {
        CharSequence[] mTabTitles;

        public SavedTicketPagerAdapter(FragmentManager fm, CharSequence[] tabTitles) {
            super(fm);

            mTabTitles = tabTitles;
        }

        @Override
        public Fragment getItem(int position) {
            return SavedTicketFragment.newInstance(String.valueOf(mTabTitles[position]));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }

        @Override
        public int getCount() {
            return mPrimaryDrawTypes.length;
        }
    }
}
