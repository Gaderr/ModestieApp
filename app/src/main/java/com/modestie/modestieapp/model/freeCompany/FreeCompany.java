package com.modestie.modestieapp.model.freeCompany;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FreeCompany
{
    /// PROFILE ATTRIBUTES
    private int ID;
    private String name;
    private String slogan;

    private String crestBackground;
    private String crestFrame;
    private String crestLogo;

    private int formed; //Formation date

    private int rank;

    private String grandCompanyName;
    private String grandCompanyRank;
    private int grandCompanyProgress;

    private int monthlyRanking;
    private int weeklyRanking;

    private String estateName;
    private String estatePlot;

    private ArrayList<FreeCompanyMember> members;

    /// ACTIVITY ATTRIBUTES
    private String active;
    private String recruitment;
    private ArrayList<FreeCompanyFocus> focuses;
    private ArrayList<FreeCompanySeekedRole> seekedRoles;

    private static final String TAG = "XIVAPI.FC";

    public FreeCompany(JSONObject obj)
    {
        try
        {
            JSONObject freeCompany = obj.getJSONObject("FreeCompany");
            JSONArray crest = freeCompany.getJSONArray("Crest");
            JSONArray reputation = freeCompany.getJSONArray("Reputation");
            JSONObject ranking = freeCompany.getJSONObject("Ranking");
            JSONObject estate = freeCompany.getJSONObject("Estate");

            this.ID = freeCompany.getInt("ID");
            this.name = freeCompany.getString("Name");
            this.slogan = freeCompany.getString("Slogan");

            this.crestBackground = crest.getString(0);
            this.crestFrame = crest.getString(1);
            this.crestLogo = crest.getString(2);

            this.formed = freeCompany.getInt("Formed");
            this.rank = freeCompany.getInt("Rank");
            this.grandCompanyName = freeCompany.getString("GrandCompany");

            for (int i = 0; i < reputation.length(); i++)
            {
                JSONObject gcData = reputation.getJSONObject(i);
                if(gcData.getString("Name").equals(this.grandCompanyName))
                {
                    this.grandCompanyRank = gcData.getString("Rank");
                    this.grandCompanyProgress = gcData.getInt("Progress");
                }
            }

            this.monthlyRanking = ranking.getInt("Monthly");
            this.weeklyRanking = ranking.getInt("Weekly");

            this.estateName = estate.getString("Name");
            this.estatePlot = estate.getString("Plot");

            JSONObject freeCompanyMembers = obj.getJSONObject("FreeCompanyMembers");
            this.members = new ArrayList<>();
            JSONArray data = freeCompanyMembers.getJSONArray("Data");
            for(int i = 0; i < data.length(); i++)
            {
                this.members.add(new FreeCompanyMember(data.getJSONObject(i)));
            }

            this.active = freeCompany.getString("Active");
            this.recruitment = freeCompany.getString("Recruitment");

            JSONArray focus = freeCompany.getJSONArray("Focus");
            this.focuses = new ArrayList<>();
            for(int i = 0; i < focus.length(); i++)
            {
                this.focuses.add(new FreeCompanyFocus(focus.getJSONObject(i)));
            }

            JSONArray seeking = freeCompany.getJSONArray("Seeking");
            this.seekedRoles = new ArrayList<>();
            for(int i = 0; i < seeking.length(); i++)
            {
                this.seekedRoles.add(new FreeCompanySeekedRole(seeking.getJSONObject(i)));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }
}
