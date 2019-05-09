package com.modestie.modestieapp.model.freeCompany;

import android.database.Cursor;
import android.util.Log;

import com.modestie.modestieapp.sql.FreeCompanyReaderContract;

import org.json.JSONObject;

public class FreeCompanyMember
{
    private int ID;
    private String name;
    private String avatarURL;
    private String rank;
    private String rankIconURL;
    private int feastMatches;

    private static final String TAG = "XIVAPI.FC.FCMEMBER";

    public FreeCompanyMember(JSONObject obj)
    {
        try
        {
            this.ID             = obj.getInt("ID");
            this.name           = obj.getString("Name");
            this.avatarURL      = obj.getString("Avatar");
            this.rank           = obj.getString("Rank");
            this.rankIconURL    = obj.getString("RankIcon");
            this.feastMatches   = obj.getInt("FeastMatches");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public FreeCompanyMember(Cursor cursor)
    {
        this.name = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_NAME));
        this.avatarURL = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_AVATAR));
        this.rank = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANK));
        this.rankIconURL = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANKICON));
        this.feastMatches = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_FEASTMATCHES));
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

    public String getAvatarURL()
    {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL)
    {
        this.avatarURL = avatarURL;
    }

    public String getRank()
    {
        return rank;
    }

    public void setRank(String rank)
    {
        this.rank = rank;
    }

    public String getRankIconURL()
    {
        return rankIconURL;
    }

    public void setRankIconURL(String rankIconURL)
    {
        this.rankIconURL = rankIconURL;
    }

    public int getFeastMatches()
    {
        return feastMatches;
    }

    public void setFeastMatches(int feastMatches)
    {
        this.feastMatches = feastMatches;
    }
}
