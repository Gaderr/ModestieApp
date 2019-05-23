package com.modestie.modestieapp.model.event;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

public class Event
{
    private String name;
    private int promoterID;
    private Long eventEpochTime;
    private String imageURL;
    private String description;
    private int maxParticipants;
    private ArrayList<Integer> participantsIDs;

    public static Comparator<Event> EventDateComparator = (e1, e2) -> Long.compare(e1.getEventEpochTime(), e2.getEventEpochTime());

    public static final String TAG = "MODL.EVENT";

    public Event(String name, int promoterID, Long eventEpochTime, String imageURL, String description, int maxParticipants, ArrayList<Integer> participantsIDs)
    {
        this.name = name;
        this.promoterID = promoterID;
        this.eventEpochTime = eventEpochTime;
        this.imageURL = imageURL;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.participantsIDs = participantsIDs;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPromoterID()
    {
        return promoterID;
    }

    public void setPromoterID(int promoterID)
    {
        this.promoterID = promoterID;
    }

    public Long getEventEpochTime()
    {
        return eventEpochTime;
    }

    public void setEventEpochTime(Long eventEpochTime)
    {
        this.eventEpochTime = eventEpochTime;
    }

    public String getImageURL()
    {
        return imageURL;
    }

    public void setImageURL(String imageURL)
    {
        this.imageURL = imageURL;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getMaxParticipants()
    {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants)
    {
        this.maxParticipants = maxParticipants;
    }

    public ArrayList<Integer> getParticipantsIDs()
    {
        return participantsIDs;
    }

    public void setParticipantsIDs(ArrayList<Integer> participantsIDs)
    {
        this.participantsIDs = participantsIDs;
    }

    public void removeParticipant(Integer ID)
    {
        for(int i = 0; i < this.participantsIDs.size(); i++)
        {
            if(this.participantsIDs.get(i).equals(ID))
            {
                this.participantsIDs.remove(i);
                Log.e(TAG, "Participant found and removed");
                break;
            }
        }
    }
}
