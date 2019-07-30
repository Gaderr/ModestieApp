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
import android.text.TextUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.login.LoginActivity;
import com.modestie.modestieapp.model.freeCompany.FreeCompany;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;
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

    private ProgressBar bar;

    private boolean pending;
    private boolean doLogin;
    private boolean checkRegistrationDone;
    private boolean databaseUpdateDone;
    private boolean isAlive;

    private static final String GET_FC_DATA_REQUEST = "https://xivapi.com/freecompany/9232660711086299979?data=FCM";
    private static final String CHECK_REGISTRATION_REQUEST = "https://modestie.fr/wp-json/modestieevents/v1/checkregistration";

    private FreeCompanyDbHelper dbHelper;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "ON CREATE");

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

        this.pending = false;
        this.doLogin = true;
        this.checkRegistrationDone = false;
        this.databaseUpdateDone = false;
        this.isAlive = true;

        ImageView crestView = findViewById(R.id.crest);
        TextView appNameView = findViewById(R.id.textAppName);
        this.bar = findViewById(R.id.progressBar);
        this.bar.setVisibility(View.INVISIBLE);

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
                if (doLogin) startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
        this.isAlive = false;
    }

    private void updateData()
    {
        if (pending || !isAlive) return;

        Log.e(TAG, "Update data");

        this.pending = true;

        this.bar.setVisibility(View.VISIBLE);

        boolean doUpdate = true;
        long currentTime = System.currentTimeMillis() / 1000;

        Hawk.init(this).build();

        this.dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME, null);
        if (cursor.moveToFirst())
        {
            long lastUpdate = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED));
            if (currentTime - lastUpdate < 3600)
            {
                doUpdate = false;
            }
        }

        cursor.close();

        //JWT validation
        if (Hawk.contains("LoggedInUser") && Hawk.contains("UserCharacter"))
        {
            Log.e(TAG, "CHECKING JWT AND CHARACTER");
            LoggedInUser user = Hawk.get("LoggedInUser");
            addToRequestQueue(
                    new JsonObjectRequest(
                            Request.Method.GET,
                            CHECK_REGISTRATION_REQUEST + "?userEmail=" + user.getUserEmail(),
                            null,
                            response ->
                            {
                                try
                                {
                                    Log.e(TAG, "CHECK CHARACTER");
                                    if (response.getBoolean("result"))
                                    {
                                        Log.e(TAG, "CHARACTER VERIFIED");
                                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                                        if (user.getExpiration() > System.currentTimeMillis() && sharedPref.getBoolean("AutoLogin", false))
                                        {
                                            Log.e(TAG, "JWT VALID");
                                            this.doLogin = false;
                                        }
                                    }
                                    this.checkRegistrationDone = true;
                                    next();
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            },
                            error -> Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show()
                    ));
        }
        else
        {
            Log.e(TAG, "NO CREDENTIALS");
            this.checkRegistrationDone = true;
            next();
        }

        if (doUpdate)
        {
            Log.e(TAG, "UPDATING DATABASE");
            addToRequestQueue(
                    new JsonObjectRequest(
                            GET, GET_FC_DATA_REQUEST, null,
                            response ->
                            {
                                Log.e(TAG, "DATABASE UPDATED");
                                new FreeCompany(response, dbHelper);
                                this.databaseUpdateDone = true;
                                next();
                            },
                            error ->
                            {
                                switch (error.networkResponse.statusCode)
                                {
                                    case 503:
                                        new MaterialAlertDialogBuilder(SplashScreenActivity.this)
                                                .setTitle("Erreur code 503")
                                                .setMessage("De tièrces parties nécéssaires à l'obtention d'informations sont temporairement indisponibles en raison d'une maintenance ou de difficultés techniques. Veuillez réessayer plus tard.")
                                                .setPositiveButton("Ok", (dialog, which) -> finish())
                                                .show();
                                        break;

                                    default:
                                        new MaterialAlertDialogBuilder(SplashScreenActivity.this)
                                                .setTitle("Erreur inconnue")
                                                .setMessage("Obtention des données impossible. Veuillez contacter un grand modeste si l'erreur persiste.")
                                                .setPositiveButton("Ok", (dialog, which) -> finish())
                                                .show();
                                        break;
                                }
                            }));
        }
        else
        {
            Log.e(TAG, "DATABASE UPDATE SKIPPED");
            this.databaseUpdateDone = true;
            next();
        }
    }

    /**
     * This function creates an intent to Home or Login activity, depending on JWT validation.
     */
    private void next()
    {
        Log.e(TAG, "NEXT CALLED");
        if (this.databaseUpdateDone && this.checkRegistrationDone && this.isAlive)
        {
            this.bar.setVisibility(View.INVISIBLE);
            this.animatorSetOut.start();
        }
        else
            Log.e(TAG, "NEXT DENIED");
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

