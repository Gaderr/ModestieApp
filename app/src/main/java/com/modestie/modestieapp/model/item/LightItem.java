package com.modestie.modestieapp.model.item;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LightItem
{
    public int itemID;
    public String iconURL;
    public String itemName;

    public static final String TAG = "XIVAPI.LIGHTITEM";

    public LightItem(int itemID, String iconURL, String itemName)
    {
        this.itemID = itemID;
        this.iconURL = iconURL;
        this.itemName = itemName;
    }

    public LightItem(JSONObject obj)
    {
        try
        {
            this.itemID = obj.getInt("ID");
            this.iconURL = "https://xivapi.com" + obj.getString("Icon");
            this.itemName = obj.getString("Name");
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
