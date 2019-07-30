package com.modestie.modestieapp.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.modestie.modestieapp.model.character.Character;
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
    private AnimatorSet animatorSetIn;
    private AnimatorSet animatorSetOut;
    private ObjectAnimator animationTextRight;
    private ObjectAnimator animationTextLeft;
    private ObjectAnimator animationCrestLeft;
    private ObjectAnimator animationCrestRight;
    private ObjectAnimator animationTextFadeIn;
    private ObjectAnimator animationTextFadeOut;
    private ObjectAnimator animationCrestFadeIn;
    private ObjectAnimator animationCrestFadeOut;

    private static final int inSpeed = 900;
    private static final int outSpeed = 900;

    private ImageView touchAppIcon;
    private ProgressBar bar;

    private boolean pending;
    private boolean login;

    private boolean setInAnimDone;
    private boolean permissionsCheckDone;
    private boolean checkRegistrationDone;
    private boolean databaseUpdateDone;

    private static final String GETFCDATA_REQUEST = "https://xivapi.com/freecompany/9232660711086299979?data=FCM";
    private static final String CHECK_REGISTRATION_REQUEST = "https://modestie.fr/wp-json/modestieevents/v1/checkregistration";

    private FreeCompanyDbHelper dbHelper;

    private RequestQueue mRequestQueue;

    public static final String TAG = "ACTVT.SPLSHSCRN";

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
        this.login = true;

        this.setInAnimDone = false;
        this.permissionsCheckDone = false;
        this.checkRegistrationDone = false;
        this.databaseUpdateDone = false;

        ImageView crestView = findViewById(R.id.crest);
        TextView appNameView = findViewById(R.id.textAppName);
        this.touchAppIcon = findViewById(R.id.touchIcon);
        this.touchAppIcon.setVisibility(View.INVISIBLE);
        this.bar = findViewById(R.id.progressBar);
        this.bar.setVisibility(View.INVISIBLE);

        this.animatorSetIn = new AnimatorSet();
        this.animatorSetOut = new AnimatorSet();

        //Text animations
        this.animationTextRight = ObjectAnimator.ofFloat(appNameView, "translationX", 150f, 0)
                .setDuration(inSpeed);
        this.animationTextRight.setInterpolator(Easings.QUART_OUT);

        this.animationTextLeft = ObjectAnimator.ofFloat(appNameView, "translationX", 0, -150f)
                .setDuration(outSpeed);
        this.animationTextLeft.setInterpolator(Easings.QUART_IN);

        this.animationTextFadeIn = ObjectAnimator.ofFloat(appNameView, "alpha", 0f, 1f)
                .setDuration(inSpeed);
        this.animationTextFadeIn.setInterpolator(Easings.QUART_OUT);

        this.animationTextFadeOut = ObjectAnimator.ofFloat(appNameView, "alpha", 1f, 0f)
                .setDuration(outSpeed);
        this.animationTextFadeOut.setInterpolator(Easings.QUART_IN);

        //Crest animations
        this.animationCrestLeft = ObjectAnimator.ofFloat(crestView, "translationX", -150f, 0)
                .setDuration(inSpeed);
        this.animationCrestLeft.setInterpolator(Easings.QUART_OUT);

        this.animationCrestRight = ObjectAnimator.ofFloat(crestView, "translationX", 0, 150f)
                .setDuration(outSpeed);
        this.animationCrestRight.setInterpolator(Easings.QUART_IN);

        this.animationCrestFadeIn = ObjectAnimator.ofFloat(crestView, "alpha", 0f, 1f)
                .setDuration(inSpeed);
        this.animationCrestFadeIn.setInterpolator(Easings.QUART_OUT);

        this.animationCrestFadeOut = ObjectAnimator.ofFloat(crestView, "alpha", 1f, 0f)
                .setDuration(outSpeed);
        this.animationCrestFadeOut.setInterpolator(Easings.QUART_IN);

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

        this.animatorSetIn
                .play(this.animationTextRight)
                .with(this.animationTextFadeIn)
                .with(this.animationCrestLeft)
                .with(this.animationCrestFadeIn);

        this.animationTextRight.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                setInAnimDone = true;
                next();
            }
        });

        this.animatorSetIn.start();

        updateData();
    }

    private void updateData()
    {
        if (pending) return;

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
                                            this.login = false;
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
                            GET, GETFCDATA_REQUEST, null,
                            response ->
                            {
                                Log.e(TAG, "DATABASE UPDATED");
                                new FreeCompany(response, dbHelper);
                                this.databaseUpdateDone = true;
                                checkPermissions();
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
            checkPermissions();
        }
    }

    public void checkPermissions()
    {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
        else
        {
            this.permissionsCheckDone = true;
            next();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case READ_EXTERNAL_STORAGE_REQUEST:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    this.permissionsCheckDone = true;
                    next();
                }
                else
                    finish();
            }
        }
    }

    /**
     * This function creates an intent to Home or Login activity, depending on JWT validation.
     */
    private void next()
    {
        Log.e(TAG, "NEXT CALLED");
        if (!this.databaseUpdateDone || !this.checkRegistrationDone || !this.permissionsCheckDone || !this.setInAnimDone)
        {
            Log.e(TAG, "NEXT DENIED");
            return;
        }

        Log.e(TAG, "NEXT");

        this.bar.setVisibility(View.INVISIBLE);

        this.animatorSetOut
                .play(this.animationTextLeft)
                .with(this.animationTextFadeOut)
                .with(this.animationCrestRight)
                .with(this.animationCrestFadeOut);

        this.animatorSetOut.start();

        this.animationTextLeft.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (login) startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                else startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });
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

