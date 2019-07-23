package com.modestie.modestieapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;

import com.modestie.modestieapp.R;

public class LoginActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener, CharacterRegistrationFragment.OnFragmentInteractionListener
{
    private ViewPager pager;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.pager = findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        LoginFragmentPager pagerAdapter = new LoginFragmentPager(fm);
        this.pager.setAdapter(pagerAdapter);
    }

    @Override public void onLoginSuccess() { this.pager.setCurrentItem(1); }

    @Override public void onFragmentInteraction(int page)
    {
        Log.e("LOGIN", "NEXT : " + page);
        this.pager.setCurrentItem(page);
    }
}
