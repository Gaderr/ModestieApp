package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class CharacterAttribute
{
    private static final String TAG = "XIVAPI.CHA.ATTR";

    private int ID;
    private String name;
    private int value;

    public CharacterAttribute(JSONObject obj)
    {
        try
        {
            JSONObject attr = obj.getJSONObject("Attribute");
            this.name = attr.getString("Name");
            this.ID = attr.getInt("ID");

            this.value = obj.getInt("Value");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public int getID()
    {
        return ID;
    }

    public void setID(int ID)
    {
        this.ID = ID;
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
