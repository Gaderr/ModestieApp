package com.modestie.modestieapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.EventPrice;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StaticEventPriceAdapter extends RecyclerView.Adapter<StaticEventPriceAdapter.ViewHolder>
{
    private ArrayList<EventPrice> dataset;

    public static final String TAG = "ADPTR.EVENTPRICE";

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        View rootview;

        TextView title;
        TextView reward;
        ImageView priceIcon;
        ImageView dragHandle;

        ViewHolder(final View v)
        {
            super(v);
            this.rootview = v;
            this.title = v.findViewById(R.id.priceTitle);
            this.reward = v.findViewById(R.id.priceReward);
            this.priceIcon = v.findViewById(R.id.priceIcon);
            this.dragHandle = v.findViewById(R.id.dragHandle);
            this.dragHandle.setVisibility(View.GONE);
        }
    }

    public StaticEventPriceAdapter(ArrayList<EventPrice> list)
    {
        this.dataset = list;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_price_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        EventPrice price = this.dataset.get(position);

        String title = "prix";
        if(price.getPriceRewardDegree() == 1)
            title = "1er " + title;
        else
            title = price.getPriceRewardDegree() + "ème " + title;
        holder.title.setText(title);

        String reward;
        if(price.getItemID() != 1)
            reward = price.priceToString();
        else
        {
            String pattern = "###,###.###";
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern(pattern);
            String output = df.format(price.getAmount());
            reward = output + " gils";
        }
        holder.reward.setText(reward);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(3)
                .oval(false)
                .build();

        if(!price.getItemIconURL().equals(""))
        {
            Picasso.get()
                    .load(price.getItemIconURL())
                    .fit()
                    .centerInside()
                    .into(holder.priceIcon);
            holder.priceIcon.setAlpha(1f);
        }
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return this.dataset.size();
    }
}
