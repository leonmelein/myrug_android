package thesociallions.myrug;

/**
 * Created by leon on 14-12-14.
 */
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class CampusDetailFragment extends Fragment {
    private double lat;
    private double lon;

    public CampusDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus_detail, container, false);
        setHasOptionsMenu(true);


        Bundle bundle = this.getArguments();
        LatLng pos = bundle.getParcelable("location");
        String name = bundle.getString("name");
        lat = pos.latitude;
        lon = pos.longitude;

        getActivity().setTitle(name);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getBuilding();
    }

    @Override
    public void onPause(){
        super.onPause();
        App.getInstance().cancelRequests(this);
    }

    public void getBuilding(){
        TextView naam = (TextView)getView().findViewById(R.id.title);
        TextView nummer = (TextView)getView().findViewById(R.id.buildingid);
        TextView faculteit = (TextView)getView().findViewById(R.id.faculty);
        TextView beschrijving = (TextView) getView().findViewById(R.id.description);

        naam.setVisibility(View.INVISIBLE);
        nummer.setVisibility(View.INVISIBLE);
        faculteit.setVisibility(View.INVISIBLE);
        beschrijving.setVisibility(View.INVISIBLE);

        ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest locationRequest = new JsonObjectRequest
                (Request.Method.GET, String.format(getString(R.string.location_nearby_url), String.valueOf(lat), String.valueOf(lon)), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject building = response.getJSONObject("item");
                            String theName = building.getString("name");
                            String theBuilding = building.optString("id", "----");
                            String theFaculty = building.optString("faculty");
                            String theDescription = building.optString("description");

                            ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
                            progressBar.setVisibility(View.GONE);

                            TextView naam = (TextView) getView().findViewById(R.id.title);
                            TextView nummer = (TextView) getView().findViewById(R.id.buildingid);
                            TextView faculteit = (TextView) getView().findViewById(R.id.faculty);
                            TextView beschrijving = (TextView) getView().findViewById(R.id.description);

                            naam.setText(theName);
                            nummer.setText(theBuilding);
                            faculteit.setText(theFaculty);
                            beschrijving.setText(theDescription);

                            naam.setVisibility(View.VISIBLE);
                            nummer.setVisibility(View.VISIBLE);
                            faculteit.setVisibility(View.VISIBLE);
                            beschrijving.setVisibility(View.VISIBLE);

                            // Image
                            ImageLoader imageLoader = App.getInstance().getImageLoader();
                            NetworkImageView img = (NetworkImageView) getView().findViewById(R.id.image);
                            img.setImageUrl(building.getString("image"), imageLoader);
                            img.setVisibility(View.VISIBLE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getString(R.string.campus_error), Toast.LENGTH_LONG).show();
                    }
                });
        App.getInstance().addToRequestQueue(locationRequest);
    }
}
