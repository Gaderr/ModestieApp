package com.modestie.modestieapp.activities.login;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import static com.modestie.modestieapp.activities.login.LoginActivity.*;

/**
 * This fragment subclass instantiates the character verification fragments in the LoginFragmentPager
 */
public class CharacterRegistrationFragment extends Fragment
{
    private static final String TAG = "CHRCTR-REGISTR-FRG";

    private Button nextBtn;
    private View loadingView;
    private ImageView clipboardAction;
    private TextView hashTextView;
    private FreeCompanyMember member;
    private RoundedImageView avatarView;
    private ImageView rankIcon;
    private TextView memberNameView;
    private TextView memberRankView;

    private OnFragmentInteractionListener mListener;

    private static final String ARG_PAGE = "page";
    private int page;

    private boolean pending; //Used to disable some elements during a process

    public CharacterRegistrationFragment()
    {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CharacterRegistrationFragment.
     */
    static CharacterRegistrationFragment newInstance(int page)
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
        this.pending = false;
        if (getArguments() != null)
        {
            this.page = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView;
        switch (page)
        {
            case CHARACTER_REGISTRATION_FIRST_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_1, container, false);
                createFirstPage(rootView);
                break;

            case CHARACTER_REGISTRATION_FREE_COMPANY_MEMBER_SELECTION_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_2, container, false);
                createSecondPage(rootView);
                break;

            case CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_3, container, false);
                createThirdPage(rootView);
                break;

            case CHARACTER_REGISTRATION_DONE_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_4, container, false);
                createFourthPage(rootView);
                break;

            default:
                return null;
        }

        return rootView;
    }

    /**
     * Setup views in the second fragment
     * @param rootView Parent view
     */
    private void createFirstPage(View rootView)
    {
        this.nextBtn = rootView.findViewById(R.id.yesBtn);
        this.nextBtn.setOnClickListener(v -> nextFragment(2));
    }

    /**
     * Setup views in the second fragment
     * @param rootView Parent view
     */
    private void createSecondPage(View rootView)
    {
        this.loadingView = rootView.findViewById(R.id.loadingView);
    }

    /**
     * Setup views in the third fragment
     * @param rootView Parent view
     */
    private void createThirdPage(View rootView)
    {
        this.nextBtn = rootView.findViewById(R.id.yesBtn);
        this.member = null;

        this.avatarView = rootView.findViewById(R.id.promoterAvatar);
        this.rankIcon = rootView.findViewById(R.id.rankIcon);
        this.memberNameView = rootView.findViewById(R.id.memberName);
        this.memberRankView = rootView.findViewById(R.id.memberRank);
        this.hashTextView = rootView.findViewById(R.id.hash);
        this.clipboardAction = rootView.findViewById(R.id.clipboardAction);
        Button openLodestoneBtn = rootView.findViewById(R.id.lodestoneOpenBtn);
        Button beginRegistrationBtn = rootView.findViewById(R.id.verifyBtn);

        this.loadingView = rootView.findViewById(R.id.loadingView);

        //Intent to the character lodestone page
        openLodestoneBtn.setOnClickListener(
                v ->
                {
                    if (!this.pending)
                    {
                        String url = "https://fr.finalfantasyxiv.com/lodestone/character/" + this.member.getID();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });

        //Call registration request
        beginRegistrationBtn.setOnClickListener(
                v ->
                {
                    this.pending = true;
                    beginRegistration(this.hashTextView.getText() + "");
                });
    }

    /**
     * Setup views in the fourth fragment
     * @param rootView Parent view
     */
    private void createFourthPage(View rootView)
    {
        Button finishBtn = rootView.findViewById(R.id.finish);
        finishBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), HomeActivity.class)));
    }

    /**
     * Edit character information preview in third fragment
     * @param member The member chosen
     */
    void setMember(FreeCompanyMember member)
    {
        this.member = member;

        this.memberNameView.setText(this.member.getName());
        this.memberRankView.setText(this.member.getRank());

        Picasso.get()
                .load(this.member.getRankIconURL())
                .into(this.rankIcon);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.get()
                .load(this.member.getAvatarURL())
                .fit()
                .transform(transformation)
                .into(this.avatarView);

        this.hashTextView.setText(Utils.bin2hex(Utils.getSHA256Hash(this.member.getID() + this.member.getName())));

        copyHashToClipboard(); //Immediately copy hash

        this.clipboardAction.setOnClickListener(v -> copyHashToClipboard());
    }

    /**
     * Copy text present into hashTextView into clipboard
     */
    private void copyHashToClipboard()
    {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("lodestone hash", this.hashTextView.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copié dans le presse-papier", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    /**
     * Swipe to the specified fragment
     *
     * @param page Fragment position
     */
    private void nextFragment(int page)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(page);
        }
    }

    /**
     * Begin character verification
     *
     * @param hash Hash used for verification
     */
    private void beginRegistration(String hash)
    {
        if (mListener != null && this.member != null)
        {
            mListener.onBeginRegistrationInteraction(this.member, hash);
        }
    }

    void pendingEnded()
    {
        this.pending = false;
    }

    /**
     * Toggles the visibility of loadingView (Present in some layouts)
     */
    void toggleMembersListVisibility()
    {
        this.loadingView.setVisibility(this.loadingView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onAttach(@NotNull Context context)
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
        void onBeginRegistrationInteraction(FreeCompanyMember member, String hash);
    }
}
