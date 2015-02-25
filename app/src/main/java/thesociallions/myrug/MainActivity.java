package thesociallions.myrug;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import nl.thesociallions.myrug.account.RUGAccountHandler;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final String PREFS_NAME = "myrugsettings";
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Account control
         */
        // Check if we have an logged in account
        RUGAccountHandler rugAccountHandler = new RUGAccountHandler(this);
        try {
            username = rugAccountHandler.getName();
        } catch (Exception e) {
            // If not, go to the Settings dialog
            Intent intent = new Intent(this, SetupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        /**
         * Toolbar Setup
         */
        // Set up a Toolbar instance as ActionBar replacement
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        // Set the title in the app_toolbar
        mToolbar.setTitle("My RUG");

        mTitle = mDrawerTitle = "My RUG";
        mPlanetTitles = getResources().getStringArray(R.array.new_menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        View user = getLayoutInflater().inflate(R.layout.listitem_drawer_head, null);
        TextView name = (TextView)user.findViewById(R.id.name);
        TextView email = (TextView)user.findViewById(R.id.email);
        String em = username +"@rug.nl";
        name.setText(username);
        email.setText(em);
        mDrawerList.addHeaderView(user, null, false);

        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,R.layout.listitem_drawer_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close
        ){
            public void onDrawerClosed(View view) {
                mToolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                MenuItem search = (mToolbar.getMenu().findItem(R.id.menu_search));
                if (search != null) {
                    search.collapseActionView();
                }
                mToolbar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mToolbar.getMenu().clear();
        mToolbar.setSubtitle(null);

        if(position == 1){
            // update selected item title, then close the drawer
            setTitle(mPlanetTitles[position-1]);
            Fragment fragment = new TodayFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 2) {
            //setTitle(getString(R.string.today));
            // update selected item title, then close the drawer
            Fragment fragment = new ScheduleFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 3) {
            setTitle(mPlanetTitles[position-1]);
            Fragment fragment = new GradeFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 4) {
            setTitle(mPlanetTitles[position-1]);
            Fragment fragment = new CampusFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 5) {
            // update selected item title, then close the drawer
            setTitle(mPlanetTitles[position-1]);
            Fragment fragment = new SettingsFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mToolbar.setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
