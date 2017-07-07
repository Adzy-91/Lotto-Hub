package ie.adampurser.lottohub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.machinarius.preferencefragment.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    private static final String LOG_TAG_DEBUG = "SettingsFragment";

    private ActionBar mToolbar;

    private PreferenceManager mPreferenceManager;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.preferences);

        mPreferenceManager = getPreferenceManager();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mToolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(mToolbar != null) {
            mToolbar.setTitle(R.string.toolbar_title_settings);
        }

        Spinner spinner = (Spinner)getActivity().findViewById(R.id.toolbarSpinner);
        spinner.setVisibility(View.GONE);

        if(v != null) {
            v.setBackgroundColor(getResources().getColor(R.color.app_background));
            ListView listView = (ListView) v.findViewById(android.R.id.list);
            listView.setDivider(null);
        }

        Preference preference = mPreferenceManager
                .findPreference(getResources().getString(R.string.settings_key_version));
        preference.setSummary(String.valueOf(BuildConfig.VERSION_NAME));

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for(int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.fragmentContainer);

                fm.beginTransaction()
                        .remove(currentFragment)
                        .commit();
                fm.popBackStack();
                return true;
            default:
                return false;
        }
    }
}