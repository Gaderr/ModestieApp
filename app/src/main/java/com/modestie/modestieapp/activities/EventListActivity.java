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

        /*ArrayList<Integer> participants = new ArrayList<>();
        participants.add(6287877);
        participants.add(14416025);
        participants.add(11572096);
        participants.add(6710801);
        participants.add(14194163);

        ArrayList<Integer> participants2 = (ArrayList<Integer>) participants.clone();
        ArrayList<Integer> participants3 = (ArrayList<Integer>) participants.clone();
        ArrayList<Integer> participants4 = (ArrayList<Integer>) participants.clone();
        participants3.add(11148489);
        participants4.add(11148489);*/

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
                        for(int i = 0; i < count - 1 ; i++)
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

                }, error -> Toast.makeText(EventListActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show()
                ));

        //https://img.finalfantasyxiv.com/lds/promo/h/T/1dnML8rMAkaWePJz0AZsY9Vf18.jpg

        /*events.add(new Event(
                "Top des tops",
                6655397,
                1559390400L,
                "https://cdn.discordapp.com/attachments/503700802242609162/580093259007787008/tdtr.png",
                "Ca y est, Shadow Bringer arrive et l'été est avec lui. Jamais deux sans trois, le top des tops fête aussi son grand retour.\n\n" +
                "Immanquablement, toute cette hype, toute cette tension et toute cette chaleur provoquent des réactions chimiques, souvent prétextes à des " +
                        "manifestations douteuses. Mais pas ici, nous continuerons à réagir aux stimulis modestement. Je profite donc de l'éveil de nos sens, " +
                        "pour vous proposer cette fois-çi, un concours de screenshot incroyable, qui mettra à l'épreuve votre sens de l'observation et l'ardeur " +
                        "de vos désirs.\n\nMoult goudizes à gagner, dignes d'une arnaque moldave !",
                20,
                participants));*/

        /*events.add(new Event(
                "Banco Bingo",
                11148489,
                1559563200L,
                "https://xivapi.com/img-misc/061643.png",
                null,
                -1,
                participants4,
                new ArrayList<>()));

        events.add(new Event(
                "Modeste booty contest",
                6287877,
                1559476800L,
                null,
                "Statut «Modeste booty» à la clé !",
                10,
                participants2,
                new ArrayList<>()));

        events.add(new Event(
                "Practice O12S",
                11148489,
                1558634400L,
                "https://i.ytimg.com/vi/RxfoGKxNzb0/maxresdefault.jpg",
                "\"SORTEZ-VOUS LES DOIGTS DU CUUUUUUUUUUUL !\" A dit un grand homme. Marchons dans sa lumière.",
                8,
                participants3,
                new ArrayList<>()));*/

        Collections.sort(this.events, Event.EventDateComparator);

        // used to improve performance if changes in content do not change the layout size of
        // the RecyclerView
        //recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPref.getBoolean("nightmode", false))
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
            startActivityForResult(new Intent(getApplicationContext(), NewEventActivity.class), 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
