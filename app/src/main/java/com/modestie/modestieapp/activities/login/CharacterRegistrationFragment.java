package com.modestie.modestieapp.activities.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CharacterRegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CharacterRegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterRegistrationFragment extends Fragment
{
    private static final String TAG = "CHRCTR-REGISTR-FRG";

    private Button nextBtn;
    private Button skipBtn;

    private OnFragmentInteractionListener mListener;

    private static final String ARG_PAGE = "page";
    private int page;

    public CharacterRegistrationFragment()
    {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CharacterRegistrationFragment.
     */
    public static CharacterRegistrationFragment newInstance(int page)
    {
        CharacterRegistrationFragment fragment = new CharacterRegistrationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            this.page = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView;
        switch (page)
        {
            case 1:
                rootView = inflater.inflate(R.layout.fragment_character_regist_1, container, false);
                createFirstPage(rootView);
                break;

            case 2:
                rootView = inflater.inflate(R.layout.fragment_character_regist_2, container, false);
                createSecondPage(rootView);
                break;

            default:
                return null;
        }

        return rootView;
    }

    private void createFirstPage(View rootView)
    {
        this.nextBtn = rootView.findViewById(R.id.yesBtn);
        this.nextBtn.setOnClickListener(v ->nextFragment(2));

        this.skipBtn = rootView.findViewById(R.id.toapp);
        this.skipBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), HomeActivity.class)));
    }

    private void createSecondPage(View rootView)
    {

    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    public void nextFragment(int page)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(page);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
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
        mListener = null;
    }

    /**
     * This interface allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(int page);
    }
}
