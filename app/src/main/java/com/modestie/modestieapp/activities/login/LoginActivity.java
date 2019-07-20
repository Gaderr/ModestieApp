package com.modestie.modestieapp.activities.login;

import android.app.Activity;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.model.login.LoggedInUser;
import com.modestie.modestieapp.model.login.UserCredentials;
import com.orhanobut.hawk.Hawk;

public class LoginActivity extends AppCompatActivity
{
    private TextInputLayout usernameEditText;
    private TextInputLayout passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        this.usernameEditText = findViewById(R.id.username);
        this.passwordEditText = findViewById(R.id.password);
        this.loginButton = findViewById(R.id.login);
        this.loadingProgressBar = findViewById(R.id.loading);

        Hawk.init(this).build();

        if (Hawk.contains("UserCredentials"))
        {
            UserCredentials data = Hawk.get("UserCredentials");
            this.usernameEditText.getEditText().setText(data.getUsername());
            this.passwordEditText.getEditText().setText(data.getPassword());
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
                    this.loadingProgressBar.setVisibility(View.GONE);
                    if (loginResult == null)
                    {
                        this.usernameEditText.setEnabled(true);
                        this.passwordEditText.setEnabled(true);
                        this.loginButton.setEnabled(true);
                        return;
                    }
                    if (loginResult.getError() != null)
                    {
                        showLoginFailed(loginResult.getError());
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
                        updateUiWithUser(loginResult.getSuccess());
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    }
                    setResult(Activity.RESULT_OK);
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

    private void beginLogin()
    {
        this.loadingProgressBar.setVisibility(View.VISIBLE);
        hideKeyboardFrom(this, this.loginButton);
        this.usernameEditText.setEnabled(false);
        this.passwordEditText.setEnabled(false);
        this.loginButton.setEnabled(false);
        this.loginViewModel.login(this.usernameEditText.getEditText().getText().toString(),
                                  this.passwordEditText.getEditText().getText().toString(),
                                  this);
    }

    private void updateUiWithUser(LoggedInUser model)
    {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // Initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString)
    {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
