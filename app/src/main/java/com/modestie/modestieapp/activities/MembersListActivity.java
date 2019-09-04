package com.modestie.modestieapp.activities;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;

import java.util.Objects;

public class MembersListActivity extends AppCompatActivity implements MemberFragment.OnListFragmentInteractionListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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
        Intent characterActivityIntent = new Intent(getApplicationContext(), CharacterActivity.class);
        characterActivityIntent.putExtra("CharacterID", member.getID());
        characterActivityIntent.putExtra("Name", member.getName());
        startActivity(characterActivityIntent);
    }
}
