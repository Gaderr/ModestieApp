package com.modestie.modestieapp.model.character;

import android.util.Log;

import org.json.JSONObject;

public class ClassJob
{
    private static final String TAG = "XIVAPI.CHA.CLSSJB";

    private Class _class;
    private Job _job;
    private int level;
    private String category;

    private int expLevel;
    private int expLevelMax;
    private int expLevelTogo;
    private boolean specialized;

    public ClassJob(JSONObject obj)
    {
        try
        {
            this._class = new Class(obj.getJSONObject("Class"));
            this._job = new Job(obj.getJSONObject("Job"));
            this.level = obj.getInt("Level");

            this.expLevel = obj.getInt("ExpLevel");
            this.expLevelMax = obj.getInt("ExpLevelMax");
            this.expLevelTogo = obj.getInt("ExpLevelTogo");
            this.specialized = obj.getBoolean("IsSpecialised");
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    public Class get_class()
    {
        return _class;
    }

    public void set_class(Class _class)
    {
        this._class = _class;
    }

    public Job get_job()
    {
        return _job;
    }

    public void set_job(Job _job)
    {
        this._job = _job;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getExpLevel()
    {
        return expLevel;
    }

    public void setExpLevel(int expLevel)
    {
        this.expLevel = expLevel;
    }

    public int getExpLevelMax()
    {
        return expLevelMax;
    }

    public void setExpLevelMax(int expLevelMax)
    {
        this.expLevelMax = expLevelMax;
    }

    public int getExpLevelTogo()
    {
        return expLevelTogo;
    }

    public void setExpLevelTogo(int expLevelTogo)
    {
        this.expLevelTogo = expLevelTogo;
    }

    public boolean isSpecialized()
    {
        return specialized;
    }

    public void setSpecialized(boolean specialized)
    {
        this.specialized = specialized;
    }
}
