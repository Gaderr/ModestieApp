package com.modestie.modestieapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.modestie.modestieapp.R;

public class LoginActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewPager pager = findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        LoginFragmentPager pagerAdapter = new LoginFragmentPager(fm);
        pager.setAdapter(pagerAdapter);
    }

    @Override
    public void onLoginSuccess()
    {

    }
}
