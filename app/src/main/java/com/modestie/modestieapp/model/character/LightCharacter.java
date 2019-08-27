package com.modestie.modestieapp.model.character;

import org.json.JSONException;
import org.json.JSONObject;

public class LightCharacter
{
    private String avatarURL;
    private String name;
    private long ID;
    private String server;
    private long lastUpdate;

    public LightCharacter(String avatarURL, String name, int ID, String server)
    {
        this.avatarURL = avatarURL;
        this.name = name;
        this.ID = ID;
        this.server = server;
        this.lastUpdate = System.currentTimeMillis();
    }

    public LightCharacter(JSONObject object)
    {
        try
        {
            this.avatarURL = object.getString("Avatar");
            this.name = object.getString("Name");
            this.ID = object.getLong("ID");
            this.server = object.getString("Server");
            this.lastUpdate = System.currentTimeMillis();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getAvatarURL()
    {
        return avatarURL;
    }

    public String getName()
    {
        return name;
    }

    public long getID()
    {
        return ID;
    }

    public String getServer()
    {
        return server;
    }

    public void setLastUpdate(long lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public long getLastUpdate()
    {
        return lastUpdate;
    }
}
