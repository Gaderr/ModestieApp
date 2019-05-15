package com.modestie.modestieapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.character.Character;
import com.modestie.modestieapp.sqlite.CharacterDbHelper;
import com.modestie.modestieapp.sqlite.CharacterReaderContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.android.volley.Request.Method.GET;

public class CharacterActivity extends AppCompatActivity
{
    public static final String TAG = "ACTIVITY - CHARACTER";

    private int characterID;
    private Character character;
    private String name;

    private String apiURL = "https://xivapi.com";
    private String apiCharacterURL = "/character/";
    private String apiCharacterExtra_Get = "?language=fr&extended=1";
    private String apiCharacterExtra_Update = "/update";
    //private String apiURLRequest = apiURL + "/character/11148489?language=fr&extended=1";
    private RequestQueue mRequestQueue;

    private ImageView upAction;

    private ImageView jobIcon;
    private RoundedImageView portrait;
    private Map<String, ImageView> itemImageViews;

    private TextView classJobName;
    private TextView classJobLevel;
    private TextView characterName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        this.upAction = findViewById(R.id.upAction);
        this.upAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                navigateUp();
            }
        });

        Intent intent = getIntent();
        this.characterID = intent.getIntExtra("CharacterID", 0);
        this.name = intent.getStringExtra("Name");

        if(this.characterID == 0)
        {
            Toast.makeText(this, "Erreur à la récupération du personnage", Toast.LENGTH_SHORT).show();
            navigateUp();
        }

        jobIcon = findViewById(R.id.jobIcon);
        portrait = findViewById(R.id.portrait);
        this.itemImageViews = new HashMap<>();
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[0], (ImageView) findViewById(R.id.itemMainHandIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[1], (ImageView) findViewById(R.id.itemHeadIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[2], (ImageView) findViewById(R.id.itemBodyIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[3], (ImageView) findViewById(R.id.itemHandsIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[4], (ImageView) findViewById(R.id.itemWaistIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[5], (ImageView) findViewById(R.id.itemLegsIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[6], (ImageView) findViewById(R.id.itemFeetIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[7], (ImageView) findViewById(R.id.itemOffHandIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[8], (ImageView) findViewById(R.id.itemEarringIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[9], (ImageView) findViewById(R.id.itemNecklaceIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[10], (ImageView) findViewById(R.id.itemBraceletIcon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[11], (ImageView) findViewById(R.id.itemRing1Icon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[12], (ImageView) findViewById(R.id.itemRing2Icon));
        this.itemImageViews.put(Character.GEAR_ITEM_KEYS[13], (ImageView) findViewById(R.id.itemSoulCrystalIcon));

        this.classJobName = findViewById(R.id.classJobName);
        this.classJobLevel = findViewById(R.id.classJobLevel);
        this.characterName = findViewById(R.id.characterName);
        characterName.setText(this.name); //Set immediately to adapt layout

        CharacterDbHelper characterDbHelper = new CharacterDbHelper(getApplicationContext());
        SQLiteDatabase database = characterDbHelper.getWritableDatabase();
        characterDbHelper.onCreate(database);

        Cursor cursor = database.rawQuery(
                "SELECT " + CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_LAST_UPDATE +
                        " FROM " + CharacterReaderContract.CharacterUpdateEntry.TABLE_NAME +
                        " WHERE " + CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_CHARACTER_ID +
                        "=" + this.characterID, null);
        if(cursor.moveToFirst())
        {
            long currentTime = System.currentTimeMillis() / 1000;
            int lastUpdate = cursor.getInt(cursor.getColumnIndex(CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_LAST_UPDATE));
            //DO UPDATE IF TOO OLD
            if(currentTime - lastUpdate > 43200) // > 12 hours
            {
                updateCharacterAPI(characterDbHelper);
            }
            else
            {
                getCharacterAPI(characterDbHelper);
            }
        }
        else
        {
            updateCharacterAPI(characterDbHelper);
        }

        cursor.close();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(character != null && character.isLoaded())
            updateCharacterViews();
    }

    private void hideCharacterViews()
    {
        jobIcon.setVisibility(View.INVISIBLE);
    }

    private void showCharacterViews()
    {
        jobIcon.setVisibility(View.VISIBLE);
    }

    private void updateCharacterViews()
    {
        hideCharacterViews();

        characterName.setText(this.name);

        if(character.getGearItems().get("SoulCrystal") != null)
            Picasso.get()
                    .load(apiURL + character.getActiveClassJob().get_job().getIconURL())
                    .fit()
                    .into(jobIcon);
        else
            Picasso.get()
                    .load(apiURL + character.getActiveClassJob().get_class().getIconURL())
                    .fit()
                    .into(jobIcon);


        final Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(5)
                .oval(false)
                .build();

        Picasso.get()
                .load(character.getPortraitURL())
                .fit()
                .centerCrop()
                .transform(transformation)
                .into(portrait);

        for(String gearItemKey : Character.getGearItemKeys())
        {
            if(character.getGearItems().get(gearItemKey) != null)
                Picasso.get()
                        .load(apiURL + character.getGearItems().get(gearItemKey).getItemIcon())
                        .fit()
                        .into(this.itemImageViews.get(gearItemKey));
        }

        String name;
        if(character.getGearItems().get("SoulCrystal") != null)
            name = character.getActiveClassJob().get_job().getName();
        else
            name = character.getActiveClassJob().get_class().getName();

        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        classJobName.setText(name);
        classJobLevel.setText(String.format(Locale.FRANCE, "niveau %d", character.getActiveClassJob().getLevel()));

        showCharacterViews();
    }

    private void updateCharacterAPI(final CharacterDbHelper dbHelper)
    {
        String request = this.apiURL + this.apiCharacterURL + this.characterID + this.apiCharacterExtra_Update;
        addToRequestQueue(new StringRequest(GET, request, new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            //Log.e(TAG, "Update response received : [" + response + "]");
                            getCharacterAPI(dbHelper);
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(CharacterActivity.this, "Échec de la mise à jour du personnage", Toast.LENGTH_SHORT).show();
                            navigateUp();
                            finish();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(CharacterActivity.this, "Échec de la mise à jour du personnage", Toast.LENGTH_SHORT).show();
                        //Log.e(TAG, error.getMessage());
                        navigateUp();
                        finish();
                    }
                }));
    }

    private void getCharacterAPI(final CharacterDbHelper dbHelper)
    {
        String request = this.apiURL + this.apiCharacterURL + this.characterID + this.apiCharacterExtra_Get;
        addToRequestQueue(new JsonObjectRequest(GET, request, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            character = new Character(response.getJSONObject("Character"), dbHelper);

                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {

                                    while(!character.isLoaded()) {}

                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Log.e(TAG, "Data acquired");
                                            updateCharacterViews();
                                        }
                                    });
                                }
                            }).run();
                        }
                        catch (Exception e)
                        {
                            //Log.e(TAG, e.getMessage());
                            Toast.makeText(CharacterActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                            navigateUp();
                            finish();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //Toast.makeText(CharacterActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                        navigateUp();
                        finish();
                    }
                }));
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null)
        {
            this.mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

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

    private void navigateUp()
    {
        NavUtils.navigateUpFromSameTask(this);
    }
}
