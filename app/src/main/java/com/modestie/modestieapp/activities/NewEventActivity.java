package com.modestie.modestieapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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
            R.style.Dialog,
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
                    if(Integer.parseInt(s.subSequence(0, 1).toString()) == day
                            && Integer.parseInt(s.subSequence(3, 4).toString()) == month
                            && Integer.parseInt(s.subSequence(6, 9).toString()) == year)
                        today = true;
                    else
                        today = false;

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
            R.style.Dialog,
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

                if(today && (sSeconds - cSeconds) / 60 < 30)
                {
                    Toast.makeText(NewEventActivity.this, "Veuillez organiser votre événement au moins une demie-heure en avance", Toast.LENGTH_LONG).show();
                    s.clear();
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    //TODO Override up action to confirm cancelling

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
