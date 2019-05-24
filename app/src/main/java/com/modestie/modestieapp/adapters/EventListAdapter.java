package com.modestie.modestieapp.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimatedVectorDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListCardViewHolder>
{
    public Context context;

    private ArrayList<Event> events;
    private Map<Integer, FreeCompanyMember> members;

    public static final String TAG = "ACTVT.EVNTLSTADPTR";

    // This class provide a reference to the views for each data item
    public static class EventListCardViewHolder extends RecyclerView.ViewHolder
    {
        // each data item is just a string in this case
        View v;
        TextView title;
        TextView promoter;
        ImageView promoterAvatar;
        ImageView image;
        TextView date;
        TextView description;
        TextView participantCount;
        ImageView expand;
        Button action;
        ImageView participationCheck;
        TextView participationText;

        AnimatorSet animatorSetExpandMore;
        AnimatorSet animatorSetExpandLess;
        ObjectAnimator animationExpandMore;
        ObjectAnimator animationExpandLess;
        boolean expanded;

        int userID;
        boolean participation;

        public EventListCardViewHolder(View v)
        {
            super(v);
            this.v = v;
            this.title = v.findViewById(R.id.eventTitle);
            this.promoter = v.findViewById(R.id.characterPromoter);
            this.promoterAvatar = v.findViewById(R.id.promoterAvatar);
            this.image = v.findViewById(R.id.eventImage);
            this.date = v.findViewById(R.id.eventDate);
            this.description = v.findViewById(R.id.eventDescription);
            this.participantCount = v.findViewById(R.id.participantsCount);
            this.participationCheck = v.findViewById(R.id.participationCheck);
            this.participationText = v.findViewById(R.id.participationText);

            this.expand = v.findViewById(R.id.expand);
            this.action = v.findViewById(R.id.actionBtn);
            this.expanded = false;

            this.animatorSetExpandMore = new AnimatorSet();
            this.animatorSetExpandLess = new AnimatorSet();
            this.animationExpandLess = ObjectAnimator.ofFloat(this.expand, "rotation", 180f).setDuration(100);
            this.animationExpandMore = ObjectAnimator.ofFloat(this.expand, "rotation", -180f).setDuration(100);
            this.animatorSetExpandMore.play(animationExpandMore);
            this.animatorSetExpandLess.play(animationExpandLess);

            this.userID = 11148489;
            this.participation = false;
        }
    }

    @SuppressLint("UseSparseArrays")
    public EventListAdapter(ArrayList<Event> events, SQLiteDatabase database, Context context)
    {
        this.events = events;
        this.context = context;

        Cursor cursor = database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME, null);
        this.members = new HashMap<>();
        if(cursor.moveToFirst())
        {
            do
            {
                FreeCompanyMember member = new FreeCompanyMember(cursor);
                this.members.put(member.getID(), member);
            }
            while(cursor.moveToNext());
        }
        else
        {
            Log.e(TAG, "No entries found in " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME + " table");
        }

        cursor.close();
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public EventListAdapter.EventListCardViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);

        return new EventListCardViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull EventListCardViewHolder holder, int position)
    {
        Event event = this.events.get(position);
        FreeCompanyMember member = this.members.get(event.getPromoterID());
        assert member != null;

        //Get participation status
        holder.participation = event.getParticipantsIDs().contains(holder.userID);

        //Initialize views

        //Header (avatar + title + promoter)
        holder.title.setText(event.getName());
        holder.promoter.setText(String.format(Locale.FRANCE, "Organisé par %s", member.getName()));
        Picasso.get()
                .load(member.getAvatarURL())
                .into(holder.promoterAvatar);

        //Event image
        if(event.getImageURL() != null)
            Picasso.get()
                    .load(event.getImageURL())
                    .into(holder.image);

        //Expand More/Less icon
        AnimatedVectorDrawable animatedExpandMore = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_more_animatable, null);
        holder.expand.setImageDrawable(animatedExpandMore);
        holder.expand.setOnClickListener(v -> expandOrCollapseDescription(holder));

        //Date
        Date eventTime = new Date(event.getEventEpochTime() * 1000);
        SimpleDateFormat eventDateFormat = new SimpleDateFormat("d MMMM YYYY", Locale.FRANCE);
        SimpleDateFormat eventTimeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        holder.date.setText(String.format(Locale.FRANCE, "Le %s à %s", eventDateFormat.format(eventTime), eventTimeFormat.format(eventTime)));

        //Description
        if(event.getDescription() == null || event.getDescription().isEmpty())
            holder.description.setText(R.string.event_description_null);
        else
            holder.description.setText(event.getDescription());
        holder.description.setOnClickListener(v -> expandOrCollapseDescription(holder));

        //Participation text feedback
        if(event.getPromoterID() == holder.userID)
        {
            holder.action.setVisibility(View.INVISIBLE);
            holder.participationText.setText(R.string.event_self_promoter_feedback);

            ConstraintSet set = new ConstraintSet();
            ConstraintLayout parentLayout = holder.v.findViewById(R.id.eventCardContent);
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
            set.clone(parentLayout);
            set.connect(R.id.participationCheck, ConstraintSet.START, R.id.eventCardContent, ConstraintSet.START, margin);
            set.applyTo(parentLayout);
        }
        else
        {
            holder.participationText.setText(R.string.event_pariticpation_feedback);
        }

        //Participants count
        updateParticipantsViews(holder, event);

        //Action button
        updateParticipationButton(holder);
        holder.action.setOnClickListener(v ->
            {
                if(!holder.participation) //Participation request -> check if a place is available
                {
                    if(event.getMaxParticipants() == -1)
                    {
                        holder.participation = true;
                        updateParticipation(holder, event);
                    }
                    else if(event.getParticipantsIDs().size() < event.getMaxParticipants())
                    {
                        holder.participation = true;
                        updateParticipation(holder, event);
                    }
                    else
                    {
                        Toast.makeText(context, "Désolé, c'est complet !", Toast.LENGTH_SHORT).show();
                    }
                }
                else //Cancel participation
                {
                    //Promoters can't cancel participation to their own events
                    if(event.getPromoterID() != holder.userID)
                    {
                        holder.participation = false;
                        updateParticipation(holder, event);
                    }
                }

                updateParticipationButton(holder);
                updateParticipantsViews(holder, event);
            });
    }

    private void updateParticipation(EventListCardViewHolder holder, Event event)
    {
        if(holder.participation)
            event.getParticipantsIDs().add(holder.userID);
        else
            event.removeParticipant(holder.userID);
    }

    private void updateParticipantsViews(EventListCardViewHolder holder, Event event)
    {
        if(event.getMaxParticipants() == -1)
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/∞", event.getParticipantsIDs().size()));
        else
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/%d", event.getParticipantsIDs().size(), event.getMaxParticipants()));
    }

    private void updateParticipationButton(EventListCardViewHolder holder)
    {
        if (holder.participation)
        {
            //holder.action.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorEventCancelParticipation)));
            //holder.action.setTextColor(ContextCompat.getColor(context, R.color.colorOnBackground));
            holder.action.setText(R.string.button_cancel_participation);
            holder.participationCheck.setVisibility(View.VISIBLE);
            holder.participationText.setVisibility(View.VISIBLE);
        }
        else
        {
            //holder.action.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorOnBackground)));
            //holder.action.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.action.setText(R.string.button_participate);
            holder.participationCheck.setVisibility(View.INVISIBLE);
            holder.participationText.setVisibility(View.INVISIBLE);
        }
    }

    private void expandOrCollapseDescription(EventListCardViewHolder holder)
    {
        if(holder.expanded)
        {
            AnimatedVectorDrawable animatedExpandLess = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_less_animatable, null);
            holder.expand.setImageDrawable(animatedExpandLess);
            animatedExpandLess.start();
            holder.description.setMaxLines(2);
        }
        else
        {
            AnimatedVectorDrawable animatedExpandMore = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_more_animatable, null);
            holder.expand.setImageDrawable(animatedExpandMore);
            animatedExpandMore.start();
            holder.description.setMaxLines(Integer.MAX_VALUE);
        }

        holder.expanded = !holder.expanded;
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return events.size();
    }
}
