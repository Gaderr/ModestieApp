package com.modestie.modestieapp.model.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.modestie.modestieapp.R;

import org.json.JSONObject;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource
{
    private static final String TAG = "LOGINDATASOURCE";
    private Context context;
    private RequestQueue mRequestQueue;

    private static final String JWTRequest = "https://modestie.fr/wp-json/jwt-auth/v1/token";

    public void login(String username, String password, Context context, LoginServerCallback callback)
    {
        this.context = context;
        try
        {
            JSONObject postParams = new JSONObject();
            postParams.put("username", username);
            postParams.put("password", password);

            addToRequestQueue(new JsonObjectRequest(
                    Request.Method.POST, JWTRequest, postParams,
                    response -> callback.onServerResponse(new Result.Success<>(new LoggedInUser(response))),
                    error -> callback.onServerResponse(new Result.Error(R.string.login_wrong_credentials))
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

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     *
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null)
            this.mRequestQueue = Volley.newRequestQueue(this.context);

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag)
    {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }
}
