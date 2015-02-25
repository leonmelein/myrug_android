package nl.thesociallions.myrug;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;

/**
 * Created by leon on 14-12-14.
 */
public class GradeDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private Uri todoUri;
    Toolbar mToolbar;
    SimpleCursorAdapter mAdapter;


    public GradeDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grade_detail, container, false);
        getActivity().setTitle(getString(R.string.TITLE_schedule_detail));
        // check from the saved Instance
        todoUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable("URI");

        Bundle bundle = this.getArguments();
        todoUri = bundle.getParcelable("URI");
        getActivity().setTitle(getString(R.string.TITLE_schedule_detail));

        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                switch (item.getItemId())
                {
                    case R.id.action_share:
                        String vak = (String) ((TextView)getView().findViewById(R.id.name)).getText();
                        String cijfer = (String) ((TextView)getView().findViewById(R.id.date)).getText();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.gradedetail_share_text), cijfer, vak));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.gradedetail_share)));
                        return true;
                }
                return false;
            }
        });

        // Inflate a menu to be displayed in the app_toolbar
        mToolbar.inflateMenu(R.menu.gradedetail);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        fillData(todoUri);
        ListView gradlist = (ListView)getView().findViewById(R.id.gradelist);
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.listitem_grades,
                null,
                new String[] {DB.KEY_DATE, DB.KEY_GRADE},
                new int[] {R.id.coursetitle, R.id.grade}, 0);
        gradlist.setAdapter(mAdapter);
        gradlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri todoUri = Uri.parse(DBProvider.CONTENT_URI_GRADES + "/" + id);
                Bundle bundle = new Bundle();
                bundle.putParcelable("URI", todoUri);
                Fragment fragment = new GradeDetailFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            }
        });
        getLoaderManager().restartLoader(1, null, GradeDetailFragment.this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("URI", todoUri);
    }

    private void fillData(Uri uri) {
        String locale = getResources().getConfiguration().locale.getDisplayName();
        if (locale.contains("Nederlands")) {
            String[] projection = { DB.KEY_DISPLAYDATE, DB.KEY_COURSE_NL, DB.KEY_GRADE, DB.KEY_COURSECODE};
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null) {
                cursor.moveToFirst();

                TextView EN = (TextView)getView().findViewById(R.id.name);
                TextView DT = (TextView)getView().findViewById(R.id.date);
                TextView CD = (TextView)getView().findViewById(R.id.coursecode);
                TextView GR = (TextView)getView().findViewById(R.id.grade);

                EN.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_COURSE_NL)));
                DT.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_DISPLAYDATE)));
                CD.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_COURSECODE)));
                GR.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_GRADE)));

                // always close the cursor
                cursor.close();
            }
        } else {
            String[] projection = { DB.KEY_DISPLAYDATE, DB.KEY_COURSE_EN, DB.KEY_GRADE, DB.KEY_COURSECODE};
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null) {
                cursor.moveToFirst();

                TextView EN = (TextView)getView().findViewById(R.id.name);
                TextView DT = (TextView)getView().findViewById(R.id.date);
                TextView CD = (TextView)getView().findViewById(R.id.coursecode);
                TextView GR = (TextView)getView().findViewById(R.id.grade);

                EN.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_COURSE_EN)));
                DT.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_DISPLAYDATE)));
                CD.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_COURSECODE)));
                GR.setText(cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_GRADE)));

                // always close the cursor
                cursor.close();
            }
        }


    }

    /** A callback method invoked by the loader when initLoader() is called */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = DBProvider.CONTENT_URI_GRADES;
        String course = (String)((TextView)getView().findViewById(R.id.coursecode)).getText();
        String where = DB.KEY_COURSECODE + " = '" + course + "' AND " + DB.KEY_LATEST + " = '0'";
        String orderby = DB.KEY_DATE + " DESC";
        return new CursorLoader(getActivity(), uri, null, where, null, orderby);
    }

    /** A callback method, invoked after the requested content provider returned all the data */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }
}