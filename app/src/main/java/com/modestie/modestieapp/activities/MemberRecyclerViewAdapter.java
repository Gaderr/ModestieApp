package com.modestie.modestieapp.activities;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.MemberFragment.OnListFragmentInteractionListener;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FreeCompanyMember} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MemberRecyclerViewAdapter extends RecyclerView.Adapter<MemberRecyclerViewAdapter.ViewHolder>
{

    private final ArrayList<FreeCompanyMember> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MemberRecyclerViewAdapter(ArrayList<FreeCompanyMember> items, OnListFragmentInteractionListener listener)
    {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);

        holder.memberNameView.setText(mValues.get(position).getName());
        holder.memberRankView.setText(mValues.get(position).getRank());

        Picasso.get()
                .load(mValues.get(position).getRankIconURL())
                .into(holder.rankIcon);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.get()
                .load(mValues.get(position).getAvatarURL())
                .fit()
                .transform(transformation)
                .into(holder.avatarView);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final RoundedImageView avatarView;
        public final ImageView rankIcon;
        public final TextView memberNameView;
        public final TextView memberRankView;
        public FreeCompanyMember mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            avatarView = view.findViewById(R.id.avatar);
            rankIcon = view.findViewById(R.id.rankIcon);
            memberNameView = view.findViewById(R.id.memberName);
            memberRankView = view.findViewById(R.id.memberRank);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + memberRankView.getText() + "'";
        }
    }
}
