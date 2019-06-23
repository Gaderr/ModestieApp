package com.modestie.modestieapp.model.event;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventPrice
{
    private static final String TAG = "XIVAPI.EVN.EVNTPRCE";

    private int eventID;
    private int priceType;
    private int priceRewardDegree;
    private int itemID;
    private String itemName;
    private String itemIconURL;
    private int gilsAmount;

    public EventPrice(int eventID, int priceType, int priceRewardDegree, int itemID, String itemName, String iconURL, int gilsAmount)
    {
        this.eventID = eventID;
        this.priceType = priceType;
        this.priceRewardDegree = priceRewardDegree;
        this.itemIconURL = iconURL;
        if(this.priceType == 0)
        {
            this.itemID = -1;
            this.itemName = "";
            this.gilsAmount = gilsAmount;
        }
        else
        {
            this.itemID = itemID;
            this.itemName = itemName;
            this.gilsAmount = 0;
        }
    }

    public EventPrice(JSONObject obj)
    {
        try
        {
            this.eventID = Integer.parseInt(obj.getString("eventID"));
            this.priceType = Integer.parseInt(obj.getString("priceType"));
            this.priceRewardDegree = Integer.parseInt(obj.getString("priceRewardDegree"));
            if(this.priceType == 0)
            {
                this.itemID = -1;
                this.itemName = "";
                this.gilsAmount = Integer.parseInt(obj.getString("gilsAmount"));
            }
            else
            {
                this.itemID = Integer.parseInt(obj.getString("itemID"));
                this.itemName = obj.getString("itemName");
                this.gilsAmount = 0;
            }
            this.itemIconURL = obj.getString("itemIconURL");
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public String priceToString()
    {
        if(this.priceType == 0)
            return this.gilsAmount + "";
        else
            return this.itemName;
    }

    public int getEventID()
    {
        return eventID;
    }

    public void setEventID(int eventID)
    {
        this.eventID = eventID;
    }

    public int getPriceType()
    {
        return priceType;
    }

    public void setPriceType(int priceType)
    {
        this.priceType = priceType;
    }

    public int getPriceRewardDegree()
    {
        return priceRewardDegree;
    }

    public void setPriceRewardDegree(int priceRewardDegree)
    {
        this.priceRewardDegree = priceRewardDegree;
    }

    public int getItemID()
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

    public int getGilsAmount()
    {
        return gilsAmount;
    }

    public void setGilsAmount(int gilsAmount)
    {
        this.gilsAmount = gilsAmount;
    }
}
