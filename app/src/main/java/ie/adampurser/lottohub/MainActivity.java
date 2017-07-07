package ie.adampurser.lottohub;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements OnNavSelectionChangedListener {
    private static final String LOG_TAG_DEBUG = "MainActivity.DEBUG";

    private static final int NAV_DRAWER_LATEST_RESULTS_INDEX = 1;
    private static final int NAV_DRAWER_RESULTS_INDEX = 2;
    private static final int NAV_DRAWER_CHECKER_INDEX = 3;
    private static final int NAV_DRAWER_SAVED_TICKETS_INDEX = 4;
    private static final int NAV_DRAWER_SETTINGS_INDEX = 5;
    private static final int NAV_DRAWER_SIZE = 6;
    public static final int RC_OCR_CAPTURE = 1;
    public static final String Ticket = "ticket";

    private Toolbar mToolbar;
    private Spinner mSpinner;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecyclerView;

    private DrawSpinnerAdapter mSpinnerAdapter;
    private NavDrawerAdapter mNavDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mPrimaryDrawTypes;
    private NavDrawerItem[] mDrawerItems;
    private String[] mDrawerTitles;
    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPreferences;
    private int mCurrentDrawerItemIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        mFragmentManager = getSupportFragmentManager();

        mToolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPrimaryDrawTypes = DrawType.getPrimaryDrawTypes();
        mSpinner = (Spinner)mToolbar.findViewById(R.id.toolbarSpinner);
        mSpinnerAdapter = new DrawSpinnerAdapter(
                this,
                mPrimaryDrawTypes
        );
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
                String fragmentTag = currentFragment.getTag();
                String clickedDrawType = mPrimaryDrawTypes[position];
                if (fragmentTag != null) {
                    if (currentFragment.getTag().equals(clickedDrawType)) {
                        return;
                    }
                }

                Fragment fragment;
                if (currentFragment instanceof CheckerFragment) {
                    fragment = CheckerFragment.newInstance(clickedDrawType);
                } else if (currentFragment instanceof ResultFragmentPager) {
                    fragment = ResultFragmentPager.newInstance(
                            ResultHelper.getCompleteResult(clickedDrawType, MainFragment.sAllResults)
                    );
                } else {
                    fragment = SavedTicketFragment.newInstance(mPrimaryDrawTypes[position]);
                }

                mFragmentManager.beginTransaction()
                        .remove(currentFragment)
                        .add(R.id.fragmentContainer, fragment, mPrimaryDrawTypes[position])
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Navigation Drawer
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutNavDrawer);
        mDrawerLayout.setDrawerShadow(R.drawable.nav_drawer_shadow, GravityCompat.START);

        mDrawerRecyclerView = (RecyclerView)findViewById(R.id.recyclerViewNavDrawer);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.nav_drawer_open,
                R.string.nav_drawer_closed
                ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mDrawerTitles = getResources().getStringArray(R.array.nav_drawer_titles);
        int[] drawerIconsResIds = {
                0,
                R.drawable.nav_icon_latest_results,
                R.drawable.nav_icon_results,
                R.drawable.nav_icon_checker,
                R.drawable.nav_icon_saved_tickets,
                R.drawable.nav_icon_settings
        };
        int[] drawerIconsSelectedResIds = {
                0,
                R.drawable.nav_icon_latest_results_selected,
                R.drawable.nav_icon_results_selected,
                R.drawable.nav_icon_checker_selected,
                R.drawable.nav_icon_saved_tickets_selected,
                R.drawable.nav_icon_settings_selected
        };

        mDrawerItems = new NavDrawerItem[NAV_DRAWER_SIZE];
        for(int i = 0; i < NAV_DRAWER_SIZE; i++) {
            mDrawerItems[i] = new NavDrawerItem(
                    mDrawerTitles[i],
                    drawerIconsResIds[i],
                    drawerIconsSelectedResIds[i]
            );
        }

        mDrawerItems[NAV_DRAWER_LATEST_RESULTS_INDEX].setIsSelected(true);
        mCurrentDrawerItemIndex = NAV_DRAWER_LATEST_RESULTS_INDEX;

        int[] drawerDividerIndexes = getResources()
                .getIntArray(R.array.nav_drawer_divider_indexes);

        for(int i = 0; i < drawerDividerIndexes.length; i++) {
            mDrawerItems[drawerDividerIndexes[i]].setHasDivider(true);
        }

        mNavDrawerAdapter = new NavDrawerAdapter(
                mDrawerItems,
                new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Fragment currentFragment =
                        mFragmentManager.findFragmentById(R.id.fragmentContainer);

                // default draw type when opening new fragments
                String drawType = DrawType.LOTTO;

                Fragment fragment = null;
                switch (position) {
                    case NAV_DRAWER_LATEST_RESULTS_INDEX:
                        if (currentFragment instanceof MainFragment) {
                            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                            return;
                        }
                        mFragmentManager.beginTransaction()
                                .remove(currentFragment)
                                .commit();
                        mFragmentManager.popBackStack();

                        mSpinner.setVisibility(View.GONE);
                        mDrawerLayout.closeDrawer(mDrawerRecyclerView);

                        return;

                    case NAV_DRAWER_RESULTS_INDEX:
                        if (currentFragment instanceof ResultFragmentPager) {
                            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                            return;
                        }
                        fragment = ResultFragmentPager.newInstance(
                                ResultHelper.getCompleteResult(drawType, MainFragment.sAllResults));
                        break;

                    case NAV_DRAWER_CHECKER_INDEX:
                        if (currentFragment instanceof CheckerFragment) {
                            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                            return;
                        }
                        mSpinner.setSelection(0);
                        fragment = CheckerFragment.newInstance(drawType);
                        break;

                    case NAV_DRAWER_SAVED_TICKETS_INDEX:
                        if (currentFragment instanceof SavedTicketFragment) {
                            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                            return;
                        }
                        fragment = new SavedTicketFragmentPager();
                        mSpinner.setSelection(0);
                        break;

                    case NAV_DRAWER_SETTINGS_INDEX:
                        fragment = new SettingsFragment();
                        mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                        mDrawerToggle.setDrawerIndicatorEnabled(false);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }

                if (fragment != null) {
                    if (currentFragment instanceof MainFragment) {
                        mFragmentManager.beginTransaction()
                                .add(R.id.fragmentContainer, fragment, drawType)
                                .addToBackStack(null)
                                .commit();

                    } else {
                        mFragmentManager.beginTransaction()
                                .remove(currentFragment)
                                .add(R.id.fragmentContainer, fragment, drawType)
                                .commit();
                    }
                }

                setSelectedDrawerItem(position);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(mDrawerRecyclerView);
                    }
                }, 200);
            }
        });
        mDrawerRecyclerView.setAdapter(mNavDrawerAdapter);

        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                switch(mFragmentManager.getBackStackEntryCount()) {
                    case 0:
                        mToolbar.setTitle(R.string.toolbar_title_main);
                        mDrawerToggle.setDrawerIndicatorEnabled(true);
                        setSelectedDrawerItem(NAV_DRAWER_LATEST_RESULTS_INDEX);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        break;
                }
            }
        });



        // Start MainFragment
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);
        if(fragment == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit();
        }
    }


    private void setSelectedDrawerItem(int position) {
        mDrawerItems[mCurrentDrawerItemIndex].setIsSelected(false);
        mDrawerItems[position].setIsSelected(true);
        mCurrentDrawerItemIndex = position;
        mNavDrawerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setSelection(int position) {
        setSelectedDrawerItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            mDrawerLayout.openDrawer(mDrawerRecyclerView);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);


        if (mDrawerLayout.isDrawerOpen(mDrawerRecyclerView)) {
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
            return;
        }

        if (fragment instanceof MainFragment) {
            super.onBackPressed();
        }

        else if (fragment instanceof PrizeFragment) {
            final PrizeFragment prizeFragment = (PrizeFragment)fragment;
            if(!prizeFragment.savedTicket() && !prizeFragment.usedSavedTicket()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this, R.style.DialogAccent);
                builder.setTitle("Save ticket?");
                builder.setNegativeButton("DON'T SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        prizeFragment.removePrizeFragment();
                        mToolbar.setTitle(R.string.toolbar_title_main);
                    }
                });
                builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        prizeFragment.saveTicket();
                        prizeFragment.removePrizeFragment();
                        Toast.makeText(MainActivity.this, "Ticket saved", Toast.LENGTH_SHORT).show();
                        mToolbar.setTitle(R.string.toolbar_title_main);
                    }
                });

                final AlertDialog dialog = builder.create();
                // change text color of dialog
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
                    }
                });
                dialog.show();
            }
            else {
                prizeFragment.removePrizeFragment();
                mToolbar.setTitle(R.string.toolbar_title_main);
            }
        }

        else {
            mSpinner.setVisibility(View.GONE);
            mSpinner.setSelection(0);

            mFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commit();
            mFragmentManager.popBackStack();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RC_OCR_CAPTURE:
                    Ticket ticket = (Ticket) data.getExtras().get(Ticket);
                    PrizeFragment fragment = PrizeFragment.newInstance(
                            ticket, false, PrizeFragment.PREVIOUS_FRAGMENT_MAIN
                    );
                    mFragmentManager.beginTransaction()
                            .add(R.id.fragmentContainer, fragment, null)
                            .commit();
                    break;
            }
        }
    }

    public void toggleDrawer() {
        boolean hamburgerEnabled = mDrawerToggle.isDrawerIndicatorEnabled();
        if(hamburgerEnabled) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void hideSpinner() {
        mSpinner.setVisibility(View.GONE);
    }

    public void showSpinner() {
        mSpinner.setVisibility(View.VISIBLE);
    }
}

