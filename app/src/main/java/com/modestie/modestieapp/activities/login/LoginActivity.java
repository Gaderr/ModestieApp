package com.modestie.modestieapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.activities.MemberFragment;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.modestie.modestieapp.utils.network.RequestURLs;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

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

    private FirebaseAuth fbAuth;

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

    @Override
    public void onBackPressed()
    {
        if(this.pager.getCurrentItem() == CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE || this.pager.getCurrentItem() == CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE)
        {
            int previousPage = this.pager.getCurrentItem() - 1;
            if (previousPage == CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE)
                ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE)).toggleMembersListVisibility();

            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);
        }
    }

    /**
     * Called by LoginFragment to check if the user have a registered character.
     * If not, it swipe to the first page of the character verification.
     * If yes, it requests the character avatar from XIVAPI and redirects to Home Activity.
     *
     * @param userID The user's Firebase user ID
     */
    @Override
    public void onLoginSuccess(String userID)
    {
        this.fbAuth = FirebaseAuth.getInstance();
        LoginFragment loginFragment = (LoginFragment) this.pagerAdapter.getFragment(LOGIN_PAGE);
        loginFragment.setFeedbackText(getString(R.string.login_feedback_modestiefr_check_registration));

        //Check if user have a character registration in database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("registrations").document(this.fbAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(
                task ->
                {
                    if (task.isSuccessful())
                    {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists())
                        {
                            getCharacterThenFinish((long) document.get("lodestone ID"), false);
                        }
                        else
                        {
                            Log.d(TAG, "No character registered for this user, showing first registration page...");
                            loginFragment.hideProgressBar();
                            this.pager.setCurrentItem(CHARACTER_REGISTRATION_FIRST_PAGE);
                        }
                    }
                    else
                    {
                        Log.e(TAG, "Error getting document: ", task.getException());
                    }
                });
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
     * This listener is called by the FC Member selection. It calls the registration check request of
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

        //Check if this character is already registered
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registrations")
                .whereEqualTo("lodestone ID", member.getID())
                .get()
                .addOnCompleteListener(
                        task ->
                        {
                            if(task.isSuccessful())
                            {
                                if(task.getResult().getDocuments().isEmpty()) //False positive
                                {
                                    Log.d(TAG, "No character registered");
                                    registrationFragment.setCharacter(member);
                                    this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
                                }
                                else
                                {
                                    Log.d(TAG, "Character already registered");
                                    memberSelectionFragment.toggleMembersListVisibility();
                                    Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Log.d(TAG, "get failed with ", task.getException());
                                Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                memberSelectionFragment.toggleMembersListVisibility();
                            }
                            this.pending = false;
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
    public void onCharacterSelection(LightCharacter character)
    {
        CharacterRegistrationFragment memberSelectionFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE);
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);

        //Check if this character is already registered
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registrations")
                .whereEqualTo("lodestone ID", character.getID())
                .get()
                .addOnCompleteListener(
                        task ->
                        {
                            if(task.isSuccessful())
                            {
                                if(task.getResult().getDocuments().isEmpty()) //False positive
                                {
                                    Log.d(TAG, "No character registered");
                                    registrationFragment.setCharacter(character);
                                    this.pager.setCurrentItem(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
                                }
                                else
                                {
                                    Log.d(TAG, "Character already registered");
                                    memberSelectionFragment.toggleMembersListVisibility();
                                    Toast.makeText(this, "Ce personnage est déjà enregistré", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Log.d(TAG, "get failed with ", task.getException());
                                Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                memberSelectionFragment.toggleMembersListVisibility();
                            }
                        });
    }

    /**
     * Listener called to begin a character verification then register this character into Firestore db
     *
     * @param characterID The lodestone ID character to register
     */
    @Override
    public void onBeginRegistrationInteraction(long characterID, String hash)
    {
        CharacterRegistrationFragment registrationFragment = (CharacterRegistrationFragment) this.pagerAdapter.getFragment(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);

        registrationFragment.toggleMembersListVisibility();

        this.pending = true;

        this.requestHelper.addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.POST, RequestURLs.XIVAPI_CHARACTER_REQ + "/" + characterID + RequestURLs.XIVAPI_CHARACTER_PARAM_BIO, null,
                        response ->
                        {
                            try
                            {
                                registrationFragment.pendingEnded();

                                if (response.getJSONObject("Character").getString("Bio").equals(hash))
                                {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                    Map<String, Object> character = new HashMap<>();
                                    character.put("lodestone ID", characterID);

                                    db.collection("registrations").document(this.fbAuth.getCurrentUser().getUid())
                                            .set(character)
                                            .addOnSuccessListener(
                                                    aVoid ->
                                                    {
                                                        Log.d(TAG, "Character registered");
                                                        this.pending = false;
                                                        getCharacterThenFinish(characterID, true);
                                                    })
                                            .addOnFailureListener(
                                                    e ->
                                                    {
                                                        Log.e(TAG, "Character registration failed");
                                                        this.pending = false;
                                                        Toast.makeText(this, "Une erreur serveur s'est produite, veuillez réessayer", Toast.LENGTH_SHORT).show();
                                                        registrationFragment.toggleMembersListVisibility();
                                                        registrationFragment.pendingEnded();
                                                    });
                                }
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
                ));
    }

    /**
     * Stores the character of the logged in user and creates an intent to Home Activity or the last
     * character registration page if the character just been registered.
     *
     * @param characterID  The user's character ID
     * @param newCharacter If true, the character will be considered as newly registered and the last character registration page will be showed
     */
    private void getCharacterThenFinish(long characterID, boolean newCharacter)
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
                                    if (newCharacter)
                                    {
                                        ((CharacterRegistrationFragment) this.pagerAdapter.getFragment(
                                                CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE))
                                                .toggleMembersListVisibility();
                                        this.pager.setCurrentItem(CHARACTER_REGISTRATION_DONE_PAGE);
                                    }
                                    else
                                    {
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

    @Override
    public void onAttachFragment(@NotNull Fragment fragment)
    {
        if (fragment instanceof MemberFragment)
        {
            MemberFragment memberFragment = (MemberFragment) fragment;
            memberFragment.setOnMemberSelectedListener(this);
        }
    }
}
