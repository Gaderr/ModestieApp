package com.modestie.modestieapp.activities.login;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class LoginFragmentPager extends FragmentPagerAdapter
{
    private static final int PAGE_COUNT = 3;

    LoginFragmentPager(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0: return new LoginFragment();
            case 1: return CharacterRegistrationFragment.newInstance(1);
            case 2: return CharacterRegistrationFragment.newInstance(2);
            case 3: return CharacterRegistrationFragment.newInstance(3);

            default: return null;
        }
    }

    @Override
    public int getCount()
    {
        return this.PAGE_COUNT;
    }
}
