package com.modestie.modestieapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.EventListAdapter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class EventListActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<EventListAdapter.EventListCardViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton fab;

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

        recyclerView = findViewById(R.id.eventsCardsView);
        /*fab = findViewById(R.id.add_event_fab);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) //Up
                {
                    fab.hide();
                }
                else //Down
                {
                    fab.show();
                }
            }
        });

        fab.setOnClickListener(v ->
            {
                startActivityForResult(new Intent(getApplicationContext(), NewEventActivity.class), 1);
            });*/

        FreeCompanyDbHelper dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        ArrayList<Integer> participants = new ArrayList<>();
        participants.add(6287877);
        participants.add(14416025);
        participants.add(11572096);
        participants.add(6710801);
        participants.add(14194163);

        ArrayList<Integer> participants2 = (ArrayList<Integer>) participants.clone();
        ArrayList<Integer> participants3 = (ArrayList<Integer>) participants.clone();
        ArrayList<Integer> participants4 = (ArrayList<Integer>) participants.clone();
        participants4.add(11148489);

        //https://img.finalfantasyxiv.com/lds/promo/h/T/1dnML8rMAkaWePJz0AZsY9Vf18.jpg

        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event(
                "Top des tops",
                6655397,
                1559390400L,
                "https://cdn.discordapp.com/attachments/503700802242609162/580093259007787008/tdtr.png",
                "Ca y est, Shadow Bringer arrive et l'été est avec lui. Jamais deux sans trois, le top des tops fête aussi son grand retour. \n\n" +
                "Immanquablement, toute cette hype, toute cette tension et toute cette chaleur provoquent des réactions chimiques, souvent prétextes à des " +
                        "manifestations douteuses. Mais pas ici, nous continuerons à réagir aux stimulis modestement. Je profite donc de l'éveil de nos sens, " +
                        "pour vous proposer cette fois-çi, un concours de screenshot incroyable, qui mettra à l'épreuve votre sens de l'observation et l'ardeur " +
                        "de vos désirs.\n\nMoult goudizes à gagner, dignes d'une arnaque moldave !",
                20,
                participants));

        events.add(new Event(
                "Banco Bingo",
                11148489,
                1559563200L,
                "https://xivapi.com/img-misc/061643.png",
                null,
                -1,
                participants4));

        events.add(new Event(
                "Modeste booty contest",
                6287877,
                1559476800L,
                null,
                "Statut «Modeste booty» à la clé !",
                10,
                participants2));

        events.add(new Event(
                "Practice O12S",
                6655397,
                1558634400L,
                "https://i.ytimg.com/vi/RxfoGKxNzb0/maxresdefault.jpg",
                "\"SORTEZ-VOUS LES DOIGTS DU CUUUUUUUUUUUL !\" A dit un grand homme. Marchons dans sa lumière.",
                8,
                participants3));

        Collections.sort(events, Event.EventDateComparator);

        // used to improve performance if changes in content do not change the layout size of
        // the RecyclerView
        //recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EventListAdapter(events, database, getApplicationContext());
        recyclerView.setAdapter(adapter);

        /*findViewById(R.id.add_event_fab).setOnClickListener(v ->
            {
                ArrayList<Integer> parts = new ArrayList<>();
                participants.add(6287877);
                participants.add(14416025);
                participants.add(11572096);
                participants.add(6710801);
                participants.add(14194163);

                events.add(new Event(
                        "Nouvel event",
                        11148489,
                        System.currentTimeMillis() / 1000,
                        null,
                        null,
                        -1,
                        parts));

                Collections.sort(events, Event.EventDateComparator);

                adapter.notifyDataSetChanged();
            });*/
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
}
