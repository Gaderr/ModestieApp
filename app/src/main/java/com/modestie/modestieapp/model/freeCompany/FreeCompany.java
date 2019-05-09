package com.modestie.modestieapp.model.freeCompany;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.modestie.modestieapp.sql.FreeCompanyDbHelper;
import com.modestie.modestieapp.sql.FreeCompanyReaderContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FreeCompany
{
    /// PROFILE ATTRIBUTES
    private int lodestoneID;
    private String name;
    private String tag;
    private String slogan;
    private String crestBackground;
    private String crestFrame;
    private String crestLogo;
    private long formed; //Formation date
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

    private long updated;

    private SQLiteDatabase database;
    private long dbEntryID;

    private static final String TAG = "XIVAPI.FC";

    public FreeCompany(JSONObject obj)
    {
        setAttributesFromJson(obj);
        this.database = null;
        this.dbEntryID = -1;
    }

    public FreeCompany(JSONObject obj, @org.jetbrains.annotations.NotNull FreeCompanyDbHelper dbHelper)
    {
        Log.e(TAG, "CALLING CONSTRUCTION");
        setAttributesFromJson(obj);
        Log.e(TAG, "CONSTRUCTION CALLED");

        this.database = dbHelper.getWritableDatabase();
        dbHelper.resetDatabase(this.database);

        ContentValues freeCompanyValues = new ContentValues();
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_LODESTONEID, this.lodestoneID);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_NAME, this.name);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_TAG, this.tag);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_SLOGAN, this.slogan);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTBACKGROUND, this.crestBackground);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTFRAME, this.crestFrame);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTLOGO, this.crestLogo);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_FORMED, this.formed);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RANK, this.rank);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYNAME, this.grandCompanyName);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYRANK, this.grandCompanyRank);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYPROGRESS, this.grandCompanyProgress);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_MONTHLYRANKING, this.monthlyRanking);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_WEEKLYRANKING, this.weeklyRanking);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATENAME, this.estateName);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATEPLOT, this.estatePlot);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ACTIVE, this.active);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RECRUITMENT, this.recruitment);
        freeCompanyValues.put(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED, this.updated);

        long rowDbID = this.database.insert(FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME, null, freeCompanyValues);
        Log.e(TAG, "FreeCompany " + rowDbID);

        ContentValues memberValues;
        for (int i = 0; i < this.members.size(); i++)
        {
            memberValues = new ContentValues();
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_LODESTONEID, this.members.get(i).getID());
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_NAME, this.members.get(i).getName());
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_AVATAR, this.members.get(i).getAvatarURL());
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANK, this.members.get(i).getRank());
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_RANKICON, this.members.get(i).getRankIconURL());
            memberValues.put(FreeCompanyReaderContract.MemberEntry.COLUMN_NAME_FEASTMATCHES, this.members.get(i).getFeastMatches());

            rowDbID = this.database.insert(FreeCompanyReaderContract.MemberEntry.TABLE_NAME, null, memberValues);
            Log.e(TAG, "Member " + rowDbID);
        }

        ContentValues focusValues;
        for (int i = 0; i < this.focuses.size(); i++)
        {
            focusValues = new ContentValues();
            focusValues.put(FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_NAME, this.focuses.get(i).getName());
            focusValues.put(FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_ICON, this.focuses.get(i).getIconURL());
            focusValues.put(FreeCompanyReaderContract.FocusEntry.COLUMN_NAME_STATUS, this.focuses.get(i).isStatus() ? 1 : 0);

            rowDbID = this.database.insert(FreeCompanyReaderContract.FocusEntry.TABLE_NAME, null, focusValues);
            Log.e(TAG, "Focus " + rowDbID);
        }

        ContentValues seekedRolesValues;
        for (int i = 0; i < this.seekedRoles.size(); i++)
        {
            seekedRolesValues = new ContentValues();
            seekedRolesValues.put(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_NAME, this.seekedRoles.get(i).getName());
            seekedRolesValues.put(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_ICON, this.seekedRoles.get(i).getIconURL());
            seekedRolesValues.put(FreeCompanyReaderContract.SeekedRoleEntry.COLUMN_NAME_STATUS, this.seekedRoles.get(i).isStatus() ? 1 : 0);

            rowDbID = this.database.insert(FreeCompanyReaderContract.SeekedRoleEntry.TABLE_NAME, null, seekedRolesValues);
            Log.e(TAG, "SeekedRole " + rowDbID);
        }
    }

    /**
     * Construct a FreeCompany object from given database and position
     * @param database The database from where to get the FreeCompanies table
     * @param index Position of the free company in the given database
     */
    public FreeCompany(SQLiteDatabase database, int index)
    {
        this.database = database;
        Cursor cursor = this.database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME, null);

        if(cursor.moveToPosition(index))
        {
            this.lodestoneID            = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_LODESTONEID));
            this.name                   = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_NAME));
            this.tag                    = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_TAG));
            this.slogan                 = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_SLOGAN));
            this.crestBackground        = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTBACKGROUND));
            this.crestFrame             = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTFRAME));
            this.crestLogo              = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_CRESTLOGO));
            this.formed                 = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_FORMED));
            this.rank                   = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RANK));
            this.grandCompanyName       = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYNAME));
            this.grandCompanyRank       = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYRANK));
            this.grandCompanyProgress   = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_GRANDCOMPANYPROGRESS));
            this.monthlyRanking         = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_MONTHLYRANKING));
            this.weeklyRanking          = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_WEEKLYRANKING));
            this.estateName             = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATENAME));
            this.estatePlot             = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ESTATEPLOT));
            this.active                 = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_ACTIVE));
            this.recruitment            = cursor.getString(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_RECRUITMENT));
            this.updated                = cursor.getInt(cursor.getColumnIndex(FreeCompanyReaderContract.FreeCompanyEntry.COLUMN_NAME_UPDATED));
        }
        else
        {
            Log.e(TAG, "No entry found at index " + index + " in " + FreeCompanyReaderContract.FreeCompanyEntry.TABLE_NAME + " table");
        }

        cursor = this.database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME, null);

        this.members = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            while(cursor.moveToNext())
            {
                this.members.add(new FreeCompanyMember(cursor));
            }
        }
        else
        {
            Log.e(TAG, "No entries found in " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME + " table");
        }

        cursor = this.database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.FocusEntry.TABLE_NAME, null);

        this.focuses = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            while(cursor.moveToNext())
            {
                this.focuses.add(new FreeCompanyFocus(cursor));
            }
        }
        else
        {
            Log.e(TAG, "No entries found in " + FreeCompanyReaderContract.FocusEntry.TABLE_NAME + " table");
        }

        cursor = this.database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.SeekedRoleEntry.TABLE_NAME, null);

        this.seekedRoles = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            while(cursor.moveToNext())
            {
                this.seekedRoles.add(new FreeCompanySeekedRole(cursor));
            }
        }
        else
        {
            Log.e(TAG, "No entries found in " + FreeCompanyReaderContract.SeekedRoleEntry.TABLE_NAME + " table");
        }

        cursor.close();
    }

    private void setAttributesFromJson(JSONObject obj)
    {
        try
        {
            Log.e(TAG, "CONSTRUCTION INITIATED");

            JSONObject freeCompany = obj.getJSONObject("FreeCompany");
            JSONArray crest = freeCompany.getJSONArray("Crest");
            JSONArray reputation = freeCompany.getJSONArray("Reputation");
            JSONObject ranking = freeCompany.getJSONObject("Ranking");
            JSONObject estate = freeCompany.getJSONObject("Estate");

            this.lodestoneID = freeCompany.getInt("ID");
            this.name = freeCompany.getString("Name");
            this.tag = freeCompany.getString("Tag");
            this.slogan = freeCompany.getString("Slogan");

            if(crest.length() == 2)
            {
                this.crestBackground = crest.getString(0);
                this.crestFrame = crest.getString(0);
                this.crestLogo = crest.getString(1);
            }
            else
            {
                this.crestBackground = crest.getString(0);
                this.crestFrame = crest.getString(1);
                this.crestLogo = crest.getString(2);
            }

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

            Log.e(TAG, "CONSTRUCTING MEMBERS");
            JSONObject freeCompanyMembers = obj.getJSONObject("FreeCompanyMembers");
            this.members = new ArrayList<>();
            JSONArray data = freeCompanyMembers.getJSONArray("data");
            for (int i = 0; i < data.length(); i++)
            {
                Log.e(TAG, "member " + i);
                this.members.add(new FreeCompanyMember(data.getJSONObject(i)));
            }

            this.active = freeCompany.getString("Active");
            this.recruitment = freeCompany.getString("Recruitment");

            Log.e(TAG, "CONSTRUCTING FOCUS");
            JSONArray focus = freeCompany.getJSONArray("Focus");
            this.focuses = new ArrayList<>();
            for (int i = 0; i < focus.length(); i++)
            {
                this.focuses.add(new FreeCompanyFocus(focus.getJSONObject(i)));
            }

            Log.e(TAG, "CONSTRUCTING SEEKING");
            JSONArray seeking = freeCompany.getJSONArray("Seeking");
            this.seekedRoles = new ArrayList<>();
            for (int i = 0; i < seeking.length(); i++)
            {
                this.seekedRoles.add(new FreeCompanySeekedRole(seeking.getJSONObject(i)));
            }

            this.updated = System.currentTimeMillis() / 1000;

            Log.e(TAG, "CONSTRUCTION DONE");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public int getLodestoneID()
    {
        return lodestoneID;
    }

    public void setLodestoneID(int lodestoneID)
    {
        this.lodestoneID = lodestoneID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getSlogan()
    {
        return slogan;
    }

    public void setSlogan(String slogan)
    {
        this.slogan = slogan;
    }

    public String getCrestBackground()
    {
        return crestBackground;
    }

    public void setCrestBackground(String crestBackground)
    {
        this.crestBackground = crestBackground;
    }

    public String getCrestFrame()
    {
        return crestFrame;
    }

    public void setCrestFrame(String crestFrame)
    {
        this.crestFrame = crestFrame;
    }

    public String getCrestLogo()
    {
        return crestLogo;
    }

    public void setCrestLogo(String crestLogo)
    {
        this.crestLogo = crestLogo;
    }

    public long getFormed()
    {
        return formed;
    }

    public void setFormed(long formed)
    {
        this.formed = formed;
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

    public String getGrandCompanyName()
    {
        return grandCompanyName;
    }

    public void setGrandCompanyName(String grandCompanyName)
    {
        this.grandCompanyName = grandCompanyName;
    }

    public String getGrandCompanyRank()
    {
        return grandCompanyRank;
    }

    public void setGrandCompanyRank(String grandCompanyRank)
    {
        this.grandCompanyRank = grandCompanyRank;
    }

    public int getGrandCompanyProgress()
    {
        return grandCompanyProgress;
    }

    public void setGrandCompanyProgress(int grandCompanyProgress)
    {
        this.grandCompanyProgress = grandCompanyProgress;
    }

    public int getMonthlyRanking()
    {
        return monthlyRanking;
    }

    public void setMonthlyRanking(int monthlyRanking)
    {
        this.monthlyRanking = monthlyRanking;
    }

    public int getWeeklyRanking()
    {
        return weeklyRanking;
    }

    public void setWeeklyRanking(int weeklyRanking)
    {
        this.weeklyRanking = weeklyRanking;
    }

    public String getEstateName()
    {
        return estateName;
    }

    public void setEstateName(String estateName)
    {
        this.estateName = estateName;
    }

    public String getEstatePlot()
    {
        return estatePlot;
    }

    public void setEstatePlot(String estatePlot)
    {
        this.estatePlot = estatePlot;
    }

    public ArrayList<FreeCompanyMember> getMembers()
    {
        return members;
    }

    public void setMembers(ArrayList<FreeCompanyMember> members)
    {
        this.members = members;
    }

    public String getActive()
    {
        return active;
    }

    public void setActive(String active)
    {
        this.active = active;
    }

    public String getRecruitment()
    {
        return recruitment;
    }

    public void setRecruitment(String recruitment)
    {
        this.recruitment = recruitment;
    }

    public ArrayList<FreeCompanyFocus> getFocuses()
    {
        return focuses;
    }

    public void setFocuses(ArrayList<FreeCompanyFocus> focuses)
    {
        this.focuses = focuses;
    }

    public ArrayList<FreeCompanySeekedRole> getSeekedRoles()
    {
        return seekedRoles;
    }

    public void setSeekedRoles(ArrayList<FreeCompanySeekedRole> seekedRoles)
    {
        this.seekedRoles = seekedRoles;
    }

    public long getUpdated()
    {
        return updated;
    }

    public void setUpdated(long updated)
    {
        this.updated = updated;
    }

    public long getLastUpdateTime()
    {
        return this.updated;
    }
}
