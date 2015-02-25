package nl.thesociallions.myrug;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import nl.thesociallions.myrug.helper.Constants;

/**
 * Created by leon on 14-12-14.
 */
public class CampusFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Location mLocation;
    Toolbar mToolbar;
    String locationname;
    LatLng closestLoc = new LatLng(0, 0);
    private GoogleApiClient mGoogleApiClient;


    public CampusFragment() {
    }

    /**
     * Lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus, container, false);
        getActivity().setTitle("Campus");

        NetworkImageView image = (NetworkImageView)rootView.findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", locationname);
                bundle.putParcelable("location", closestLoc);
                Fragment fragment = new CampusDetailFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            }
        });
        Button computer = (Button)rootView.findViewById(R.id.computers);
        computer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                computeropen();
            }
        });
        Button people = (Button)rootView.findViewById(R.id.persons);
        people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peopleopen();
            }
        });
        Button location = (Button)rootView.findViewById(R.id.locations);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationopen();
            }
        });
        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
        mToolbar.getMenu().clear();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause(){
        super.onPause();
        App.getInstance().cancelRequests(this);
        mGoogleApiClient.disconnect();
    }

    public void computeropen () {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate();
        //Fragment fragment = new ComputerFragment();
        Fragment fragment = new CampusComputerFragment();
        fragmentManager.beginTransaction().addToBackStack("main").replace(R.id.content_frame, fragment).commit();
        getActivity().setTitle(getString(R.string.TITLE_computers));
    }

    public void peopleopen () {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate();
        Fragment fragment = new CampusPeopleFragment();
        fragmentManager.beginTransaction().addToBackStack("main").replace(R.id.content_frame, fragment).commit();
        getActivity().setTitle(getString(R.string.TITLE_people));
    }

    public void locationopen () {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate();
        Fragment fragment = new CampusSearchFragment();
        fragmentManager.beginTransaction().addToBackStack("main").replace(R.id.content_frame, fragment).commit();
        getActivity().setTitle(getString(R.string.TITLE_locations));
    }

    /**
     * Information getters
     */
    public void getLocation(){
        final TextView naam = (TextView)getView().findViewById(R.id.location);
        NetworkImageView building = (NetworkImageView) getView().findViewById(R.id.image);
        building.setVisibility(View.INVISIBLE);
        naam.setVisibility(View.GONE);
        ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest locationRequest = new JsonObjectRequest
                (Request.Method.GET, String.format(getString(R.string.location_nearby_url), String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude())), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e(Constants.TAG, "CAMPUS LOAD");
                            // Progress Bar
                            ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);

                                // Building Name
                                JSONObject theBuilding = response.getJSONObject("item");
                                locationname = theBuilding.getString("name");
                                naam.setText(locationname);
                                naam.setVisibility(View.VISIBLE);
                                closestLoc = new LatLng(theBuilding.getDouble("lat"),theBuilding.getDouble("lng"));

                                // Image
                                ImageLoader imageLoader = App.getInstance().getImageLoader();
                                NetworkImageView building = (NetworkImageView) getView().findViewById(R.id.image);
                                building.setImageUrl(theBuilding.getString("image"), imageLoader);
                                building.setVisibility(View.VISIBLE);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                            NetworkImageView building = (NetworkImageView) getView().findViewById(R.id.image);
                            building.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), getString(R.string.campus_error), Toast.LENGTH_LONG).show();
                        }
                    }
        });
        locationRequest.setTag(this);
        App.getInstance().addToRequestQueue(locationRequest);
    }

    /**
     * Location Services connectors
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting() && mLocation != null) {
            getLocation();
        } else {
            NetworkImageView building = (NetworkImageView) getView().findViewById(R.id.image);
            building.setVisibility(View.GONE);
            ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Constants.TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(Constants.TAG, "GoogleApiClient connection has failed");
    }

}