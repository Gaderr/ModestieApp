package com.modestie.modestieapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.EventPriceAdapter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.model.item.LightItem;
import com.woxthebox.draglistview.DragListView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class NewEventActivity
        extends AppCompatActivity
        implements EventPriceEditDialogFragment.OnFragmentInteractionListener,
        ItemSelectionDialogFragment.OnItemSelectedListener
{
    private static TextInputLayout formEventName;
    private static TextInputLayout formEventDate;
    private static TextInputLayout formEventTime;
    private static TextInputLayout formEventMaxParticipants;
    private static AutoCompleteTextView formEventMaxParticipantsType;

    private DragListView pricesList;
    private EventPriceAdapter adapter;
    private EventPriceOptionsModal priceBottomModal;

    private Button newPrice;

    private Event event;
    private ArrayList<Pair<Long, EventPrice>> listPrices;

    private int count = 0;

    private boolean today;

    public static final String TAG = "ACTVT.NEWEVNT";

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
        formEventMaxParticipants = findViewById(R.id.FormMaxParticipants);
        formEventMaxParticipantsType = findViewById(R.id.FormMaxParticipantsType);

        pricesList = findViewById(R.id.PricesLayout);

        newPrice = findViewById(R.id.addPriceButton);

        event = new Event();

        //Event date field
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

        //Event time field
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

                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
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

        //Participations type dropdown
        String[] MAXPARTSTYPE = new String[] {getString(R.string.form_participations_type_0), getString(R.string.form_participations_type_1)};
        final ArrayAdapter newPriceTypeAdapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, MAXPARTSTYPE);
        formEventMaxParticipantsType.setAdapter(newPriceTypeAdapter);

        //Prices

        this.listPrices = new ArrayList<>();
        this.adapter = new EventPriceAdapter(this.listPrices, false, this.event, this);

        this.pricesList.getRecyclerView().setHorizontalScrollBarEnabled(false);
        this.pricesList.setScrollingEnabled(false);
        this.pricesList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        this.pricesList.setAdapter(this.adapter, true);
        this.pricesList.setCanDragHorizontally(false);

        //New price button
        this.newPrice.setOnClickListener(v ->
            {
                PopupMenu popup = new PopupMenu(this, v);
                popup.setOnMenuItemClickListener(item ->
                    {
                        EventPrice newPrice = null;

                        switch (item.getItemId())
                        {
                            case R.id.itemPrice:
                                newPrice = new EventPrice(0, 0, 2, "Éclat de feu", "https://xivapi.com/i/020000/020001.png", 1);
                                break;

                            case R.id.gilsPrice:
                                newPrice = new EventPrice(0, 0, 1, "Gil", "https://xivapi.com/i/065000/065002.png", 100000);
                                break;

                            default:
                                break;
                        }

                        if(newPrice != null)
                        {
                            listPrices.add(new Pair<>((long) ++count, newPrice));
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                        else
                        {
                            return false;
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
    public void onAttachFragment(@NotNull Fragment fragment)
    {
        if (fragment instanceof EventPriceEditDialogFragment)
        {
            EventPriceEditDialogFragment dialogFragment = (EventPriceEditDialogFragment) fragment;
            dialogFragment.setOnFragmentInteractionListener(this);
        }
        if (fragment instanceof ItemSelectionDialogFragment)
        {
            ItemSelectionDialogFragment dialogFragment = (ItemSelectionDialogFragment) fragment;
            dialogFragment.setOnItemSelectedListener(this);
        }
    }

    @Override
    public void onFragmentInteraction(EventPrice editedPrice, int position)
    {

    }

    @Override
    public void OnItemSelectedListener(LightItem item)
    {
        EventPriceEditDialogFragment fragment = (EventPriceEditDialogFragment) getSupportFragmentManager().findFragmentByTag(EventPriceEditDialogFragment.TAG);
        assert fragment != null;
        fragment.updatePriceItem(item);
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
            snackbar("En cours de développement");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void snackbar(String text)
    {
        Snackbar.make(findViewById(R.id.context_view), text, Snackbar.LENGTH_LONG).show();
    }

    //TODO Override up action to confirm cancelling

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
