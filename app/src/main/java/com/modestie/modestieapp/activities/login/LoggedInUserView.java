package com.modestie.modestieapp.activities.login;

import com.modestie.modestieapp.model.login.LoggedInUser;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView
{
    private String displayName;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(LoggedInUser data)
    {
        this.displayName = data.getDisplayName();
    }

    String getDisplayName()
    {
        return displayName;
    }
}
