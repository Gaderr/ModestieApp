package com.modestie.modestieapp.activities.login;

import androidx.annotation.Nullable;

import com.modestie.modestieapp.model.login.LoggedInUser;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult
{
    @Nullable
    private LoggedInUser success;
    @Nullable
    private Integer error;

    LoginResult(@Nullable Integer error)
    {
        this.error = error;
    }

    LoginResult(@Nullable LoggedInUser success)
    {
        this.success = success;
    }

    @Nullable
    LoggedInUser getSuccess()
    {
        return success;
    }

    @Nullable
    Integer getError()
    {
        return error;
    }
}
