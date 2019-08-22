package com.modestie.modestieapp.activities.events.form;

import android.annotation.SuppressLint;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.utils.Utils;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EventModificationActivity extends EventFormActivity
{
    public static final String TAG = "ACTVT.EVENTMODIF";

    private Event event;

    private JSONObject postParams;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Get event to modify
        this.event = Hawk.get("SelectedEvent");
        //Set timestamp before calling superclass for proper instantiation
        this.EPOCH = this.event.getEventEpochTime() * 1000;

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
                    Log.e(TAG, "MODIFY EVENT OPTION SELECTED");
                    if (!this.pending)
                    {
                        if (this.validateForm())
                        {
                            Log.e(TAG, "FORM VALID");
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

        if(!this.event.getImageURL().equals(""))
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
    }

    /**
     * New update post / Step 1 :: Upload illustration to Imgur if changed
     * Then proceed to step 2
     */
    private void eventUpdateStep1()
    {
        this.pending = true;
        this.postParams = new JSONObject();

        if (this.illustrationChanged)
        {
            //Check if an image was picked by user
            if (this.bitmapConvertedImage == null)
            {
                try
                {
                    this.postParams.put("image", "");
                    eventUpdateStep2();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    pending = false;
                    eventUpdateFinal(false, "image");
                }
            }
            else
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
                                {
                                    this.postParams.put("image", jsonResponse.getJSONObject("data").getString("link"));
                                    eventUpdateStep2();
                                }
                                else
                                    imageUploadError.show();
                            }
                            catch (JSONException e)
                            {
                                Log.e(TAG, e.getLocalizedMessage());
                                pending = false;
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
            eventUpdateStep2();
    }

    /**
     * New update post / Step 2 :: Update prices list - Delete prices
     * If price list changed delete current prices then proceed to step 2.1
     * If not, ignore and proceed to step 3
     */
    private void eventUpdateStep2()
    {
        if (this.pricesChanged)
        {
            try
            {
                JSONObject pricesRemovalParams = new JSONObject();
                pricesRemovalParams.put("eventID", event.getID());
                pricesRemovalParams.put("apiKey", this.loggedInUser.getAPIKey());

                JsonObjectRequest priceRequest = new JsonObjectRequest(
                        Request.Method.POST, RequestURLs.MODESTIE_PRICES_REMOVE, pricesRemovalParams,
                        response ->
                        {
                            if(this.listPrices.size() == 0)
                                eventUpdateStep3();
                            else
                                eventUpdateStep2_1();
                        },
                        error -> Log.e(TAG, "PRICE DELETION FAILED")
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

                priceRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                this.requestHelper.addToRequestQueue(priceRequest, "newPricePostRequest" + this.pricePendingRequests);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                eventUpdateFinal(false, "event");
            }
        }
        else
            eventUpdateStep3();
    }

    /**
     * New update post / Step 2.1 :: Update prices list - Upload new prices
     * Then proceed to step 3
     */
    private void eventUpdateStep2_1()
    {
        this.pricePendingRequests = 0;
        for (int i = 0; i < this.listPrices.size(); i++)
        {
            try
            {
                EventPrice price = this.listPrices.get(i).second;
                JSONObject postParams = new JSONObject();
                postParams.put("eventID", this.event.getID());
                postParams.put("degree", this.listPrices.get(i).first);
                postParams.put("itemID", price.getItemID());
                postParams.put("itemName", price.getItemName());
                postParams.put("itemIcon", price.getItemIconURL());
                postParams.put("amount", price.getAmount());
                postParams.put("apiKey", this.loggedInUser.getAPIKey());

                ++this.pricePendingRequests;
                Log.e(TAG, "PRICE SENDING NUMBER " + this.pricePendingRequests + " DEGREE " + this.listPrices.get(i).first);
                JsonObjectRequest priceRequest = new JsonObjectRequest(
                        Request.Method.POST, RequestURLs.MODESTIE_PRICES_ADD, postParams,
                        response ->
                        {
                            if (--this.pricePendingRequests == 0)
                                eventUpdateStep3();
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
                eventUpdateFinal(false, "event");
            }
        }
    }

    /**
     * New update post / Step 3 :: Update event
     * Then finalize
     */
    @SuppressLint("SimpleDateFormat")
    private void eventUpdateStep3()
    {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        df.setTimeZone(TimeZone.getDefault());
        try
        {
            this.postParams.put("eventID", this.event.getID());
            this.postParams.put("name", this.formEventName.getEditText().getText());
            this.postParams.put("promoter", this.loggedInUser.getCharacterID());
            this.postParams.put("epoch", EPOCH / 1000);
            this.postParams.put("description", this.formEventDescription.getEditText().getText() + "");
            if (this.formEventMaxParticipantsType.getCheckedRadioButtonId() == R.id.participationType1)
                this.postParams.put("maxparticipants", this.formEventMaxParticipants.getEditText().getText() + "");
            else
                this.postParams.put("maxparticipants", "-1");
            if (this.formEventPromoterParticipant.isChecked())
                this.postParams.put("promoterParticipant", "1");
            else
                this.postParams.put("promoterParticipant", "0");
            this.postParams.put("apiKey", loggedInUser.getAPIKey());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, RequestURLs.MODESTIE_EVENTS_UPDATE, this.postParams,
                    response ->
                    {
                        this.pending = false;
                        eventUpdateFinal(true, null);
                    },
                    error ->
                    {
                        //Log.e(TAG, error.getLocalizedMessage());
                        Log.e(TAG, "EVENT UPDATE FAILED");
                        eventUpdateFinal(false, "event");
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
            eventUpdateFinal(false, "event");
        }
    }

    private void eventUpdateFinal(boolean success, @Nullable String errorValue)
    {
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
            pending = false;
        }
    }
}
