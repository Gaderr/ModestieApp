package com.modestie.modestieapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.activities.MemberFragment;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        CharacterRegistrationFragment.OnFragmentInteractionListener,
        MemberFragment.OnListFragmentInteractionListener
{
    public static final String TAG = "ACTVT.LOGIN";

    private ViewPager pager;
    private LoginFragmentPager pagerAdapter;

    private static final String REGISTRATION_REQUEST = "https://modestie.fr/wp-json/modestieevents/v1/registercharacter";
    private static final String CHECK_REGISTRATION_REQUEST = "https://modestie.fr/wp-json/modestieevents/v1/checkregistration";

    public static final int LOGIN_PAGE = 0;
    public static final int CHARACTER_REGISTRATION_FIRST_PAGE = 1;
    public static final int CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE = 2;
    public static final int CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE = 3;
    public static final int CHARACTER_REGISTRATION_DONE_PAGE = 4;

    private RequestQueue mRequestQueue;

    private boolean pending;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.pager = findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        this.pagerAdapter = new LoginFragmentPager(fm);
        this.pager.setAdapter(pagerAdapter);

        this.pending = false;
    }

    /**
     * Called by LoginFragment to swipe to the first page of the character verification
     */
    @Override
    public void onLoginSuccess(String userEmail)
    {
        LoginFragment loginFragment = (LoginFragment) this.pagerAdapter.getFragment(LOGIN_PAGE);
        addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.GET,
                        CHECK_REGISTRATION_REQUEST + "?userEmail=" + userEmail,
                        null,
                        response ->
                        {
                            try
                            {
                                if(response.getBoolean("result"))
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                else
                                    this.pager.setCurrentItem(CHARACTER_REGISTRATION_FIRST_PAGE);
                                loginFragment.hideProgressBar();
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        },
                        error ->
                        {
                            Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                            this.pending = false;
                        }));
    }

    /**
     * Set up character selection view depending on user type
     * @param FCMember Is user a FC member?
     */
    @Override
    public void onUserTypeSelection(boolean FCMember)
    {
        CharacterRegistrationFragment fragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE);
        fragment.setCharacterSelectionView(FCMember);
        this.pager.setCurrentItem(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE);
    }

    /**
     * Listener called to begin a character verification
     * @param characterID The lodestone ID character to verify
     * @param hash The generated verification hash
     */
    @Override
    public void onBeginRegistrationInteraction(int characterID, String hash)
    {
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);

        LoggedInUser user = Hawk.get("LoggedInUser");
        try
        {
            JSONObject postParams = new JSONObject();
            postParams.put("lodestoneID", characterID);
            postParams.put("ownerEmail", user.getUserEmail());
            postParams.put("hash", hash);

            registrationFragment.toggleMembersListVisibility();

            this.pending = true;

            addToRequestQueue(
                    new JsonObjectRequest(
                            Request.Method.POST, REGISTRATION_REQUEST, postParams,
                            response ->
                            {
                                try
                                {
                                    if(response.getBoolean("result"))
                                        this.pager.setCurrentItem(CHARACTER_REGISTRATION_DONE_PAGE);
                                    else
                                        Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();
                                    registrationFragment.toggleMembersListVisibility();
                                    registrationFragment.pendingEnded();
                                    this.pending = false;
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            },
                            error ->
                            {
                                Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                registrationFragment.toggleMembersListVisibility();
                                registrationFragment.pendingEnded();
                                this.pending = false;
                            }));
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment)
    {
        if (fragment instanceof MemberFragment)
        {
            MemberFragment memberFragment = (MemberFragment) fragment;
            memberFragment.setOnMemberSelectedListener(this);
        }
    }

    /**
     * This listener is called by the FC Member selection. It call the registration check request of
     * ModestieEvents API and swipes to the registration page if the picked character is not
     * registered.
     * @param member The character picked by the user
     */
    public void onListFragmentInteraction(FreeCompanyMember member)
    {
        if(this.pending)
            return;

        CharacterRegistrationFragment memberSelectionFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE);
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);

        this.pending = true;

        memberSelectionFragment.toggleMembersListVisibility();

        //Check if the user have already a character registered
        addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.GET,
                        CHECK_REGISTRATION_REQUEST + "?lodestoneID=" + member.getID(),
                        null,
                        response ->
                        {
                            try
                            {
                                if(!response.getBoolean("result"))
                                {
                                    registrationFragment.setCharacter(member);
                                    this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
                                }
                                else
                                    Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();

                                memberSelectionFragment.toggleMembersListVisibility();
                                this.pending = false;
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        },
                        error ->
                        {
                            Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                            memberSelectionFragment.toggleMembersListVisibility();
                            this.pending = false;
                        }));
    }

    /**
     * This listener is called by the character selection. It call the registration check request of
     * ModestieEvents API and swipes to the registration page if the picked character is not
     * registered.
     * @param character The character picked by the user
     */
    @Override
    public void onCharacterSelection(Object character)
    {
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
        registrationFragment.setCharacter(character);
        this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
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
            this.mRequestQueue = Volley.newRequestQueue(getApplicationContext());

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
