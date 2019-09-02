package com.modestie.modestieapp.activities.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.model.login.UserCredentials;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

/**
 * Fragment implementing login process and UI for email+password an google accounts sign in
 * and account creation
 */
public class LoginFragment extends Fragment
{
    private static final String TAG = "LOGINFRAGMNT";

    private View rootView;
    private TextInputLayout usernameEditText;
    private TextInputLayout passwordEditText;
    private Button loginButton;
    private Button googleSignInButton;
    private Button guestButton;
    private Button toWebsite;
    private ProgressBar loadingProgressBar;
    private TextView loadingFeedback;

    private FirebaseAuth fbAuth;
    private GoogleSignInClient googleSignInClient;

    private SharedPreferences preferences;

    private OnFragmentInteractionListener mListener;

    private int RC_SIGN_IN = 1;

    public LoginFragment()
    {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance()
    {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        this.fbAuth = FirebaseAuth.getInstance();

        this.usernameEditText = rootView.findViewById(R.id.username);
        this.passwordEditText = rootView.findViewById(R.id.password);
        this.loginButton = rootView.findViewById(R.id.login);
        this.loginButton.setEnabled(true);
        this.googleSignInButton = rootView.findViewById(R.id.googleSignIn);
        this.guestButton = rootView.findViewById(R.id.loginGuest);
        this.toWebsite = rootView.findViewById(R.id.toWebsite);
        this.loadingProgressBar = rootView.findViewById(R.id.loading);
        this.loadingFeedback = rootView.findViewById(R.id.loadingFeedback);
        this.rootView = rootView;
        //Get key-value storage
        Hawk.init(getContext()).build();

        //Guest login
        this.guestButton.setOnClickListener(
                v ->
                {
                    //Delete user stored data
                    //Hawk.delete("UserCredentials");
                    Hawk.delete("UserCharacter");
                    this.preferences
                            .edit()
                            .putBoolean("RememberMe", false)
                            .putBoolean("AutoLogin", false)
                            .apply();
                    startActivity(new Intent(getContext(), HomeActivity.class));
                });

        //Load login preferences
        this.preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Load user credentials if stored
        if (Hawk.contains("UserCredentials"))
        {
            UserCredentials data = Hawk.get("UserCredentials");
            this.usernameEditText.getEditText().setText(data.getUsername());
            this.passwordEditText.getEditText().setText(data.getPassword());
        }
        else
        {
            this.usernameEditText.getEditText().setText("");
            this.passwordEditText.getEditText().setText("");
        }

        //To website button
        this.toWebsite.setOnClickListener(
                v ->
                {
                    String url = "https://modestie.fr";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
        );

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        this.googleSignInButton.setOnClickListener(v -> googleSignIn());

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        this.loginButton.setOnClickListener(v -> beginLogin());
    }

    private void onLoginSuccess(String userID)
    {
        if (mListener != null)
        {
            mListener.onLoginSuccess(userID);
        }
    }

    @Override
    public void onAttach(@NotNull Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            this.mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnParticipationChanged");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e)
            {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    void hideProgressBar()
    {
        this.loadingProgressBar.setVisibility(View.GONE);
    }

    private void beginLogin()
    {
        boolean formValid = true;

        if (this.usernameEditText.getEditText().getText().toString().isEmpty())
        {
            this.usernameEditText.setError("Requis");
            formValid = false;
        }

        if (this.passwordEditText.getEditText().getText().toString().isEmpty())
        {
            this.passwordEditText.setError("Requis");
            formValid = false;
        }

        if (!formValid)
        {
            Log.e(TAG, "form is empty");
            return;
        }

        disableLoginElements();

        this.fbAuth.signInWithEmailAndPassword(this.usernameEditText.getEditText().getText().toString(), this.passwordEditText.getEditText().getText().toString())
                .addOnCompleteListener(getActivity(), task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        registerSignedInUserAndContinue();
                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getContext(), getString(R.string.login_wrong_credentials), Toast.LENGTH_SHORT).show();
                        resetLoginElements();
                    }
                });
    }

    private void googleSignIn()
    {
        Intent signInIntent = this.googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        disableLoginElements();

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        this.fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        registerSignedInUserAndContinue();
                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(this.rootView, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        resetLoginElements();
                    }
                });
    }

    private void disableLoginElements()
    {
        this.loadingProgressBar.setVisibility(View.VISIBLE);
        this.loadingFeedback.setText(R.string.login_feedback_modestiefr_connection);
        hideKeyboardFrom(getContext(), this.loginButton);
        this.usernameEditText.setEnabled(false);
        this.passwordEditText.setEnabled(false);

        this.loginButton.setEnabled(false);
        this.googleSignInButton.setEnabled(false);
        this.guestButton.setEnabled(false);
        this.toWebsite.setEnabled(false);
    }

    void resetLoginElements()
    {
        this.loadingProgressBar.setVisibility(View.INVISIBLE);
        this.loadingFeedback.setText("");
        this.usernameEditText.setEnabled(true);
        this.passwordEditText.setEnabled(true);
        this.loginButton.setEnabled(true);
        this.googleSignInButton.setEnabled(true);
        this.toWebsite.setEnabled(true);
    }

    void setFeedbackText(String text)
    {
        this.loadingFeedback.setText(text);
    }

    private void registerSignedInUserAndContinue()
    {
        //Store user credentials
        Hawk.put("UserCredentials", new UserCredentials(
                this.usernameEditText.getEditText().getText().toString(),
                this.passwordEditText.getEditText().getText().toString()));

        //Call listener
        onLoginSuccess(this.fbAuth.getCurrentUser().getUid());
    }

    private static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * This interface allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener
    {
        void onLoginSuccess(String userID);
    }
}
