package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class CharacterGrandCompany
{
    private static final String TAG = "XIVAPI.CHA.GC";

    private String gcName;
    private int gcID;
    private String gcApiURL;

    private String rankName;
    private int rankID;
    private String rankIconURL;
    private String rankApiURL;

    public CharacterGrandCompany(JSONObject obj)
    {
        try
        {
            JSONObject gc = obj.getJSONObject("Company");
            JSONObject rank = obj.getJSONObject("Rank");

            this.gcName = gc.getString("Name");
            this.gcID = gc.getInt("ID");
            this.gcApiURL = gc.getString("Url");

            this.rankName = rank.getString("Name");
            this.rankID = rank.getInt("ID");
            this.rankIconURL = rank.getString("Icon");
            this.rankApiURL = rank.getString("Url");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }
}
