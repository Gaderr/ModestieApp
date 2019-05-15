package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class CharacterGuardianDeity
{
    private static final String TAG = "XIVAPI.CHA.DEITY";

    private String name;
    private int ID;
    private String iconURL;

    public CharacterGuardianDeity(JSONObject obj)
    {
        try
        {
            this.name = obj.getString("Name");
            this.ID = obj.getInt("ID");
            this.iconURL = obj.getString("Icon");
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

    public int getID()
    {
        return ID;
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }

    public String getIconURL()
    {
        return iconURL;
    }

    public void setIconURL(String iconURL)
    {
        this.iconURL = iconURL;
    }
}
