package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class Materia
{
    private static final String TAG = "XIVAPI.ITEM.MATERIA";

    private String name;
    private int ID;
    private String icon;
    //private String apiURL;

    public Materia(JSONObject obj)
    {
        try
        {
            this.name = obj.getString("Name");
            this.ID = obj.getInt("ID");
            this.icon = obj.getString("Icon");
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

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }
}
