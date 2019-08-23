package com.modestie.modestieapp.model.login;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser
{
    private static final String TAG = "MODEL.LGDINUSER";

    private String token;
    private String userEmail;
    private String userNiceName;
    private String displayName;
    private String APIKey;
    private int characterID;
    private long expiration;

    public LoggedInUser(String token, String userEmail, String userNiceName, String displayName, String apiKey, int characterID, long expiration)
    {
        this.token = token;
        this.userEmail = userEmail;
        this.userNiceName = userNiceName;
        this.displayName = displayName;
        this.APIKey = apiKey;
        this.characterID = characterID;
        this.expiration = expiration;
    }

    public LoggedInUser(JSONObject object, long expiration)
    {
        try
        {
            this.token = object.getString("token");
            this.userEmail = object.getString("user_email");
            this.userNiceName = object.getString("user_nicename");
            this.displayName = object.getString("user_display_name");
            this.APIKey = object.getString("api_key");
            JSONObject registration = object.getJSONObject("registration");
            if(registration.getBoolean("status"))
                this.characterID = registration.getInt("character");
            else
                this.characterID = 0;
            this.expiration = expiration;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getToken()
    {
        return token;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public String getUserNiceName()
    {
        return userNiceName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getAPIKey()
    {
        return APIKey;
    }

    public int getCharacterID()
    {
        return characterID;
    }

    public void setCharacterID(int characterID)
    {
        this.characterID = characterID;
    }

    public long getExpiration()
    {
        return expiration;
    }
}
