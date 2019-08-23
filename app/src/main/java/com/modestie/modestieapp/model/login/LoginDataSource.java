package com.modestie.modestieapp.model.login;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;

import org.json.JSONObject;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource
{
    private static final String TAG = "LOGINDATASOURCE";
    private RequestHelper requestHelper;

    private static final long dayInMillis = 86400000;
    private static final int expirationDays = 7;
    private static final long maxDelayMillis = 60000; //1 minute

    public void login(String username, String password, Context context, LoginServerCallback callback)
    {
        this.requestHelper = new RequestHelper(context);
        try
        {
            JSONObject postParams = new JSONObject();
            postParams.put("username", username);
            postParams.put("password", password);

            //Set expiration time to : current + 7 days - 1 minute (preventing )
            long expiration = System.currentTimeMillis() + (expirationDays * dayInMillis) - maxDelayMillis;

            this.requestHelper.addToRequestQueue(new JsonObjectRequest(
                    Request.Method.POST, RequestURLs.MODESTIE_GET_JWT_APIKEY, postParams,
                    response -> callback.onServerResponse(new Result.Success<>(new LoggedInUser(response, expiration))),
                    error ->
                    {
                        if (error.networkResponse != null)
                        {
                            if(error.networkResponse.statusCode == 403)
                                callback.onServerResponse(new Result.Error(R.string.login_wrong_credentials));
                            else
                                callback.onServerResponse(new Result.Error(R.string.login_network_error));
                        }
                    }
            ));
        }
        catch (Exception e)
        {
            callback.onServerResponse(new Result.Error(R.string.login_fatal));
        }
    }

    public void logout()
    {
        // TODO: revoke authentication
    }
}
