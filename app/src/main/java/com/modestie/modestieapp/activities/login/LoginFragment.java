package com.modestie.modestieapp.activities.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.model.login.UserCredentials;
import com.orhanobut.hawk.Hawk;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment
{
    private TextInputLayout usernameEditText;
    private TextInputLayout passwordEditText;
    private CheckBox rememberMeCheckBox;
    private CheckBox autologinCheckBox;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    private LoginViewModel loginViewModel;

    // Initialization parameters
    private static final String ARG_USERNAME = "username";
    private static final String ARG_PASSWORD = "password";

    private String username;
    private String password;

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

        Hawk.init(getContext()).build();
        if (Hawk.contains("UserCredentials"))
        {
            UserCredentials data = Hawk.get("UserCredentials");
            this.username = data.getUsername();
            this.password = data.getPassword();
        }
        else
        {
            this.username = "";
            this.password = "";
        }

        this.loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory()).get(LoginViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        this.usernameEditText = rootView.findViewById(R.id.username);
        this.passwordEditText = rootView.findViewById(R.id.password);
        this.loginButton = rootView.findViewById(R.id.login);
        this.loadingProgressBar = rootView.findViewById(R.id.loading);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //Auto-login attempt
        if(!this.username.equals("") && !this.password.equals(""))
        {
            this.usernameEditText.getEditText().setText(this.username);
            this.passwordEditText.getEditText().setText(this.password);
            this.username = null;
            this.password = null;
            beginLogin();
        }

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
                });

        this.loginViewModel.getLoginResult().observe(
                this, loginResult ->
                {
                    if (loginResult == null)
                    {
                        hideProgressBar();
                        this.usernameEditText.setEnabled(true);
                        this.passwordEditText.setEnabled(true);
                        this.loginButton.setEnabled(true);
                        return;
                    }
                    if (loginResult.getError() != null)
                    {
                        showLoginFailed(loginResult.getError());
                        hideProgressBar();
                        this.usernameEditText.setEnabled(true);
                        this.passwordEditText.setEnabled(true);
                        this.loginButton.setEnabled(true);
                        return;
                    }
                    if (loginResult.getSuccess() != null)
                    {
                        //Store user details and token
                        Hawk.put("LoggedInUser", loginResult.getSuccess());
                        //Store user credentials
                        Hawk.put("UserCredentials", new UserCredentials(
                                this.usernameEditText.getEditText().getText().toString(),
                                this.passwordEditText.getEditText().getText().toString()));
                        //Call listener
                        onLoginSuccess(loginResult.getSuccess().getUserEmail());
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

    public void onLoginSuccess(String userEmail)
    {
        if (mListener != null)
        {
            mListener.onLoginSuccess(userEmail);
        }
    }

    @Override
    public void onAttach(Context context)
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
        hideKeyboardFrom(getContext(), this.loginButton);
        this.usernameEditText.setEnabled(false);
        this.passwordEditText.setEnabled(false);
        this.loginButton.setEnabled(false);
        this.loginViewModel.login(this.usernameEditText.getEditText().getText().toString(),
                                  this.passwordEditText.getEditText().getText().toString(),
                                  getContext());
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

    public static void hideKeyboardFrom(Context context, View view)
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
        void onLoginSuccess(String userEmail);
    }
}
