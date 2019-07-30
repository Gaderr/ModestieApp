package com.modestie.modestieapp.model.login;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser
{
    private String token;
    private String userEmail;
    private String userNiceName;
    private String displayName;
    private long expiration;

    public LoggedInUser(String token, String userEmail, String userNiceName, String displayName, long expiration)
    {
        this.token = token;
        this.userEmail = userEmail;
        this.userNiceName = userNiceName;
        this.displayName = displayName;
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

    public String getDisplayName()
    {
        return displayName;
    }

    public long getExpiration() {
        return expiration;
    }
}
