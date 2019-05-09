package com.modestie.modestieapp.model.freeCompany;

import android.database.Cursor;
import android.util.Log;

import com.modestie.modestieapp.sql.FreeCompanyReaderContract;

import org.json.JSONObject;

public class FreeCompanySeekedRole
{
    private String name;
    private String iconURL;
    private boolean status;

    private static final String TAG = "XIVAPI.FC.SEEKEDROLE";

    public FreeCompanySeekedRole(JSONObject obj)
    {
        try
        {
            this.name = obj.getString("Name");
            this.iconURL = obj.getString("Icon");
            this.status = obj.getBoolean("Status");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public FreeCompanySeekedRole(Cursor cursor)
    {
        this.name = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_NAME));
        this.iconURL = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_ICON));
        this.status = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_STATUS)) == 1;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIconURL()
    {
        return iconURL;
    }

    public void setIconURL(String iconURL)
    {
        this.iconURL = iconURL;
    }

    public boolean isStatus()
    {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }
}
