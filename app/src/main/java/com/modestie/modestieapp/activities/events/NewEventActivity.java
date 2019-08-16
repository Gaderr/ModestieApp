package com.modestie.modestieapp.activities.events;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.EventPriceAdapter;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.model.item.LightItem;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.utils.Utils;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;
import com.woxthebox.draglistview.DragListView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class NewEventActivity
        extends AppCompatActivity
        implements EventPriceEditDialogFragment.OnFragmentInteractionListener,
        ItemSelectionDialogFragment.OnItemSelectedListener
{
    private LinearLayoutCompat loadingLayout;
    private LinearLayoutCompat formLayout;
    private TextInputLayout formEventName;
    private TextInputLayout formEventDate;
    private TextInputLayout formEventTime;
    private TextInputLayout formEventMaxParticipants;
    private CheckBox formEventPromoterParticipant;
    private TextInputLayout formEventDescription;
    private TextInputLayout formEventImage;
    private RadioGroup formEventMaxParticipantsType;
    private EventPriceAdapter adapter;

    private LinearLayout eventCardPreview;

    private EventPriceEditDialogFragment editDialogFragment;
    private ItemSelectionDialogFragment selectionDialogFragment;

    Button formRemoveImage;
    Button formNewPrice;

    Event event;
    private ArrayList<Pair<Long, EventPrice>> listPrices;
    private int count = 0;

    private boolean today;
    private long EPOCH; //Milliseconds

    private Uri pickedImage;
    private Bitmap bitmapConvertedImage;

    private AlertDialog imageUploadError;

    public static final String TAG = "ACTVT.NEWEVNT";
    public static final int IMAGE_PICK_INTENT = 1;

    private LoggedInUser loggedInUser;

    private RequestHelper requestHelper;
    int SOCKET_TIMEOUT = 3000;
    int MAX_RETRIES = 3;
    private boolean pending;
    private int pricePendingRequests;

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 1;

    /*
        ----------------------------
        LIFECYCLE OVERRIDDEN METHODS
        ----------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        Log.e(TAG, "ON CREATE");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /*----------
            Init
        ----------*/

        this.requestHelper = new RequestHelper(getApplicationContext());

        this.loadingLayout = findViewById(R.id.loadingLayout);
        this.formLayout = findViewById(R.id.formLayout);
        this.formEventName = findViewById(R.id.FormEventName);
        this.formEventDate = findViewById(R.id.FormEventDate);
        this.formEventTime = findViewById(R.id.FormEventTime);
        this.formEventDescription = findViewById(R.id.FormEventDescription);
        this.formEventImage = findViewById(R.id.FormEventImage);
        this.formEventMaxParticipants = findViewById(R.id.FormMaxParticipants);
        this.formEventMaxParticipantsType = findViewById(R.id.selectParticipationTypes);
        this.formEventPromoterParticipant = findViewById(R.id.FormEventPromoterParticipant);
        this.formRemoveImage = findViewById(R.id.removeImage);
        this.formNewPrice = findViewById(R.id.addPriceButton);

        this.eventCardPreview = findViewById(R.id.cardPreview);

        this.formEventMaxParticipants.setEnabled(false);
        this.formEventMaxParticipants.setHelperText("");

        Hawk.init(getApplicationContext()).build();

        loggedInUser = Hawk.get("LoggedInUser");

        this.event = new Event();
        this.pickedImage = null;
        this.bitmapConvertedImage = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(NewEventActivity.this, R.style.ThemeOverlay_ModestieTheme_Dialog);
        builder.setTitle(getString(R.string.image_upload_error_dialog_title))
                .setMessage(getString(R.string.image_upload_error_dialog_message))
                .setPositiveButton(getString(R.string.image_upload_error_dialog_pos_btn), (dialog, which) -> postNewEvent(""))
                .setNegativeButton(R.string.image_upload_error_dialog_neg_btn, (dialog, which) ->
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("Error", "image");
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                });
        this.imageUploadError = builder.create();

        this.today = true;
        this.EPOCH = 0L;
        final Calendar c = Calendar.getInstance(Locale.FRANCE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String cDay, cMonth;
        if (day < 10)
            cDay = "0" + day;
        else
            cDay = day + "";
        if (month < 10)
            cMonth = "0" + (month + 1);
        else
            cMonth = (month + 1) + "";
        this.formEventDate.getEditText().setText(String.format(Locale.FRANCE, "%s/%s/%d", cDay, cMonth, year));

        ((TextView) this.eventCardPreview.findViewById(R.id.participantsCount)).setText("--/∞");

        this.pending = false;

        /*-----------------
            Text fields
        -----------------*/

        this.formEventName.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { ((TextView) eventCardPreview.findViewById(R.id.eventTitle)).setText(s.toString()); }
        });

        this.formEventMaxParticipants.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { ((TextView) eventCardPreview.findViewById(R.id.participantsCount)).setText(String.format(Locale.FRANCE, "--/%s", s.toString())); }
        });

        this.formEventDescription.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { ((TextView) eventCardPreview.findViewById(R.id.eventDescription)).setText(s.toString()); }
        });

        /*----------------------
            Event date field
        ----------------------*/

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, R.style.ThemeOverlay_ModestieTheme_Dialog,
                (view, pickedYear, pickedMonth, pickedDay) ->
                {
                    String sDay, sMonth;
                    if (pickedDay < 10)
                        sDay = "0" + pickedDay;
                    else
                        sDay = pickedDay + "";
                    if (++pickedMonth < 10)
                        sMonth = "0" + pickedMonth;
                    else
                        sMonth = pickedMonth + "";

                    formEventDate.getEditText().setText(String.format(Locale.FRANCE, "%s/%s/%d", sDay, sMonth, pickedYear));
                },
                year,
                month,
                day
        );
        datePickerDialog.setOnCancelListener(
                dialog ->
                {
                    formEventDate.getEditText().clearFocus();
                    hideKeyboardFrom(getApplicationContext(), formEventDate);
                });
        this.formEventDate.setStartIconOnClickListener(
                v -> datePickerDialog.show());
        this.formEventDate.getEditText().setOnFocusChangeListener(
                (v, hasFocus) ->
                {
                    if (hasFocus)
                        datePickerDialog.show();
                });
        this.formEventDate.getEditText().addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override

            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            public void afterTextChanged(Editable s)
            {
                formEventDate.getEditText().clearFocus();
                hideKeyboardFrom(getApplicationContext(), formEventDate);

                if (s.length() == 0)
                    return;

                try
                {
                    today = Integer.parseInt(s.subSequence(0, 2).toString()) == day
                            && Integer.parseInt(s.subSequence(3, 5).toString()) == (month + 1)
                            && Integer.parseInt(s.subSequence(6, 10).toString()) == year;

                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    df.setTimeZone(TimeZone.getDefault());
                    long millis = df.parse(s.toString()).getTime() + 86399000; //This date + 23:59:59
                    if (millis < System.currentTimeMillis())
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez entrer une date supérieure ou égale à celle d'aujourd'hui", Toast.LENGTH_LONG).show();
                        s.clear();
                        ((TextView) eventCardPreview.findViewById(R.id.eventDate)).setText("Le -- ---- ---- à --:--");
                    }
                    else
                    {
                        SimpleDateFormat format = new SimpleDateFormat("'Le' dd MMMM 'à' HH'h'mm ");
                        format.setTimeZone(TimeZone.getDefault());
                        EPOCH = millis - 86399000;
                        String time = formEventTime.getEditText().getText().toString();
                        if (!time.equals(""))
                        {
                            int hour = Integer.parseInt(time.substring(0, 2));
                            int minute = Integer.parseInt(time.substring(3, 5));
                            EPOCH += hour * 3600000 + minute * 60000; //Conversion in MILLISECONDS
                        }
                        ((TextView) eventCardPreview.findViewById(R.id.eventDate)).setText(format.format(EPOCH));
                    }
                }
                catch (ParseException e)
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        /*----------------------
            Event time field
        ----------------------*/

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this, R.style.ThemeOverlay_ModestieTheme_Dialog,
                (view, pickedHour, pickedMinute) ->
                {
                    String sHour, sMinute;
                    if (pickedHour < 10)
                        sHour = "0" + pickedHour;
                    else
                        sHour = pickedHour + "";

                    if (pickedMinute < 10)
                        sMinute = "0" + pickedMinute;
                    else
                        sMinute = pickedMinute + "";

                    formEventTime.getEditText().setText(String.format(Locale.FRANCE, "%s:%s", sHour, sMinute));
                },
                12,
                0,
                true
        );
        timePickerDialog.setOnCancelListener(
                dialog ->
                {
                    formEventTime.getEditText().clearFocus();
                    hideKeyboardFrom(getApplicationContext(), formEventTime);
                });
        timePickerDialog.setOnDismissListener(
                dialog ->
                {
                    formEventTime.getEditText().clearFocus();
                    hideKeyboardFrom(getApplicationContext(), formEventTime);
                });
        this.formEventTime.setStartIconOnClickListener(
                v -> timePickerDialog.show());
        this.formEventTime.getEditText().setOnFocusChangeListener(
                (v, hasFocus) ->
                {
                    if (hasFocus)
                        timePickerDialog.show();
                });
        this.formEventTime.getEditText().addTextChangedListener(new TextWatcher()
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
                boolean correct = true;

                if (s.length() == 0)
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

                if (today)
                {
                    if (timediff <= 0)
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez choisir un horaire supérieur à l'heure actuelle", Toast.LENGTH_LONG).show();
                        s.clear();
                        correct = false;
                    }
                    else if (timediff < 30)
                    {
                        Toast.makeText(NewEventActivity.this, "Veuillez organiser votre événement au moins une demie-heure en avance", Toast.LENGTH_LONG).show();
                        s.clear();
                        correct = false;
                    }
                }

                if (correct)
                    try
                    {
                        String date = formEventDate.getEditText().getText().toString();
                        SimpleDateFormat df_date = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                        df_date.setTimeZone(TimeZone.getDefault());
                        EPOCH = df_date.parse(date).getTime() + sHour * 3600000 + sMinute * 60000;

                        SimpleDateFormat format = new SimpleDateFormat("'Le' dd MMMM 'à' HH'h'mm ", Locale.FRANCE);
                        format.setTimeZone(TimeZone.getDefault());
                        ((TextView) eventCardPreview.findViewById(R.id.eventDate)).setText(format.format(EPOCH));
                    }
                    catch (ParseException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("Error", "unk");
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
            }
        });

        /*------------------------------------
            Participation type radio group
        ------------------------------------*/

        this.formEventMaxParticipantsType.setOnCheckedChangeListener(
                (group, checkedId) ->
                {
                    switch (checkedId)
                    {
                        case R.id.participationType0: //Unlimited
                            this.formEventMaxParticipants.setEnabled(false);
                            this.formEventMaxParticipants.setHelperText("");
                            ((TextView) this.eventCardPreview.findViewById(R.id.participantsCount)).setText("--/∞");
                            break;

                        case R.id.participationType1: //Limited
                            this.formEventMaxParticipants.setEnabled(true);
                            this.formEventMaxParticipants.setHelperText(getString(R.string.form_required));
                            ((TextView) this.eventCardPreview.findViewById(R.id.participantsCount)).setText(String.format(Locale.FRANCE, "--/%s", this.formEventMaxParticipants.getEditText().getText()));
                            break;
                    }
                });

        /*---------------------
            Image selection
        ---------------------*/

        this.formEventImage.setEnabled(false);
        this.formEventImage.setHelperText("10MB max");
        findViewById(R.id.fileUploadIcon).setOnClickListener(
                v ->
                {
                    if (!checkPermissions())
                        return;
                    //Create an Intent with action as ACTION_PICK
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    //Sets the type as image/*. This ensures only components of type image are selected
                    intent.setType("image/*");
                    //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                    String[] mimeTypes = {"image/jpeg", "image/png"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    //Launching the Intent
                    startActivityForResult(intent, IMAGE_PICK_INTENT);
                });

        /*-------------------------
            Remove image button
        -------------------------*/

        this.formRemoveImage.setOnClickListener(
                v ->
                {
                    this.bitmapConvertedImage = null;
                    this.formEventImage.getEditText().setText("");
                    this.formRemoveImage.setEnabled(false);
                    ImageView imagePreview = this.eventCardPreview.findViewById(R.id.eventImage);
                    Picasso.get().load(R.drawable.logout_icon1).into(imagePreview);
                });

        /*------------------------------------
            Event preview card static data
        ------------------------------------*/

        LightCharacter character = Hawk.get("UserCharacter");

        Picasso.get()
                .load(character.getAvatarURL())
                .into((ImageView) this.eventCardPreview.findViewById(R.id.promoterAvatar));

        ((TextView) this.eventCardPreview.findViewById(R.id.characterPromoter)).setText(String.format("Organisé par %s", character.getName()));

        /*-----------------
            Prices list
        -----------------*/

        DragListView formPricesList = findViewById(R.id.PricesLayout);
        this.listPrices = new ArrayList<>();
        this.adapter = new EventPriceAdapter(this.listPrices, false, this.event, this);
        formPricesList.getRecyclerView().setHorizontalScrollBarEnabled(false);
        formPricesList.setScrollingEnabled(false);
        formPricesList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        formPricesList.setAdapter(this.adapter, true);
        formPricesList.setCanDragHorizontally(false);

        /*----------------------
            New price button
        ----------------------*/

        this.formNewPrice.setOnClickListener(
                v ->
                {
                    PopupMenu popup = new PopupMenu(this, v);
                    popup.setOnMenuItemClickListener(
                            item ->
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

                                if (newPrice != null)
                                {
                                    this.listPrices.add(new Pair<>((long) ++count, newPrice));
                                    this.adapter.notifyDataSetChanged();
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
        if (sharedPref.getBoolean("nightmode", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /*
        -----------------------------------
        OUT-OF-LIFECYCLE OVERRIDDEN METHODS
        -----------------------------------
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_event_bar_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == IMAGE_PICK_INTENT)
            {
                //data.getData returns the content URI for the selected Image
                this.pickedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(this.pickedImage, filePathColumn, null, null, null);
                if (cursor.moveToFirst())
                {
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    this.bitmapConvertedImage = BitmapFactory.decodeFile(imagePath, options);
                    this.formEventImage.getEditText().setText(new File(imagePath).getName());
                    this.formRemoveImage.setEnabled(true);
                    ImageView imagePreview = this.eventCardPreview.findViewById(R.id.eventImage);
                    Picasso.get().load(this.pickedImage).fit().centerCrop().into(imagePreview);
                }
                cursor.close();
            }
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment)
    {
        if (fragment instanceof EventPriceEditDialogFragment)
        {
            this.editDialogFragment = (EventPriceEditDialogFragment) fragment;
            this.editDialogFragment.setOnFragmentInteractionListener(this);
        }
        else if (fragment instanceof ItemSelectionDialogFragment)
        {
            this.selectionDialogFragment = (ItemSelectionDialogFragment) fragment;
            this.selectionDialogFragment.setOnItemSelectedListener(this);
        }
    }

    @Override
    public void onFragmentInteraction(@NotNull EventPrice editedPrice, int position)
    {
        assert this.selectionDialogFragment != null;
        Long id = this.listPrices.get(position - 1).first;
        this.listPrices.set(position - 1, new Pair<>(id, editedPrice));
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemSelectedListener(@NotNull LightItem item)
    {
        assert this.editDialogFragment != null;
        this.editDialogFragment.updatePriceItem(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parentView activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_event)
        {
            Log.e(TAG, "SAVE EVENT OPTION SELECTED");
            if (!this.pending)
            {
                if (validateForm())
                {
                    Log.e(TAG, "FORM VALID");
                    this.loadingLayout.setVisibility(View.VISIBLE);
                    this.formLayout.setVisibility(View.INVISIBLE);
                    hideKeyboardFrom(NewEventActivity.this, this.formLayout);
                    beginEventPost();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        --------------
        CUSTOM METHODS
        --------------
     */

    public boolean validateForm()
    {
        boolean result = true;
        String error = getString(R.string.form_required);

        if ((this.formEventName.getEditText().getText() + "").equals(""))
        {
            this.formEventName.setError(error);
            result = false;
        }
        else
            this.formEventName.setError("");

        if ((this.formEventDate.getEditText().getText() + "").equals(""))
        {
            this.formEventDate.setError(error);
            result = false;
        }
        else
            this.formEventDate.setError("");

        if ((this.formEventTime.getEditText().getText() + "").equals(""))
        {
            this.formEventTime.setError(error);
            result = false;
        }
        else
            this.formEventTime.setError("");

        if (this.EPOCH == 0L)
            result = false;

        if (this.formEventMaxParticipantsType.getCheckedRadioButtonId() == R.id.participationType1)
        {
            if ((this.formEventMaxParticipants.getEditText().getText() + "").equals(""))
            {
                this.formEventMaxParticipants.setError(error);
                result = false;
            }
            else
            {
                if (Integer.parseInt(this.formEventMaxParticipants.getEditText().getText() + "") < 2
                        && this.formEventPromoterParticipant.isChecked())
                {
                    this.formEventMaxParticipants.setError(getString(R.string.form_too_few_participants));
                    result = false;
                }
                else
                    this.formEventMaxParticipants.setError("");
            }
        }

        return result;
    }

    private void beginEventPost()
    {
        Log.e(TAG, "BEGIN EVENT POST");
        StringRequest imageUploadRequest = new StringRequest(
                Request.Method.POST, RequestURLs.IMGUR_IMG_UPLOAD,
                response ->
                {
                    try
                    {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.e(TAG, jsonResponse.toString());

                        if (jsonResponse.getBoolean("success"))
                            postNewEvent(jsonResponse.getJSONObject("data").getString("link"));
                        else
                            imageUploadError.show();
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                        pending = false;
                        finalizeEventPostThenActivity(false, "image");
                    }
                },
                error ->
                {
                    Log.e(TAG, error.toString());
                    Log.e(TAG, "finish/error upload");
                    imageUploadError.show();
                })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Client-ID " + RequestURLs.IMGUR_CLIENT_ID);
                return headers;
            }

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put(RequestURLs.IMGUR_TAG_IMAGE, Utils.getBase64Image(bitmapConvertedImage));
                params.put(RequestURLs.IMGUR_TAG_TYPE, "base64");
                params.put(RequestURLs.IMGUR_TAG_TITLE, formEventImage.getEditText().getText() + "");
                params.put(RequestURLs.IMGUR_TAG_NAME, String.valueOf(System.currentTimeMillis()));
                return params;
            }
        };

        //Retry MAX_RETRIES times, one every SOCKET_TIMEOUT milliseconds
        imageUploadRequest.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Check if an image was picked by user
        if (this.bitmapConvertedImage != null)
            this.requestHelper.addToRequestQueue(imageUploadRequest);
        else
            postNewEvent("");

        this.pending = true;
    }

    @SuppressLint("SimpleDateFormat")
    public void postNewEvent(String imageLink)
    {
        Log.e(TAG, "POST NEW EVENT");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        df.setTimeZone(TimeZone.getDefault());
        try
        {
            JSONObject postparams = new JSONObject();
            postparams.put("name", this.formEventName.getEditText().getText());
            postparams.put("promoter", "11148489");
            postparams.put("epoch", EPOCH / 1000);
            postparams.put("description", this.formEventDescription.getEditText().getText() + "");
            if (this.formEventMaxParticipantsType.getCheckedRadioButtonId() == R.id.participationType1)
                postparams.put("maxparticipants", this.formEventMaxParticipants.getEditText().getText() + "");
            else
                postparams.put("maxparticipants", "-1");
            if (this.formEventPromoterParticipant.isChecked())
                postparams.put("promoterParticipant", "1");
            else
                postparams.put("promoterParticipant", "0");
            postparams.put("image", imageLink);
            postparams.put("apiKey", loggedInUser.getAPIKey());

            Log.e(TAG, "ADDEVENT");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, RequestURLs.MODESTIE_EVENTS_ADD, postparams,
                    response ->
                    {
                        try
                        {
                            if (this.listPrices.size() > 0)
                            {
                                postPrices(response.getInt("id"));
                            }
                            else
                            {
                                pending = false;
                                finalizeEventPostThenActivity(true, null);
                            }
                        }
                        catch (JSONException e)
                        {
                            Log.e(TAG, e.getLocalizedMessage());
                            pending = false;
                            finalizeEventPostThenActivity(false, "event");
                        }
                    },
                    error ->
                    {
                        //Log.e(TAG, error.getLocalizedMessage());
                        Log.e(TAG, "EVENT SENDING FAILED");
                        finalizeEventPostThenActivity(false, "event");
                    }
            )
            {
                @Override
                public Map<String, String> getHeaders()
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + loggedInUser.getToken());
                    return params;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            this.requestHelper.addToRequestQueue(request, "newEventPostRequest");
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public void postPrices(int eventID)
    {
        pricePendingRequests = 0;
        for (int i = 0; i < this.listPrices.size(); i++)
        {
            try
            {
                EventPrice price = this.listPrices.get(i).second;
                JSONObject postparams = new JSONObject();
                postparams.put("eventID", eventID);
                postparams.put("degree", this.listPrices.get(i).first);
                postparams.put("itemID", price.getItemID());
                postparams.put("itemName", price.getItemName());
                postparams.put("itemIcon", price.getItemIconURL());
                postparams.put("amount", price.getAmount());
                postparams.put("apiKey", loggedInUser.getAPIKey());

                ++pricePendingRequests;
                Log.e(TAG, "PRICE SENDING NUMBER " + pricePendingRequests + " DEGREE " + this.listPrices.get(i).first);
                JsonObjectRequest priceRequest = new JsonObjectRequest(
                        Request.Method.POST, RequestURLs.MODESTIE_PRICES_ADD, postparams,
                        response ->
                        {
                            if (--pricePendingRequests == 0)
                                finalizeEventPostThenActivity(true, null);
                        },
                        error ->
                        {
                            //Log.e(TAG, error.getMessage());
                            Log.e(TAG, "PRICE SENDING FAILED");
                            pricePendingRequests--;
                        }
                )
                {
                    @Override
                    public Map<String, String> getHeaders()
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " + loggedInUser.getToken());
                        return params;
                    }
                };

                priceRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                this.requestHelper.addToRequestQueue(priceRequest, "newPricePostRequest" + pricePendingRequests);
            }
            catch (JSONException e)
            {
                Log.e(TAG, e.getLocalizedMessage());
                pricePendingRequests--;
            }
        }
    }

    public void finalizeEventPostThenActivity(boolean success, @Nullable String errorValue)
    {
        Log.e(TAG, "FINAL");
        if (success)
        {
            Toast.makeText(this, "Événement créé", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Error", errorValue);
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            pending = false;
        }
    }

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Check if permission READ_EXTERNAL_STORAGE is granted by the user and creates a request
     * permission if not.
     *
     * @return Permission state : true = permission granted
     */
    public boolean checkPermissions()
    {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
            return false;
        }
        else return true;
    }
}
