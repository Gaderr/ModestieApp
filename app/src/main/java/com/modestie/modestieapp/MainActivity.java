package com.modestie.modestieapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private ConstraintLayout layout;

    private AnimatorSet animatorSetIn;
    private ObjectAnimator animationTextUp;
    private ObjectAnimator animationCrestDown;
    private ObjectAnimator animationTextFadeIn;
    private ObjectAnimator animationCrestFadeIn;

    private ImageView crestView;
    private TextView appNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.mainActivityLayout);

        layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

        this.crestView = findViewById(R.id.crest);
        this.appNameView = findViewById(R.id.textAppName);

        this.animatorSetIn = new AnimatorSet();

        this.animationTextUp = ObjectAnimator.ofFloat(this.appNameView, "translationY", -50f)
                .setDuration(1750);
        this.animationTextFadeIn = ObjectAnimator.ofFloat(this.appNameView, "alpha", 0f, 1f)
                .setDuration(1750);

        this.animationCrestDown = ObjectAnimator.ofFloat(this.crestView, "translationY", +50f)
                .setDuration(1750);
        this.animationCrestFadeIn = ObjectAnimator.ofFloat(this.crestView, "alpha", 0f, 1f)
                .setDuration(1750);

        this.appNameView.setAlpha(0f);
        this.appNameView.setTranslationY(+100f);

        this.crestView.setAlpha(0f);
        this.crestView.setTranslationY(-100f);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        this.animatorSetIn
                .play(this.animationTextUp)
                .with(this.animationTextFadeIn)
                .with(this.animationCrestDown)
                .with(this.animationCrestFadeIn);
        this.animatorSetIn.start();
    }
}
