package com.modestie.modestieapp.activities.events.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.events.form.EventModificationActivity;
import com.modestie.modestieapp.adapters.StaticEventPriceAdapter;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.android.volley.Request.Method.POST;

public class EventDetailsDialogFragment extends DialogFragment
{
    private static final String TAG = "DFRAG.EVENTDETAILS";

    private View rootview;
    private Toolbar toolbar;

    private ImageView participationCheck;
    private TextView participationText;
    private TextView attributeParticipations;
    private Button participationButton;
    private Button withdrawButton;
    private ProgressBar progressBar;

    private boolean userLoggedIn;
    private boolean userIsPromoter;
    private boolean userIsParticipant;
    private boolean pending;

    private Event event;
    private Object promoter;
    private LightCharacter user;

    private OnParticipationChanged callback;

    private RequestHelper requestHelper;

    public EventDetailsDialogFragment() { }

    public static EventDetailsDialogFragment display(FragmentManager fragmentManager)
    {
        EventDetailsDialogFragment dialog = new EventDetailsDialogFragment();
        dialog.show(fragmentManager, TAG);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_ModestieTheme_FullScreenDialog);
        this.requestHelper = new RequestHelper(getContext());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_event_details, container, false);

        //Toolbar and action button click listener
        this.toolbar = rootView.findViewById(R.id.toolbar);

        //Get selected event
        this.event = Hawk.get("SelectedEvent");
        this.promoter = Hawk.get("SelectedEventPromoter");
        this.userLoggedIn = Hawk.contains("UserCharacter") && Hawk.contains("UserCredentials");
        this.userIsPromoter = this.userLoggedIn && this.event.getPromoterID() == ((LightCharacter) Hawk.get("UserCharacter")).getID();
        if (this.userLoggedIn)
        {
            this.userIsParticipant = this.event.getParticipantsIDs().contains(((LightCharacter) Hawk.get("UserCharacter")).getID());
            this.user = Hawk.get("UserCharacter");
        }
        else
            this.userIsParticipant = false;

        this.pending = false;
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Toolbar setup
        this.toolbar.setNavigationOnClickListener(
                v ->
                {
                    if (!this.pending) dismiss();
                });

        if (this.userIsPromoter)
        {
            Button editEventAction = this.toolbar.findViewById(R.id.eventDetailsEditAction);
            editEventAction.setVisibility(View.VISIBLE);
            editEventAction.setOnClickListener(v -> startActivity(new Intent(getContext(), EventModificationActivity.class)));
        }
        this.rootview = view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        this.participationCheck = this.rootview.findViewById(R.id.participationCheck);
        this.participationText = this.rootview.findViewById(R.id.participationText);
        this.progressBar = this.rootview.findViewById(R.id.progressBar);
        this.attributeParticipations = this.rootview.findViewById(R.id.eventAttributeParticipationsTextView);
        this.participationButton = this.rootview.findViewById(R.id.participationButton);
        this.withdrawButton = this.rootview.findViewById(R.id.withdrawButton);

        ImageView illustration = this.rootview.findViewById(R.id.illustration);
        ImageView promoterAvatar = this.rootview.findViewById(R.id.promoterAvatar);
        TextView eventTitle = this.rootview.findViewById(R.id.eventTitle);
        TextView eventDate = this.rootview.findViewById(R.id.eventDate);
        TextView attributePrices = this.rootview.findViewById(R.id.eventAttributeRewardsTextView);
        TextView promoterTextView = this.rootview.findViewById(R.id.promoterTextView);
        TextView description = this.rootview.findViewById(R.id.eventDescription);

        RecyclerView pricesList = this.rootview.findViewById(R.id.pricesList);


        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        {
            this.participationCheck.setColorFilter(getContext().getColor(R.color.colorValidateLight));
            this.participationText.setTextColor(getContext().getColor(R.color.colorValidateLight));
        }

        if (!this.event.getImageURL().equals(""))
            Picasso.get()
                    .load(this.event.getImageURL())
                    .fit()
                    .centerCrop()
                    .into(illustration);

        if (this.promoter instanceof FreeCompanyMember)
            Picasso.get()
                    .load(((FreeCompanyMember) this.promoter).getAvatarURL())
                    .placeholder(R.color.color_surface_dimmed)
                    .into(promoterAvatar);

        if (this.promoter instanceof LightCharacter)
            Picasso.get()
                    .load(((LightCharacter) this.promoter).getAvatarURL())
                    .placeholder(R.color.color_surface_dimmed)
                    .into(promoterAvatar);

        //Event title
        eventTitle.setText(this.event.getName());

        //Event date
        SimpleDateFormat eventDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
        SimpleDateFormat eventTimeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        eventDate.setText(String.format(Locale.FRANCE, "Le %s à %s", eventDateFormat.format(this.event.getEventDate()), eventTimeFormat.format(this.event.getEventDate())));

        //Event attributes summary details
        //  Participations count
        setupParticipationsCount();

        //  Prices count
        if (this.event.getPrices().size() == 0)
            attributePrices.setText(getString(R.string.event_attribute_prices_no_price));
        else if (this.event.getPrices().size() == 1)
            attributePrices.setText(getString(R.string.event_attribute_prices));
        else
            attributePrices.setText(String.format("%d " + getString(R.string.event_attribute_prices_plural), this.event.getPrices().size()));

        //Promoter details
        if (this.userIsPromoter)
        {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                promoterTextView.setTextColor(getContext().getColor(R.color.colorValidateLight));
            else
                promoterTextView.setTextColor(getContext().getColor(R.color.colorValidate));

            if (this.event.isPromoterParticipant())
                promoterTextView.setText(getString(R.string.event_connected_promoter_is_participating_details));
            else
                promoterTextView.setText(getString(R.string.event_connected_promoter_details_text_view));
        }
        else
        {
            String promoterName = "";
            if (this.promoter instanceof FreeCompanyMember)
                promoterName = ((FreeCompanyMember) this.promoter).getName();
            if (this.promoter instanceof LightCharacter)
                promoterName = ((LightCharacter) this.promoter).getName();

            if (this.event.isPromoterParticipant())
                promoterTextView.setText(String.format("%s " + getString(R.string.event_promoter_is_participating_details), promoterName));
            else
                promoterTextView.setText(String.format("%s " + getString(R.string.event_promoter_details_text_view), promoterName));
        }

        //Description
        description.setText(this.event.getDescription());

        //Prices list
        ArrayList<EventPrice> prices = this.event.getPrices();
        Collections.sort(prices);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        pricesList.setLayoutManager(layoutManager);
        RecyclerView.Adapter<StaticEventPriceAdapter.ViewHolder> adapter = new StaticEventPriceAdapter(prices);
        pricesList.setAdapter(adapter);

        //Participation buttons and feeback elements
        setupParticipationButtons();

        // Listeners
        this.participationButton.setOnClickListener(
                v ->
                {
                    this.participationButton.setEnabled(false);
                    this.progressBar.setVisibility(View.VISIBLE);
                    LoggedInUser user = Hawk.get("LoggedInUser");
                    this.pending = true;
                    try
                    {
                        JSONObject postparams = new JSONObject();
                        postparams.put("eventID", this.event.getID());
                        postparams.put("characterID", this.user.getID());
                        postparams.put("apiKey", user.getAPIKey());
                        this.requestHelper.addToRequestQueue(new JsonObjectRequest(
                                POST, RequestURLs.MODESTIE_PARTICIPANTS_ADD, postparams,
                                response ->
                                {
                                    try
                                    {
                                        if (!response.getBoolean("result"))
                                        {
                                            if (response.getString("status").equals("Event full"))
                                                Snackbar.make(this.rootview, R.string.snackbar_message_event_full, Snackbar.LENGTH_LONG).show();
                                            else if (response.getString("status").equals("This character is already a participant"))
                                                Snackbar.make(this.rootview, R.string.snackbar_message_event_already_participant, Snackbar.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            this.event.getParticipantsIDs().add(this.user.getID());
                                            this.userIsParticipant = true;
                                            this.callback.participationChanged();
                                        }
                                        setupParticipationButtons();
                                        setupParticipationsCount();
                                        this.progressBar.setVisibility(View.INVISIBLE);
                                        this.pending = false;
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                                },
                                error ->
                                {
                                    Toast.makeText(getContext(), "Une erreur est survenue, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                    this.participationButton.setEnabled(true);
                                    this.progressBar.setVisibility(View.INVISIBLE);
                                    this.pending = false;
                                }
                        )
                        {
                            @Override
                            public Map<String, String> getHeaders()
                            {
                                Map<String, String> params = new HashMap<>();
                                params.put("Authorization", "Bearer " + user.getToken());
                                return params;
                            }
                        });
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                });

        this.withdrawButton.setOnClickListener(
                v ->
                {
                    this.withdrawButton.setEnabled(false);
                    this.progressBar.setVisibility(View.VISIBLE);
                    this.participationCheck.setVisibility(View.INVISIBLE);
                    this.participationText.setVisibility(View.INVISIBLE);
                    LoggedInUser user = Hawk.get("LoggedInUser");
                    this.pending = true;
                    try
                    {
                        JSONObject postparams = new JSONObject();
                        postparams.put("eventID", this.event.getID());
                        postparams.put("characterID", this.user.getID());
                        postparams.put("apiKey", user.getAPIKey());
                        this.requestHelper.addToRequestQueue(new JsonObjectRequest(
                                POST, RequestURLs.MODESTIE_PARTICIPANTS_REMOVE, postparams,
                                response ->
                                {
                                    this.event.getParticipantsIDs().remove(this.event.getParticipantsIDs().indexOf(this.user.getID()));
                                    this.userIsParticipant = false;
                                    setupParticipationButtons();
                                    setupParticipationsCount();
                                    this.callback.participationChanged();
                                    this.progressBar.setVisibility(View.INVISIBLE);
                                    this.pending = false;
                                },
                                error ->
                                {
                                    Toast.makeText(getContext(), "Erreur", Toast.LENGTH_SHORT).show();
                                    this.withdrawButton.setEnabled(true);
                                    this.progressBar.setVisibility(View.INVISIBLE);
                                    this.pending = false;

                                }
                        )
                        {
                            @Override
                            public Map<String, String> getHeaders()
                            {
                                Map<String, String> params = new HashMap<>();
                                params.put("Authorization", "Bearer " + user.getToken());
                                return params;
                            }
                        });
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                });
    }

    private void setupParticipationsCount()
    {
        int participations = this.event.getParticipantsIDs().size() + (this.event.isPromoterParticipant() ? 1 : 0);
        if (participations == 0) //Limited
            this.attributeParticipations.setText(getString(R.string.event_attribute_participations_no_participation_details));
        else if (this.event.getMaxParticipants() == -1) // >1 participations && unlimited
            if (participations == 1)
                this.attributeParticipations.setText(
                        String.format(
                                "%d " + getString(R.string.event_attribute_participations_details),
                                participations
                        ));
            else
                this.attributeParticipations.setText(
                        String.format(
                                "%d " + getString(R.string.event_attribute_participations_plural_details),
                                participations
                        ));
        else // >1 participations && limited
            this.attributeParticipations.setText(
                    String.format(
                            "%d / %d " + getString(R.string.event_attribute_participations_plural_details),
                            participations,
                            this.event.getMaxParticipants()
                    ));
    }

    private void setupParticipationButtons()
    {
        if (this.userIsPromoter || !this.userLoggedIn)
        {
            this.participationButton.setVisibility(View.GONE);
            this.withdrawButton.setVisibility(View.GONE);
            return;
        }

        //Withdraw
        this.withdrawButton.setEnabled(true);
        if (this.userIsParticipant)
        {
            this.withdrawButton.setVisibility(View.VISIBLE);
            this.participationCheck.setVisibility(View.VISIBLE);
            this.participationText.setVisibility(View.VISIBLE);
            this.participationText.setText(getString(R.string.event_participation_feedback));
        }
        else
        {
            this.withdrawButton.setVisibility(View.INVISIBLE);
            this.participationCheck.setVisibility(View.INVISIBLE);
            this.participationText.setVisibility(View.INVISIBLE);
        }

        //Participation
        // Check if there's still room for participants
        if (this.event.getMaxParticipants() != -1
                && this.event.getParticipantsIDs().size() + (this.event.isPromoterParticipant() ? 1 : 0) >= this.event.getMaxParticipants())
        {
            this.participationText.setVisibility(View.VISIBLE);
            this.participationText.setTextColor(getContext().getColor(R.color.color_error));
            this.participationText.setText(getString(R.string.event_full_feedback));
            this.participationButton.setEnabled(false);
        }
        else
        {
            this.participationButton.setEnabled(true);
            if (this.userIsParticipant || !this.userLoggedIn)
                this.participationButton.setVisibility(View.INVISIBLE);
            else
                this.participationButton.setVisibility(View.VISIBLE);
        }
    }

    void setOnParticipationChanged(OnParticipationChanged callback)
    {
        this.callback = callback;
    }

    /**
     * This interface allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnParticipationChanged
    {
        void participationChanged();
    }
}
