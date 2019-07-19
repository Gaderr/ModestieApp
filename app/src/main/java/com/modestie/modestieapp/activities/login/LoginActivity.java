package com.modestie.modestieapp.activities.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;

public class LoginActivity extends AppCompatActivity
{

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final TextInputLayout usernameEditText = findViewById(R.id.username);
        final TextInputLayout passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(
                this, loginFormState ->
                {
                    if (loginFormState == null)
                    {
                        return;
                    }
                    loginButton.setEnabled(loginFormState.isDataValid());
                    if (loginFormState.getUsernameError() != null)
                    {
                        usernameEditText.getEditText().setError(getString(loginFormState.getUsernameError()));
                    }
                    if (loginFormState.getPasswordError() != null)
                    {
                        passwordEditText.getEditText().setError(getString(loginFormState.getPasswordError()));
                    }
                });

        loginViewModel.getLoginResult().observe(
                this, loginResult ->
                {
                    if (loginResult == null)
                    {
                        usernameEditText.setEnabled(true);
                        passwordEditText.setEnabled(true);
                        loginButton.setEnabled(true);
                        return;
                    }
                    loadingProgressBar.setVisibility(View.GONE);
                    if (loginResult.getError() != null)
                    {
                        showLoginFailed(loginResult.getError());
                    }
                    if (loginResult.getSuccess() != null)
                    {
                        updateUiWithUser(loginResult.getSuccess());
                    }
                    setResult(Activity.RESULT_OK);

                    //Complete and destroy login activity once successful
                    //finish();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                });

        TextWatcher afterTextChangedListener = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                loginViewModel.loginDataChanged(usernameEditText.getEditText().getText().toString(),
                                                passwordEditText.getEditText().getText().toString());
            }
        };
        usernameEditText.getEditText().addTextChangedListener(afterTextChangedListener);
        passwordEditText.getEditText().addTextChangedListener(afterTextChangedListener);
        passwordEditText.getEditText().setOnEditorActionListener(
                (v, actionId, event) ->
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        loginViewModel.login(usernameEditText.getEditText().getText().toString(),
                                             passwordEditText.getEditText().getText().toString(),
                                             this);
                    }
                    return false;
                });

        loginButton.setOnClickListener(
                v ->
                {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    hideKeyboardFrom(this, loginButton);
                    usernameEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    loginButton.setEnabled(false);
                    loginViewModel.login(usernameEditText.getEditText().getText().toString(),
                                         passwordEditText.getEditText().getText().toString(),
                                         this);
                });
    }

    private void updateUiWithUser(LoggedInUserView model)
    {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // Initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString)
    {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboardFrom(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
