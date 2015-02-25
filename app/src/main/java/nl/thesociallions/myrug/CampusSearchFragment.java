package nl.thesociallions.myrug;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 14-12-14.
 */
public class CampusSearchFragment extends Fragment{
    Toolbar mToolbar;

    public CampusSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus_search, container, false);
        getActivity().setTitle(getString(R.string.TITLE_locations));

        RecyclerView pers = (RecyclerView)rootView.findViewById(R.id.schedule);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        pers.setLayoutManager(llm);
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                switch (item.getItemId())
                {
                    case R.id.menu_search:
                        return true;
                }
                return false;
            }
        });

        // Inflate a menu to be displayed in the app_toolbar
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.campussearch);
        final SearchView search = (SearchView) (mToolbar.getMenu().findItem(R.id.menu_search)).getActionView();
        search.setQueryHint(getString(R.string.LOCATION_hint));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                try {
                    search(URLEncoder.encode(s, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mToolbar.getMenu().findItem(R.id.menu_search).expandActionView();
    }

    public void search(String query){
        final RecyclerView pers = (RecyclerView)getView().findViewById(R.id.schedule);
        final ProgressBar progress = (ProgressBar)getView().findViewById(R.id.progressBar);
        pers.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        JsonArrayRequest req = new JsonArrayRequest("http://stdy.it/app/location/?location=" + query, new Response.Listener<JSONArray> () {
            @Override
            public void onResponse(JSONArray response) {
                LocationAdapter locationAdapter;
                ArrayList<Location> locationList = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    String name = null;
                    String faculty = null;
                    String buildingid = null;
                    String img = null;
                    Double lat = null;
                    Double lng = null;
                    try {
                        JSONObject building = response.getJSONObject(i);
                        name = building.getString("name");
                        faculty = building.getString("faculty");
                        buildingid = building.getString("buildingid");
                        img = building.getString("image");
                        lat = Double.valueOf(building.getString("lat"));
                        lng = Double.valueOf(building.getString("lng"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Location location = new Location(name, faculty, buildingid, img, lat, lng);
                    locationList.add(location);
                }
                locationAdapter = new LocationAdapter(locationList);
                progress.setVisibility(View.GONE);
                pers.setAdapter(locationAdapter);
                pers.setVisibility(View.VISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        App.getInstance().addToRequestQueue(req);
    }

    public class mapsListener implements View.OnClickListener {
        private String location;

        public mapsListener(Double lat, Double lng, String name) {
            this.location = "geo:0,0?q=" + String.valueOf(lat) + "," + String.valueOf(lng) + "(" + name + ")";
        }

        public void onClick(View v) {
            Intent Map = new Intent(Intent.ACTION_VIEW);
            Map.setData(Uri.parse(location));
            startActivity(Map);
        }
    }

    public class Location {
        final public String name;
        final public String faculty;
        final public String buildingid;
        final public String img;
        final public Double lat;
        final public Double lng;

        public Location(String theName, String theFaculty, String theId, String theImg, Double theLat, Double theLng) {
            name = theName;
            faculty = theFaculty;
            buildingid = theId;
            img = theImg;
            lat = theLat;
            lng = theLng;
        }
    }

    /**
     * RecyclerView adapter
     */
    public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ContactViewHolder> {
        ImageLoader imageLoader = App.getInstance().getImageLoader();


        private List<Location> contactList;

        public LocationAdapter(List<Location> contactList) {
            this.contactList = contactList;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        @Override
        public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
            Location ci = contactList.get(i);
            contactViewHolder.vName.setText(ci.name);
            contactViewHolder.vFaculty.setText(ci.faculty);
            contactViewHolder.vBuildingID.setText(ci.buildingid);
            contactViewHolder.vImage.setImageUrl(ci.img, imageLoader);
            contactViewHolder.vLocation.setOnClickListener(new mapsListener(ci.lat, ci.lng, ci.name));
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.listitem_campus_search_image, viewGroup, false);

            return new ContactViewHolder(itemView);
        }

        public class ContactViewHolder extends RecyclerView.ViewHolder {
            protected TextView vName;
            protected TextView vFaculty;
            protected TextView vBuildingID;
            protected TextView vLocation;
            protected NetworkImageView vImage;


            public ContactViewHolder(View v) {
                super(v);
                vName =  (TextView) v.findViewById(R.id.name);
                vFaculty = (TextView)  v.findViewById(R.id.faculty);
                vBuildingID = (TextView)  v.findViewById(R.id.buildingid);
                vLocation = (Button)  v.findViewById(R.id.maps);
                vImage = (NetworkImageView)  v.findViewById(R.id.image);
            }
        }
    }
}
