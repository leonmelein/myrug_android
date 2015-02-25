package thesociallions.myrug;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nl.thesociallions.myrug.helper.DB;

public class ScheduleDetailFragment extends Fragment {
    String maplocation;
    String maplocationname;
    Uri scheduleItemUri;
    Toolbar mToolbar;
    MapView mapView;

    public ScheduleDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_detail, container, false);
        getActivity().setTitle(getString(R.string.TITLE_schedule_detail));
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Check if savedInstanceState has an URI from a previous Pause event.
        scheduleItemUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable("URI");

        Bundle bundle = this.getArguments();
        scheduleItemUri = bundle.getParcelable("URI");
        getDetailInfo(scheduleItemUri, savedInstanceState);

        mToolbar = (Toolbar)getActivity().findViewById(R.id.my_awesome_toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();

                switch (id){
                    case R.id.action_ocasys:
                        String url;
                        String lookup_url = getString(R.string.SCHEDULE_DETAIL_ocasys_url);
                        String course = (String) ((TextView)getView().findViewById(R.id.title)).getText();
                        try {
                            url = lookup_url + URLEncoder.encode(course, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            url = lookup_url + course;
                        }

                        Intent web = new Intent(Intent.ACTION_VIEW);
                        web.setData(Uri.parse(url));
                        startActivity(web);
                        return true;
                }
                return false;
            }
        });

        // Inflate a menu to be displayed in the app_toolbar
        mToolbar.inflateMenu(R.menu.scheduledetail);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("URI", scheduleItemUri);
    }

    private void getDetailInfo(Uri uri, Bundle savedInstanceState) {
        String[] projection = {DB.KEY_DAT, DB.KEY_END, DB.KEY_STT, DB.KEY_LOC, DB.KEY_LOC_MAP, DB.KEY_LOC_MAP_NAME, DB.KEY_SUB, DB.KEY_TYPE, DB.KEY_ROW_ID};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {

            // Setting Schedule item data in UI
            cursor.moveToFirst();
            String theTitle = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_SUB));
            String theType = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_TYPE));
            String theStart = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_STT));
            String theEnd = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_END));
            String theTimespan = "van " + theStart + " tot " + theEnd; // TODO: Localize
            String theLocation = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_LOC));
            maplocation = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_LOC_MAP));
            maplocationname = cursor.getString(cursor.getColumnIndexOrThrow(DB.KEY_LOC_MAP_NAME));


            TextView title = (TextView) getView().findViewById(R.id.title);
            TextView type = (TextView) getView().findViewById(R.id.type);
            TextView time = (TextView) getView().findViewById(R.id.time);
            TextView location = (TextView) getView().findViewById(R.id.location);

            title.setText(theTitle);
            type.setText(theType);
            time.setText(theTimespan);
            location.setText(theLocation);

            cursor.close();

            // Setting up map with item's location);
            mapView = (MapView) getView().findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);

            GoogleMap map = mapView.getMap();
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(false);


            String[] parts = maplocation.split(";");
            if (!parts[0].equals("null")) {
                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                MapsInitializer.initialize(this.getActivity());
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(Float.valueOf(parts[0]), Float.valueOf(parts[1])))
                        .title(maplocationname))
                        .showInfoWindow();


                // Updates the location and zoom of the MapView
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(Float.valueOf(parts[0]), Float.valueOf(parts[1])), 17);
                map.animateCamera(cameraUpdate);
                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        mToolbar.getMenu().clear();
                        Bundle bundle = new Bundle();
                        bundle.putString("name", marker.getTitle());
                        bundle.putParcelable("location", marker.getPosition());
                        Fragment fragment = new CampusDetailFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                        transaction.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
