package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class Class
{
    private static final String TAG = "XIVAPI.CHA.CLSSJB.CLASS";

    private String name;
    private String abbreviation;
    private int ID;
    private String iconURL;
    private String detailsURL;

    private String categoryName;
    private int categoryID;

    public Class(JSONObject obj)
    {
        try
        {
            this.name = obj.getString("Name");
            this.abbreviation = obj.getString("Abbreviation");
            this.ID = obj.getInt("ID");
            this.iconURL = obj.getString("Icon");
            this.detailsURL = obj.getString("Icon");

            JSONObject classJobCategory = obj.getJSONObject("ClassJobCategory");

            this.categoryName = classJobCategory.getString("Name");
            this.categoryID = classJobCategory.getInt("ID");
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

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public int getCategoryID()
    {
        return categoryID;
    }

    public void setCategoryID(int categoryID)
    {
        this.categoryID = categoryID;
    }
}
