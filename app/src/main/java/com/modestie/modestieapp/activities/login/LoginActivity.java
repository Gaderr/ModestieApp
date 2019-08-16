package com.modestie.modestieapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.activities.MemberFragment;
import com.modestie.modestieapp.model.character.Character;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        CharacterRegistrationFragment.OnFragmentInteractionListener,
        MemberFragment.OnListFragmentInteractionListener
{
    public static final String TAG = "ACTVT.LOGIN";

    private ViewPager pager;
    private LoginFragmentPager pagerAdapter;

    public static final int LOGIN_PAGE = 0;
    public static final int CHARACTER_REGISTRATION_FIRST_PAGE = 1;
    public static final int CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE = 2;
    public static final int CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE = 3;
    public static final int CHARACTER_REGISTRATION_DONE_PAGE = 4;

    private LoggedInUser loggedInUser;

    private RequestHelper requestHelper;

    private boolean pending;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.requestHelper = new RequestHelper(getApplicationContext());
        this.pager = findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        this.pagerAdapter = new LoginFragmentPager(fm);
        this.pager.setAdapter(pagerAdapter);

        this.pending = false;
    }

    /**
     * Called by LoginFragment to check if the user have a registered character.
     * If not, it swipe to the first page of the character verification.
     * If yes, it requests the character avatar from XIVAPI and redirects to Home Activity.
     *
     * @param userEmail The user's email address
     */
    @Override
    public void onLoginSuccess(String userEmail, int characterID)
    {
        LoginFragment loginFragment = (LoginFragment) this.pagerAdapter.getFragment(LOGIN_PAGE);
        this.loggedInUser = Hawk.get("LoggedInUser");
        loginFragment.setFeedbackText(getString(R.string.login_feedback_modestiefr_check_registration));

        if (characterID != 0)
            getCharacterThenFinish(characterID, false); //Get the character's avatar then finish
        else
        {
            loginFragment.hideProgressBar();
            this.pager.setCurrentItem(CHARACTER_REGISTRATION_FIRST_PAGE);
        }
    }

    /**
     * Stores the character of the logged in user and creates an intent to Home Activity or the last
     * character registration page if the character just been registered.
     *
     * @param characterID  The user's character ID
     * @param newCharacter If true, the character will be considered as newly registered and the last character registration page will be showed
     */
    private void getCharacterThenFinish(int characterID, boolean newCharacter)
    {
        LoginFragment loginFragment = (LoginFragment) this.pagerAdapter.getFragment(LOGIN_PAGE);
        loginFragment.setFeedbackText(getString(R.string.login_feedback_modestiefr_get_character));
        this.requestHelper.addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.GET,
                        RequestURLs.XIVAPI_CHARACTER_REQ + "/" + characterID + RequestURLs.XIVAPI_CHARACTER_PARAM_LIGHT,
                        null,
                        response ->
                        {
                            try
                            {
                                if (Hawk.put("UserCharacter", new LightCharacter(response.getJSONObject("Character")))) //Store avatar URL
                                {
                                    loginFragment.hideProgressBar();

                                    if (newCharacter)
                                    {
                                        ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(
                                                CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE))
                                                .toggleMembersListVisibility();
                                        this.pager.setCurrentItem(CHARACTER_REGISTRATION_DONE_PAGE);
                                    }
                                    else
                                    {
                                        loginFragment.setFeedbackText(getString(R.string.login_feedback_modestiefr_finished));
                                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    }
                                }
                                else
                                {
                                    if (newCharacter)
                                        ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(
                                                CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE))
                                                .toggleMembersListVisibility();
                                    loginFragment.hideProgressBar();
                                    Toast.makeText(this, "Une erreur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                    loginFragment.resetLoginElements();
                                }
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                                loginFragment.hideProgressBar();
                                if (newCharacter)
                                    ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(
                                            CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE))
                                            .toggleMembersListVisibility();
                                loginFragment.resetLoginElements();
                            }
                        },
                        error ->
                        {
                            Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                            loginFragment.hideProgressBar();
                            if (newCharacter)
                                ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(
                                        CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE))
                                        .toggleMembersListVisibility();
                            this.pending = false;
                            loginFragment.resetLoginElements();
                        }
                ));
    }

    /**
     * Set up character selection view depending on user type
     *
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
     *
     * @param characterID The lodestone ID character to verify
     * @param hash        The generated verification hash
     */
    @Override
    public void onBeginRegistrationInteraction(int characterID, String characterAvatar, String hash)
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

            this.requestHelper.addToRequestQueue(
                    new JsonObjectRequest(
                            Request.Method.POST, RequestURLs.MODESTIE_REGISTRATIONS_REGISTER_REQ, postParams,
                            response ->
                            {
                                try
                                {
                                    registrationFragment.pendingEnded();
                                    this.pending = false;

                                    if (response.getBoolean("result"))
                                    {
                                        getCharacterThenFinish(characterID, true);
                                    }
                                    else
                                        Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();
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
                            }
                    )
                    {
                        @Override
                        public Map<String, String> getHeaders()
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("Authorization", "Bearer " + loggedInUser.getToken());
                            return params;
                        }
                    });
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
     *
     * @param member The character picked by the user
     */
    public void onListFragmentInteraction(FreeCompanyMember member)
    {
        if (this.pending)
            return;

        CharacterRegistrationFragment memberSelectionFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE);
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);

        this.pending = true;

        memberSelectionFragment.toggleMembersListVisibility();

        //Check if the user have already a character registered
        this.requestHelper.addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.GET,
                        RequestURLs.MODESTIE_REGISTRATIONS_CHECK_REQ + RequestURLs.MODESTIE_REGISTRATIONS_CHECK_ID_PARAM + member.getID(),
                        null,
                        response ->
                        {
                            try
                            {
                                if (!response.getBoolean("result"))
                                {
                                    registrationFragment.setCharacter(member);
                                    this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
                                }
                                else
                                {
                                    memberSelectionFragment.toggleMembersListVisibility();
                                    Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();
                                }
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
                        }
                )
                {
                    @Override
                    public Map<String, String> getHeaders()
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " + loggedInUser.getToken());
                        return params;
                    }
                });
    }

    /**
     * This listener is called by the character selection. It call the registration check request of
     * ModestieEvents API and swipes to the registration page if the picked character is not
     * registered.
     *
     * @param character The character picked by the user
     */
    @Override
    public void onCharacterSelection(Object character)
    {
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
        registrationFragment.setCharacter(character);
        this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
    }
}
