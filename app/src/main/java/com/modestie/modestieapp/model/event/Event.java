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
    private int ID;
    private String name;
    private Long promoterID;
    private Long eventEpochTime;
    private String imageURL;
    private String description;
    private int maxParticipants;
    private boolean promoterIsParticipant;
    private ArrayList<Long> participantsIDs;
    private ArrayList<EventPrice> prices;

    public static Comparator<Event> EventDateComparator = (e1, e2) -> Long.compare(e1.getEventEpochTime(), e2.getEventEpochTime());

    public static Comparator<EventPrice> PriceDegreeComparator = (e1, e2) -> Integer.compare(e1.getPriceRewardDegree(), e2.getPriceRewardDegree());

    public static final String TAG = "MODL.EVENT";

    public Event()
    {
        this.name = "";
        this.promoterID = 0L;
        this.eventEpochTime = 0L;
        this.imageURL = "";
        this.description = "";
        this.maxParticipants = 0;
        this.promoterIsParticipant = false;
        this.participantsIDs = new ArrayList<>();
        this.prices = new ArrayList<>();
    }

    public Event(String name, Long promoterID, Long eventEpochTime, String imageURL, String description, int maxParticipants, boolean promoterIsParticipant, ArrayList<Long> participantsIDs, ArrayList<EventPrice> prices)
    {
        this.name = name;
        this.promoterID = promoterID;
        this.eventEpochTime = eventEpochTime;
        this.imageURL = imageURL;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.promoterIsParticipant = promoterIsParticipant;
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

            this.ID = event.getInt("id");
            this.name = event.getString("eventName");
            this.promoterID = Long.parseLong(event.getString("promoterID"));
            this.eventEpochTime = Long.parseLong(event.getString("eventEPOCH"));
            this.imageURL = event.getString("image_url");
            this.description = event.getString("description");
            this.maxParticipants = Integer.parseInt(event.getString("maxParticipants"));
            this.promoterIsParticipant = Integer.parseInt(event.getString("promoterIsParticipant")) == 1;

            this.participantsIDs = new ArrayList<>();
            for(int i = 0; i < participants.length(); i++)
            {
                this.participantsIDs.add(Long.parseLong(participants.getJSONObject(i).getString("participantID")));
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
            s.append(price.priceToString()).append("\n");
        }
        return s.toString();
    }

    /*
        GETTERS & SETTERS
     */

    public int getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getPromoterID()
    {
        return promoterID;
    }

    public void setPromoterID(long promoterID)
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

    public boolean isPromoterParticipant()
    {
        return promoterIsParticipant;
    }

    public void setPromoterIsParticipant(boolean promoterIsParticipant)
    {
        this.promoterIsParticipant = promoterIsParticipant;
    }

    public ArrayList<Long> getParticipantsIDs()
    {
        return participantsIDs;
    }

    public void setParticipantsIDs(ArrayList<Long> participantsIDs)
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

    public void removeParticipant(long ID)
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

    @NotNull
    @Override
    public String toString()
    {
        return "Event{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", promoterID=" + promoterID +
                ", eventEpochTime=" + eventEpochTime +
                ", imageURL='" + imageURL + '\'' +
                ", description='" + description + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", promoterIsParticipant=" + promoterIsParticipant +
                ", participants=" + participantsIDs.size() +
                ", prices=" + prices.size() +
                "}";
    }
}
