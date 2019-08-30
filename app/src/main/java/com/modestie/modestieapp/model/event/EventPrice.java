package com.modestie.modestieapp.model.event;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class EventPrice implements Comparable<EventPrice>
{
    private static final String TAG = "XIVAPI.EVN.EVNTPRCE";

    private String eventID;
    private long priceRewardDegree;
    private long itemID;
    private String itemName;
    private String itemIconURL;
    private long amount;

    public EventPrice(String eventID, long priceRewardDegree, long itemID, String itemName, String iconURL, long amount)
    {
        this.eventID = eventID;
        this.priceRewardDegree = priceRewardDegree;
        this.itemIconURL = iconURL;
        this.itemID = itemID;
        this.itemName = itemName;
        this.amount = amount;
    }

    public EventPrice(JSONObject obj)
    {
        try
        {
            this.eventID = obj.getString("eventID");
            this.priceRewardDegree = Integer.parseInt(obj.getString("priceRewardDegree"));
            this.itemID = Integer.parseInt(obj.getString("itemID"));
            this.itemName = obj.getString("itemName");
            this.itemIconURL = obj.getString("itemIconURL");
            this.amount = obj.getInt("amount");
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public EventPrice(Bundle attrs)
    {
        this.eventID = attrs.getString("eventID");
        this.priceRewardDegree = attrs.getInt("priceRewardDegree");
        this.itemID = attrs.getInt("itemID");
        this.itemName = attrs.getString("itemName");
        this.itemIconURL = attrs.getString("itemIconURL");
        this.amount = attrs.getInt("amount");
    }

    public Bundle toBundle()
    {
        Bundle attrs = new Bundle();
        attrs.putString("eventID", this.eventID);
        attrs.putLong("priceRewardDegree", this.priceRewardDegree);
        attrs.putLong("itemID", this.itemID);
        attrs.putString("itemName", this.itemName);
        attrs.putString("itemIconURL", this.itemIconURL);
        attrs.putLong("amount", this.amount);
        return attrs;
    }

    public String priceToString()
    {
        return this.itemName + " x" + this.amount;
    }

    public String getEventID()
    {
        return eventID;
    }

    public void setEventID(String eventID)
    {
        this.eventID = eventID;
    }

    public long getPriceRewardDegree()
    {
        return priceRewardDegree;
    }

    public void setPriceRewardDegree(int priceRewardDegree)
    {
        this.priceRewardDegree = priceRewardDegree;
    }

    public long getItemID()
    {
        return itemID;
    }

    public void setItemID(int itemID)
    {
        this.itemID = itemID;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemIconURL()
    {
        return itemIconURL;
    }

    public void setItemIconURL(String itemIconURL)
    {
        this.itemIconURL = itemIconURL;
    }

    public long getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    @Override
    public int compareTo(EventPrice e)
    {
        return Integer.parseInt(this.getPriceRewardDegree() + "") - Integer.parseInt(e.getPriceRewardDegree() + "");
    }
}
