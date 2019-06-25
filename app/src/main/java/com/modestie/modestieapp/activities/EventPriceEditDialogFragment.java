package com.modestie.modestieapp.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.EventPrice;
import com.modestie.modestieapp.model.item.LightItem;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EventPriceEditDialogFragment extends DialogFragment
{
    public static final String TAG = "DFRAG.PRICEEDIT";

    private View rootview;
    private Toolbar toolbar;

    private ImageView priceIcon;
    private TextInputLayout itemName;
    private TextInputLayout itemQuantity;

    private EventPrice price;
    private EventPrice dumpGilsPrice;
    private EventPrice dumpItemPrice;
    private String tempIconURL;
    private int position;
    private OnFragmentInteractionListener callback;

    public EventPriceEditDialogFragment()
    {
        // Required empty public constructor
    }

    public static EventPriceEditDialogFragment display(FragmentManager fragmentManager, Bundle attrs)
    {
        EventPriceEditDialogFragment dialog = new EventPriceEditDialogFragment();
        dialog.setArguments(attrs);
        dialog.show(fragmentManager, TAG);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_ModestieTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_event_price_edit, container, false);

        this.toolbar = rootView.findViewById(R.id.toolbar);

        assert this.getArguments() != null;
        this.price = new EventPrice(this.getArguments());
        this.position = this.getArguments().getInt("position") + 1;

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if(this.position == 1) this.toolbar.setTitle(this.position + "er prix");
        else this.toolbar.setTitle(this.position + "ème prix");

        this.toolbar.setNavigationOnClickListener(v -> dismiss());
        this.toolbar.setOnMenuItemClickListener(item ->
            {
                if(!this.itemQuantity.getEditText().getText().toString().equals(""))
                {
                    this.itemQuantity.setError("");
                    //callback.onFragmentInteraction(); TODO
                    dismiss();
                    return true;
                }
                else
                {
                    this.itemQuantity.setError("Requis");

                    return false;
                }
            });
        this.toolbar.inflateMenu(R.menu.edit_price_dialog_menu);

        this.rootview = view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }

        /* INIT */

        RadioGroup selectPriceType = this.rootview.findViewById(R.id.selectPriceTypeGroup);
        this.priceIcon = this.rootview.findViewById(R.id.priceIcon);
        this.itemName = this.rootview.findViewById(R.id.fieldItemName);
        this.itemQuantity = this.rootview.findViewById(R.id.fieldItemQuantity);

        //Init dump prices + radiogroup
        this.dumpGilsPrice = new EventPrice(0, 0, 1, "Gil", "https://xivapi.com/i/065000/065002.png", 100000);
        this.dumpItemPrice = new EventPrice(0, 0, 2, "Éclat de feu", "https://xivapi.com/i/020000/020001.png", 1);
        if(this.price.getItemID() == 1)
        {
            selectPriceType.check(R.id.selectGilsType);
            this.dumpGilsPrice = this.price;
            this.itemName.setEnabled(false);
        }
        else
        {
            selectPriceType.check(R.id.selectItemType);
            this.dumpItemPrice = this.price;
            this.tempIconURL = this.price.getItemIconURL();
        }

        //Init attributes
        Objects.requireNonNull(this.itemName.getEditText()).setText(this.price.getItemName());
        Picasso.get().load(this.price.getItemIconURL()).fit().centerInside().into(this.priceIcon);
        Objects.requireNonNull(this.itemQuantity.getEditText()).setText(this.price.getAmount()+"");

        /* LISTENERS */

        selectPriceType.setOnCheckedChangeListener((group, checkedId) ->
        {
            EventPrice priceToLoad = null;
            //Save and load
            switch(checkedId)
            {
                case R.id.selectGilsType :
                    //Save item price
                    this.itemName.setEnabled(false);
                    //Load gils price
                    priceToLoad = this.dumpGilsPrice;
                    break;

                case R.id.selectItemType :
                    //Save gils price
                    this.dumpGilsPrice.setAmount(Integer.parseInt(this.itemQuantity.getEditText().getText() + ""));
                    this.itemName.setEnabled(true);
                    //Load item price
                    priceToLoad = this.dumpItemPrice;
                    break;
            }
            //Load price
            if(priceToLoad != null)
            {
                this.itemName.getEditText().setText(priceToLoad.getItemName());
                Picasso.get().load(priceToLoad.getItemIconURL()).fit().centerInside().into(this.priceIcon);
                this.itemQuantity.getEditText().setText(priceToLoad.getAmount()+"");
            }
        });

        Objects.requireNonNull(this.itemName.getEditText()).setOnFocusChangeListener((v, hasFocus) ->
        {
            if(hasFocus)
            {
                ItemSelectionDialogFragment.display(((AppCompatActivity) Objects.requireNonNull(getContext())).getSupportFragmentManager());
                this.itemName.clearFocus();
            }
        });
    }

    public void updatePriceItem(LightItem item)
    {
        this.itemName.getEditText().setText(item.itemName);
        Picasso.get().load(item.iconURL).fit().centerInside().into(this.priceIcon);
        this.dumpItemPrice.setItemID(item.itemID);
        this.dumpItemPrice.setItemName(item.itemName);
        this.dumpItemPrice.setItemIconURL(item.iconURL);
        this.tempIconURL = this.price.getItemIconURL();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener callback)
    {
        this.callback = callback;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(EventPrice editedPrice, int position);
    }
}
