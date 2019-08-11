package com.modestie.modestieapp.utils.network;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

public class RequestHelper
{
    private Context context;
    private RequestQueue requestQueue;
    private String TAG = "REQUESTS-HELPER";

    public RequestHelper(Context context)
    {
        this.context = context;
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     *
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if(this.context == null) return null;

        if (this.requestQueue == null)
            this.requestQueue = Volley.newRequestQueue(context);

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag)
    {
        if(this.context == null) return;

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        if(this.context == null) return;

        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if (requestQueue != null)
            requestQueue.cancelAll(tag);
    }
}
