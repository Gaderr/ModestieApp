package com.modestie.modestieapp.activities.events.form;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.utils.Utils;
import com.modestie.modestieapp.utils.network.RequestURLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewEventActivity extends EventFormActivity
{
    public static final String TAG = "ACTVT.NEWEVENT";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.db = FirebaseFirestore.getInstance();

        this.activityTitle.setText(getString(R.string.title_new_event_activity));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_ModestieTheme_Dialog);
        builder.setTitle(getString(R.string.image_upload_error_dialog_title))
                .setMessage(getString(R.string.image_upload_error_dialog_message))
                .setPositiveButton(getString(R.string.image_upload_error_dialog_pos_btn), (dialog, which) -> eventPostStep2(""))
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
                            this.eventPostStep1();
                        }
                    }
                }
        );
    }

    /**
     * New event post / Step 1 :: Upload illustration to Imgur
     * Then proceed to step 2
     */
    void eventPostStep1()
    {
        StringRequest imageUploadRequest = new StringRequest(
                Request.Method.POST, RequestURLs.IMGUR_IMG_UPLOAD,
                response ->
                {
                    try
                    {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.e(TAG, jsonResponse.toString());

                        if (jsonResponse.getBoolean("success"))
                            eventPostStep2(jsonResponse.getJSONObject("data").getString("link"));
                        else
                            this.imageUploadError.show();
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                        this.pending = false;
                        eventPostFinal(false, "image");
                    }
                },
                error ->
                {
                    Log.e(TAG, error.toString());
                    Log.e(TAG, "finish/error upload");
                    this.imageUploadError.show();
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
            eventPostStep2("");
    }

    /**
     * New event post / Step 2 :: Post event to Firebase
     * Then finalize
     */
    public void eventPostStep2(String imageLink)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.formEventName.getEditText().getText() + "");
        data.put("promoterID", this.userCharacter.getID());
        data.put("timestamp", new Timestamp(this.pickedDate));
        data.put("illustration", imageLink);
        data.put("description", this.formEventDescription.getEditText().getText() + "");
        if (this.formEventMaxParticipantsType.getCheckedRadioButtonId() == R.id.participationType1)
            data.put("maxParticipants", Integer.parseInt(this.formEventMaxParticipants.getEditText().getText() + ""));
        else
            data.put("maxParticipants", -1);
        data.put("promoterParticipant", this.formEventPromoterParticipant.isChecked());
        data.put("participants", null);
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
        data.put("prices", prices);

        this.db.collection("events")
                .add(data)
                .addOnCompleteListener(
                        task ->
                        {
                            this.pending = false;
                            eventPostFinal(true, null);
                        })
                .addOnFailureListener(
                        e ->
                        {
                            Log.e(TAG, e.getLocalizedMessage());
                            this.pending = false;
                            eventPostFinal(false, "event");
                        });
    }

    public void eventPostFinal(boolean success, @Nullable String errorValue)
    {
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
}
