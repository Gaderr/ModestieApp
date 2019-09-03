package com.modestie.modestieapp.activities.events.form;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.events.list.EventListActivity;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.utils.Utils;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventModificationActivity extends EventFormActivity
{
    public static final String TAG = "ACTVT.EVENTMODIF";

    private Event event;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.db = FirebaseFirestore.getInstance();
        //Get event to modify
        this.event = Hawk.get("SelectedEvent");
        //Set timestamp before calling superclass for proper instantiation
        this.pickedDate = this.event.getEventDate();

        //Call super class to instantiate fields
        super.onCreate(savedInstanceState);

        this.activityTitle.setText(getString(R.string.title_event_modification_activity));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_ModestieTheme_Dialog);
        builder.setTitle(getString(R.string.image_upload_error_dialog_title))
                .setMessage(getString(R.string.image_upload_error_dialog_message))
                .setPositiveButton(getString(R.string.image_upload_error_dialog_pos_btn), (dialog, which) -> eventUpdateStep1())
                .setNegativeButton(R.string.image_upload_error_dialog_neg_btn, (dialog, which) ->
                {
                    this.loadingLayout.setVisibility(View.VISIBLE);
                    this.formLayout.setVisibility(View.INVISIBLE);
                    this.toolbarAction.setEnabled(true);
                });
        this.imageUploadError = builder.create();

        this.toolbarAction.setOnClickListener(
                v ->
                {
                    if (!this.pending)
                    {
                        if (this.validateForm())
                        {
                            this.loadingLayout.setVisibility(View.VISIBLE);
                            this.formLayout.setVisibility(View.INVISIBLE);
                            hideKeyboardFrom(this, this.formLayout);
                            this.toolbarAction.setEnabled(false);
                            eventUpdateStep1();
                        }
                    }
                }
        );

        //Setup event fields
        this.formEventName.getEditText().setText(this.event.getName());

        String cHour, cMinutes;
        if (this.hourOfDay < 10)
            cHour = "0" + this.hourOfDay;
        else
            cHour = "" + this.hourOfDay;

        if (this.minutesOfDay < 10)
            cMinutes = "0" + this.minutesOfDay;
        else
            cMinutes = "" + this.minutesOfDay;
        this.formEventTime.getEditText().setText(String.format(Locale.FRANCE, "%s:%s", cHour, cMinutes));

        this.formEventDescription.getEditText().setText(this.event.getDescription());
        this.formEventImage.getEditText().setText(this.event.getImageURL());

        if (!this.event.getImageURL().equals(""))
        {
            ImageView imagePreview = this.eventCardPreview.findViewById(R.id.eventImage);
            Picasso.get().load(this.event.getImageURL()).fit().centerCrop().into(imagePreview);
        }

        if (this.event.getMaxParticipants() == -1)
        {
            this.formEventMaxParticipantsType.check(R.id.participationType0);
        }
        else
        {
            this.formEventMaxParticipantsType.check(R.id.participationType1);
            this.formEventMaxParticipants.getEditText().setText(String.format(Locale.FRANCE, "%d", this.event.getMaxParticipants()));
        }

        this.formEventPromoterParticipant.setChecked(this.event.isPromoterParticipant());

        this.listPrices.clear();
        this.count = 0;
        for (int i = 0; i < this.event.getPrices().size(); i++)
        {
            this.listPrices.add(new Pair<>((long) ++count, this.event.getPrices().get(i)));
        }
        this.adapter.notifyDataSetChanged();

        this.eventManagementLayout.setVisibility(View.VISIBLE);

        //Delete event button listener
        this.deleteEventButton.setOnClickListener(
                v ->
                {
                    AlertDialog.Builder deleteEventAlertBuilder = new AlertDialog.Builder(this, R.style.ThemeOverlay_ModestieTheme_Dialog);
                    deleteEventAlertBuilder.setTitle(getString(R.string.event_deletion_alert_title))
                            .setMessage(getString(R.string.event_deletion_alert_body))
                            .setPositiveButton(getString(R.string.event_deletion_alert_confirm), (dialog, which) ->
                            {
                                this.loadingLayout.setVisibility(View.VISIBLE);
                                this.formLayout.setVisibility(View.INVISIBLE);
                                hideKeyboardFrom(this, this.formLayout);
                                this.toolbarAction.setEnabled(false);
                                eventDelete();
                            })
                            .setNegativeButton(R.string.event_deletion_alert_cancel, (dialog, which) ->
                            {
                            });
                    deleteEventAlertBuilder.create().show();
                }
        );
    }

    /**
     * New update post / Step 1 :: Upload illustration to Imgur if changed
     * Then proceed to step 2
     */
    private void eventUpdateStep1()
    {
        this.pending = true;

        if (this.illustrationChanged)
        {
            //Check if an image was picked by user
            if (this.bitmapConvertedImage == null)
                eventUpdateStep2("");
            else
            {
                StringRequest imageUploadRequest = new StringRequest(
                        Request.Method.POST, RequestURLs.IMGUR_IMG_UPLOAD,
                        response ->
                        {
                            try
                            {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.getBoolean("success"))
                                {
                                    eventUpdateStep2(jsonResponse.getJSONObject("data").getString("link"));
                                }
                                else
                                    imageUploadError.show();
                            }
                            catch (JSONException e)
                            {
                                Log.e(TAG, e.getLocalizedMessage());
                                eventUpdateFinal(false, "image");
                            }
                        },
                        error ->
                        {
                            //Log.e(TAG, error.toString());
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

                this.requestHelper.addToRequestQueue(imageUploadRequest);
            }
        }
        else
            eventUpdateStep2(this.event.getImageURL());
    }

    /**
     * New update post / Step 2 :: Update event
     * Then finalize
     */
    private void eventUpdateStep2(String imageLink)
    {
        int maxParticipants;
        if (this.formEventMaxParticipantsType.getCheckedRadioButtonId() == R.id.participationType1)
            maxParticipants = Integer.parseInt(this.formEventMaxParticipants.getEditText().getText() + "");
        else
            maxParticipants = -1;

        Map<String, Map<String, Object>> prices = new HashMap<>();
        int count = 0;
        for (Pair<Long, EventPrice> eventPrice : this.listPrices)
        {
            Map<String, Object> documentPrice = new HashMap<>();
            documentPrice.put("degree", eventPrice.first);
            documentPrice.put("amount", eventPrice.second.getAmount());
            documentPrice.put("itemID", eventPrice.second.getItemID());
            documentPrice.put("itemIconURL", eventPrice.second.getItemIconURL());
            documentPrice.put("itemName", eventPrice.second.getItemName());
            prices.put("price" + ++count, documentPrice);
        }

        this.db.collection("events").document(this.event.getID())
                .update(
                        "name", this.formEventName.getEditText().getText() + "",
                        "promoterID", this.userCharacter.getID(),
                        "timestamp", new Timestamp(this.pickedDate),
                        "illustration", imageLink,
                        "description", this.formEventDescription.getEditText().getText() + "",
                        "maxParticipants", maxParticipants,
                        "promoterParticipant", this.formEventPromoterParticipant.isChecked(),
                        "prices", prices)
                .addOnCompleteListener(task -> eventUpdateFinal(true, null))
                .addOnFailureListener(e -> eventUpdateFinal(false, "update"));
    }

    /**
     * Deletes the event document from Firebase
     */
    private void eventDelete()
    {
        this.pending = true;
        this.db.collection("events").document(this.event.getID())
                .delete()
                .addOnSuccessListener(aVoid -> eventDeleteFinal(true, null))
                .addOnFailureListener(e -> eventDeleteFinal(false, "delete"));
    }

    private void eventUpdateFinal(boolean success, @Nullable String errorValue)
    {
        this.pending = false;
        if (success)
        {
            Toast.makeText(this, "Événement modifié", Toast.LENGTH_SHORT).show();
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
        }
    }

    private void eventDeleteFinal(boolean success, @Nullable String errorValue)
    {
        this.pending = false;
        if (success)
        {
            Toast.makeText(this, "Événement supprimé", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            setResult(EventListActivity.RESULT_OK, returnIntent);
            finish();
        }
        else
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Error", errorValue);
            setResult(EventListActivity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }
}
