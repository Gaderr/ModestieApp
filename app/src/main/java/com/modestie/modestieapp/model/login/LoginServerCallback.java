package com.modestie.modestieapp.model.login;

public interface LoginServerCallback
{
    void onServerResponse(Result<LoggedInUser> result);
}
