package com.modestie.modestieapp.activities.login;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

import static com.modestie.modestieapp.activities.login.LoginActivity.*;

public class LoginFragmentPager extends FragmentPagerAdapter
{
    private static final int PAGE_COUNT = 5;

    private ArrayList<Fragment> fragments;

    LoginFragmentPager(FragmentManager fragmentManager)
    {
        super(fragmentManager);
        this.fragments = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment;
        switch (position)
        {
            case LOGIN_PAGE:
                fragment = new LoginFragment();
                break;

            case CHARACTER_REGISTRATION_FIRST_PAGE:
                fragment = CharacterRegistrationFragment.newInstance(CHARACTER_REGISTRATION_FIRST_PAGE);
                break;

            case CHARACTER_REGISTRATION_FREE_COMPANY_MEMBER_SELECTION_PAGE:
                fragment = CharacterRegistrationFragment.newInstance(CHARACTER_REGISTRATION_FREE_COMPANY_MEMBER_SELECTION_PAGE);
                break;

            case CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE:
                fragment = CharacterRegistrationFragment.newInstance(CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE);
                break;

            case CHARACTER_REGISTRATION_DONE_PAGE:
                fragment = CharacterRegistrationFragment.newInstance(CHARACTER_REGISTRATION_DONE_PAGE);
                break;

            default: fragment = new Fragment();
        }

        this.fragments.add(fragment);
        return fragment;
    }

    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }

    Fragment getFragment(int position)
    {
        return this.fragments.get(position);
    }
}
