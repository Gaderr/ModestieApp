package com.modestie.modestieapp.model.login;

import android.content.Context;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository
{
    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // User credentials will be cached in local storage, encryption is needed
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource)
    {
        if (instance == null)
        {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn()
    {
        return user != null;
    }

    public void logout()
    {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user)
    {
        this.user = user;
        // User credentials will be cached in local storage, encryption is needed
        // @see https://developer.android.com/training/articles/keystore
    }

    public void login(String username, String password, Context context, LoginServerCallback callback)
    {
        // handle login
        dataSource.login(username, password, context, callback);
    }
}
