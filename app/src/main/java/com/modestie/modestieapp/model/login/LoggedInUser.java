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

    public LoggedInUser(String token, String userEmail, String userNiceName, String displayName)
    {
        this.token = token;
        this.userEmail = userEmail;
        this.userNiceName = userNiceName;
        this.displayName = displayName;
    }

    public LoggedInUser(JSONObject object)
    {
        try
        {
            this.token = object.getString("token");
            this.userEmail = object.getString("user_email");
            this.userNiceName = object.getString("user_nicename");
            this.displayName = object.getString("user_display_name");
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
}
