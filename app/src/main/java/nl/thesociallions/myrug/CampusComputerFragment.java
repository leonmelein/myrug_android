package nl.thesociallions.myrug;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.thesociallions.myrug.helper.Constants;
import nl.thesociallions.myrug.helper.Networking;


public class CampusComputerFragment extends Fragment {
    JSONObject results;

    public CampusComputerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus_computer, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.TITLE_computers));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new ResultGetter().execute();
    }

    private class ResultGetter extends AsyncTask<String, Void, String> {                            // TODO: Clean up hacked together code and anticipate on network errors (Captive Portal etc)
        private BuildingListAdapter buildinglistadap = null;
        private ArrayList<Building> buildinglst = null;

        @Override
        protected String doInBackground(String... params) {
            String result;

            buildinglst = new ArrayList<Building>();
            // Controleer of er internetverbinding is
            if (Networking.isConnected(getActivity())) {
                // Infrastructuur om cookies van de ene netwerkactie bij de volgende in te kunnen zetten.
                BasicCookieStore cs = new BasicCookieStore();
                HttpClient httpClient = Networking.getThreadSafeClient();
                HttpContext localContext = new BasicHttpContext();
                localContext.setAttribute(ClientContext.COOKIE_STORE, cs);
                HttpResponse response;


                // Netwerkactie 2: Cookie van My University ophalen.
                HttpGet getCookie = new HttpGet("http://statler.service.rug.nl:8080/pctool/PCBooked?output=json");


                // Voer netwerkacties uit en verkrijg saldo
                try {
                    response = httpClient.execute(getCookie, localContext);
                    HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString(entity);

                    results = new JSONObject(result);
                    JSONArray buildinglist = results.getJSONArray("buildingList");
                    for (int i = 0; i < buildinglist.length(); i++) {
                        JSONObject building = buildinglist.getJSONObject(i);
                        String name = building.getString("name");
                        String id = building.getString("id");
                        Building theBuilding = new Building(name, id);
                        buildinglst.add(theBuilding);
                    }
                    buildinglistadap = new BuildingListAdapter(getActivity(), android.R.layout.simple_list_item_2, buildinglst);

                    Log.e(Constants.TAG, "Got result ");
                    return result;
                }

                // Als de server niet reageert, dan is er iets met de server aan de hand.
                catch (Exception e) {
                    return Constants.CREDIT_NoServerResponse;
                }

            } else {
                // Fout 4: Geen internetverbinding
                return Constants.CREDIT_NoInternetConnection;
            }

        }

        @Override
        public void onPreExecute() {
            ListView listView = (ListView) getView().findViewById(R.id.listView);
            listView.setVisibility(View.INVISIBLE);
            ProgressBar progressSpinner = (ProgressBar) getView().findViewById(R.id.progressBar);
            progressSpinner.setVisibility(View.VISIBLE);
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String result) {
            ProgressBar progress = (ProgressBar) getView().findViewById(R.id.progressBar);
            progress.setVisibility(View.INVISIBLE);
            if (result == Constants.CREDIT_NoInternetConnection) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_nointernetconnection), Toast.LENGTH_LONG).show();
            } else if (result == Constants.CREDIT_NoServerResponse) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.people_error_noserverresponse), Toast.LENGTH_LONG).show();
            } else {
                ListView buildings = (ListView) getView().findViewById(R.id.listView);
                buildings.setAdapter(buildinglistadap);
                buildings.setVisibility(View.VISIBLE);
                buildings.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Bundle savestate = new Bundle();
                        onSaveInstanceState(savestate);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", position);
                        bundle.putString("buildings", results.toString());
                        Fragment fragment = new CampusComputerDetailFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                        transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                    }
                });
            }
            super.onPostExecute(result);
        }
    }

    /**
     * People List Adapter
     * Adapts people data tot ListView compatible format
     */
    class BuildingListAdapter extends ArrayAdapter<Building> {
        private List<Building> objects = null;

        public BuildingListAdapter(Context context, int textviewid, List<Building> objects) {
            super(context, textviewid, objects);
            this.objects = objects;
        }

        @Override
        public int getCount() {
            return ((null != objects) ? objects.size() : 0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Building getItem(int position) {
            return ((null != objects) ? objects.get(position) : null);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (null == view) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(android.R.layout.simple_list_item_2, null);
            }

            Building data = objects.get(position);
            if (null != data) {
                assert view != null;
                TextView name = (TextView) view.findViewById(android.R.id.text1);
                name.setText(data.name);
                TextView id = (TextView) view.findViewById(android.R.id.text2);
                id.setText(data.id);
            }
            return view;
        }
    }

    public class Building {
        final public String name;
        final public String id;

        public Building(String buildingName, String buildingID) {
            name = buildingName;
            id = buildingID;
        }
    }
}