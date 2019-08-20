package com.modestie.modestieapp.activities.events.form;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.DraggableEventPriceAdapter;

public class EventPriceOptionsModal extends BottomSheetDialogFragment
{
    private DraggableEventPriceAdapter adapter;
    private Context context;
    private int itemPosition;

    private static final String TAG = "ACTVT.EVNTPRCEOPTMODAL";

    public EventPriceOptionsModal(DraggableEventPriceAdapter adapter, Context context, int position)
    {
        this.adapter = adapter;
        this.context = context;
        this.itemPosition = position;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.event_price_options_modal_layout, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance)
    {
        ConstraintLayout editLayout = view.findViewById(R.id.optionEdit);
        ConstraintLayout deleteLayout = view.findViewById(R.id.optionDelete);

        editLayout.setOnClickListener(v ->
            {
                //Log.e(TAG, itemPosition+"");
                Bundle bundle = adapter.dataset.get(itemPosition).second.toBundle();
                bundle.putInt("position", itemPosition);
                //((EventFormActivity) getContext()).editPriceFragment(bundle);
                EventPriceEditDialogFragment.display(((AppCompatActivity) getContext()).getSupportFragmentManager(), bundle);
                dismiss();
            });

        deleteLayout.setOnClickListener(v ->
            {
                //Log.e("MODAL", "REMOVED");
                adapter.dataset.remove(itemPosition);
                adapter.notifyDataSetChanged();
                dismiss();
            });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
    }
}