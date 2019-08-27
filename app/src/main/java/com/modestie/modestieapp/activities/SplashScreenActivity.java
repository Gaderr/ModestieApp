package com.modestie.modestieapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.login.LoginActivity;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.freeCompany.FreeCompany;
import com.modestie.modestieapp.model.login.UserCredentials;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.modestie.modestieapp.utils.ui.Easings;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;

import static com.android.volley.Request.Method.GET;

public class SplashScreenActivity extends AppCompatActivity
{
    public static final String TAG = "ACTVT.SPLSHSCRN";

    private AnimatorSet animatorSetIn;
    private AnimatorSet animatorSetOut;
    private static final int inSpeed = 900;
    private static final int outSpeed = 900;
    private static final int startDelay = 500;
    private static final long updateDelay = 3600 * 2;

    private ProgressBar bar;
    private TextView dbUpdateFeedback;
    private TextView characterUpdateFeedback;

    private boolean pending;
    private boolean ACTIVITY_IS_ALIVE;

    private boolean LOADING_DO_LOGIN;
    private boolean LOADING_CHARACTER_UPDATED;
    private boolean LOADING_DB_UPDATED;

    private RequestHelper requestHelper;

    private LightCharacter character;

    private FreeCompanyDbHelper dbHelper;

    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Log.e(TAG, "ON CREATE");

        FirebaseAnalytics fbFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        this.fbAuth = FirebaseAuth.getInstance();

        //Load theme preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Load night theme on first start
        if (!sharedPref.contains("nightmode"))
            sharedPref.edit().putBoolean("nightmode", true).apply();

        if (sharedPref.getBoolean("nightmode", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_splash);

        //HTTP requests
        this.requestHelper = new RequestHelper(getApplicationContext());

        //Init vars
        this.pending = false;
        this.LOADING_DO_LOGIN = true;
        this.LOADING_CHARACTER_UPDATED = false;
        this.LOADING_DB_UPDATED = false;
        this.ACTIVITY_IS_ALIVE = true;

        ImageView crestView = findViewById(R.id.crest);
        TextView appNameView = findViewById(R.id.textAppName);
        this.bar = findViewById(R.id.progressBar);
        this.bar.setVisibility(View.INVISIBLE);
        this.dbUpdateFeedback = findViewById(R.id.dbUpdateFeedback);
        this.characterUpdateFeedback = findViewById(R.id.characterUpdateFeedback);

        //Animations
        this.animatorSetIn = new AnimatorSet();
        this.animatorSetOut = new AnimatorSet();

        //Text animations
        ObjectAnimator animationTextRight = ObjectAnimator.ofFloat(appNameView, "translationX", 150f, 0)
                .setDuration(inSpeed);
        animationTextRight.setInterpolator(Easings.QUART_OUT);

        ObjectAnimator animationTextLeft = ObjectAnimator.ofFloat(appNameView, "translationX", 0, -150f)
                .setDuration(outSpeed);
        animationTextLeft.setInterpolator(Easings.QUART_IN);

        ObjectAnimator animationTextFadeIn = ObjectAnimator.ofFloat(appNameView, "alpha", 0f, 1f)
                .setDuration(inSpeed);
        animationTextFadeIn.setInterpolator(Easings.QUART_OUT);

        ObjectAnimator animationTextFadeOut = ObjectAnimator.ofFloat(appNameView, "alpha", 1f, 0f)
                .setDuration(outSpeed);
        animationTextFadeOut.setInterpolator(Easings.QUART_IN);

        //Crest animations
        ObjectAnimator animationCrestLeft = ObjectAnimator.ofFloat(crestView, "translationX", -150f, 0)
                .setDuration(inSpeed);
        animationCrestLeft.setInterpolator(Easings.QUART_OUT);

        ObjectAnimator animationCrestRight = ObjectAnimator.ofFloat(crestView, "translationX", 0, 150f)
                .setDuration(outSpeed);
        animationCrestRight.setInterpolator(Easings.QUART_IN);

        ObjectAnimator animationCrestFadeIn = ObjectAnimator.ofFloat(crestView, "alpha", 0f, 1f)
                .setDuration(inSpeed);
        animationCrestFadeIn.setInterpolator(Easings.QUART_OUT);

        ObjectAnimator animationCrestFadeOut = ObjectAnimator.ofFloat(crestView, "alpha", 1f, 0f)
                .setDuration(outSpeed);
        animationCrestFadeOut.setInterpolator(Easings.QUART_IN);

        this.animatorSetIn
                .play(animationTextRight)
                .with(animationTextFadeIn)
                .with(animationCrestLeft)
                .with(animationCrestFadeIn);

        this.animatorSetIn.setStartDelay(startDelay);

        animationTextRight.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                updateData();
            }
        });

        this.animatorSetOut
                .play(animationTextLeft)
                .with(animationTextFadeOut)
                .with(animationCrestRight)
                .with(animationCrestFadeOut);

        animationTextLeft.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (LOADING_DO_LOGIN)
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                else startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

        appNameView.setAlpha(0f);
        crestView.setAlpha(0f);
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
    protected void onStart()
    {
        super.onStart();
        this.animatorSetIn.start();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        this.ACTIVITY_IS_ALIVE = false;
    }

    private void updateData()
    {
        if (pending || !ACTIVITY_IS_ALIVE) return;

        this.pending = true;

        this.bar.setVisibility(View.VISIBLE);

        Hawk.init(this).build();

        Log.d(TAG, "AUTH");

        //Authenticate user then get character if registered (not registered => Login activity)
        if(Hawk.contains("UserCredentials"))
        {
            UserCredentials credentials = Hawk.get("UserCredentials");
            Log.d(TAG, "AUTHENTICATING");
            this.characterUpdateFeedback.setText(getString(R.string.login_feedback_modestiefr_connection));
            this.fbAuth.signInWithEmailAndPassword(credentials.getUsername(), credentials.getPassword())
                    .addOnCompleteListener(this, task ->
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "AUTH OK");
                            Log.d(TAG, "CHECKING CHARACTER STORED");
                            if (Hawk.contains("UserCharacter")) //Check character stored in device
                            {
                                this.LOADING_DO_LOGIN = false;

                                //Update character
                                this.character = Hawk.get("UserCharacter");
                                if(System.currentTimeMillis() - this.character.getLastUpdate() < updateDelay)
                                {
                                    Log.d(TAG, "UPDATING CHARACTER");
                                    this.characterUpdateFeedback.setText(getString(R.string.login_feedback_modestiefr_get_character));
                                    this.requestHelper.addToRequestQueue(getCharacterUpdateRequest());
                                }
                                else
                                {
                                    Log.d(TAG, "NO CHARACTER UPDATE");
                                    this.characterUpdateFeedback.setText("");
                                    this.LOADING_CHARACTER_UPDATED = true;
                                    next();
                                }
                            }
                            else //No character saved in device
                            {
                                Log.d(TAG, "CHARACTER NOT FOUND");
                                this.LOADING_DO_LOGIN = true;
                                next();
                            }
                        }
                        else
                        {
                            Log.d(TAG, "AUTH FAILED");
                            this.LOADING_DO_LOGIN = true;
                            this.LOADING_CHARACTER_UPDATED = true;
                            next();
                        }
                    });
        }
        else
        {
            Log.d(TAG, "NO CREDENTIALS");
            this.LOADING_DO_LOGIN = true;
            next();
        }

        //Update database
        boolean doUpdate = true;
        long currentTime = System.currentTimeMillis() / 1000;
        this.dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME, null);
        if (cursor.moveToFirst())
        {
            long lastUpdate = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED));
            if (currentTime - lastUpdate < updateDelay)
            {
                doUpdate = false;
            }
        }
        cursor.close();

        if (doUpdate)
        {
            Log.d(TAG, "UPDATING DB");
            this.dbUpdateFeedback.setText(getString(R.string.login_feedback_modestiefr_update_db));
            this.requestHelper.addToRequestQueue(getDatabaseUpdateRequest());
        }
        else
        {
            Log.d(TAG, "NO DB UPDATE");
            this.LOADING_DB_UPDATED = true;
            next();
        }
    }

    /**
     * This function creates an intent to Home or Login activity, depending on loading workflow.
     */
    private void next()
    {
        Log.d(TAG, "NEXT CALLED");
        if (this.LOADING_DB_UPDATED && this.LOADING_CHARACTER_UPDATED && this.ACTIVITY_IS_ALIVE)
        {
            this.bar.setVisibility(View.INVISIBLE);
            this.animatorSetOut.start();
        }
        else
            Log.d(TAG, "NEXT DENIED");
    }

    private JsonObjectRequest getCharacterUpdateRequest()
    {
        return new JsonObjectRequest(
                Request.Method.GET,
                RequestURLs.XIVAPI_CHARACTER_REQ + "/" + this.character.getID() + RequestURLs.XIVAPI_CHARACTER_PARAM_LIGHT,
                null,
                response ->
                {
                    try
                    {
                        Log.d(TAG, "CHARACTER UPDATED");
                        Hawk.put("UserCharacter", new LightCharacter(response.getJSONObject("Character")));
                        this.characterUpdateFeedback.setText("");
                        this.LOADING_CHARACTER_UPDATED = true;
                        next();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show()
        );
    }

    private JsonObjectRequest getDatabaseUpdateRequest()
    {
        return new JsonObjectRequest(
                GET, RequestURLs.XIVAPI_FREECOMPANY_REQ + "/" + RequestURLs.MODESTIE_FC_ID + RequestURLs.XIVAPI_FREECOMPANY_PARAM_FCM, null,
                response ->
                {
                    Log.d(TAG, "DB UPDATED");
                    new FreeCompany(response, dbHelper);
                    this.dbUpdateFeedback.setText("");
                    this.LOADING_DB_UPDATED = true;
                    next();
                },
                error ->
                {
                    if (error.networkResponse == null)
                    {
                        new MaterialAlertDialogBuilder(SplashScreenActivity.this)
                                .setTitle("Erreur inconnue")
                                .setMessage("Obtention des données impossible. Veuillez contacter un grand modeste si l'erreur persiste.")
                                .setPositiveButton("Ok", (dialog, which) -> finish())
                                .show();
                    }
                    else
                    {
                        if (error.networkResponse.statusCode == 503)
                        {
                            new MaterialAlertDialogBuilder(SplashScreenActivity.this)
                                    .setTitle("Erreur code 503")
                                    .setMessage("De tièrces parties nécéssaires à l'obtention d'informations sont temporairement indisponibles en raison d'une maintenance ou de difficultés techniques. Veuillez réessayer plus tard.")
                                    .setPositiveButton("Ok", (dialog, which) -> finish())
                                    .show();
                        }
                    }
                });
    }
}

