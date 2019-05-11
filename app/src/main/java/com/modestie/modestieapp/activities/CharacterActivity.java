package com.modestie.modestieapp.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class CharacterActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.TRANSPARENT)
                .borderWidthDp(0)
                .cornerRadiusDp(5)
                .oval(false)
                .build();

        Picasso.get()
                .load(R.drawable.portrait_argus)
                .fit()
                .centerCrop()
                .transform(transformation)
                .into((RoundedImageView) findViewById(R.id.portrait));
    }
}
