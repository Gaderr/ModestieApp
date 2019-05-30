package com.modestie.modestieapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class NewEventActivity extends AppCompatActivity
{
    private static TextInputLayout formEventName;
    private static TextInputLayout formEventDate;
    private static TextInputLayout formEventTime;

    private Button newPrice;

    private boolean today;

    public static final String TAG = "ACTVT.NWEVNT";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        formEventName = findViewById(R.id.FormEventName);
        formEventDate = findViewById(R.id.FormEventDate);
        formEventTime = findViewById(R.id.FormEventTime);
        newPrice = findViewById(R.id.addPriceButton);

        today = true;

        final Calendar c = Calendar.getInstance(Locale.FRANCE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        String cDay, cMonth;

        if(day < 10)
            cDay = "0" + day;
        else
            cDay = day + "";
        if(month < 10)
            cMonth = "0" + (month + 1);
        else
            cMonth = (month + 1) + "";

        formEventDate.getEditText().setText(String.format(Locale.FRANCE, "%s/%s/%d", cDay, cMonth, year));

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            R.style.ThemeOverlay_ModestieTheme_Dialog,
            (view, pickedYear, pickedMonth, pickedDay) ->
            {
                String sDay, sMonth;
                if(pickedDay < 10)
                    sDay = "0" + pickedDay;
                else
                    sDay = pickedDay + "";
                if(++pickedMonth < 10)
                    sMonth = "0" + pickedMonth;
                else
                    sMonth = pickedMonth + "";

                formEventDate.getEditText().setText(String.format(Locale.FRANCE, "%s/%s/%d", sDay, sMonth, pickedYear));
            },
            year,
            month,
            day
        );
        datePickerDialog.setOnCancelListener(dialog ->
             {formEventDate.getEditText().clearFocus(); hideKeyboardFrom(getApplicationContext(), formEventDate);});
        formEventDate.setStartIconOnClickListener(v ->  datePickerDialog.show());
        formEventDate.getEditText().setOnFocusChangeListener((v, hasFocus) -> {if(hasFocus) datePickerDialog.show();});
        formEventDate.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override

            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            @SuppressLint("SimpleDateFormat")
            public void afterTextChanged(Editable s)
            {
                formEventDate.getEditText().clearFocus();
                hideKeyboardFrom(getApplicationContext(), formEventDate);

                if(s.length() == 0)
                    return;

                try
                {
                    if(Integer.parseInt(s.subSequence(0, 2).toString()) == day
                            && Integer.parseInt(s.subSequence(3, 5).toString()) == (month + 1)
                            && Integer.parseInt(s.subSequence(6, 10).toString()) == year)
                        today = true;
                    else
                        today = false;

                    //Log.e(TAG, "Today : [" + Integer.parseInt(s.subSequence(0, 2).toString()) + "|" + day + "]["
                    //        + Integer.parseInt(s.subSequence(3, 5).toString()) + "|" + (month + 1) + "]["
                    //        + Integer.parseInt(s.subSequence(6, 10).toString()) + "|" + year + "]");
                    //Log.e(TAG, "Today : " + today);

                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    df.setTimeZone(TimeZone.getDefault());
                    long millis = df.parse(s.toString()).getTime() + 86399000; //This date + 23:59:59

                    if(millis >= System.currentTimeMillis())
                    {
                        //Toast.makeText(NewEventActivity.this, "Superior", Toast.LENGTH_SHORT).show();
                        /*if(formEventDate.getHelperText() != null)
                            formEventDate.setHelperText("jj/mm/aaaa *requis");
                        if(formEventDate.getError() != null)
                            formEventDate.setError(null);*/
                        //TODO Watch andoidx updates : setError() is making incorrect layout requests
                        // indefinitely and overloads the processor;
                    }
                    else
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez entrer une date supérieure ou égale à celle d'aujourd'hui", Toast.LENGTH_LONG).show();
                        /*if(formEventDate.getHelperText() != null)
                            formEventDate.setHelperText(null);
                        if(formEventDate.getError() != null)
                            formEventDate.setError("Veuillez entrer une date valide");*/
                        s.clear();
                    }
                }
                catch (ParseException e)
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
            R.style.ThemeOverlay_ModestieTheme_Dialog,
            (view, pickedHour, pickedMinute) ->
            {
                String sHour, sMinute;
                if(pickedHour < 10)
                    sHour = "0" + pickedHour;
                else
                    sHour = pickedHour + "";

                if(pickedMinute < 10)
                    sMinute = "0" + pickedMinute;
                else
                    sMinute = pickedMinute + "";

                formEventTime.getEditText().setText(String.format(Locale.FRANCE, "%s:%s", sHour, sMinute));
            },
            12,
            0,
            true
        );
        timePickerDialog.setOnCancelListener(dialog ->
            {formEventTime.getEditText().clearFocus(); hideKeyboardFrom(getApplicationContext(), formEventTime);});
        timePickerDialog.setOnDismissListener(dialog ->
            {formEventTime.getEditText().clearFocus(); hideKeyboardFrom(getApplicationContext(), formEventTime);});
        formEventTime.setStartIconOnClickListener(v -> timePickerDialog.show());
        formEventTime.getEditText().setOnFocusChangeListener((v, hasFocus) -> {if(hasFocus) timePickerDialog.show();});
        formEventTime.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s)
            {
                formEventDate.getEditText().clearFocus();
                hideKeyboardFrom(getApplicationContext(), formEventDate);

                if(s.length() == 0)
                    return;

                int sHour = Integer.parseInt(s.toString().substring(0, 2));
                int sMinute = Integer.parseInt(s.toString().substring(3, 5));
                long sSeconds = sHour * 3600 + sMinute * 60;

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                final String currentTime = sdf.format(Calendar.getInstance().getTime());
                int cHour = Integer.parseInt(currentTime.subSequence(0, 2).toString());
                int cMinute = Integer.parseInt(currentTime.subSequence(3, 5).toString());
                long cSeconds = cHour * 3600 + cMinute * 60;

                final long timediff = (sSeconds - cSeconds) / 60;

                //Log.e(TAG, "Today : " + today);
                //Log.e(TAG, timediff + "");

                if(today)
                {
                    if(timediff <= 0)
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez choisir un horaire supérieur à l'heure actuelle", Toast.LENGTH_LONG).show();
                        s.clear();
                    }
                    else if(timediff < 30)
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez organiser votre événement au moins une demie-heure en avance", Toast.LENGTH_LONG).show();
                        s.clear();
                    }
                }
            }
        });

        newPrice.setOnClickListener(v ->
            {
                PopupMenu popup = new PopupMenu(this, v);
                popup.setOnMenuItemClickListener(item ->
                    {
                        switch (item.getItemId())
                        {
                            case R.id.itemPrice:
                                createSnackbar("En cours de développpement");
                                return true;

                            case R.id.gilsPrice:
                                createSnackbar("En cours de développpement");
                                return true;

                            default:
                                return true;
                        }
                    });
                popup.getMenuInflater().inflate(R.menu.new_event_selection_menu, popup.getMenu());
                popup.show();
            });
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
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_event_bar_menu, menu);
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
        if (id == R.id.save_event)
        {
            createSnackbar("En cours de développpement");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createSnackbar(String text)
    {
        Snackbar.make(findViewById(R.id.context_view), text, Snackbar.LENGTH_LONG)
                .show();
    }

    //TODO Override up action to confirm cancelling

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
