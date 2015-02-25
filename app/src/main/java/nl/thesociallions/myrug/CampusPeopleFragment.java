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
import com.android.volley.toolbox.JsonArrayRequest;

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
public class CampusPeopleFragment extends Fragment{
    Toolbar mToolbar;

    public CampusPeopleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_campus_people, container, false);
        getActivity().setTitle(getString(R.string.TITLE_people));

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
        SearchView search = (SearchView) (mToolbar.getMenu().findItem(R.id.menu_search)).getActionView();
        search.setQueryHint(getString(R.string.PEOPLE_hint));
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

        JsonArrayRequest req = new JsonArrayRequest("http://gadgets.rug.nl/gadgets/addressbook/search?q=" + query, new Response.Listener<JSONArray> () {
            @Override
            public void onResponse(JSONArray response) {
                PeopleAdapter peoplelistadap;
                ArrayList<Persons> peoplelist = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    String name = null;
                    String email = null;
                    String web = null;
                    try {
                        JSONObject person = response.getJSONObject(i);
                        name = person.getString("firstName") + " " +  person.getString("lastName");
                        email = person.getString("email");
                        web = "http://www.rug.nl/" + person.getString("mepaHome");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Persons new_person = new Persons(name, email, web);
                    peoplelist.add(new_person);
                }
                peoplelistadap = new PeopleAdapter(peoplelist);
                progress.setVisibility(View.GONE);
                pers.setAdapter(peoplelistadap);
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

    /**
     * RecyclerView adapter
     */
    public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ContactViewHolder> {

        private List<Persons> contactList;

        public PeopleAdapter(List<Persons> contactList) {
            this.contactList = contactList;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        @Override
        public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
            Persons ci = contactList.get(i);
            contactViewHolder.vName.setText(ci.name);
            contactViewHolder.vWeb.setOnClickListener(new webListener(ci.web));
            contactViewHolder.vEmail.setOnClickListener(new emailListener(ci.email));
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.listitem_campus_people, viewGroup, false);

            return new ContactViewHolder(itemView);
        }

        public class ContactViewHolder extends RecyclerView.ViewHolder {
            protected TextView vName;
            protected TextView vWeb;
            protected TextView vEmail;

            public ContactViewHolder(View v) {
                super(v);
                vName =  (TextView) v.findViewById(R.id.name);
                vWeb = (TextView)  v.findViewById(R.id.web);
                vWeb = (Button)  v.findViewById(R.id.web);
                vEmail = (Button)  v.findViewById(R.id.email);
            }
        }
    }

    public class emailListener implements View.OnClickListener {
        private String email;

        public emailListener(String email){
            this.email = email;
        }

        public void onClick(View v) {
            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("text/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
            startActivity(Intent.createChooser(Email, "Send an Email"));
        }
    }

    public class webListener implements View.OnClickListener {
        private String web;

        public webListener(String web){
            this.web = web;
        }

        public void onClick(View v) {
            Intent Web = new Intent(Intent.ACTION_VIEW);
            Web.setData(Uri.parse(web));
            startActivity(Web);
        }
    }

    public class Persons {
        final public String name;
        final public String email;
        final public String web;

        public Persons(String fullname, String emailaddress, String website){
            name = fullname;
            email = emailaddress;
            web = website;
        }
    }
}
