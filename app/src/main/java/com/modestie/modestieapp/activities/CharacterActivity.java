package com.modestie.modestieapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.character.ExtendedCharacter;
import com.modestie.modestieapp.sqlite.CharacterDbHelper;
import com.modestie.modestieapp.sqlite.CharacterReaderContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.android.volley.Request.Method.GET;

public class CharacterActivity extends AppCompatActivity
{
    public static final String TAG = "ACTVT.CHRCTR";

    private int characterID;
    private ExtendedCharacter character;
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

    private TextView ilvlTextView;

    private TextView classJobName;
    private TextView classJobLevel;
    private TextView characterName;

    //3 firsts base character attributes (HP, MP, etc.)
    private TextView param1Label;
    private TextView param2Label;
    private TextView param3Label;
    private ImageView param1Bar;
    private ImageView param2Bar;
    private ImageView param3Bar;
    private TextView param1Value;
    private TextView param2Value;
    private TextView param3Value;

    //ExtendedCharacter attributes layouts
    private ConstraintLayout globalAttributesLayout;
    private ConstraintLayout fighterAttributesLayout;
    private ConstraintLayout crafterAttributesLayout;

    //Global character attributes layouts
    private LinearLayout labelsAttributesLayout;
    private LinearLayout valuesAttributesLayout;
    private LinearLayout labelsOffensivePropsLayout;
    private LinearLayout valuesOffensivePropsLayout;
    private LinearLayout labelsDefensivePropsLayout;
    private LinearLayout valuesDefensivePropsLayout;
    private LinearLayout labelsPhysicalPropsLayout;
    private LinearLayout valuesPhysicalPropsLayout;
    private LinearLayout labelsCrafterPropsLayout;
    private LinearLayout valuesCrafterPropsLayout;
    private LinearLayout labelsMentalPropsLayout;
    private LinearLayout valuesMentalPropsLayout;
    private LinearLayout labelsRolePropsLayout;
    private LinearLayout valuesRolePropsLayout;


    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        this.upAction = findViewById(R.id.upAction);
        this.upAction.setOnClickListener(v -> navigateUp());

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
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[0], findViewById(R.id.itemMainHandIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[1], findViewById(R.id.itemHeadIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[2], findViewById(R.id.itemBodyIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[3], findViewById(R.id.itemHandsIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[4], findViewById(R.id.itemWaistIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[5], findViewById(R.id.itemLegsIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[6], findViewById(R.id.itemFeetIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[7], findViewById(R.id.itemOffHandIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[8], findViewById(R.id.itemEarringIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[9], findViewById(R.id.itemNecklaceIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[10], findViewById(R.id.itemBraceletIcon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[11], findViewById(R.id.itemRing1Icon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[12], findViewById(R.id.itemRing2Icon));
        this.itemImageViews.put(ExtendedCharacter.GEAR_ITEM_KEYS[13], findViewById(R.id.itemSoulCrystalIcon));

        this.ilvlTextView = findViewById(R.id.ilvlValue);

        this.classJobName = findViewById(R.id.classJobName);
        this.classJobLevel = findViewById(R.id.classJobLevel);
        this.characterName = findViewById(R.id.characterName);
        characterName.setText(this.name); //Set immediately to adapt layout

        ConstraintLayout param1 = findViewById(R.id.param1);
        ConstraintLayout param2 = findViewById(R.id.param2);
        ConstraintLayout param3 = findViewById(R.id.param3);

        this.param1Label = param1.findViewById(R.id.paramLabel);
        this.param2Label = param2.findViewById(R.id.paramLabel);
        this.param3Label = param3.findViewById(R.id.paramLabel);
        this.param1Bar = param1.findViewById(R.id.paramBar);
        this.param2Bar = param2.findViewById(R.id.paramBar);
        this.param3Bar = param3.findViewById(R.id.paramBar);
        this.param1Value = param1.findViewById(R.id.paramValue);
        this.param2Value = param2.findViewById(R.id.paramValue);
        this.param3Value = param3.findViewById(R.id.paramValue);

        this.globalAttributesLayout = findViewById(R.id.globalAttributesLayout);
        this.fighterAttributesLayout = findViewById(R.id.fighterAttributesLayout);
        this.crafterAttributesLayout = findViewById(R.id.crafterAttributesLayout);
        this.labelsAttributesLayout = globalAttributesLayout.findViewById(R.id.labelsAttributesLayout);
        this.valuesAttributesLayout = globalAttributesLayout.findViewById(R.id.valuesAttributesLayout);
        this.labelsOffensivePropsLayout = globalAttributesLayout.findViewById(R.id.labelsOffensivePropsLayout);
        this.valuesOffensivePropsLayout = globalAttributesLayout.findViewById(R.id.valuesOffensivePropsLayout);
        this.labelsDefensivePropsLayout = globalAttributesLayout.findViewById(R.id.labelsDefensivePropsLayout);
        this.valuesDefensivePropsLayout = globalAttributesLayout.findViewById(R.id.valuesDefensivePropsLayout);
        this.labelsPhysicalPropsLayout = globalAttributesLayout.findViewById(R.id.labelsPhysicalPropsLayout);
        this.valuesPhysicalPropsLayout = globalAttributesLayout.findViewById(R.id.valuesPhysicalPropsLayout);
        this.labelsCrafterPropsLayout = crafterAttributesLayout.findViewById(R.id.labelsCraftingLayout);
        this.valuesCrafterPropsLayout = crafterAttributesLayout.findViewById(R.id.valuesCraftingLayout);
        this.labelsMentalPropsLayout = fighterAttributesLayout.findViewById(R.id.labelsMentalPropsLayout);
        this.valuesMentalPropsLayout = fighterAttributesLayout.findViewById(R.id.valuesMentalPropsLayout);
        this.labelsRolePropsLayout = fighterAttributesLayout.findViewById(R.id.labelsRoleLayout);
        this.valuesRolePropsLayout = fighterAttributesLayout.findViewById(R.id.valuesRoleLayout);


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
        this.jobIcon.setVisibility(View.INVISIBLE);

        this.labelsAttributesLayout.removeAllViews();
        this.valuesAttributesLayout.removeAllViews();
        this.labelsOffensivePropsLayout.removeAllViews();
        this.valuesOffensivePropsLayout.removeAllViews();
        this.labelsDefensivePropsLayout.removeAllViews();
        this.valuesDefensivePropsLayout.removeAllViews();
        this.labelsPhysicalPropsLayout.removeAllViews();
        this.valuesPhysicalPropsLayout.removeAllViews();
        this.labelsCrafterPropsLayout.removeAllViews();
        this.valuesCrafterPropsLayout.removeAllViews();
        this.labelsMentalPropsLayout.removeAllViews();
        this.valuesMentalPropsLayout.removeAllViews();
        this.labelsRolePropsLayout.removeAllViews();
        this.valuesRolePropsLayout.removeAllViews();

        this.fighterAttributesLayout.setVisibility(View.GONE);
        this.crafterAttributesLayout.setVisibility(View.GONE);
    }

    private void showCharacterViews()
    {
        jobIcon.setVisibility(View.VISIBLE);
    }

    private void updateCharacterViews()
    {
        hideCharacterViews();

        //// IMAGES

        //Load job icon if a soul crystal is equipped
        if(character.getGearItems().get("SoulCrystal") != null)
            Picasso.get()
                    .load(this.apiURL + this.character.getActiveClassJob().get_job().getIconURL())
                    .fit()
                    .into(this.jobIcon);
        else
            Picasso.get()
                    .load(this.apiURL + this.character.getActiveClassJob().get_class().getIconURL())
                    .fit()
                    .into(this.jobIcon);


        //Load portrait
        final Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(5)
                .oval(false)
                .build();

        Picasso.get()
                .load(this.character.getPortraitURL())
                .fit()
                .centerCrop()
                .transform(transformation)
                .into(this.portrait);

        //Load item icons
        for(String gearItemKey : ExtendedCharacter.getGearItemKeys())
        {
            if(this.character.getGearItems().get(gearItemKey) != null)
                Picasso.get()
                        .load(this.apiURL + Objects.requireNonNull(this.character.getGearItems().get(gearItemKey)).getItemIcon())
                        .fit()
                        .into(this.itemImageViews.get(gearItemKey));
        }

        //// TEXTS

        this.characterName.setText(this.name);

        //Load ilvl
        this.ilvlTextView.setText(String.format(Locale.FRANCE, "%d", character.getIlvl()));


        String name;
        if(this.character.getGearItems().get("SoulCrystal") != null)
            name = this.character.getActiveClassJob().get_job().getName();
        else
            name = this.character.getActiveClassJob().get_class().getName();

        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        classJobName.setText(name);
        classJobLevel.setText(String.format(Locale.FRANCE, "niveau %d", character.getActiveClassJob().getLevel()));

        //Set base character attributes (HP, MP, etc.)
        int attributesCount = this.character.getAttributes().size();
        this.param1Label.setText(this.character.getAttributes().get(attributesCount - 3).getName());
        setParamBarColor(this.param1Label.getText().toString(), this.param1Bar);
        this.param2Label.setText(this.character.getAttributes().get(attributesCount - 2).getName());
        setParamBarColor(this.param2Label.getText().toString(), this.param2Bar);
        this.param3Label.setText(this.character.getAttributes().get(attributesCount - 1).getName());
        setParamBarColor(this.param3Label.getText().toString(), this.param3Bar);
        this.param1Value.setText(String.format(Locale.FRANCE, "%d",this.character.getAttributes().get(attributesCount - 3).getValue()));
        this.param2Value.setText(String.format(Locale.FRANCE, "%d",this.character.getAttributes().get(attributesCount - 2).getValue()));
        this.param3Value.setText(String.format(Locale.FRANCE, "%d",this.character.getAttributes().get(attributesCount - 1).getValue()));

        boolean crafter;

        if(this.character.getActiveClassJob().get_class().getCategoryName().equals("artisans"))
        {
            this.crafterAttributesLayout.setVisibility(View.VISIBLE);
            crafter = true;
        }
        else
        {
            this.fighterAttributesLayout.setVisibility(View.VISIBLE);
            crafter = false;
        }

        for(int i = 0; i < attributesCount - 3; i++)
        {
            TextView attributeLabel = getAttributeTextView(100 + i);
            TextView attributeValue = getAttributeTextView(200 + i);
            attributeLabel.setText(this.character.getAttributes().get(i).getName());
            attributeValue.setText(String.format(Locale.FRANCE, "%d", this.character.getAttributes().get(i).getValue()));

            if(i < 5)
            {
                this.labelsAttributesLayout.addView(attributeLabel);
                this.valuesAttributesLayout.addView(attributeValue);
            }
            else if(i < 8)
            {
                this.labelsOffensivePropsLayout.addView(attributeLabel);
                this.valuesOffensivePropsLayout.addView(attributeValue);
            }
            else if(i < 10)
            {
                this.labelsDefensivePropsLayout.addView(attributeLabel);
                this.valuesDefensivePropsLayout.addView(attributeValue);
            }
            else if(i < 12)
            {
                this.labelsPhysicalPropsLayout.addView(attributeLabel);
                this.valuesPhysicalPropsLayout.addView(attributeValue);
            }
            else
            {
                if(crafter)
                {
                    this.labelsCrafterPropsLayout.addView(attributeLabel);
                    this.valuesCrafterPropsLayout.addView(attributeValue);
                }
                else
                {
                    if(i < 15)
                    {
                        this.labelsMentalPropsLayout.addView(attributeLabel);
                        this.valuesMentalPropsLayout.addView(attributeValue);
                    }
                    else
                    {
                        this.labelsRolePropsLayout.addView(attributeLabel);
                        this.valuesRolePropsLayout.addView(attributeValue);
                    }
                }
            }
        }

        showCharacterViews();
    }

    private TextView getAttributeTextView(int id)
    {
        TextView attribute = new TextView(getApplicationContext());
        attribute.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.davidsans));
        if(id >= 200)
        {
            attribute.setLetterSpacing(0.075f);
        }
        attribute.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        attribute.setGravity(Gravity.CENTER_HORIZONTAL);
        attribute.setPadding(0, 0, 0, 8);
        attribute.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        attribute.setTextSize(18f);
        attribute.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.characterOnBackground));
        attribute.setText(this.character.getAttributes().get(0).getName());
        attribute.setId(id);
        return attribute;
    }

    private void setParamBarColor(String label, ImageView imageView)
    {
        Drawable bar = getDrawable(R.drawable.param_bar_shape);
        switch (label)
        {
            case "PV":
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.paramPV));
                imageView.setImageDrawable(bar);
                break;

            case "PM":
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.paramPM));
                imageView.setImageDrawable(bar);
                break;

            case "PR":
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.paramPR));
                imageView.setImageDrawable(bar);
                break;

            case "PS":
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.paramPS));
                imageView.setImageDrawable(bar);
                break;

            case "PT":
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.paramPT));
                imageView.setImageDrawable(bar);
                break;

            default:
                assert bar != null;
                bar.setTint(getResources().getColor(R.color.characterOnBackground));
                imageView.setImageDrawable(bar);
                break;
        }
    }

    private void updateCharacterAPI(final CharacterDbHelper dbHelper)
    {
        String request = this.apiURL + this.apiCharacterURL + this.characterID + this.apiCharacterExtra_Update;
        addToRequestQueue(new StringRequest(GET, request, response ->
            {
                try
                {
                    Log.e(TAG, "Update response received : [" + response + "]");
                    getCharacterAPI(dbHelper);
                }
                catch (Exception e)
                {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(CharacterActivity.this, "Échec de la mise à jour du personnage", Toast.LENGTH_SHORT).show();
                    navigateUp();
                    finish();
                }
            }, error ->
            {
                Toast.makeText(CharacterActivity.this, "Échec de la mise à jour du personnage", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
                navigateUp();
                finish();
            }));
    }

    private void getCharacterAPI(final CharacterDbHelper dbHelper)
    {
        String request = this.apiURL + this.apiCharacterURL + this.characterID + this.apiCharacterExtra_Get;
        addToRequestQueue(new JsonObjectRequest(GET, request, null, response ->
            {
                try
                {
                    character = new ExtendedCharacter(response.getJSONObject("Character"), dbHelper);

                    new Thread(() ->
                        {
                            //noinspection StatementWithEmptyBody
                            while(!character.isLoaded()) {}

                            runOnUiThread(() ->
                                {
                                    Log.e(TAG, "Data acquired");
                                    updateCharacterViews();
                                });
                        }).run();
                }
                catch (Exception e)
                {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(CharacterActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                    navigateUp();
                    finish();
                }
            }, error ->
            {
                //Toast.makeText(CharacterActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show();
                navigateUp();
                finish();
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
