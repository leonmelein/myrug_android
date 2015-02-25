package thesociallions.myrug;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONObject;

import java.util.UUID;

import nl.thesociallions.myrug.grades.GradeRequest;
import nl.thesociallions.myrug.helper.Constants;
import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;

/**
 * Created by leon on 14-12-14.
 */
public class GradeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public View rootView;
    private View mContentView;
    private View mLoadingView;
    private boolean mContentLoaded;
    SimpleCursorAdapter mAdapter;
    ListView gradlist;
    Toolbar mToolbar;

    public GradeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_grades, container, false);

        mContentView = rootView.findViewById(R.id.content);
        mLoadingView = rootView.findViewById(R.id.load);
        // Initially hide the content view.
        mContentView.setVisibility(View.INVISIBLE);

        gradlist = (ListView)rootView.findViewById(R.id.grdlst);

        String locale = getResources().getConfiguration().locale.getDisplayName();
        if (locale.contains("Nederlands")) {
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.listitem_grades_material,
                    null,
                    new String[] {DB.KEY_COURSE_NL, DB.KEY_GRADE, DB.KEY_COURSECODE, DB.KEY_DISPLAYDATE},
                    new int[] {R.id.name, R.id.grade, R.id.coursecode, R.id.date}, 0);
        } else {
            mAdapter = new SimpleCursorAdapter(getActivity(),           // TODO: Change to new Listitem
                    R.layout.listitem_grades,
                    null,
                    new String[] {DB.KEY_COURSE_EN, DB.KEY_GRADE},
                    new int[] {R.id.coursetitle, R.id.grade}, 0);
        }

        gradlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContentLoaded = !mContentLoaded;
                mToolbar.getMenu().clear();
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

        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                switch (item.getItemId())
                {
                    case R.id.action_refresh:
                        mLoadingView.setVisibility(View.VISIBLE);
                        mContentView.setVisibility(View.GONE);
                        getGrades();
                        return true;
                }
                return false;
            }
        });

        // Inflate a menu to be displayed in the app_toolbar
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.grades);
        gradlist.setAdapter(mAdapter);

        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            getGrades();
        } else {
            mLoadingView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        }

        getActivity().setTitle(getString(R.string.TITLE_grades));
        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();
        App.getInstance().cancelRequests(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getLoaderManager().restartLoader(1, null, GradeFragment.this);
    }

    /** A callback method invoked by the loader when initLoader() is called */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = DBProvider.CONTENT_URI_GRADES;
        String where = DB.KEY_LATEST + " = '1'";
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

    public void getGrades(){
        final GradeRequest backupReq = new GradeRequest(getAuthURL(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e(Constants.TAG, "CAMPUS LOAD");
                            mLoadingView.setVisibility(View.GONE);
                            mContentView.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                },getActivity());
        GradeRequest req = new GradeRequest
                (getAuthURL(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            Log.d("Getting data", "Grades");
                            mLoadingView.setVisibility(View.GONE);
                            mContentView.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.TAG, "Registering grades token");

                        AccountManager accountManager = AccountManager.get(getActivity());
                        Account[] allMyRUGAccount = accountManager.getAccountsByType("thesociallions.myrug.account");
                        final String S = allMyRUGAccount[0].name;
                        final String W = accountManager.getPassword(allMyRUGAccount[0]);
                        final String devicetoken = UUID.randomUUID().toString();
                        WebView myWebView = (WebView) rootView.findViewById(R.id.webView);
                        WebSettings webSettings = myWebView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        myWebView.loadUrl("https://nestor.rug.nl/webapps/RuG-Login-bb_bb60/do/registerToken?deviceAuthToken=" + devicetoken + "&deviceNotToken=789456&osToken=android");
                        myWebView.setWebViewClient(new WebViewClient() {

                            public void onPageFinished(WebView myWebView, String url) {
                                Log.e("My RUG", url);
                                SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
                                String title = myWebView.getTitle();

                                if (url.contains("https://nestor.rug.nl/webapps/login/")) {
                                    myWebView.loadUrl("javascript:(function(){document.getElementsByName('user_id')[0].value='" + S + "';document.getElementsByName('password')[0].value='" + W + "';validate_form(document.getElementsByName('login')[0], false, false);document.getElementsByName('login')[0].submit();})();");
                                } else if (title.contains("USER")) {
                                    SharedPreferences.Editor edit = settings.edit();
                                    String usertoken = title.replace("USER", "");
                                    edit.putString("usertoken", usertoken);
                                    edit.putString("devicetoken", devicetoken);
                                    edit.commit();
                                    backupReq.setTag(this);
                                    App.getInstance().addToRequestQueue(backupReq);
                                }
                            }
                        });
                    }
                }, getActivity());
        req.setTag(this);
        App.getInstance().addToRequestQueue(req);
    }

    public String getAuthURL(){
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final String usertoken = settings.getString("usertoken", null);
        final String devicetoken = settings.getString("devicetoken", null);

        return String.format(getString(R.string.grades_url),devicetoken,usertoken);
    }
}