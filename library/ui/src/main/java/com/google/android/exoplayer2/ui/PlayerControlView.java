/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Looper;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.listeners.AnaliticsListener;
import com.google.android.exoplayer2.ui.listeners.ControllerVisibilityListener;
import com.google.android.exoplayer2.ui.listeners.FullscreenListener;
import com.google.android.exoplayer2.ui.listeners.OnPlayerReady;
import com.google.android.exoplayer2.ui.listeners.OnSeekPreviewListener;
import com.google.android.exoplayer2.ui.listeners.OnLivePlayerListener;
import com.google.android.exoplayer2.ui.listeners.SwitchItemsListener;
import com.google.android.exoplayer2.ui.miniContent.decoration.ItemDecoration;
import com.google.android.exoplayer2.ui.utilities.DimensionsExtensionsKt;
import nl.kpn.components.chips.FilterBarItem;
import nl.kpn.components.utilities.overscroll.OverScrollDecoratorHelperX;

import com.google.android.exoplayer2.ui.views.MetadataView;
import com.google.android.exoplayer2.ui.views.PlaybackButton;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;
import static java.lang.Math.abs;

/**
 * A view for controlling {@link Player} instances.
 *
 * <p>A PlayerControlView can be customized by setting attributes (or calling corresponding
 * methods), overriding the view's layout file or by specifying a custom view layout file, as
 * outlined below.
 *
 * <h3>Attributes</h3>
 *
 * The following attributes can be set on a PlayerControlView when used in a layout XML file:
 *
 * <ul>
 *   <li><b>{@code show_timeout}</b> - The time between the last user interaction and the controls
 *       being automatically hidden, in milliseconds. Use zero if the controls should not
 *       automatically timeout.
 *       <ul>
 *         <li>Corresponding method: {@link #setShowTimeoutMs(int)}
 *         <li>Default: {@link #DEFAULT_SHOW_TIMEOUT_MS}
 *       </ul>
 *   <li><b>{@code rewind_increment}</b> - The duration of the rewind applied when the user taps the
 *       rewind button, in milliseconds. Use zero to disable the rewind button.
 *       <ul>
 *         <li>Corresponding method: {@link #setRewindIncrementMs(int)}
 *         <li>Default: {@link #DEFAULT_REWIND_MS}
 *       </ul>
 *   <li><b>{@code fastforward_increment}</b> - Like {@code rewind_increment}, but for fast forward.
 *       <ul>
 *         <li>Corresponding method: {@link #setFastForwardIncrementMs(int)}
 *         <li>Default: {@link #DEFAULT_FAST_FORWARD_MS}
 *       </ul>
 *   <li><b>{@code time_bar_min_update_interval}</b> - Specifies the minimum interval between time
 *       bar position updates.
 *       <ul>
 *         <li>Corresponding method: {@link #setTimeBarMinUpdateInterval(int)}
 *         <li>Default: {@link #DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS}
 *       </ul>
 *   <li><b>{@code controller_layout_id}</b> - Specifies the id of the layout to be inflated. See
 *       below for more details.
 *       <ul>
 *         <li>Corresponding method: None
 *         <li>Default: {@code R.layout.exo_player_control_view}
 *       </ul>
 *   <li>All attributes that can be set on {@link DefaultTimeBar} can also be set on a
 *       PlayerControlView, and will be propagated to the inflated {@link DefaultTimeBar} unless the
 *       layout is overridden to specify a custom {@code exo_progress} (see below).
 * </ul>
 *
 * <h3>Overriding the layout file</h3>
 *
 * To customize the layout of PlayerControlView throughout your app, or just for certain
 * configurations, you can define {@code exo_player_control_view.xml} layout files in your
 * application {@code res/layout*} directories. These layouts will override the one provided by the
 * ExoPlayer library, and will be inflated for use by PlayerControlView. The view identifies and
 * binds its children by looking for the following ids:
 *
 * <p>
 *
 * <ul>
 *   <li><b>{@code exo_play}</b> - The play button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_pause}</b> - The pause button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_ffwd}</b> - The fast forward button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_rew}</b> - The rewind button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_prev}</b> - The previous track button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_next}</b> - The next track button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_repeat_toggle}</b> - The repeat toggle button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_shuffle}</b> - The shuffle button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_vr}</b> - The VR mode button.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_position}</b> - Text view displaying the current playback position.
 *       <ul>
 *         <li>Type: {@link TextView}
 *       </ul>
 *   <li><b>{@code exo_duration}</b> - Text view displaying the current media duration.
 *       <ul>
 *         <li>Type: {@link TextView}
 *       </ul>
 *   <li><b>{@code exo_progress_placeholder}</b> - A placeholder that's replaced with the inflated
 *       {@link DefaultTimeBar}. Ignored if an {@code exo_progress} view exists.
 *       <ul>
 *         <li>Type: {@link View}
 *       </ul>
 *   <li><b>{@code exo_progress}</b> - Time bar that's updated during playback and allows seeking.
 *       {@link DefaultTimeBar} attributes set on the PlayerControlView will not be automatically
 *       propagated through to this instance. If a view exists with this id, any {@code
 *       exo_progress_placeholder} view will be ignored.
 *       <ul>
 *         <li>Type: {@link TimeBar}
 *       </ul>
 * </ul>
 *
 * <p>All child views are optional and so can be omitted if not required, however where defined they
 * must be of the expected type.
 *
 * <h3>Specifying a custom layout file</h3>
 *
 * Defining your own {@code exo_player_control_view.xml} is useful to customize the layout of
 * PlayerControlView throughout your application. It's also possible to customize the layout for a
 * single instance in a layout file. This is achieved by setting the {@code controller_layout_id}
 * attribute on a PlayerControlView. This will cause the specified layout to be inflated instead of
 * {@code exo_player_control_view.xml} for only the instance on which the attribute is set.
 */
public class PlayerControlView extends FrameLayout {

  static { ExoPlayerLibraryInfo.registerModule("goog.exo.ui"); }

  private FullscreenListener fullscreenListener;
  private ControllerVisibilityListener visibilityListener;
  private SwitchItemsListener switchItemsListener;
  private OnPlayerReady onPlayerReady;
  private LinkedList<AnaliticsListener> skippableListener = new LinkedList();
  private OnSeekPreviewListener seekListener;
  private OnLivePlayerListener onStartOverListener;
  private int liveColor = Color.GREEN;

  private Function1<Unit, Unit> onMoreContentToggleEvent = unit -> Unit.INSTANCE;

  public void setOnPlayerReady(OnPlayerReady onPlayerReady) {
    this.onPlayerReady = onPlayerReady;
  }

  public void setSwitchItemsListener(SwitchItemsListener listener) {
    this.switchItemsListener = listener;
  }

  public void setFullscreenListener(FullscreenListener fullscreenListener, boolean inFullscreen) {
    this.fullscreenListener = fullscreenListener;
    screenSize.setImageResource(inFullscreen ? R.drawable.icon_fullscreen_exit : R.drawable.icon_fullscreen);
  }

  public void setVisibilityListener(ControllerVisibilityListener visibilityListener) {
    this.visibilityListener = visibilityListener;
  }

  public boolean isMoreContentVisible() {
    return bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
  }

  public void hideFullscreenIcon() {
    screenSize.setVisibility(View.GONE);
  }

  private void fixLeftCutout(DisplayCutoutCompat displayCutout) {
    Rect rect = getLeftRectangle();
    int guide = 0;
    for (Rect displayCutoutRect: displayCutout.getBoundingRects()) {
      if (Rect.intersects(rect, displayCutoutRect)) {
        guide = displayCutoutRect.right - displayCutoutRect.left;
      }
    }
    Guideline guideline = findViewById(R.id.notch_guideline_start);
    ConstraintLayout.LayoutParams paramsGuideline = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
    paramsGuideline.guideBegin = guide;
    guideline.setLayoutParams(paramsGuideline);
  }

  private void fixRightCutout(DisplayCutoutCompat displayCutout) {
    Rect rect = getRightRectangle();
    int guide = 0;
    for (Rect displayCutoutRect: displayCutout.getBoundingRects()) {
      if (Rect.intersects(rect, displayCutoutRect)) {
        guide = displayCutoutRect.right - displayCutoutRect.left;
      }
    }
    Guideline guideline = findViewById(R.id.notch_guideline_end);
    ConstraintLayout.LayoutParams paramsGuideline = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
    paramsGuideline.guideEnd = guide;
    guideline.setLayoutParams(paramsGuideline);
  }

  private Rect getLeftRectangle() {
    Rect metadataRect = getViewRectangle(metadataView);
    Rect currentRect = getViewRectangle(current);
    metadataRect.union(currentRect);
    return metadataRect;
  }

  private Rect getRightRectangle() {
    Rect durationRect = getViewRectangle(durationView);
    if (screenSize.getVisibility() == VISIBLE) {
      durationRect.union(getViewRectangle(screenSize));
    }
    durationRect.union(getViewRectangle(live));
    return durationRect;
  }

  private Rect getViewRectangle(View view) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    int left = location[0];
    int top = location[1];
    int right = left + view.getWidth();
    int bottom = top + view.getHeight();
    return new Rect(left, top, right, bottom);
  }

  public void setSkippableListener(AnaliticsListener listener) {
    this.skippableListener.add(listener);
  }

  public void removeSkippableListener(AnaliticsListener listener) {
    this.skippableListener.remove(listener);
  }


  public void setSeekListener(OnSeekPreviewListener listener) {
    this.seekListener = listener;
  }

  public void setProgressColor(int color) {
    ((DefaultTimeBar)timeBar).setPlayedColor(color);
    liveColor = color;
  }

  public boolean ignoreTouch() {
    return hasMiniContent && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
  }

  /** Listener to be notified when progress has been updated. */
  public interface ProgressUpdateListener {

    /**
     * Called when progress needs to be updated.
     *
     * @param position The current position.
     * @param bufferedPosition The current buffered position.
     */
    void onProgressUpdate(long position, long bufferedPosition);
  }

  public void onSwipeUp() {
    if (hasMiniContent) {
      removeCallbacks(hideAction);
      hideAtMs = C.TIME_UNSET;
      if (!isVisible())
        fade(true, () -> handleMoreContentState(BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_EXPANDED), FADE_CAROUSEL_ANIMATION_INTERVAL);
      else
        handleMoreContentState(BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  public void onSwipeDown() {
    if (hasMiniContent) { handleMoreContentState(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED); }
  }

  public void toggleContentSheet(Function1<Unit, Unit> onEnd) {
    onMoreContentToggleEvent = onEnd;

    if (hasContentSheet() && bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
      onSwipeDown();
    } else {
      onSwipeUp();
    }
  }

  private void handleMoreContentState(int predicate, int newState) {
    if (hasContentSheet() && bottomSheetBehavior != null && bottomSheetBehavior.getState() == predicate) bottomSheetBehavior.setState(newState);
  }

  private boolean hasContentSheet() {
    return moreContentSheet != null;
  }

  public void setMiniContentList(RecyclerView.Adapter adapter, int scrollPosition, boolean isCircular) {
    if (moreContentList != null) {
      showMiniContentList();

      LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

      moreContentList.setAdapter(adapter);
      moreContentList.setLayoutManager(lm);

      if (!isCircular) { OverScrollDecoratorHelperX.setUpOverScroll(moreContentList, OverScrollDecoratorHelperX.ORIENTATION_HORIZONTAL); }
      lm.scrollToPositionWithOffset(scrollPosition, 0);
    }
  }

  public void hideMiniContent() {
    if (moreContentList != null) {
      hideMiniContentList();
      moreContentList.setAdapter(null);
    }
  }

  public void setMiniContentList(RecyclerView.Adapter adapter, int scrollPosition, boolean isCircular, String title, Function1<Unit, Unit> onTitleClick) {
    setMiniContentList(adapter, scrollPosition, isCircular);

    if (seasonPicker != null) {
    	seasonPicker.setVisibility(View.VISIBLE);
        seasonPicker.hideLeftIcon();
    	seasonPicker.text(title);

    	if (onTitleClick != null) {
		    seasonPicker.setOnClickListener(v -> onTitleClick.invoke(Unit.INSTANCE));
		    seasonPicker.showRightIcon();
	    } else {
    		seasonPicker.hideRightIcon();
	    }
    }
  }

  /** The default fast forward increment, in milliseconds. */
  public static final int DEFAULT_FAST_FORWARD_MS = 30000;
  /** The default rewind increment, in milliseconds. */
  public static final int DEFAULT_REWIND_MS = 30000;
  /** The default show timeout, in milliseconds. */
  public static final int DEFAULT_SHOW_TIMEOUT_MS = 3000;

  public static final int DEFAULT_HIDE_CAROUSEL_MS = 5000;

  // The stream has a default 15 secs delay from the "realtime"
  // So the offset is 5 secs from it.
  public static final int DEFAULT_LIVE_OFFSET_MS = 20000;

  /** The default minimum interval between time bar position updates. */
  public static final int DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200;
  /** The maximum number of windows that can be shown in a multi-window time bar. */
  public static final int MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100;

  private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;
  /** The maximum interval between time bar position updates. */
  private static final int MAX_UPDATE_INTERVAL_MS = 1000;

  private static final int FADE_ANIMATION_INTERVAL = 350;
  private static final int FADE_CAROUSEL_ANIMATION_INTERVAL = 75;

  private static final int DEFAULT_MARGIN_BOTTOM = 16;
  private static final int MORE_CONTENT_MARGIN_BOTTOM = 36;

  private final ComponentListener componentListener;
  private final View previousButton;
  private final View nextButton;
  private final PlaybackButton playback;
  private final PlaybackButton thirtySecondsBack;
  private final PlaybackButton thirtySecondsForward;
  private final MetadataView metadataView;
  private final LinearLayoutCompat current;
  private final AppCompatImageView startOver;
  private final TextView durationView;
  private final TextView positionView;

  private final LinearLayoutCompat live;
  private final AppCompatImageView liveIcon;
  private final AppCompatTextView liveText;
  private final AppCompatImageView screenSize;
  private final TimeBar timeBar;
  private final StringBuilder formatBuilder;
  private final Formatter formatter;
  private final Timeline.Period period;
  private final Timeline.Window window;
  private final Runnable updateProgressAction;
  private final Runnable hideAction;
  private final Runnable hideCarousel;
  private final LinearLayoutCompat moreContentSheet;
  private final ConstraintLayout rootView;

  private boolean isFading = false;

  @Nullable private Player player;
  private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;
  @Nullable private ProgressUpdateListener progressUpdateListener;
  @Nullable private PlaybackPreparer playbackPreparer;
  @Nullable private GestureDetectorCompat swipeGestureDetector;
  @Nullable private BottomSheetBehavior bottomSheetBehavior;
  @Nullable private RecyclerView moreContentList;
  @Nullable private FilterBarItem seasonPicker;

  private boolean isAttachedToWindow;
  private boolean multiWindowTimeBar;
  private boolean scrubbing;
  private int rewindMs;
  private int fastForwardMs;
  private int showTimeoutMs;
  private int timeBarMinUpdateIntervalMs;
  private long hideAtMs;
  private long[] adGroupTimesMs;
  private boolean[] playedAdGroups;
  private long[] extraAdGroupTimesMs;
  private boolean[] extraPlayedAdGroups;
  private long currentWindowOffset;
  private boolean hasMiniContent = false;
  private boolean hasStartOverRights = false;

  public PlayerControlView(Context context) {
    this(context, /* attrs= */ null);
  }

  public PlayerControlView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, attrs);
  }

  protected LinkedList<View> carouselItems() {
    return new LinkedList<>(Arrays.asList(new View[]{durationView, positionView, metadataView, playback, ((View) timeBar), thirtySecondsBack,
            thirtySecondsForward}));
  }

  protected int layoutID() {
    return R.layout.matcha_player_control_view;
  }

  public PlayerControlView(
      Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      @Nullable AttributeSet playbackAttrs) {
    super(context, attrs, defStyleAttr);
    int controllerLayoutId = layoutID();
    rewindMs = DEFAULT_REWIND_MS;
    fastForwardMs = DEFAULT_FAST_FORWARD_MS;
    showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
    timeBarMinUpdateIntervalMs = DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS;
    hideAtMs = C.TIME_UNSET;
    if (playbackAttrs != null) {
      TypedArray a =
          context
              .getTheme()
              .obtainStyledAttributes(playbackAttrs, R.styleable.PlayerControlView, 0, 0);
      try {
        rewindMs = a.getInt(R.styleable.PlayerControlView_rewind_increment, rewindMs);
        fastForwardMs = a.getInt(R.styleable.PlayerControlView_fastforward_increment, fastForwardMs);
        showTimeoutMs = a.getInt(R.styleable.PlayerControlView_show_timeout, showTimeoutMs);

        setTimeBarMinUpdateInterval(
            a.getInt(
                R.styleable.PlayerControlView_time_bar_min_update_interval,
                timeBarMinUpdateIntervalMs));
      } finally {
        a.recycle();
      }
    }
    period = new Timeline.Period();
    window = new Timeline.Window();
    formatBuilder = new StringBuilder();
    formatter = new Formatter(formatBuilder, Locale.getDefault());
    adGroupTimesMs = new long[0];
    playedAdGroups = new boolean[0];
    extraAdGroupTimesMs = new long[0];
    extraPlayedAdGroups = new boolean[0];
    componentListener = new ComponentListener();
    controlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();
    updateProgressAction = this::updateProgress;
    hideAction = this::hide;
    hideCarousel = this::hideCarousel;

    LayoutInflater.from(context).inflate(controllerLayoutId, this);
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

    timeBar = findViewById(R.id.seekbar);
    metadataView = findViewById(R.id.metadata);
    thirtySecondsBack = findViewById(R.id.backwards);
    moreContentSheet = findViewById(R.id.moreContentSheet);
    rootView = findViewById(R.id.rootView);
    thirtySecondsForward = findViewById(R.id.forward);
    previousButton = findViewById(R.id.previous);
    playback = findViewById(R.id.playback);
    nextButton = findViewById(R.id.next);
    durationView = findViewById(R.id.duration);
    positionView = findViewById(R.id.currentTime);
    screenSize = findViewById(R.id.mode);
    moreContentList = findViewById(R.id.moreContentList);
    seasonPicker = findViewById(R.id.seasonPicker);
    current = findViewById(R.id.current);
    startOver = findViewById(R.id.startover);
    live = findViewById(R.id.live);
    liveIcon = findViewById(R.id.playLiveIcon);
    liveText = findViewById(R.id.playLiveText);

    swipeGestureDetector = new GestureDetectorCompat(context, new GestureDetector.OnGestureListener() {
	    @Override
	    public boolean onDown(MotionEvent e) { return false; }

	    @Override
	    public void onShowPress(MotionEvent e) { }

	    @Override
	    public boolean onSingleTapUp(MotionEvent e) { return false; }

	    @Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }

	    @Override
	    public void onLongPress(MotionEvent e) { }

	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		    float diffY = e2.getY() - e1.getY();
		    float diffX = e2.getX() - e1.getX();

		    return abs(diffX) <= abs(diffY);
	    }
    });

    if (moreContentSheet != null) {
      bottomSheetBehavior = BottomSheetBehavior.from(moreContentSheet);
      bottomSheetBehavior.setGestureInsetBottomIgnored(true);
      bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
          switch (newState) {
            case BottomSheetBehavior.STATE_EXPANDED:
              onHide(false);
              hideCarouselAfterTimeout();

              break;
            case BottomSheetBehavior.STATE_COLLAPSED:
              onShow();
              break;
          }

          if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
            onMoreContentToggleEvent.invoke(Unit.INSTANCE);

            if (newState == BottomSheetBehavior.STATE_EXPANDED) { moreContentList.requestFocus(); }
            onMoreContentToggleEvent = unit -> Unit.INSTANCE;
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
          float alpha = 1.0f - slideOffset;

          LinkedList<View> views = carouselItems();

          if (hasStartOverRights) { views.add(startOver); }
          if (player != null && player.isCurrentWindowDynamic()) {
            views.add(liveIcon);
            views.add(liveText);
          }

          for (View v : views) {
            if (v != null) {
              float calculatedAlpha = alpha;
              boolean isEnabled = alpha > 0.2f;

              if (v == thirtySecondsForward) {
                calculatedAlpha = alpha > 0.3f && !hasThirtySecondsForwardOption() ? 0.3f : alpha;
                isEnabled = !(alpha > 0.3f) || hasThirtySecondsForwardOption();

              } else if (v == thirtySecondsBack) {
                calculatedAlpha = alpha > 0.3f && !hasThirtySecondsBackOption() ? 0.3f : alpha;
                isEnabled = !(alpha > 0.3f) || hasThirtySecondsBackOption();

              }

              v.setAlpha(calculatedAlpha);
              v.setEnabled(isEnabled);

              v.setFocusable(isEnabled);
              v.setFocusableInTouchMode(isEnabled);
            }
          }
        }
      });
    }

    if (thirtySecondsBack != null) thirtySecondsBack.setOnClickListener(componentListener);
    if (thirtySecondsForward != null) thirtySecondsForward.setOnClickListener(componentListener);
    if (timeBar != null) timeBar.addListener(componentListener);
    if (playback != null) playback.setOnClickListener(componentListener);
    if (previousButton != null) previousButton.setOnClickListener(componentListener);
    if (nextButton != null) nextButton.setOnClickListener(componentListener);
    if (screenSize != null) screenSize.setOnClickListener(v -> toggleFullscreen());
    if (moreContentList != null) moreContentList.addItemDecoration(new ItemDecoration());
    if (startOver != null) startOver.setOnClickListener(componentListener);
    if (liveText != null) liveText.setOnClickListener(componentListener);
    if (liveIcon != null) liveIcon.setOnClickListener(componentListener);

    hideMiniContentList();


    if (rootView != null) {
      ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
        if (insets.getDisplayCutout() != null) {
          getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                fixLeftCutout(insets.getDisplayCutout());
                fixRightCutout(insets.getDisplayCutout());
              }
            }
          });
        }
        return insets;
      });
    }
  }

  private void hideMiniContentList() { handleMiniContentListState(DEFAULT_MARGIN_BOTTOM, View.GONE); }
  private void showMiniContentList() { handleMiniContentListState(MORE_CONTENT_MARGIN_BOTTOM, View.VISIBLE); }

  private void handleMiniContentListState(int pixels, int visibility) {
    if (moreContentSheet != null) {
      changeBottomMargin(DimensionsExtensionsKt.getPx(pixels));
    }

    hasMiniContent = visibility != View.GONE;
  }

  protected void changeBottomMargin(int pixels) {
    if (rootView != null) {
      ConstraintSet constraintSet = new ConstraintSet();

      for (int i = 0; i < rootView.getChildCount(); i++) {
        if (rootView.getChildAt(i).getId() == -1) {
          rootView.getChildAt(i).setId(View.generateViewId());
        }
      }

      constraintSet.clone(rootView);
      constraintSet.connect(R.id.current, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, pixels);
      constraintSet.applyTo(rootView);
    }
  }

  public void toggleFullscreen() {
    if (fullscreenListener != null) {
      fullscreenListener.handleConfigurationChange();
      screenSize.setImageResource(fullscreenListener.inFullscreen() ? R.drawable.icon_fullscreen_exit : R.drawable.icon_fullscreen);
    }
  }

  public void metadata(String image, String channelLogo, String channelName, String programTitle, String programDuration) {
    if (metadataView != null) {
        metadataView.metadata(image, channelLogo, channelName, programTitle, programDuration);
    }
  }

  /**
   * Returns the {@link Player} currently being controlled by this view, or null if no player is
   * set.
   */
  @Nullable
  public Player getPlayer() {
    return player;
  }

  /**
   * Sets the {@link Player} to control.
   *
   * @param player The {@link Player} to control, or {@code null} to detach the current player. Only
   *     players which are accessed on the main thread are supported ({@code
   *     player.getApplicationLooper() == Looper.getMainLooper()}).
   */
  public void setPlayer(@Nullable Player player) {
    Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
    Assertions.checkArgument(
        player == null || player.getApplicationLooper() == Looper.getMainLooper());
    if (this.player == player) { return; }
    if (this.player != null) { this.player.removeListener(componentListener); }
    this.player = player;
    if (player != null) { player.addListener(componentListener); }
    updateAll();
  }

  /**
   * Sets the millisecond positions of extra ad markers relative to the start of the window (or
   * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
   * markers are shown in addition to any ad markers for ads in the player's timeline.
   *
   * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
   *     {@code null} to show no extra ad markers.
   * @param extraPlayedAdGroups Whether each ad has been played. Must be the same length as {@code
   *     extraAdGroupTimesMs}, or {@code null} if {@code extraAdGroupTimesMs} is {@code null}.
   */
  public void setExtraAdGroupMarkers(
      @Nullable long[] extraAdGroupTimesMs, @Nullable boolean[] extraPlayedAdGroups) {
    if (extraAdGroupTimesMs == null) {
      this.extraAdGroupTimesMs = new long[0];
      this.extraPlayedAdGroups = new boolean[0];
    } else {
      extraPlayedAdGroups = Assertions.checkNotNull(extraPlayedAdGroups);
      Assertions.checkArgument(extraAdGroupTimesMs.length == extraPlayedAdGroups.length);
      this.extraAdGroupTimesMs = extraAdGroupTimesMs;
      this.extraPlayedAdGroups = extraPlayedAdGroups;
    }
    updateTimeline();
  }

  /**
   * Sets the {@link ProgressUpdateListener}.
   *
   * @param listener The listener to be notified about when progress is updated.
   */
  public void setProgressUpdateListener(@Nullable ProgressUpdateListener listener) {
    this.progressUpdateListener = listener;
  }

  /**
   * Sets the {@link PlaybackPreparer}.
   *
   * @param playbackPreparer The {@link PlaybackPreparer}.
   */
  public void setPlaybackPreparer(@Nullable PlaybackPreparer playbackPreparer) {
    this.playbackPreparer = playbackPreparer;
  }

  /**
   * Sets the {@link com.google.android.exoplayer2.ControlDispatcher}.
   *
   * @param controlDispatcher The {@link com.google.android.exoplayer2.ControlDispatcher}, or null
   *     to use {@link com.google.android.exoplayer2.DefaultControlDispatcher}.
   */
  public void setControlDispatcher(
      @Nullable com.google.android.exoplayer2.ControlDispatcher controlDispatcher) {
    this.controlDispatcher =
        controlDispatcher == null
            ? new com.google.android.exoplayer2.DefaultControlDispatcher()
            : controlDispatcher;
  }

  /**
   * Sets the rewind increment in milliseconds.
   *
   * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
   *     rewind button to be disabled.
   */
  public void setRewindIncrementMs(int rewindMs) {
    this.rewindMs = rewindMs;
    updateNavigation();
  }

  /**
   * Sets the fast forward increment in milliseconds.
   *
   * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
   *     cause the fast forward button to be disabled.
   */
  public void setFastForwardIncrementMs(int fastForwardMs) {
    this.fastForwardMs = fastForwardMs;
    updateNavigation();
  }

  public void hasStartOverRights(boolean value, OnLivePlayerListener listener) {
    this.hasStartOverRights = value;
    this.onStartOverListener = listener;
    updateNavigation();
  }

  /**
   * Returns the playback controls timeout. The playback controls are automatically hidden after
   * this duration of time has elapsed without user input.
   *
   * @return The duration in milliseconds. A non-positive value indicates that the controls will
   *     remain visible indefinitely.
   */
  public int getShowTimeoutMs() {
    return showTimeoutMs;
  }

  /**
   * Sets the playback controls timeout. The playback controls are automatically hidden after this
   * duration of time has elapsed without user input.
   *
   * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
   *     to remain visible indefinitely.
   */
  public void setShowTimeoutMs(int showTimeoutMs) {
    this.showTimeoutMs = showTimeoutMs;
    if (isVisible()) {
      // Reset the timeout.
      hideAfterTimeout();
    }
  }

  /**
   * Sets the minimum interval between time bar position updates.
   *
   * <p>Note that smaller intervals, e.g. 33ms, will result in a smooth movement but will use more
   * CPU resources while the time bar is visible, whereas larger intervals, e.g. 200ms, will result
   * in a step-wise update with less CPU usage.
   *
   * @param minUpdateIntervalMs The minimum interval between time bar position updates, in
   *     milliseconds.
   */
  public void setTimeBarMinUpdateInterval(int minUpdateIntervalMs) {
    // Do not accept values below 16ms (60fps) and larger than the maximum update interval.
    timeBarMinUpdateIntervalMs =
        Util.constrainValue(minUpdateIntervalMs, 16, MAX_UPDATE_INTERVAL_MS);
  }

  /**
   * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
   * be automatically hidden after this duration of time has elapsed without user input.
   */
  public void show() {
    if (!isVisible()) {
      updatePlayPauseButton();
      fade(true, this::onShow, FADE_ANIMATION_INTERVAL);
    }
  }

  private void onShow() {
    updateAll();
    requestPlayPauseFocus();
    hideAfterTimeout();
  }

  private void onHide(boolean hideCarousel) {
    removeCallbacks(updateProgressAction);
    removeCallbacks(hideAction);


    if (bottomSheetBehavior != null) {
      if (hideCarousel && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
      }
    }

    hideAtMs = C.TIME_UNSET;
  }

  /** Hides the controller. */
  public void hide() {
    if (isVisible() && (bottomSheetBehavior == null || bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_SETTLING)) {
      fade(false, () -> {
        setVisibility(GONE);
        onHide(true);
      }, FADE_ANIMATION_INTERVAL);
    }
  }

  private void hideCarousel() {
    if (bottomSheetBehavior != null && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_SETTLING) {
      removeCallbacks(hideCarousel);
      onSwipeDown();
    }
  }

  public void forceHide() {
    if (isVisible() && (bottomSheetBehavior == null || bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_SETTLING)) {
      if (visibilityListener != null) {
        visibilityListener.onAnimationEnded(true);
      }

      setVisibility(View.GONE);
      onHide(true);
    }
  }

  private void fade(boolean out, Runnable onEnd, int duration) {
    if (!isFading) {
      isFading = true;
      ViewPropertyAnimator animation = this.animate();
      animation.setDuration(duration);
      animation.setInterpolator(new FadeInterpolator(out));
      animation.setListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
          if (visibilityListener != null) {
            visibilityListener.onAnimationStarted(!out);
          }

          removeCallbacks(hideAction);
          setAlpha(out ? 1.0f : 0.0f);
          setVisibility(VISIBLE);

          updateAll();
          requestPlayPauseFocus();

        }

        @Override
        public void onAnimationEnd(Animator animation) {
          onEnd.run();

          if (visibilityListener != null) {
            visibilityListener.onAnimationEnded(!out);
          }
          isFading = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) { isFading = false; }

        @Override
        public void onAnimationRepeat(Animator animation) { }
      });

      animation.start();
    }
  }

  /** Returns whether the controller is currently visible. */
  public boolean isVisible() {
    return getVisibility() == VISIBLE;
  }

  private void hideAfterTimeout() {
    removeCallbacks(hideAction);
    if (showTimeoutMs > 0) {
      hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
      if (isAttachedToWindow) {
        postDelayed(hideAction, showTimeoutMs);
      }
    } else {
      hideAtMs = C.TIME_UNSET;
    }
  }

  public void hideCarouselAfterTimeout() {
    removeCallbacks(hideCarousel);
    if (isAttachedToWindow) {
      postDelayed(hideCarousel, DEFAULT_HIDE_CAROUSEL_MS);
    }
  }

  private void updateAll() {
    updatePlayPauseButton();
    updateNavigation();
    updateTimeline();
  }

  private void updatePlayPauseButton() {
    if (!isVisible() || !isAttachedToWindow) {
      return;
    }
    boolean requestPlayPauseFocus = false;

    boolean playing = isPlaying();
    if (playback != null) {
      requestPlayPauseFocus |= !playback.isActive() && playback.isFocused();
      playback.state(playing);
    }

    if (requestPlayPauseFocus) {
      requestPlayPauseFocus();
    }
  }

  private boolean hasThirtySecondsBackOption() {
    if (onStartOverListener != null && !hasStartOverRights && player != null && player.isCurrentWindowDynamic()) {
      long position = currentWindowOffset + player.getContentPosition();
      long calculatedPosition = position - rewindMs;

      return calculatedPosition > onStartOverListener.startOverPosition();
    }

    if (player != null) {
    	Timeline timeline = player.getCurrentTimeline();
    	if (!timeline.isEmpty() && !player.isPlayingAd()) {
    		timeline.getWindow(player.getCurrentWindowIndex(), window);
    		return window.isSeekable && rewindMs > 0;
    	}
    }

    return false;
  }

  private boolean hasThirtySecondsForwardOption() {
    if (seekListener != null && player != null) {
      long position = currentWindowOffset + player.getContentPosition();

      return seekListener.allowSeeking(position + fastForwardMs);
    }

    if (player != null) {
    	Timeline timeline = player.getCurrentTimeline();
    	if (!timeline.isEmpty() && !player.isPlayingAd()) {
    		timeline.getWindow(player.getCurrentWindowIndex(), window);
    		return window.isSeekable && fastForwardMs > 0;
    	}
    }

    return false;
  }

  private void updateNavigation() {
    if (!isVisible() || !isAttachedToWindow) {
      return;
    }
    boolean enableSeeking = false;
    boolean enablePrevious = switchItemsListener != null && switchItemsListener.hasPrevious();
    boolean enableRewind = hasThirtySecondsBackOption();
    boolean enableFastForward = hasThirtySecondsForwardOption();
    boolean enableNext = switchItemsListener != null && switchItemsListener.hasNext();

    if (player != null) {
      Timeline timeline = player.getCurrentTimeline();
      if (!timeline.isEmpty() && !player.isPlayingAd()) {
        timeline.getWindow(player.getCurrentWindowIndex(), window);
        enableSeeking = window.isSeekable;
      }
    }

    setButtonEnabled(enablePrevious, previousButton, true);
    setButtonEnabled(enableNext, nextButton, true);

    if (bottomSheetBehavior != null && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
      setButtonEnabled(enableRewind, thirtySecondsBack, false);
      setButtonEnabled(enableFastForward, thirtySecondsForward, false);
    }

    if (startOver != null) {
      startOver.setVisibility(hasStartOverRights ? VISIBLE : GONE);

      if (positionView != null) {
        LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams) positionView.getLayoutParams();
        params.setMarginStart(DimensionsExtensionsKt.getPx(hasStartOverRights? 16 : 4));

        positionView.setLayoutParams(params);
      }
    }

    if (liveText != null && player != null) {
      liveText.setVisibility(player.isCurrentWindowDynamic() ? VISIBLE : GONE);

    }
    if (liveIcon != null && player != null) {
      liveIcon.setVisibility(player.isCurrentWindowDynamic() ? VISIBLE : GONE);
    }
    if (timeBar != null) { timeBar.setEnabled(enableSeeking); }
  }

  private boolean isOnLiveFrame() {
    if (player != null) {
      return seekListener != null && !seekListener.allowSeeking(currentWindowOffset + player.getContentPosition() + DEFAULT_LIVE_OFFSET_MS);
    }

    return false;
  }

  private void updateTimeline() {
    if (player == null) {
      return;
    }
    multiWindowTimeBar = canShowMultiWindowTimeBar(player.getCurrentTimeline(), window);
    currentWindowOffset = 0;
    long durationUs = 0;
    int adGroupCount = 0;
    Timeline timeline = player.getCurrentTimeline();
    if (!timeline.isEmpty()) {
      int currentWindowIndex = player.getCurrentWindowIndex();
      int firstWindowIndex = multiWindowTimeBar ? 0 : currentWindowIndex;
      int lastWindowIndex = multiWindowTimeBar ? timeline.getWindowCount() - 1 : currentWindowIndex;
      for (int i = firstWindowIndex; i <= lastWindowIndex; i++) {
        if (i == currentWindowIndex) {
          currentWindowOffset = C.usToMs(durationUs);
        }
        timeline.getWindow(i, window);
        if (window.durationUs == C.TIME_UNSET) {
          Assertions.checkState(!multiWindowTimeBar);
          break;
        }
        for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
          timeline.getPeriod(j, period);
          int periodAdGroupCount = period.getAdGroupCount();
          for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount; adGroupIndex++) {
            long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
            if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
              if (period.durationUs == C.TIME_UNSET) {
                // Don't show ad markers for postrolls in periods with unknown duration.
                continue;
              }
              adGroupTimeInPeriodUs = period.durationUs;
            }
            long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period.getPositionInWindowUs();
            if (adGroupTimeInWindowUs >= 0 && adGroupTimeInWindowUs <= window.durationUs) {
              if (adGroupCount == adGroupTimesMs.length) {
                int newLength = adGroupTimesMs.length == 0 ? 1 : adGroupTimesMs.length * 2;
                adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                playedAdGroups = Arrays.copyOf(playedAdGroups, newLength);
              }
              adGroupTimesMs[adGroupCount] = C.usToMs(durationUs + adGroupTimeInWindowUs);
              playedAdGroups[adGroupCount] = period.hasPlayedAdGroup(adGroupIndex);
              adGroupCount++;
            }
          }
        }
        durationUs += window.durationUs;
      }
    }
    long durationMs = C.usToMs(durationUs);

    if (durationView != null) {
      if(onPlayerReady != null) {
        onPlayerReady.onReady();
      }
      durationView.setText(Util.getStringForTime(formatBuilder, formatter, durationMs));
    }
    if (timeBar != null) {
      timeBar.setDuration(durationMs);
      int extraAdGroupCount = extraAdGroupTimesMs.length;
      int totalAdGroupCount = adGroupCount + extraAdGroupCount;
      if (totalAdGroupCount > adGroupTimesMs.length) {
        adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, totalAdGroupCount);
        playedAdGroups = Arrays.copyOf(playedAdGroups, totalAdGroupCount);
      }
      System.arraycopy(extraAdGroupTimesMs, 0, adGroupTimesMs, adGroupCount, extraAdGroupCount);
      System.arraycopy(extraPlayedAdGroups, 0, playedAdGroups, adGroupCount, extraAdGroupCount);
      timeBar.setAdGroupTimesMs(adGroupTimesMs, playedAdGroups, totalAdGroupCount);
    }
    updateProgress();
  }

  protected boolean showRemainingTime() {
    return true;
  }

  private void updateProgress() {
    if (!isVisible() || !isAttachedToWindow) {
      return;
    }

    long position = 0;
    long bufferedPosition = 0;
    if (player != null) {
      position = currentWindowOffset + player.getContentPosition();
      bufferedPosition = currentWindowOffset + player.getContentBufferedPosition();
    }

    if (positionView != null && !scrubbing) { positionView.setText(Util.getStringForTime(formatBuilder, formatter, position)); }
    int color = isOnLiveFrame() ? liveColor : ContextCompat.getColor(getContext(), R.color.subtle_grey);

    if (liveText != null) { liveText.setTextColor(color); }
    if (liveIcon != null) { liveIcon.setColorFilter(color); }

    if (showRemainingTime() && player != null) {
      long time = abs(player.getContentPosition() - player.getContentDuration());

      if (player.getContentDuration() == C.TIME_UNSET) {
        durationView.setText(":D");
      } else {
        durationView.setText(Util.getStringForTime(formatBuilder, formatter, time));
      }
    }

    if (timeBar != null) {
      timeBar.setPosition(position);
      timeBar.setBufferedPosition(bufferedPosition);
    }
    if (progressUpdateListener != null) {
      progressUpdateListener.onProgressUpdate(position, bufferedPosition);
    }

    // Cancel any pending updates and schedule a new one if necessary.
    removeCallbacks(updateProgressAction);
    int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
    if (player != null && player.isPlaying()) {
      long mediaTimeDelayMs =
          timeBar != null ? timeBar.getPreferredUpdateDelay() : MAX_UPDATE_INTERVAL_MS;

      // Limit delay to the start of the next full second to ensure position display is smooth.
      long mediaTimeUntilNextFullSecondMs = 1000 - position % 1000;
      mediaTimeDelayMs = Math.min(mediaTimeDelayMs, mediaTimeUntilNextFullSecondMs);

      // Calculate the delay until the next update in real time, taking playbackSpeed into account.
      float playbackSpeed = player.getPlaybackParameters().speed;
      long delayMs =
          playbackSpeed > 0 ? (long) (mediaTimeDelayMs / playbackSpeed) : MAX_UPDATE_INTERVAL_MS;

      // Constrain the delay to avoid too frequent / infrequent updates.
      delayMs = Util.constrainValue(delayMs, timeBarMinUpdateIntervalMs, MAX_UPDATE_INTERVAL_MS);
      postDelayed(updateProgressAction, delayMs);
    } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
      postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS);
    }
  }

  private void requestPlayPauseFocus() {
    if (playback != null) { playback.requestFocus(); }
  }

  private void setButtonEnabled(boolean enabled, View view, boolean changeVisibility) {
    if (view == null) {
      return;
    }
    view.setEnabled(enabled);
    view.setAlpha(enabled ? 1f : 0.3f);

    if (changeVisibility) {
      view.setVisibility(enabled ? VISIBLE : INVISIBLE);
    }
  }

  private void previous(Player player) {
    Timeline timeline = player.getCurrentTimeline();
    if (timeline.isEmpty() || player.isPlayingAd()) {
      return;
    }
    int windowIndex = player.getCurrentWindowIndex();
    timeline.getWindow(windowIndex, window);
    int previousWindowIndex = player.getPreviousWindowIndex();
    if (previousWindowIndex != C.INDEX_UNSET
        && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
            || (window.isDynamic && !window.isSeekable))) {
      seekTo(player, previousWindowIndex, C.TIME_UNSET);
    } else {
      seekTo(player, windowIndex, /* positionMs= */ 0);
    }
  }

  private void next(Player player) {
    Timeline timeline = player.getCurrentTimeline();
    if (timeline.isEmpty() || player.isPlayingAd()) {
      return;
    }
    int windowIndex = player.getCurrentWindowIndex();
    int nextWindowIndex = player.getNextWindowIndex();
    if (nextWindowIndex != C.INDEX_UNSET) {
      seekTo(player, nextWindowIndex, C.TIME_UNSET);
    } else if (timeline.getWindow(windowIndex, window).isDynamic) {
      seekTo(player, windowIndex, C.TIME_UNSET);
    }
  }

  private void rewind(Player player) {
    if (player.isCurrentWindowSeekable() && rewindMs > 0) {
      seekToOffset(player, -rewindMs);
    }
  }

  private void startOver(Player player) {
    if (player.isCurrentWindowSeekable()) {
      if (onStartOverListener != null) onStartOverListener.onStartOver();
      seekTo(player, player.getCurrentWindowIndex(), 0);
    }
  }

  private void fastForward(Player player) {
    if (player.isCurrentWindowSeekable() && fastForwardMs > 0) {
      seekToOffset(player, fastForwardMs);
    }
  }

  private void seekToOffset(Player player, long offsetMs) {
    long positionMs = player.getCurrentPosition() + offsetMs;
    long durationMs = player.getDuration();
    if (durationMs != C.TIME_UNSET) {
      positionMs = Math.min(positionMs, durationMs);
    }
    positionMs = Math.max(positionMs, 0);
    seekTo(player, player.getCurrentWindowIndex(), positionMs);
  }

  private void seekToTimeBarPosition(Player player, long positionMs) {
    int windowIndex;
    Timeline timeline = player.getCurrentTimeline();
    if (multiWindowTimeBar && !timeline.isEmpty()) {
      int windowCount = timeline.getWindowCount();
      windowIndex = 0;
      while (true) {
        long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
        if (positionMs < windowDurationMs) {
          break;
        } else if (windowIndex == windowCount - 1) {
          // Seeking past the end of the last window should seek to the end of the timeline.
          positionMs = windowDurationMs;
          break;
        }
        positionMs -= windowDurationMs;
        windowIndex++;
      }
    } else {
      windowIndex = player.getCurrentWindowIndex();
    }
    boolean dispatched = seekTo(player, windowIndex, positionMs);
    int color = isOnLiveFrame() ? liveColor : ContextCompat.getColor(getContext(), R.color.subtle_grey);

    if (liveText != null) { liveText.setTextColor(color); }
    if (liveIcon != null) { liveIcon.setColorFilter(color); }

    if (!dispatched) {
      // The seek wasn't dispatched then the progress bar scrubber will be in the wrong position.
      // Trigger a progress update to snap it back.
      updateProgress();
    }
  }

  private boolean seekTo(Player player, int windowIndex, long positionMs) {
    hideAfterTimeout();

  	if (positionMs > player.getCurrentPosition() && seekListener != null && !seekListener.allowSeeking(positionMs)) {
		if (player.isCurrentWindowDynamic()) {
		  positionMs = C.TIME_UNSET;
        } else {
          seekListener.onBlockedSeeking();
          return false;
        }
    }

  	if (onStartOverListener != null) {
  	  if (!hasStartOverRights && onStartOverListener.startOverPosition() > positionMs && player.isCurrentWindowDynamic()) {
  	    positionMs = onStartOverListener.startOverPosition();
      }
    }

    return controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    isAttachedToWindow = true;
    if (hideAtMs != C.TIME_UNSET) {
      long delayMs = hideAtMs - SystemClock.uptimeMillis();
      if (delayMs <= 0) {
        hide();
      } else {
        postDelayed(hideAction, delayMs);
      }
    } else if (isVisible()) {
      hideAfterTimeout();

      if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
        hideCarouselAfterTimeout();
      }
    }

    if (moreContentList != null) {
      RecyclerView.Adapter adapter = moreContentList.getAdapter();
      if (adapter != null) { moreContentList.setAdapter(adapter); }
    }

    updateAll();
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttachedToWindow = false;
    removeCallbacks(updateProgressAction);
    removeCallbacks(hideAction);
    removeCallbacks(hideCarousel);
  }

  @Override
  public final boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      removeCallbacks(hideAction);

      if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
        Rect outRect = new Rect();
        moreContentSheet.getGlobalVisibleRect(outRect);

        if(!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
          onSwipeDown();
        } else {
            if (swipeGestureDetector != null) {
	            if (swipeGestureDetector.onTouchEvent(ev)) { onSwipeDown(); }
	            else { hideCarouselAfterTimeout(); }
            }
        }
      }
    } else if (ev.getAction() == MotionEvent.ACTION_UP) {
      //if (bottomSheetBehavior == null || bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) { hideAfterTimeout(); }
    }

    return super.dispatchTouchEvent(ev);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
  }

  /**
   * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
   * events will be handled.
   *
   * @param event A key event.
   * @return Whether the key event was handled.
   */
  public boolean dispatchMediaKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    if (player == null || !isHandledMediaKey(keyCode)) {
      return false;
    }
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
        fastForward(player);
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
        rewind(player);
      } else if (event.getRepeatCount() == 0) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
            break;
          case KeyEvent.KEYCODE_MEDIA_PLAY:
            controlDispatcher.dispatchSetPlayWhenReady(player, true);
            break;
          case KeyEvent.KEYCODE_MEDIA_PAUSE:
            controlDispatcher.dispatchSetPlayWhenReady(player, false);
            break;
          case KeyEvent.KEYCODE_MEDIA_NEXT:
            if (switchItemsListener != null) {
              switchItemsListener.next();
            }
            break;
          case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            if (switchItemsListener != null) {
              switchItemsListener.previous();
            }
            break;
          default:
            break;
        }
      }
    }
    return true;
  }

  private boolean isPlaying() {
    return player != null
        && player.getPlaybackState() != Player.STATE_ENDED
        && player.getPlaybackState() != Player.STATE_IDLE
        && player.getPlayWhenReady();
  }

  @SuppressLint("InlinedApi")
  private static boolean isHandledMediaKey(int keyCode) {
    return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
        || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
        || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
        || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
        || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
        || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
  }

  /**
   * Returns whether the specified {@code timeline} can be shown on a multi-window time bar.
   *
   * @param timeline The {@link Timeline} to check.
   * @param window A scratch {@link Timeline.Window} instance.
   * @return Whether the specified timeline can be shown on a multi-window time bar.
   */
  private static boolean canShowMultiWindowTimeBar(Timeline timeline, Timeline.Window window) {
    if (timeline.getWindowCount() > MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR) {
      return false;
    }
    int windowCount = timeline.getWindowCount();
    for (int i = 0; i < windowCount; i++) {
      if (timeline.getWindow(i, window).durationUs == C.TIME_UNSET) {
        return false;
      }
    }
    return true;
  }


  private final class ComponentListener
      implements Player.EventListener, TimeBar.OnScrubListener, OnClickListener {

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
      scrubbing = true;
      if (positionView != null) { positionView.setText(Util.getStringForTime(formatBuilder, formatter, position)); }
      int color = isOnLiveFrame() ? liveColor : ContextCompat.getColor(getContext(), R.color.subtle_grey);
      if (liveText != null) { liveText.setTextColor(color); }
      if (liveIcon != null) { liveIcon.setColorFilter(color); }
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
      if (positionView != null) { positionView.setText(Util.getStringForTime(formatBuilder, formatter, position)); }
      int color = isOnLiveFrame() ? liveColor : ContextCompat.getColor(getContext(), R.color.subtle_grey);

      if (liveText != null) { liveText.setTextColor(color); }
      if (liveIcon != null) { liveIcon.setColorFilter(color); }
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
      scrubbing = false;
      if (!canceled && player != null) {
        seekToTimeBarPosition(player, position);
      }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      updatePlayPauseButton();
      updateProgress();
    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
      updateNavigation();
      updateTimeline();
    }

    @Override
    public void onTimelineChanged(
        Timeline timeline, @Nullable Object manifest, @Player.TimelineChangeReason int reason) {
        updateNavigation();
        updateTimeline();
    }

    @Override
    public void onClick(View view) {
        Player player = PlayerControlView.this.player;
        if (player == null) {
          return;
        }
        if (nextButton == view && switchItemsListener != null) {
          switchItemsListener.next();
        } else if (previousButton == view && switchItemsListener != null) {
          switchItemsListener.previous();
        }
        else if (thirtySecondsForward == view) {
          for (AnaliticsListener l : skippableListener) {
            l.on30SecsForw();
          }

          fastForward(player);
        }
        else if (liveIcon == view || liveText == view) {
        	if (player != null) {
        		hideAfterTimeout();
		        player.seekTo(C.TIME_UNSET);
	        }
        }
        else if (thirtySecondsBack == view) {

          for (AnaliticsListener l : skippableListener) {
            l.on30SecsBack();
          }

          rewind(player);
        } else if (startOver == view) {
          startOver(player);
        }
        else if (playback == view) {
          if (isPlaying()) {
            if (player.getPlaybackState() == Player.STATE_IDLE) {
              if (playbackPreparer != null) {
                playbackPreparer.preparePlayback();
              }
            } else if (player.getPlaybackState() == Player.STATE_ENDED) {
              controlDispatcher.dispatchSeekTo(player, player.getCurrentWindowIndex(), C.TIME_UNSET);
            } else if (player.getPlaybackState() == Player.STATE_ENDED) {
              seekTo(player, player.getCurrentWindowIndex(), C.TIME_UNSET);
            }

            for (AnaliticsListener l : skippableListener) {
              l.onPlay();
            }
          } else {
            for (AnaliticsListener l : skippableListener) {
              l.onPause();
            }
          }
          controlDispatcher.dispatchSetPlayWhenReady(player, !isPlaying());
        }
      }
  }

  private class FadeInterpolator extends LinearInterpolator {

    private boolean fadeOut;

    FadeInterpolator(boolean fade) {
      this.fadeOut = fade;
    }

    @Override
    public float getInterpolation(float input) {
      float value = fadeOut ? input : 1.0f - input;
      setAlpha(value);

      if (visibilityListener != null) {
        visibilityListener.onAnimationProgress(value);
      }

      return super.getInterpolation(input);
    }
  }
}
