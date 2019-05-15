package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class Job
{
    private static final String TAG = "XIVAPI.CHA.CLSSJB.JOB";

    private String name;
    private String abbreviation;
    private int ID;
    private String iconURL;
    private String detailsURL;

    public Job(JSONObject obj)
    {
        try
        {
            this.name = obj.getString("Name");
            this.abbreviation = obj.getString("Abbreviation");
            this.ID = obj.getInt("ID");
            this.iconURL = obj.getString("Icon");
            this.detailsURL = obj.getString("Icon");
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

    public String getAbbreviation()
    {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation)
    {
        this.abbreviation = abbreviation;
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

    public String getDetailsURL()
    {
        return detailsURL;
    }

    public void setDetailsURL(String detailsURL)
    {
        this.detailsURL = detailsURL;
    }
}
