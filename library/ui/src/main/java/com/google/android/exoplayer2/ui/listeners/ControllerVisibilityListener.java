package com.google.android.exoplayer2.ui.listeners;


/**
 * Created by André Carvalho on 2019-09-07
 */
public interface ControllerVisibilityListener {
	public void onAnimationStarted(boolean fadeIn);
	public void onAnimationEnded(boolean fadeIn);
	public void onAnimationProgress(float newValue);
}
