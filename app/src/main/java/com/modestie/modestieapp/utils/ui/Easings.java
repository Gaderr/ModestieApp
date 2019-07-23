package com.modestie.modestieapp.utils.ui;

import android.view.animation.PathInterpolator;

public abstract class Easings
{
    public static final PathInterpolator SIN_IN         = new PathInterpolator(.47f, 0f, .745f, .715f);
    public static final PathInterpolator SIN_OUT        = new PathInterpolator(.39f, .575f, .565f, 1f);
    public static final PathInterpolator SIN_IN_OUT     = new PathInterpolator(.445f, .05f, .55f, .95f);
    public static final PathInterpolator QUAD_IN        = new PathInterpolator(.55f, .085f, .68f, .53f);
    public static final PathInterpolator QUAD_OUT       = new PathInterpolator(.25f, .46f, .45f, .94f);
    public static final PathInterpolator QUAD_IN_OUT    = new PathInterpolator(.455f, .03f, .515f, .955f);
    public static final PathInterpolator CUBIC_IN       = new PathInterpolator(.55f, .055f, .675f, .19f);
    public static final PathInterpolator CUBIC_OUT      = new PathInterpolator(.215f, .61f, .355f, 1f);
    public static final PathInterpolator CUBIC_IN_OUT   = new PathInterpolator(.645f, .045f, .355f, 1f);
    public static final PathInterpolator QUART_IN       = new PathInterpolator(.895f, .03f, .685f, .22f);
    public static final PathInterpolator QUART_OUT      = new PathInterpolator(.165f, .84f, .44f, 1f);
    public static final PathInterpolator QUART_IN_OUT   = new PathInterpolator(.77f, 0f, .175f, 1f);
    public static final PathInterpolator QUINT_IN       = new PathInterpolator(.755f, .05f, .855f, .06f);
    public static final PathInterpolator QUINT_OUT      = new PathInterpolator(.23f, 1f, .32f, 1f);
    public static final PathInterpolator QUINT_IN_OUT   = new PathInterpolator(.86f, 0f, .07f, 1f);
    public static final PathInterpolator EXP_IN         = new PathInterpolator(.95f, .05f, .795f, .035f);
    public static final PathInterpolator EXP_OUT        = new PathInterpolator(.19f, 1f, .22f, 1f);
    public static final PathInterpolator EXP_IN_OUT     = new PathInterpolator(1f, 0f, 0f, 1f);
    public static final PathInterpolator CIRC_IN        = new PathInterpolator(.6f, .04f, .98f, .335f);
    public static final PathInterpolator CIRC_OUT       = new PathInterpolator(.075f, .82f, .165f, 1f);
    public static final PathInterpolator CIRC_IN_OUT    = new PathInterpolator(.785f, .135f, .15f, .86f);
    public static final PathInterpolator BACK_IN        = new PathInterpolator(.6f, -.28f, .735f, .045f);
    public static final PathInterpolator BACK_OUT       = new PathInterpolator(.175f, .885f, .32f, 1.275f);
    public static final PathInterpolator BACK_IN_OUT    = new PathInterpolator(.68f, -.55f, .265f, 1.55f);
}
