package com.modestie.modestieapp.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;

public class MembersListActivity extends AppCompatActivity implements MemberFragment.OnListFragmentInteractionListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onAttachFragment(Fragment fragment)
    {
        if (fragment instanceof MemberFragment)
        {
            MemberFragment memberFragment = (MemberFragment) fragment;
            memberFragment.setOnMemberSelectedListener(this);
        }
    }

    public void onListFragmentInteraction(FreeCompanyMember member)
    {
        // The user selected a member from the MemberFragment
        // Do something here to display that article

    }
}
