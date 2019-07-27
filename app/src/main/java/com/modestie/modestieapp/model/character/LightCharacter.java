package com.modestie.modestieapp.model.character;

import org.json.JSONException;
import org.json.JSONObject;

public class LightCharacter
{
    private String avatarURL;
    private String name;
    private int ID;
    private String server;

    public LightCharacter(String avatarURL, String name, int ID, String server)
    {
        this.avatarURL = avatarURL;
        this.name = name;
        this.ID = ID;
        this.server = server;
    }

    public LightCharacter(JSONObject object)
    {
        try
        {
            this.avatarURL = object.getString("Avatar");
            this.name = object.getString("Name");
            this.ID = object.getInt("ID");
            this.server = object.getString("Server");
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

    public int getID()
    {
        return ID;
    }

    public String getServer()
    {
        return server;
    }
}
