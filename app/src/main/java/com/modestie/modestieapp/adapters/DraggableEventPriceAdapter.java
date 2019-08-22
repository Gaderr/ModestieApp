package com.modestie.modestieapp.adapters;

import androidx.core.util.Pair;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.events.form.EventPriceOptionsModal;
import com.modestie.modestieapp.activities.events.form.EventFormActivity;
import com.modestie.modestieapp.model.event.EventPrice;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.woxthebox.draglistview.DragItemAdapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DraggableEventPriceAdapter extends DragItemAdapter<Pair<Long, EventPrice>, DraggableEventPriceAdapter.ViewHolder>
{
    public ArrayList<Pair<Long, EventPrice>> dataset;

    private boolean readonly;
    private int grabHandleId;
    private boolean dragOnLongPress;

    private EventFormActivity parent;
    private FragmentManager fragmentManager;

    private int lastPosition = -1;

    public static final String TAG = "ADPTR.EVENTPRICE";

    class ViewHolder extends DragItemAdapter.ViewHolder
    {
        View rootview;

        TextView title;
        TextView reward;
        ImageView priceIcon;
        ImageView dragHandle;

        int priceDegree;

        ViewHolder(final View v)
        {
            super(v, grabHandleId, dragOnLongPress);
            this.rootview = v;
            this.title = v.findViewById(R.id.priceTitle);
            this.reward = v.findViewById(R.id.priceReward);
            this.priceIcon = v.findViewById(R.id.priceIcon);
            this.dragHandle = v.findViewById(R.id.dragHandle);
        }

        void clearAnimation()
        {
            this.rootview.clearAnimation();
        }
    }

    public DraggableEventPriceAdapter(ArrayList<Pair<Long, EventPrice>> list, boolean readonly, EventFormActivity context)
    {
        this.dataset = list;
        this.grabHandleId = R.id.dragHandle;
        this.dragOnLongPress = false;
        this.readonly = readonly;
        this.parent = context;
        this.fragmentManager = this.parent.getSupportFragmentManager();
        setItemList(list);
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
        super.onBindViewHolder(holder, position);

        EventPrice price = getItemList().get(position).second;

        String title = "prix";
        holder.priceDegree = position + 1;
        if(holder.priceDegree == 1)
            title = "1er " + title;
        else
            title = holder.priceDegree + "ème " + title;
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

        /*Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(3)
                .oval(false)
                .build();*/

        if(!price.getItemIconURL().equals(""))
        {
            Picasso.get()
                    .load(price.getItemIconURL())
                    .fit()
                    .centerInside()
                    .into(holder.priceIcon);
            holder.priceIcon.setAlpha(1f);
        }

        if(this.readonly)
            holder.dragHandle.setVisibility(View.INVISIBLE);

        holder.rootview.setOnClickListener(view ->
        {
            EventPriceOptionsModal modal = new EventPriceOptionsModal(this, parent, position);
            modal.show(fragmentManager, "bottom_sheet_modal");
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder)
    {
        super.onViewAttachedToWindow(holder);
        /*NestedScrollView view = this.parent.findViewById(R.id.scrollView);
        view.post(() -> view.fullScroll(ScrollView.FOCUS_DOWN));*/
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder)
    {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }

    @Override
    public long getUniqueItemId(int position)
    {
        return getItemList().get(position).first;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > this.lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(parent, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            this.lastPosition = position;
        }
    }
}
