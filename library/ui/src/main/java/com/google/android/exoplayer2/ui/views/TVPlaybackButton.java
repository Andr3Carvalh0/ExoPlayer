package com.google.android.exoplayer2.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;

import com.google.android.exoplayer2.ui.R;

import nl.kpn.components.utilities.AnimationsKt;

/**
 * Created by André Carvalho on 2019-09-04
 */
public class TVPlaybackButton extends PlaybackButton {

	@Override
	public int layout() {
		return R.layout.action_tv_button;
	}

	public TVPlaybackButton(Context context) { this(context, null); }
	public TVPlaybackButton(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
	public TVPlaybackButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		this.setOnFocusChangeListener((v, hasFocus) -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(context.getColor(hasFocus ? R.color.background_color : R.color.subtle_grey)));
				back.setBackgroundResource(hasFocus ? R.drawable.b_background_inactive : R.drawable.b_background);
			}
		});

		root.setOnFocusChangeListener((v, hasFocus) -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(context.getColor(hasFocus ? R.color.background_color : R.color.subtle_grey)));
				back.setBackgroundResource(hasFocus ? R.drawable.b_background_inactive : R.drawable.b_background);
			}
		});

		icon.setOnFocusChangeListener((v, hasFocus) -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(context.getColor(hasFocus ? R.color.background_color : R.color.subtle_grey)));
				back.setBackgroundResource(hasFocus ? R.drawable.b_background_inactive : R.drawable.b_background);
			}
		});

		back.setOnFocusChangeListener((v, hasFocus) -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(context.getColor(hasFocus ? R.color.background_color : R.color.subtle_grey)));
				back.setBackgroundResource(hasFocus ? R.drawable.b_background_inactive : R.drawable.b_background);
			}
		});
	}
}
