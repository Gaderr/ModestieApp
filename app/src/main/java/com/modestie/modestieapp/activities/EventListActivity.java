package com.modestie.modestieapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.EventListAdapter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.android.volley.Request.Method.GET;

public class EventListActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<EventListAdapter.EventListCardViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;

    private ArrayList<Event> events;

    private String MODESTIE_GETEVENTS = "https://modestie.fr/wp-json/modestieevents/v1/events";

    private RequestQueue mRequestQueue;

    private int NEW_EVENT_REQUEST = 1;

    public static final String TAG = "ACTVT - EVNTLST";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        //TODO Use setItemAnimator() for animating changes to the items in the RecyclerView

        Toolbar toolbar = findViewById(R.id.eventListToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.recyclerView = findViewById(R.id.eventsCardsView);
        this.progressBar = findViewById(R.id.progressBar);

        FreeCompanyDbHelper dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        this.events = new ArrayList<>();

        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setLayoutManager(this.layoutManager);

        this.adapter = new EventListAdapter(this.events, database, getApplicationContext());
        this.recyclerView.setAdapter(this.adapter);

        addToRequestQueue(new JsonObjectRequest(
                GET, this.MODESTIE_GETEVENTS, null,
                response ->
                {
                    try
                    {
                        JSONArray eventsArray = response.getJSONArray("Events");
                        int count = response.getInt("Count");
                        for (int i = 0; i < count; i++)
                        {
                            JSONObject tempEvent = eventsArray.getJSONObject(i);
                            this.events.add(new Event(tempEvent));
                        }
                        this.adapter.notifyDataSetChanged();
                        this.progressBar.setVisibility(View.INVISIBLE);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                    }

                }, error ->
                {
                    Toast.makeText(EventListActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
        ));

        Collections.sort(this.events, Event.EventDateComparator);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("nightmode", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_list_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_event)
        {
            startActivityForResult(new Intent(getApplicationContext(), NewEventActivity.class), this.NEW_EVENT_REQUEST);
            return true;
        }

        if(id == R.id.refresh)
        {
            reloadList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == NEW_EVENT_REQUEST)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {
                reloadList();
            }

            if(resultCode == RESULT_CANCELED)
            {
                if(data.hasExtra("Error"))
                    Toast.makeText(this, "Echec de l'envoi, nous rencontrons des difficultés techniques.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Envoi annulé.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void reloadList()
    {
        this.events.clear();
        this.adapter.notifyDataSetChanged();
        this.progressBar.setVisibility(View.VISIBLE);
        addToRequestQueue(new JsonObjectRequest(
                GET, this.MODESTIE_GETEVENTS, null,
                response ->
                {
                    try
                    {
                        JSONArray eventsArray = response.getJSONArray("Events");
                        int count = response.getInt("Count");
                        for (int i = 0; i < count; i++)
                        {
                            JSONObject tempEvent = eventsArray.getJSONObject(i);
                            this.events.add(new Event(tempEvent));
                        }
                        this.adapter.notifyDataSetChanged();
                        this.progressBar.setVisibility(View.INVISIBLE);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                    }

                }, error ->
                {
                    Toast.makeText(EventListActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
        ));

        Collections.sort(this.events, Event.EventDateComparator);
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     *
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null)
            this.mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag)
    {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }
}
