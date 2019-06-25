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
import com.modestie.modestieapp.model.item.LightItem;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ItemSearchAdapter extends RecyclerView.Adapter<ItemSearchAdapter.GameItemViewHolder>
{
    public interface OnItemClickListener
    {
        void onItemClick(LightItem item);
    }

    public Context context;

    private final OnItemClickListener listener;

    private ArrayList<LightItem> items;

    public static final String TAG = "ADPTR.ITEMSRCH";

    // This class provide a reference to the views for each data item
    public static class GameItemViewHolder extends RecyclerView.ViewHolder
    {
        View rootview;

        TextView itemName;
        ImageView itemIcon;

        public GameItemViewHolder(View v)
        {
            super(v);
            this.rootview = v;
            this.itemIcon = v.findViewById(R.id.itemIcon);
            this.itemName = v.findViewById(R.id.itemName);
        }

        public void bind(final LightItem item, final OnItemClickListener listener)
        {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @SuppressLint("UseSparseArrays")
    public ItemSearchAdapter(ArrayList<LightItem> dataset, OnItemClickListener listener)
    {
        this.items = dataset;
        this.listener = listener;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ItemSearchAdapter.GameItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item_item, parent, false);

        return new GameItemViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull GameItemViewHolder holder, int position)
    {
        LightItem item = this.items.get(position);

        holder.itemName.setText(item.itemName);
        Picasso.get()
                .load(item.iconURL)
                //.resize(80,80)
                .fit()
                .centerInside()
                .into(holder.itemIcon);

        holder.bind(item, this.listener);
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
