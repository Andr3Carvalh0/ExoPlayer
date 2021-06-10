package com.google.android.exoplayer2.ui.views;

import com.google.android.exoplayer2.ui.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import nl.kpn.components.utilities.AnimationsKt;

/**
 * Created by André Carvalho on 2019-09-04
 */
public class PlaybackButton extends ConstraintLayout {

	protected ConstraintLayout root;
	protected ConstraintLayout back;
	protected AppCompatImageView icon;
	private int activeDrawable = 0;
	private int inactiveDrawable = 0;
	private boolean isActive = false;

	public int layout() {
		return R.layout.action_button;
	}

	public PlaybackButton(Context context) { this(context, null); }
	public PlaybackButton(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
	public PlaybackButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View root = LayoutInflater.from(context).inflate(layout(), this, true);

		this.root = root.findViewById(R.id.buttonRoot);
		icon = root.findViewById(R.id.icon);
		back = root.findViewById(R.id.buttonBackground);

		if (attrs != null) {
			TypedArray att = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlaybackButton, 0, 0);

			activeDrawable = att.getResourceId(R.styleable.PlaybackButton_active_drawable, R.drawable.icon_play);
			inactiveDrawable = att.getResourceId(R.styleable.PlaybackButton_inactive_drawable, R.drawable.icon_pause);

			if (att.getDimension(R.styleable.PlaybackButton_override_size, -1) != -1) {
				ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();

				layoutParams.height = (int) att.getDimension(R.styleable.PlaybackButton_override_size, -1);
				layoutParams.width = (int) att.getDimension(R.styleable.PlaybackButton_override_size, -1);

				icon.setLayoutParams(layoutParams);
			}

			state(true);
			att.recycle();
		}
	}


	public boolean isActive() {
		return isActive;
	}

	public void state(boolean active) {
		this.isActive = active;
		icon.setImageResource(active ? activeDrawable : inactiveDrawable);
	}
}
