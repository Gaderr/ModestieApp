package com.modestie.modestieapp.model.event;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    private ArrayList<EventPrice> prices;

    public static Comparator<Event> EventDateComparator = (e1, e2) -> Long.compare(e1.getEventEpochTime(), e2.getEventEpochTime());

    public static Comparator<EventPrice> PriceDegreeComparator = (e1, e2) -> Integer.compare(e1.getPriceRewardDegree(), e2.getPriceRewardDegree());

    public static final String TAG = "MODL.EVENT";

    public Event()
    {
        this.name = "";
        this.promoterID = 0;
        this.eventEpochTime = 0L;
        this.imageURL = "";
        this.description = "";
        this.maxParticipants = 0;
        this.participantsIDs = new ArrayList<>();
        this.prices = new ArrayList<>();
    }

    public Event(String name, int promoterID, Long eventEpochTime, String imageURL, String description, int maxParticipants, ArrayList<Integer> participantsIDs, ArrayList<EventPrice> prices)
    {
        this.name = name;
        this.promoterID = promoterID;
        this.eventEpochTime = eventEpochTime;
        this.imageURL = imageURL;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.participantsIDs = participantsIDs;
        this.prices = prices;
    }

    public Event(@NotNull JSONObject obj)
    {
        try
        {
            JSONObject event = obj.getJSONObject("Event");
            JSONArray participants = obj.getJSONArray("Participants");
            JSONArray prices = obj.getJSONArray("Prices");

            this.name = event.getString("eventName");
            this.promoterID = Integer.parseInt(obj.getString("promoterID"));
            this.eventEpochTime = Long.parseLong(obj.getString("eventEPOCH"));
            this.imageURL = obj.getString("image_url");
            this.description = obj.getString("description");
            this.maxParticipants = Integer.parseInt("maxParticipants");

            this.participantsIDs = new ArrayList<>();
            for(int i = 0; i < participants.length(); i++)
            {
                this.participantsIDs.add(Integer.parseInt(participants.getJSONObject(i).getString("participantID")));
            }

            this.prices = new ArrayList<>();
            for(int i = 0; i < prices.length(); i++)
            {
                this.prices.add(new EventPrice(prices.getJSONObject(i)));
            }
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    /*
        METHODS
     */

    /**
     * Adds a blank price into this event with the given degree reward. If a reward with the given
     * degree already exists, it returns a false result and true if not.
     * @param degree Price degree
     * @param priceType Price type, 0 = gils, other = item;
     * @return Add result
     */
    public Boolean addPrice(int degree, int priceType)
    {
        for(int i = 0; i < this.prices.size(); i++)
        {
            if(this.prices.get(i).getPriceRewardDegree() == degree)
            {
                return false;
            }
        }

        this.prices.add(new EventPrice(0, 0, 1, "Gil", "https://xivapi.com/i/065000/065002.png", 100000));
        Collections.sort(this.prices, PriceDegreeComparator);

        return true;
    }

    public Boolean removePrice(int degree)
    {
        boolean removed = false;
        for(int i = 0; i < this.prices.size(); i++)
        {
            if(this.prices.get(i).getPriceRewardDegree() == degree)
            {
                this.prices.remove(i);
                removed = true;
                break;
            }
        }

        if(removed) Collections.sort(this.prices, PriceDegreeComparator);

        return removed;
    }

    public void reattributeDegrees()
    {
        for(int i = 0; i < this.prices.size(); i++)
        {
            this.prices.get(i).setPriceRewardDegree(i + 1);
        }
    }

    public String pricesToString()
    {
        StringBuilder s = new StringBuilder();
        s.append("Prices : \n");
        for (EventPrice price: prices)
        {
            s.append(price.priceToString() + "\n");
        }
        return s.toString();
    }

    /*
        GETTERS & SETTERS
     */

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

    public ArrayList<EventPrice> getPrices()
    {
        return prices;
    }

    public void setPrices(ArrayList<EventPrice> prices)
    {
        this.prices = prices;
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
