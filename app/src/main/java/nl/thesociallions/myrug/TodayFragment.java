package nl.thesociallions.myrug;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.HashMap;
import java.util.Map;

import nl.thesociallions.myrug.account.RUGAccountHandler;
import nl.thesociallions.myrug.credit.CreditHelper;
import nl.thesociallions.myrug.helper.Constants;
import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;
import nl.thesociallions.myrug.schedule.ScheduleHelper;


public class TodayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    private AbstractHttpClient mHttpClient;
    private RequestQueue mQueue;
    ListView mListView;

    public TodayFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);
        // we hold a reference to the HttpClient in order to be able to get/set cookies
        mHttpClient = new DefaultHttpClient();

        mListView = (ListView)rootView.findViewById(R.id.schedule);
        mQueue = Volley.newRequestQueue(getActivity(), new HttpClientStack(mHttpClient));


        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.listitem_today,
                null,
                new String[] {DB.KEY_SUB, DB.KEY_TYPE, DB.KEY_LOC, DB.KEY_STT},
                new int[] {R.id.course_nl, R.id.type, R.id.location, R.id.time_start}, 0);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Uri todoUri = Uri.parse(DBProvider.CONTENT_URI_SCHEDULE + "/" + id);
                Bundle bundle = new Bundle();
                bundle.putParcelable("URI", todoUri);
                Fragment fragment = new ScheduleDetailFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                fragment.setArguments(bundle);
            }
        });

        ViewGroup footer = (ViewGroup)inflater.inflate(R.layout.listitem_today_credit, mListView, false);
        TextView credit = (TextView)footer.findViewById(R.id.credit);
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String last_saldo = settings.getString("saldo", null);
        if (last_saldo != null) {
            credit.setText("€ " + last_saldo);
        }
        ImageButton topup = (ImageButton)footer.findViewById(R.id.topup);
        ImageButton refresh = (ImageButton)footer.findViewById(R.id.refresh);
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PackageManager manager = getActivity().getPackageManager();
                    assert manager != null;
                    Intent i = manager.getLaunchIntentForPackage("com.myorder");
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                } catch (Exception e) {
                    Intent topup = new Intent(Intent.ACTION_VIEW);
                    String url = "https://webdeposit.workspace.rug.nl/";
                    topup.setData(Uri.parse(url));
                    startActivity(topup);
                }
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageButton refresh = (ImageButton) getView().findViewById(R.id.refresh);
                refresh.setVisibility(View.INVISIBLE);
                ProgressBar progress = (ProgressBar) getView().findViewById(R.id.progressBar);
                progress.setVisibility(View.VISIBLE);

                final RUGAccountHandler handler = new RUGAccountHandler(getActivity());
                final StringRequest login = new StringRequest(Request.Method.POST,"https://webdeposit.workspace.rug.nl/API/checklogin.asp", new Response.Listener<String> () {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String saldo = CreditHelper.matchSaldo(response);
                            Log.e(Constants.TAG, saldo);
                            SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
                            CreditHelper.saveSaldo(settings,saldo);

                            TextView saldi = (TextView)getView().findViewById(R.id.credit);
                            saldi.setText("€ "+saldo);
                            ImageButton refresh = (ImageButton) getView().findViewById(R.id.refresh);
                            refresh.setVisibility(View.VISIBLE);
                            ProgressBar progress = (ProgressBar) getView().findViewById(R.id.progressBar);
                            progress.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        try {
                            params.put("txtPrimaryID", handler.getName());
                            params.put("txtSecondaryID", handler.getPassword());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        params.put("cmdLogin", "Login");
                        params.put("step", "2");

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        return params;
                    }
                };
                StringRequest cookies = new StringRequest(Request.Method.GET,"https://webdeposit.workspace.rug.nl/", new Response.Listener<String> () {
                    @Override
                    public void onResponse(String response) {
                        login.setTag(this);
                        mQueue.add(login);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                });
                cookies.setTag(this);
                mQueue.add(cookies);
            }
        });
        mListView.addFooterView(footer, null, false);
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.listitem_today_header, mListView, false);
        mListView.addHeaderView(header, null, false);
        mListView.setAdapter(mAdapter);

        getActivity().setTitle(getString(R.string.today));
        //setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
        ((Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar)).getMenu().clear();
    }

    @Override
    public void onPause(){
        super.onPause();
        App.getInstance().cancelPendingRequests(this);
    }

    /** A callback method invoked by the loader when initLoader() is called */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Log.d(Constants.TAG, "onCreateLoader");

        Time now = new Time();
        now.setToNow();
        String datum = now.format("%d-%m-%Y");
        String tijd = now.format("%H:%M");
        Uri uri = DBProvider.CONTENT_URI_SCHEDULE;
        String where = DB.KEY_DAT + " = '"+ datum +"' AND " + DB.KEY_END + " >= '" + tijd + "'";
        String orderby = DB.KEY_STT + " ASC";
        return new CursorLoader(getActivity(),uri,null,where,null,orderby);
    }

    /** A callback method, invoked after the requested content provider returned all the data */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        Log.d(Constants.TAG, "onLoadFinished");
        mAdapter.swapCursor(arg1);
        checkHead();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(Constants.TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

    public void checkHead(){
        RelativeLayout header = (RelativeLayout)getView().findViewById(R.id.header);
        header.removeAllViews();

        if (ScheduleHelper.isClassToday(getActivity()) == 0) {
            ViewGroup noClass = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.listitem_today_no_class, mListView, false);
            header.addView(noClass);
        }
    }

}