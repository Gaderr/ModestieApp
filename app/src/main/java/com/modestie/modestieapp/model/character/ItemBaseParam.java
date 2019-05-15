package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class ItemBaseParam
{
    private static final String TAG = "XIVAPI.ITEM.BASEPARAM";

    private String name;
    private int value;

    public ItemBaseParam(JSONObject obj, int value)
    {
        try
        {
            this.name = obj.getString("Name");
            this.value = value;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }
}
