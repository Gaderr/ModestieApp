package com.modestie.modestieapp.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.os.Bundle;
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
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.freeCompany.FreeCompany;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;

import static com.android.volley.Request.Method.GET;


public class MainActivity extends AppCompatActivity
{
    private AnimatorSet animatorSetIn;
    private ObjectAnimator animationTextUp;
    private ObjectAnimator animationCrestDown;
    private ObjectAnimator animationTextFadeIn;
    private ObjectAnimator animationCrestFadeIn;

    private ImageView touchAppIcon;
    private ProgressBar bar;

    private boolean ready;

    private static final String apiURLRequest = "https://xivapi.com/freecompany/9232660711086299979?data=FCM";

    private FreeCompanyDbHelper dbHelper;

    private RequestQueue mRequestQueue;

    public static final String TAG = "ACTVT.MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.ready = false;

        ConstraintLayout layout = findViewById(R.id.mainActivityLayout);

        layout.setOnClickListener(v ->
            {
                if(ready)
                {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    ready = false;
                }
            });

        ImageView crestView = findViewById(R.id.crest);
        TextView appNameView = findViewById(R.id.textAppName);
        this.touchAppIcon = findViewById(R.id.touchIcon);
        this.touchAppIcon.setVisibility(View.INVISIBLE);
        this.bar = findViewById(R.id.progressBar);

        this.animatorSetIn = new AnimatorSet();

        this.animationTextUp = ObjectAnimator.ofFloat(appNameView, "translationY", -50f)
                .setDuration(1750);
        this.animationTextFadeIn = ObjectAnimator.ofFloat(appNameView, "alpha", 0f, 1f)
                .setDuration(1750);

        this.animationCrestDown = ObjectAnimator.ofFloat(crestView, "translationY", +50f)
                .setDuration(1750);
        this.animationCrestFadeIn = ObjectAnimator.ofFloat(crestView, "alpha", 0f, 1f)
                .setDuration(1750);

        appNameView.setAlpha(0f);
        appNameView.setTranslationY(+100f);

        crestView.setAlpha(0f);
        crestView.setTranslationY(-100f);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean doUpdate = true;
        long currentTime = System.currentTimeMillis() / 1000;

        this.animatorSetIn
                .play(this.animationTextUp)
                .with(this.animationTextFadeIn)
                .with(this.animationCrestDown)
                .with(this.animationCrestFadeIn);
        this.animatorSetIn.start();

        this.dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME, null);
        if(cursor.moveToFirst())
        {
            long lastUpdate = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED));
            if(currentTime - lastUpdate < 3600)
            {
                doUpdate = false;
            }
        }

        cursor.close();

        if(doUpdate)
        {
            addToRequestQueue(new JsonObjectRequest(GET, apiURLRequest, null, response ->
                {
                    new FreeCompany(response, dbHelper);

                    touchAppIcon.setVisibility(View.VISIBLE);
                    bar.setVisibility(View.INVISIBLE);

                    ready = true;
                }, error -> Toast.makeText(MainActivity.this, "Échec de la récupération des données", Toast.LENGTH_SHORT).show()));
        }
        else
        {
            touchAppIcon.setVisibility(View.VISIBLE);
            bar.setVisibility(View.INVISIBLE);
            ready = true;
        }
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null) this.mRequestQueue = Volley.newRequestQueue(getApplicationContext());

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

