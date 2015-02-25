package thesociallions.myrug;

/**
 * Created by leon on 13-12-14.
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nl.thesociallions.myrug.helper.Constants;
import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;

public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    Calendar cal;
    String date;
    Toolbar mToolbar;

    public ScheduleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(Constants.TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Setup of ListAdapter for delivery of schedule items to ListView UI
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.listitem_schedule,
                null,
                new String[] {DB.KEY_SUB, DB.KEY_TYPE, DB.KEY_LOC, DB.KEY_STT},
                new int[] {R.id.course_nl, R.id.type, R.id.location, R.id.time_start}, 0);

        // Setup of ListView for display of schedule items and providing a detailed view
        ListView mListView = (ListView)rootView.findViewById(R.id.schedule);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Bundle savestate = new Bundle();
                onSaveInstanceState(savestate);
                Uri todoUri = Uri.parse(DBProvider.CONTENT_URI_SCHEDULE + "/" + id);
                Bundle bundle = new Bundle();
                mToolbar.setSubtitle(null);
                mToolbar.getMenu().clear();
                bundle.putParcelable("URI", todoUri);
                Fragment fragment = new ScheduleDetailFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            }
        });

        mListView.setEmptyView(rootView.findViewById(R.id.empty));
        mListView.setAdapter(mAdapter);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.e(Constants.TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);

        if (savedInstanceState != null){
            cal = (Calendar) savedInstanceState.getSerializable("calendar");
        } else {
            Date now = new Date();
            if (cal == null){
                cal = Calendar.getInstance();
                cal.setTime(now);
            }
        }
        SimpleDateFormat queryDate = new SimpleDateFormat("dd-MM-yyyy");
        queryDate.setCalendar(cal);
        date = queryDate.format(cal.getTime());
        getLoaderManager().initLoader(1, null, this);

        SimpleDateFormat titleDay = new SimpleDateFormat("EEEE");
        SimpleDateFormat titleDate = new SimpleDateFormat("d MMMM");
        titleDay.setCalendar(cal);
        titleDate.setCalendar(cal);

        // Display day of the week and date
        String day = titleDay.format(cal.getTime());
        String dayDisplay = Character.toUpperCase(day.charAt(0)) + day.substring(1);

        getActivity().setTitle(dayDisplay);
        mToolbar.setSubtitle(titleDate.format(cal.getTime()));

        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();

                switch (item.getItemId())
                {
                    case R.id.choose_day:
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getFragmentManager(), "datePicker");
                        return true;

                    case R.id.action_next_day:
                        cal.add( Calendar.DATE, 1);

                        SimpleDateFormat titleDay = new SimpleDateFormat("EEEE");
                        SimpleDateFormat titleDate = new SimpleDateFormat("d MMMM");
                        titleDay.setCalendar(cal);
                        titleDate.setCalendar(cal);
                        // Display day of the week and date
                        String a_day = titleDay.format(cal.getTime());
                        String dayDisplay = Character.toUpperCase(a_day.charAt(0)) + a_day.substring(1);
                        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
                        getActivity().setTitle(dayDisplay);
                        mToolbar.setSubtitle(titleDate.format(cal.getTime()));

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        date = dateFormat.format(cal.getTime());
                        refresh();
                        return true;

                    case R.id.action_previous_day:
                        cal.add( Calendar.DATE, -1);

                        titleDay = new SimpleDateFormat("EEEE");
                        titleDate = new SimpleDateFormat("d MMMM");
                        titleDay.setCalendar(cal);
                        titleDate.setCalendar(cal);
                        // Display day of the week and date
                        a_day = titleDay.format(cal.getTime());
                        dayDisplay = Character.toUpperCase(a_day.charAt(0)) + a_day.substring(1);
                        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
                        getActivity().setTitle(dayDisplay);
                        mToolbar.setSubtitle(titleDate.format(cal.getTime()));

                        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        date = dateFormat.format(cal.getTime());
                        refresh();
                        return true;
                }
                return false;
            }
        });

        // Inflate a menu to be displayed in the app_toolbar
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.schedule);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.e(Constants.TAG, "onSaveInstanceState");
        savedInstanceState.putSerializable("calendar", cal);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(Constants.TAG, "onResume");
        getLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e(Constants.TAG, "onPause");
    }

    public void refresh(){
        getLoaderManager().restartLoader(1, null, this);
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment                                          // TODO: Make Date Picker static and calendar data load as needed.
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            refreshList();
        }

        public void refreshList(){

            SimpleDateFormat titleDay = new SimpleDateFormat("EEEE");
            SimpleDateFormat titleDate = new SimpleDateFormat("d MMMM");
            titleDay.setCalendar(cal);
            titleDate.setCalendar(cal);
            // Display day of the week and date
            String a_day = titleDay.format(cal.getTime());
            String dayDisplay = Character.toUpperCase(a_day.charAt(0)) + a_day.substring(1);
            Toolbar mTb = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
            getActivity().setTitle(dayDisplay);
            mTb.setSubtitle(titleDate.format(cal.getTime()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            date = dateFormat.format(cal.getTime());
            refresh();
        }
    }



    /**
     * Database loader
     * Gets schedule data from database and delivers it to the SimpleCursorAdapter
     * for display in the UI.
     **/

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = DBProvider.CONTENT_URI_SCHEDULE;
        String where = DB.KEY_DAT + " = '"+ date +"'";
        String orderby = DB.KEY_STT + " ASC";
        return new CursorLoader(getActivity(),uri,null,where,null,orderby);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }
}