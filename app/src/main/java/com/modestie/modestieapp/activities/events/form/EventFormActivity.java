package com.modestie.modestieapp.activities.events.form;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.DraggableEventPriceAdapter;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.model.item.LightItem;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;
import com.woxthebox.draglistview.DragListView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract activity inflating and implementing UI proceedings of the event form used to create and
 * modify events.
 * Subclasses activities "NewEventActivity" and "EventModificationActivity" implements theirs
 * processes of event creation / modification as well as data upload through HTTP requests.
 */
public abstract class EventFormActivity
        extends AppCompatActivity
        implements EventPriceEditDialogFragment.OnFragmentInteractionListener,
        ItemSelectionDialogFragment.OnItemSelectedListener
{
    public static final String TAG = "ACTVT.|EVENTFORM|";

    Toolbar toolbar;
    TextView activityTitle;
    Button toolbarAction;
    LinearLayoutCompat loadingLayout;
    LinearLayoutCompat formLayout;
    TextInputLayout formEventName;
    TextInputLayout formEventDate;
    TextInputLayout formEventTime;
    TextInputLayout formEventMaxParticipants;
    CheckBox formEventPromoterParticipant;
    TextInputLayout formEventDescription;
    TextInputLayout formEventImage;
    RadioGroup formEventMaxParticipantsType;
    DraggableEventPriceAdapter adapter;

    LinearLayout eventCardPreview;

    EventPriceEditDialogFragment editDialogFragment;
    ItemSelectionDialogFragment selectionDialogFragment;

    Button formRemoveImage;
    Button formNewPrice;

    Event event;
    ArrayList<Pair<Long, EventPrice>> listPrices;
    int count = 0;

    boolean today;
    long EPOCH; //Milliseconds

    Uri pickedImage;
    Bitmap bitmapConvertedImage;

    AlertDialog imageUploadError;

    LoggedInUser loggedInUser;

    RequestHelper requestHelper;
    int SOCKET_TIMEOUT = 3000;
    int MAX_RETRIES = 3;
    boolean pending;
    int pricePendingRequests;

    private static final int READ_EXTERNAL_STORAGE_REQUEST = 1;
    private static final int IMAGE_PICK_INTENT = 1;

    /*
        ----------------------------
        LIFECYCLE OVERRIDDEN METHODS
        ----------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        Log.e(TAG, "ON CREATE");

        //Toolbar setup
        this.toolbar = findViewById(R.id.toolbar);
        this.toolbar.setNavigationOnClickListener(
                v ->
                {
                    if (!this.pending) NavUtils.navigateUpFromSameTask(this);
                });

        /*----------
            Init
        ----------*/

        this.requestHelper = new RequestHelper(getApplicationContext());

        this.activityTitle = this.toolbar.findViewById(R.id.title);
        this.toolbarAction = this.toolbar.findViewById(R.id.eventFormAction);
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
                        Toast.makeText(EventFormActivity.this, "Veuillez entrer une date supérieure ou égale à celle d'aujourd'hui", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(EventFormActivity.this, "Veuillez choisir un horaire supérieur à l'heure actuelle", Toast.LENGTH_LONG).show();
                        s.clear();
                        correct = false;
                    }
                    else if (timediff < 30)
                    {
                        Toast.makeText(EventFormActivity.this, "Veuillez organiser votre événement au moins une demie-heure en avance", Toast.LENGTH_LONG).show();
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
        this.adapter = new DraggableEventPriceAdapter(this.listPrices, false, this.event, this);
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
                    popup.getMenuInflater().inflate(R.menu.event_form_price_type_selection_menu, popup.getMenu());
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
