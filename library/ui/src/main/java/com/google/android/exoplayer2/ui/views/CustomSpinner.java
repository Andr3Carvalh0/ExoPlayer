package com.google.android.exoplayer2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

/**
 * Created by André Carvalho on 2019-09-04
 */
public final class CustomSpinner extends ProgressBar {

	public CustomSpinner(Context context) { this(context, null); }
	public CustomSpinner(Context context, AttributeSet attrs) { this(context, attrs, android.R.attr.progressBarStyle); }
	public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	public void setColor(int color) {
		getIndeterminateDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
	}
}
