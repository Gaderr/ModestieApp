package com.modestie.modestieapp.activities.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.model.login.UserCredentials;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment
{
    private static final String TAG = "LOGINFRAGMNT";

    private TextInputLayout usernameEditText;
    private TextInputLayout passwordEditText;
    private CheckBox rememberMeCheckBox;
    private CheckBox autoLoginCheckBox;
    private Button loginButton;
    private ProgressBar loadingProgressBar;
    private TextView loadingFeedback;

    private LoginViewModel loginViewModel;

    private SharedPreferences preferences;

    private boolean autoLoginAttempt;

    private OnFragmentInteractionListener mListener;

    public LoginFragment()
    {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of this fragment.
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

        this.usernameEditText = rootView.findViewById(R.id.username);
        this.passwordEditText = rootView.findViewById(R.id.password);
        this.loginButton = rootView.findViewById(R.id.login);
        Button guestButton = rootView.findViewById(R.id.loginGuest);
        Button toWebsite = rootView.findViewById(R.id.toWebsite);
        this.loadingProgressBar = rootView.findViewById(R.id.loading);
        this.loadingFeedback = rootView.findViewById(R.id.loadingFeedback);
        this.rememberMeCheckBox = rootView.findViewById(R.id.rememberMeCheckbox);
        this.rememberMeCheckBox.setChecked(false);
        this.autoLoginCheckBox = rootView.findViewById(R.id.autologinCheckbox);
        this.autoLoginCheckBox.setChecked(false);

        this.loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        //Get key-value storage
        Hawk.init(getContext()).build();

        //Guest login
        guestButton.setOnClickListener(
                v ->
                {
                    //Delete user stored data
                    //Hawk.delete("UserCredentials");
                    Hawk.delete("UserCharacter");
                    Hawk.delete("LoggedInUser");
                    this.rememberMeCheckBox.setEnabled(false);
                    this.autoLoginCheckBox.setEnabled(false);
                    this.preferences
                            .edit()
                            .putBoolean("RememberMe", false)
                            .putBoolean("AutoLogin", false)
                            .apply();
                    startActivity(new Intent(getContext(), HomeActivity.class));
                });

        //Load login preferences
        this.preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.rememberMeCheckBox.setChecked(preferences.getBoolean("RememberMe", false));
        this.autoLoginCheckBox.setEnabled(preferences.getBoolean("RememberMe", false));
        this.autoLoginCheckBox.setChecked(preferences.getBoolean("AutoLogin", false));

        //Read checkbox changes and edit preferences
        this.rememberMeCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                {
                    this.preferences.edit().putBoolean("RememberMe", isChecked).apply();
                    //The autologin must be enabled only if the user wants to be remembered
                    this.autoLoginCheckBox.setEnabled(isChecked);
                    if(!isChecked) this.preferences.edit().remove("AutoLogin").apply();
                });
        this.autoLoginCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> this.preferences.edit().putBoolean("AutoLogin", isChecked).apply());

        //Load user credentials if stored
        if (this.rememberMeCheckBox.isChecked() && Hawk.contains("UserCredentials"))
        {
            UserCredentials data = Hawk.get("UserCredentials");
            this.usernameEditText.getEditText().setText(data.getUsername());
            this.passwordEditText.getEditText().setText(data.getPassword());
            //Notify data changed -> That makes the login button active
            this.loginViewModel.loginDataChanged(this.usernameEditText.getEditText().getText().toString(), this.passwordEditText.getEditText().getText().toString());
            //Enable auto-login attempt if desired
            if(this.autoLoginCheckBox.isChecked())
                this.autoLoginAttempt = true;
        }
        else
        {
            this.usernameEditText.getEditText().setText("");
            this.passwordEditText.getEditText().setText("");
            this.autoLoginAttempt = false;
        }

        //To website button
        toWebsite.setOnClickListener(
                v ->
                {
                    String url = "https://modestie.fr";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
        );

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        this.loginViewModel.getLoginFormState().observe(
                this, loginFormState ->
                {
                    if (loginFormState == null)
                    {
                        return;
                    }
                    this.loginButton.setEnabled(loginFormState.isDataValid());

                    if (loginFormState.getUsernameError() != null)
                    {
                        this.usernameEditText.getEditText().setError(getString(loginFormState.getUsernameError()));
                    }
                    if (loginFormState.getPasswordError() != null)
                    {
                        this.passwordEditText.getEditText().setError(getString(loginFormState.getPasswordError()));
                    }
                    //Auto-login attempt (after loading of user credentials in text fields (onCreateView))
                    if(this.autoLoginAttempt)
                    {
                        this.autoLoginAttempt = false;
                        beginLogin();
                    }
                });

        this.loginViewModel.getLoginResult().observe(
                this, loginResult ->
                {
                    if (loginResult == null)
                    {
                        hideProgressBar();
                        this.usernameEditText.setEnabled(true);
                        this.passwordEditText.setEnabled(true);
                        this.rememberMeCheckBox.setEnabled(true);
                        this.autoLoginCheckBox.setEnabled(true);
                        this.loginButton.setEnabled(true);
                        return;
                    }
                    if (loginResult.getError() != null)
                    {
                        showLoginFailed(loginResult.getError());
                        hideProgressBar();
                        this.usernameEditText.setEnabled(true);
                        this.passwordEditText.setEnabled(true);
                        this.rememberMeCheckBox.setEnabled(true);
                        this.autoLoginCheckBox.setEnabled(true);
                        this.loginButton.setEnabled(true);
                        return;
                    }
                    if (loginResult.getSuccess() != null)
                    {
                        //Store user details and token
                        Hawk.put("LoggedInUser", loginResult.getSuccess());

                        //Store user credentials
                        if(this.rememberMeCheckBox.isChecked())
                            Hawk.put("UserCredentials", new UserCredentials(
                                    this.usernameEditText.getEditText().getText().toString(),
                                    this.passwordEditText.getEditText().getText().toString()));
                        else
                            Hawk.delete("UserCredentials");

                        //Call listener
                        onLoginSuccess(loginResult.getSuccess().getUserEmail(), loginResult.getSuccess().getCharacterID());
                    }
                    ((LoginActivity) getContext()).setResult(Activity.RESULT_OK);
                });

        TextWatcher afterTextChangedListener = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s)
            {
                loginViewModel.loginDataChanged(usernameEditText.getEditText().getText().toString(),
                                                passwordEditText.getEditText().getText().toString());
            }
        };
        this.usernameEditText.getEditText().addTextChangedListener(afterTextChangedListener);
        this.passwordEditText.getEditText().addTextChangedListener(afterTextChangedListener);
        this.passwordEditText.getEditText().setOnEditorActionListener(
                (v, actionId, event) ->
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE) beginLogin();
                    return false;
                });

        this.loginButton.setOnClickListener(v -> beginLogin());
    }

    private void onLoginSuccess(String userEmail, int characterID)
    {
        if (mListener != null)
        {
            mListener.onLoginSuccess(userEmail, characterID);
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
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.mListener = null;
    }

    void hideProgressBar()
    {
        this.loadingProgressBar.setVisibility(View.GONE);
    }

    private void beginLogin()
    {
        this.loadingProgressBar.setVisibility(View.VISIBLE);
        this.loadingFeedback.setText(R.string.login_feedback_modestiefr_connection);
        hideKeyboardFrom(getContext(), this.loginButton);
        this.usernameEditText.setEnabled(false);
        this.passwordEditText.setEnabled(false);
        this.rememberMeCheckBox.setEnabled(false);
        this.autoLoginCheckBox.setEnabled(false);
        this.loginButton.setEnabled(false);
        this.loginViewModel.login(this.usernameEditText.getEditText().getText().toString(),
                                  this.passwordEditText.getEditText().getText().toString(),
                                  getContext());
    }

    public void resetLoginElements()
    {
        this.loadingProgressBar.setVisibility(View.INVISIBLE);
        this.loadingFeedback.setText("");
        this.usernameEditText.setEnabled(true);
        this.passwordEditText.setEnabled(true);
        this.rememberMeCheckBox.setEnabled(true);
        this.autoLoginCheckBox.setEnabled(true);
        this.loginButton.setEnabled(true);
    }

    public void setFeedbackText(String text)
    {
        this.loadingFeedback.setText(text);
    }

    private void updateUiWithUser(LoggedInUser model)
    {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // Initiate successful logged in experience
        Toast.makeText(getContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString)
    {
        Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
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
        void onLoginSuccess(String userEmail, int characterID);
    }
}
