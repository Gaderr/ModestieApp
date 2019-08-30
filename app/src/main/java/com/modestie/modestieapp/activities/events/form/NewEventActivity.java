package com.modestie.modestieapp.activities.events.form;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.utils.Utils;
import com.modestie.modestieapp.utils.network.RequestURLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class NewEventActivity extends EventFormActivity
{
    public static final String TAG = "ACTVT.NEWEVENT";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
                    Log.e(TAG, "SAVE EVENT OPTION SELECTED");
                    if (!this.pending)
                    {
                        if (this.validateForm())
                        {
                            Log.e(TAG, "FORM VALID");
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
     * New event post / Step 2 :: Post event to modestie.fr
     * Then proceed to step 3
     */
    @SuppressLint("SimpleDateFormat")
    public void eventPostStep2(String imageLink)
    {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        df.setTimeZone(TimeZone.getDefault());
        try
        {
            JSONObject postparams = new JSONObject();
            postparams.put("name", this.formEventName.getEditText().getText());
            postparams.put("promoter", this.loggedInUser.getCharacterID());
            postparams.put("epoch", pickedDate.getTime() / 1000);
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

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, RequestURLs.MODESTIE_EVENTS_ADD, postparams,
                    response ->
                    {
                        try
                        {
                            if (this.listPrices.size() > 0)
                            {
                                eventPostStep3(response.getInt("id"));
                            }
                            else
                            {
                                this.pending = false;
                                eventPostFinal(true, null);
                            }
                        }
                        catch (JSONException e)
                        {
                            Log.e(TAG, e.getLocalizedMessage());
                            this.pending = false;
                            eventPostFinal(false, "event");
                        }
                    },
                    error ->
                    {
                        //Log.e(TAG, error.getLocalizedMessage());
                        Log.e(TAG, "EVENT SENDING FAILED");
                        eventPostFinal(false, "event");
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
            eventPostFinal(false, "event");
        }
    }

    /**
     * New event post / Step 3 :: Post prices bounded to the posted event
     * Then finalize
     *
     * Note : Prices post requests are separated from event post request to avoid pushing data
     * to modestie.fr database if the event post request failed.
     */
    public void eventPostStep3(int eventID)
    {
        this.pricePendingRequests = 0;
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
                postparams.put("apiKey", this.loggedInUser.getAPIKey());

                ++this.pricePendingRequests;
                //Log.e(TAG, "PRICE SENDING NUMBER " + this.pricePendingRequests + " DEGREE " + this.listPrices.get(i).first);
                JsonObjectRequest priceRequest = new JsonObjectRequest(
                        Request.Method.POST, RequestURLs.MODESTIE_PRICES_ADD, postparams,
                        response ->
                        {
                            if (--this.pricePendingRequests == 0)
                                eventPostFinal(true, null);
                        },
                        error ->
                        {
                            //Log.e(TAG, error.getMessage());
                            Log.e(TAG, "PRICE SENDING FAILED");
                            this.pricePendingRequests--;
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

                this.requestHelper.addToRequestQueue(priceRequest, "newPricePostRequest" + this.pricePendingRequests);
            }
            catch (JSONException e)
            {
                Log.e(TAG, e.getLocalizedMessage());
                this.pricePendingRequests--;
            }
        }
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
