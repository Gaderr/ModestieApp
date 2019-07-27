package com.modestie.modestieapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CharacterListAdapter extends RecyclerView.Adapter<CharacterListAdapter.CharacterViewHolder>
{
    public interface OnItemClickListener
    {
        void onItemClick(LightCharacter character);
    }

    public Context context;

    private final OnItemClickListener listener;

    private ArrayList<LightCharacter> characters;

    public static final String TAG = "ADPTR.ITEMSRCH";

    // This class provide a reference to the views for each data item
    public static class CharacterViewHolder extends RecyclerView.ViewHolder
    {
        View rootview;

        TextView name;
        ImageView avatar;
        TextView server;

        public CharacterViewHolder(View v)
        {
            super(v);
            this.rootview = v;
            this.avatar = v.findViewById(R.id.avatar);
            this.name = v.findViewById(R.id.name);
            this.server = v.findViewById(R.id.server);
        }

        public void bind(final LightCharacter character, final OnItemClickListener listener)
        {
            itemView.setOnClickListener(v -> listener.onItemClick(character));
        }
    }

    @SuppressLint("UseSparseArrays")
    public CharacterListAdapter(ArrayList<LightCharacter> dataset, OnItemClickListener listener)
    {
        this.characters = dataset;
        this.listener = listener;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_character_item, parent, false);

        return new CharacterViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull CharacterViewHolder holder, int position)
    {
        LightCharacter character = this.characters.get(position);

        holder.name.setText(character.getName());
        holder.server.setText(character.getServer());

        Picasso.get()
                .load(character.getAvatarURL())
                //.resize(80,80)
                .fit()
                .centerInside()
                .into(holder.avatar);

        holder.bind(character, this.listener);
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return characters.size();
    }
}
