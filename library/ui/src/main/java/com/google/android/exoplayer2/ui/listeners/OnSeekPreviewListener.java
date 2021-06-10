package com.google.android.exoplayer2.ui.listeners;


/**
 * Created by André Carvalho on 2019-09-07
 */
public interface OnSeekPreviewListener {
	public boolean allowSeeking(long position);
	public void onBlockedSeeking();
}
