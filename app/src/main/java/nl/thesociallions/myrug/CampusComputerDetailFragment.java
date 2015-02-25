package nl.thesociallions.myrug;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.thesociallions.myrug.helper.Constants;

public class CampusComputerDetailFragment extends Fragment {
    private ArrayList<Room> roomlist = null;
    private RoomsListAdapter roomlistadap = null;


    public CampusComputerDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus_computer_detail, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.TITLE_computers));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = this.getArguments();
        int id = bundle.getInt("position");
        String buildinglist = bundle.getString("buildings");
        JSONObject wrapper = null;
        try {
            wrapper = new JSONObject(buildinglist);
            JSONArray buildinglis = wrapper.getJSONArray("buildingList");
            getDetailInfo(buildinglis, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getDetailInfo(JSONArray buildings, int id) {
        roomlist = new ArrayList<Room>();
        JSONObject building = null;
        try {
            building = buildings.getJSONObject(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray rooms = null;
        try {
            rooms = building.getJSONArray("room");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < rooms.length(); i++) {
            JSONObject room = null;
            try {
                room = rooms.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (room.getBoolean("RoomAvailable") == true) {
                    String name = room.getString("name");
                    String summary = room.getString("summary");
                    roomlist.add(new Room(name, summary));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        roomlistadap = new RoomsListAdapter(getActivity(), R.layout.listitem_campus_computer, roomlist);

        ProgressBar progress = (ProgressBar)getView().findViewById(R.id.progressBar);
        progress.setVisibility(View.INVISIBLE);
        ListView therooms = (ListView)getView().findViewById(R.id.therooms);
        therooms.setAdapter(roomlistadap);
        therooms.setVisibility(View.VISIBLE);

    }

    /**
     * People List Adapter
     * Adapts people data tot ListView compatible format
     **/
    class RoomsListAdapter extends ArrayAdapter<Room> {
        private List<Room> objects = null;

        public RoomsListAdapter(Context context, int textviewid, List<Room> objects) {
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
        public Room getItem(int position) {
            return ((null != objects) ? objects.get(position) : null);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if(null == view)
            { LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.listitem_campus_computer, null);
            }

            Room data = objects.get(position);
            if(null != data)
            {
                assert view != null;
                TextView name = (TextView)view.findViewById(R.id.textView);
                name.setText(data.name);
                ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBar);
                progress.setMax(100);
                Log.e(Constants.TAG, data.name);
                Log.e(Constants.TAG, data.summary);
                Log.e(Constants.TAG, Float.toString(data.progress));
                progress.setProgress(data.progress);
                TextView sum = (TextView)view.findViewById(R.id.textView2);
                sum.setText(data.summary);

            }
            return view;
        }
    }

    public class Room {
        final public String name;
        final public String summary;
        final public int progress;

        public Room(String roomName, String roomSummary) {
            name = roomName;

            if (roomSummary.length() > 0) {
                summary = roomSummary;
                String[] parts = summary.split("/");
                float part1 = Integer.valueOf(parts[0]);
                float part2 = Integer.valueOf(parts[1]);
                Float progr = (part2-part1)/part2;
                progr = progr * 100;
                progress = progr.intValue();
            } else {
                summary = getString(R.string.unknown);
                progress = 0;
            }

        }

    }

}