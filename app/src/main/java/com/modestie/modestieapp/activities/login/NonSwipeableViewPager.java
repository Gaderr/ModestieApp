package com.modestie.modestieapp.activities.login;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.modestie.modestieapp.utils.ui.Easings;

import java.lang.reflect.Field;

public class NonSwipeableViewPager extends ViewPager
{

    public NonSwipeableViewPager(@NonNull Context context)
    {
        super(context);
        setScroller();
    }

    public NonSwipeableViewPager(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        setScroller();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean executeKeyEvent(@NonNull KeyEvent event)
    {
        // Disable swipe with keyboard keys
        return false;
    }

    private void setScroller()
    {
        try
        {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class MyScroller extends Scroller
    {
        public MyScroller(Context context)
        {
            super(context, Easings.CUBIC_IN_OUT);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration)
        {
            super.startScroll(startX, startY, dx, dy, 750);
        }
    }
}
