package com.taobao.openimui.feature.contact.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.openIMUIDemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class PullToRefreshBase<T extends View> extends LinearLayout {

	// ===========================================================
	// Constants
	// ===========================================================

//	static final boolean DEBUG = IMChannel.DEBUG;

	static final String LOG_TAG = "PullToRefresh";

	private static final float FRICTION = 2.0f;

	private static final int PULL_TO_REFRESH = 0x0;
	private static final int RELEASE_TO_REFRESH = 0x1;
	private static final int REFRESHING = 0x2;
	static final int MANUAL_REFRESHING = 0x3;

	private static final Mode DEFAULT_MODE = Mode.PULL_DOWN_TO_REFRESH;

	private static final String STATE_STATE = "ptr_state";
	private static final String STATE_MODE = "ptr_mode";
	private static final String STATE_CURRENT_MODE = "ptr_current_mode";
	private static final String STATE_DISABLE_SCROLLING_REFRESHING = "ptr_disable_scrolling";
	private static final String STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
	private static final String STATE_SUPER = "ptr_super";

	// ===========================================================
	// Fields
	// ===========================================================

	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	private float mInitialMotionY;

	private boolean mIsBeingDragged = false;
	private int mState = PULL_TO_REFRESH;
	private Mode mMode = DEFAULT_MODE;

	private Mode mCurrentMode;
	T mRefreshableView;
	private boolean mPullToRefreshEnabled = true;

	private boolean mShowViewWhileRefreshing = true;
	private boolean mDisableScrollingWhileRefreshing = true;
	private boolean mFilterTouchEvents = true;
	private LoadingLayout mHeaderLayout;
	private LoadingLayout mFooterLayout;

	private int mHeaderHeight;
	private int mInitialY; // 初始化Y的位置
	private final Handler mHandler = new Handler();

	private OnRefreshListener mOnRefreshListener;
	private OnRefreshListener2 mOnRefreshListener2;

	private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

	private Toast refreshToast; // 成功失败的toast
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm",Locale.getDefault());
	private ImageView refreshToastIcon;
	private TextView refreshToastHint;
	private Scroller mScroller;

	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		}
	};

	protected boolean isNeedAutoSelection = true;

	private boolean mScrollingCacheEnabled;

	private boolean mScrolling;

	private boolean mIsDisableRefresh;

	// ===========================================================
	// Constructors
	// ===========================================================

	PullToRefreshBase(Context context) {
		super(context);
		init(context, null);
	}

	PullToRefreshBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) { return; }
		init(context, attrs);
	}

	PullToRefreshBase(Context context, Mode mode) {
		super(context);
		mMode = mode;
		init(context, null);
	}

	/**
	 * Get the mode that this view is currently in. This is only really useful
	 * when using <code>Mode.BOTH</code>.
	 *
	 * @return Mode that the view is currently in
	 */
	public final Mode getCurrentMode() {
		return mCurrentMode;
	}

	/**
	 * Returns whether the Touch Events are filtered or not. If true is
	 * returned, then the View will only use touch events where the difference
	 * in the Y-axis is greater than the difference in the X-axis. This means
	 * that the View will not interfere when it is used in a horizontal
	 * scrolling View (such as a ViewPager).
	 *
	 * @return boolean - true if the View is filtering Touch Events
	 */
	public final boolean getFilterTouchEvents() {
		return mFilterTouchEvents;
	}

	/**
	 * Get the mode that this view has been set to. If this returns
	 * <code>Mode.BOTH</code>, you can use <code>getCurrentMode()</code> to
	 * check which mode the view is currently in
	 *
	 * @return Mode that the view has been set to
	 */
	public final Mode getMode() {
		return mMode;
	}

	/**
	 * Get the Wrapped Refreshable View. Anything returned here has already been
	 * added to the content view.
	 *
	 * @return The View which is currently wrapped
	 */
	public final T getRefreshableView() {
		return mRefreshableView;
	}

	/**
	 * Get whether the 'Refreshing' View should be automatically shown when
	 * refreshing. Returns true by default.
	 *
	 * @return - true if the Refreshing View will be show
	 */
	public final boolean getShowViewWhileRefreshing() {
		return mShowViewWhileRefreshing;
	}



	/**
	 * Returns whether the widget has disabled scrolling on the Refreshable View
	 * while refreshing.
	 *
	 * @return true if the widget has disabled scrolling while refreshing
	 */
	public final boolean isDisableScrollingWhileRefreshing() {
		return mDisableScrollingWhileRefreshing;
	}

	/**
	 * Whether Pull-to-Refresh is enabled
	 *
	 * @return enabled
	 */
	public final boolean isPullToRefreshEnabled() {
		return mPullToRefreshEnabled;
	}

	/**
	 * Returns whether the Widget is currently in the Refreshing mState
	 *
	 * @return true if the Widget is currently refreshing
	 */
	public final boolean isRefreshing() {
		return mState == REFRESHING || mState == MANUAL_REFRESHING;
	}

	public final boolean isDisableRefresh(){
		return mIsDisableRefresh;
	}

	public final void setDisableRefresh(boolean isDisable){
		if(isDisable){
			mIsDisableRefresh = isDisable;
		}else{
			mIsDisableRefresh = isDisable;
			mState = PULL_TO_REFRESH;
			resetHeaderWithOutAnimation();
			if (mMode.canPullDown()) {
				mHeaderLayout.setVisibility(View.VISIBLE);
			}
			if (mMode.canPullUp()) {
				mFooterLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public final boolean onInterceptTouchEvent(MotionEvent event) {

		if (!mPullToRefreshEnabled) {
			return false;
		}

		if (isRefreshing() && mDisableScrollingWhileRefreshing) {
			return true;
		}

		final int action = event.getAction();

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			return false;
		}

		if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
			return true;
		}

		switch (action) {
		case MotionEvent.ACTION_MOVE: {
			if (isReadyForPull()) {

				final float y = event.getY();
				final float dy = y - mLastMotionY;
				final float yDiff = Math.abs(dy);
				final float xDiff = Math.abs(event.getX() - mLastMotionX);

				if (yDiff > mTouchSlop
						&& (!mFilterTouchEvents || yDiff > xDiff)) {
					if (mMode.canPullDown() && dy >= 1f && isReadyForPullDown()) {
						mLastMotionY = y;
						mIsBeingDragged = true;
						if (mMode == Mode.BOTH) {
							mCurrentMode = Mode.PULL_DOWN_TO_REFRESH;
						}
					} else if (mMode.canPullUp() && dy <= -1f
							&& isReadyForPullUp()) {
						mLastMotionY = y;
						mIsBeingDragged = true;
						if (mMode == Mode.BOTH) {
							mCurrentMode = Mode.PULL_UP_TO_REFRESH;
						}
					}
				}
			}
			break;
		}
		case MotionEvent.ACTION_DOWN: {
			if (isReadyForPull()) {
				mLastMotionY = mInitialMotionY = event.getY() + mInitialY;
				mInitialY = 0;
				mInitialY = 0;
				mLastMotionX = event.getX();
				mIsBeingDragged = false;

				if(isDisableRefresh()){
					setDisableLoadingLayout();
				}
			}
			break;
		}
		}

		return mIsBeingDragged;
	}



	/**
	 * @param showToast
	 *            true:显示toast false:不显示toast
	 * @param isSuc
	 *            true:刷新成功 false:刷新失败
	 */
	public final void onRefreshComplete(boolean showToast, boolean isSuc) {
		if (mState != PULL_TO_REFRESH) {
			resetHeader();
		}
		if (showToast) {
			if (refreshToast != null) {
				if (!isSuc) {
					if (refreshToastIcon != null) {
						refreshToastIcon
								.setImageResource( R.drawable.aliwx_refresh_toast_failed);
						refreshToastHint.setText( R.string.aliwx_refresh_fail);
					}
				} else {
					if (refreshToastIcon != null) {
						refreshToastIcon
								.setImageResource( R.drawable.aliwx_refresh_toast_suc);
						refreshToastHint.setText( R.string.aliwx_refresh_success);
					}
				}
				refreshToast.show();
			}
		}
		if(!isNeedAutoSelection){
			setLastUpdatedLabel(getResources().getString( R.string.aliwx_last_update_time)
					+ " " + format.format(new Date(System.currentTimeMillis())));
		}
	}

	/**
	 *
	 * @param showToast
	 * @param isSuc
	 * @param hintResource 提示文案
	 */
	public final void onRefreshComplete(boolean showToast, boolean isSuc, int hintResource) {
		if (mState != PULL_TO_REFRESH) {
			resetHeader();
		}
		if (showToast) {
			if (refreshToast != null) {
				if (!isSuc) {
					if (refreshToastIcon != null) {
						refreshToastIcon
								.setImageResource( R.drawable.aliwx_refresh_toast_failed);
						refreshToastHint.setText(hintResource);
					}
				} else {
					if (refreshToastIcon != null) {
						refreshToastIcon
								.setImageResource( R.drawable.aliwx_refresh_toast_suc);
						refreshToastHint.setText(hintResource);
					}
				}
				refreshToast.show();
			}
		}
		if(!isNeedAutoSelection){
			setLastUpdatedLabel(getResources().getString( R.string.aliwx_last_update_time)
					+ " " + format.format(new Date(System.currentTimeMillis())));
		}
	}

	public final void resetHeadLayout() {
		resetHeader();
	}

	@Override
	public final boolean onTouchEvent(MotionEvent event) {
		if (!mPullToRefreshEnabled) {
			return false;
		}

		if (isRefreshing() && mDisableScrollingWhileRefreshing) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& event.getEdgeFlags() != 0) {
			return false;
		}

		switch (event.getAction()) {

		case MotionEvent.ACTION_MOVE: {
			if (mIsBeingDragged) {
				mLastMotionY = event.getY();
				pullEvent();
				return true;
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			if (isReadyForPull()) {
				mLastMotionY = mInitialMotionY = event.getY() + mInitialY;
				if(isDisableRefresh()){
					setDisableLoadingLayout();
				}
				return true;
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (mIsBeingDragged) {
				mIsBeingDragged = false;

				if (mState == RELEASE_TO_REFRESH && !mIsDisableRefresh) {

					if (null != mOnRefreshListener) {
						setRefreshingInternal(true);
						mOnRefreshListener.onRefresh();
						return true;

					} else if (null != mOnRefreshListener2) {
						setRefreshingInternal(true);
						if (mCurrentMode == Mode.PULL_DOWN_TO_REFRESH) {
							mOnRefreshListener2.onPullDownToRefresh();
						} else if (mCurrentMode == Mode.PULL_UP_TO_REFRESH) {
							mOnRefreshListener2.onPullUpToRefresh();
						}
						return true;
					}

					return true;
				}

				smoothScrollTo(0);
				return true;
			}
			break;
		}
		}

		return false;
	}

	public void setDisableLoadingImage(boolean isDisable){
		if (mMode.canPullDown()) {
			mHeaderLayout.setDisableLoading(isDisable);
		}
		if (mMode.canPullUp()) {
			mHeaderLayout.setDisableLoading(isDisable);
		}
	}

	protected void setDisableLoadingLayout() {
		if (mMode.canPullDown()) {
			mHeaderLayout.reset();
			mHeaderLayout.setVisibility(View.INVISIBLE);
		}
		if (mMode.canPullUp()) {
			mFooterLayout.reset();
			mFooterLayout.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * By default the Widget disabled scrolling on the Refreshable View while
	 * refreshing. This method can change this behaviour.
	 *
	 * @param disableScrollingWhileRefreshing
	 *            - true if you want to disable scrolling while refreshing
	 */
	public final void setDisableScrollingWhileRefreshing(
			boolean disableScrollingWhileRefreshing) {
		mDisableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
	}

	/**
	 * Set the Touch Events to be filtered or not. If set to true, then the View
	 * will only use touch events where the difference in the Y-axis is greater
	 * than the difference in the X-axis. This means that the View will not
	 * interfere when it is used in a horizontal scrolling View (such as a
	 * ViewPager), but will restrict which types of finger scrolls will trigger
	 * the View.
	 *
	 * @param filterEvents
	 *            - true if you want to filter Touch Events. Default is true.
	 */
	public final void setFilterTouchEvents(boolean filterEvents) {
		mFilterTouchEvents = filterEvents;
	}

	/**
	 * Set the Last Updated Text. This displayed under the main label when
	 * Pulling
	 *
	 * @param label
	 *            - Label to set
	 */
	public void setLastUpdatedLabel(CharSequence label) {
		if (null != mHeaderLayout) {
			mHeaderLayout.setSubHeaderText(label);
		}
		if (null != mFooterLayout) {
			mFooterLayout.setSubHeaderText(label);
		}

		// Refresh Height as it may have changed
		refreshLoadingViewsHeight();
	}

	/**
	 * Set the drawable used in the loading layout. This is the same as calling
	 * <code>setLoadingDrawable(drawable, Mode.BOTH)</code>
	 *
	 * @param drawable
	 *            - Drawable to display
	 */
	// public void setLoadingDrawable(Drawable drawable) {
	// setLoadingDrawable(drawable, Mode.BOTH);
	// }

//	/**
//	 * Set the drawable used in the loading layout.
//	 *
//	 * @param drawable
//	 *            - Drawable to display
//	 * @param mode
//	 *            - Controls which Header/Footer Views will be updated.
//	 *            <code>Mode.BOTH</code> will update all available, other values
//	 *            will update the relevant View.
//	 */
//	public void setLoadingDrawable(Drawable drawable, Mode mode) {
//		if (null != mHeaderLayout && mode.canPullDown()) {
//			mHeaderLayout.setLoadingDrawable(drawable);
//		}
//		if (null != mFooterLayout && mode.canPullUp()) {
//			mFooterLayout.setLoadingDrawable(drawable);
//		}
//
//		// The Loading Height may have changed, so refresh
//		refreshLoadingViewsHeight();
//	}

	@Override
	public void setLongClickable(boolean longClickable) {
		getRefreshableView().setLongClickable(longClickable);
	}

	/**
	 * Set the mode of Pull-to-Refresh that this view will use.
	 *
	 * @param mode
	 *            - Mode to set the View to
	 */
	public final void setMode(Mode mode) {
		if (mode != mMode) {
//			if (DEBUG) {
//				WxLog.d(LOG_TAG, "Setting mode to: " + mode);
//			}
			mMode = mode;
			updateUIForMode();
		}
	}

	/**
	 * Set OnRefreshListener for the Widget
	 *
	 * @param listener
	 *            - Listener to be used when the Widget is set to Refresh
	 */
	public final void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
	}

	/**
	 * Set OnRefreshListener for the Widget
	 *
	 * @param listener
	 *            - Listener to be used when the Widget is set to Refresh
	 */
	public final void setOnRefreshListener(OnRefreshListener2 listener) {
		mOnRefreshListener2 = listener;
	}

	/**
	 * Set Text to show when the Widget is being Pulled
	 * <code>setPullLabel(releaseLabel, Mode.BOTH)</code>
	 *
	 * @param releaseLabel
	 *            - String to display
	 */
	public void setPullLabel(String pullLabel) {
		setPullLabel(pullLabel, Mode.BOTH);
	}

	/**
	 * Set Text to show when the Widget is being Pulled
	 *
	 * @param pullLabel
	 *            - String to display
	 * @param mode
	 *            - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setPullLabel(String pullLabel, Mode mode) {
		if (null != mHeaderLayout && mode.canPullDown()) {
			mHeaderLayout.setPullLabel(pullLabel);
		}
		if (null != mFooterLayout && mode.canPullUp()) {
			mFooterLayout.setPullLabel(pullLabel);
		}
	}

	/**
	 * A mutator to enable/disable Pull-to-Refresh for the current View
	 *
	 * @param enable
	 *            Whether Pull-To-Refresh should be used
	 */
	public final void setPullToRefreshEnabled(boolean enable) {
		mPullToRefreshEnabled = enable;
	}



	/**
	 * Sets the Widget to be in the refresh mState. The UI will be updated to
	 * show the 'Refreshing' view.
	 *
	 * @param doScroll
	 *            - true if you want to force a scroll to the Refreshing view.
	 */
	public final void setRefreshing(boolean doScroll) {
		if (!isRefreshing()) {
			setRefreshingInternal(doScroll);
			mState = MANUAL_REFRESHING;
		}
	}

	/**
	 * Set Text to show when the Widget is refreshing
	 * <code>setRefreshingLabel(releaseLabel, Mode.BOTH)</code>
	 *
	 * @param releaseLabel
	 *            - String to display
	 */
	public void setRefreshingLabel(String refreshingLabel) {
		setRefreshingLabel(refreshingLabel, Mode.BOTH);
	}

	/**
	 * Set Text to show when the Widget is refreshing
	 *
	 * @param refreshingLabel
	 *            - String to display
	 * @param mode
	 *            - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setRefreshingLabel(String refreshingLabel, Mode mode) {
		if (null != mHeaderLayout && mode.canPullDown()) {
			mHeaderLayout.setRefreshingLabel(refreshingLabel);
		}
		if (null != mFooterLayout && mode.canPullUp()) {
			mFooterLayout.setRefreshingLabel(refreshingLabel);
		}
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released. This is the same as calling
	 * <code>setReleaseLabel(releaseLabel, Mode.BOTH)</code>
	 *
	 * @param releaseLabel
	 *            - String to display
	 */
	public void setReleaseLabel(String releaseLabel) {
		setReleaseLabel(releaseLabel, Mode.BOTH);
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released
	 *
	 * @param releaseLabel
	 *            - String to display
	 * @param mode
	 *            - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setReleaseLabel(String releaseLabel, Mode mode) {
		if (null != mHeaderLayout && mode.canPullDown()) {
			mHeaderLayout.setReleaseLabel(releaseLabel);
		}
		if (null != mFooterLayout && mode.canPullUp()) {
			mFooterLayout.setReleaseLabel(releaseLabel);
		}
	}

	/**
	 * A mutator to enable/disable whether the 'Refreshing' View should be
	 * automatically shown when refreshing.
	 *
	 * @param showView
	 */
	public final void setShowViewWhileRefreshing(boolean showView) {
		mShowViewWhileRefreshing = showView;
	}

	protected void addRefreshableView(Context context, T refreshableView) {
		addView(refreshableView, new LayoutParams(
				LayoutParams.FILL_PARENT, 0, 1.0f));
	}

	/**
	 * This is implemented by derived classes to return the created View. If you
	 * need to use a custom View (such as a custom ListView), override this
	 * method and return an instance of your custom class.
	 *
	 * Be sure to set the ID of the view in this method, especially if you're
	 * using a ListActivity or ListFragment.
	 *
	 * @param context
	 *            Context to create view with
	 * @param attrs
	 *            AttributeSet from wrapped class. Means that anything you
	 *            include in the XML layout declaration will be routed to the
	 *            created View
	 * @return New instance of the Refreshable View
	 */
	protected abstract T createRefreshableView(Context context,
			AttributeSet attrs);

	protected final LoadingLayout getFooterLayout() {
		return mFooterLayout;
	}

	public final int getHeaderHeight() {
		return mHeaderHeight;
	}

	protected final LoadingLayout getHeaderLayout() {
		return mHeaderLayout;
	}

	protected final int getState() {
		return mState;
	}

	/**
	 * Allows Derivative classes to handle the XML Attrs without creating a
	 * TypedArray themsevles
	 *
	 * @param a
	 *            - TypedArray of PullToRefresh Attributes
	 */
	protected void handleStyledAttributes(TypedArray a) {
	}

	/**
	 * Implemented by derived class to return whether the View is in a mState
	 * where the user can Pull to Refresh by scrolling down.
	 *
	 * @return true if the View is currently the correct mState (for example,
	 *         top of a ListView)
	 */
	protected abstract boolean isReadyForPullDown();

	/**
	 * Implemented by derived class to return whether the View is in a mState
	 * where the user can Pull to Refresh by scrolling up.
	 *
	 * @return true if the View is currently in the correct mState (for example,
	 *         bottom of a ListView)
	 */
	protected abstract boolean isReadyForPullUp();

	/**
	 * Called when the UI needs to be updated to the 'Pull to Refresh' state
	 */
	protected void onPullToRefresh() {
		switch (mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			mFooterLayout.pullToRefresh();
			break;
		case PULL_DOWN_TO_REFRESH:
			mHeaderLayout.pullToRefresh();
			break;
		default:
			break;
		}
	}

	/**
	 * Called when the UI needs to be updated to the 'Release to Refresh' state
	 */
	protected void onReleaseToRefresh() {
		switch (mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			mFooterLayout.releaseToRefresh();
			break;
		case PULL_DOWN_TO_REFRESH:
			mHeaderLayout.releaseToRefresh();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;

			mMode = Mode.mapIntToMode(bundle.getInt(STATE_MODE, 0));
			mCurrentMode = Mode.mapIntToMode(bundle.getInt(STATE_CURRENT_MODE,
					0));

			mDisableScrollingWhileRefreshing = bundle.getBoolean(
					STATE_DISABLE_SCROLLING_REFRESHING, true);
			mShowViewWhileRefreshing = bundle.getBoolean(
					STATE_SHOW_REFRESHING_VIEW, true);

			// Let super Restore Itself
			super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

			final int viewState = bundle.getInt(STATE_STATE, PULL_TO_REFRESH);
			if (viewState == REFRESHING) {
				setRefreshingInternal(true);
				mState = viewState;
			}
			return;
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putInt(STATE_STATE, mState);
		bundle.putInt(STATE_MODE, mMode.getIntValue());
		bundle.putInt(STATE_CURRENT_MODE, mCurrentMode.getIntValue());
		bundle.putBoolean(STATE_DISABLE_SCROLLING_REFRESHING,
				mDisableScrollingWhileRefreshing);
		bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, mShowViewWhileRefreshing);
		bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
		return bundle;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected void resetHeader() {
		mState = PULL_TO_REFRESH;
		mIsBeingDragged = false;
		if (mMode.canPullDown()) {
			mHeaderLayout.reset();
		}
		if (mMode.canPullUp()) {
			mFooterLayout.reset();
		}
		if(isNeedAutoSelection){
			smoothScrollTo(0);
		}else{
			if (null != mCurrentSmoothScrollRunnable) {
				mCurrentSmoothScrollRunnable.stop();
			}
			scrollTo(0 ,  0);
		}
	}

	private void resetHeaderWithOutAnimation() {
		if(isNeedAutoSelection){
			mState = PULL_TO_REFRESH;
			mIsBeingDragged = false;

			setLastUpdatedLabel(getResources().getString( R.string.aliwx_last_update_time)
					+ " " + format.format(new Date(System.currentTimeMillis())));

			if (mMode.canPullDown()) {
				mHeaderLayout.reset();
			}
			if (mMode.canPullUp()) {
				mFooterLayout.reset();
			}
		}
	}

	protected void startScrollHeader() {
		if(isNeedAutoSelection){
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScrolling = true;
					setScrollingCacheEnabled(true);
					mScroller.abortAnimation();
					mScroller.startScroll(0, getScrollY(), 0,  0 - getScrollY(), 800);
					invalidate();
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							resetHeaderWithOutAnimation();
						}
					} , 800);
				}
			} , 50);
		}
	}

	protected final void setHeaderScroll(int y) {
		scrollTo(0, y);
	}

	protected void setRefreshingInternal(boolean doScroll) {
		mState = REFRESHING;

		if (mMode.canPullDown()) {
			mHeaderLayout.refreshing();
		}
		if (mMode.canPullUp()) {
			mFooterLayout.refreshing();
		}

		if (doScroll) {
			if (mShowViewWhileRefreshing) {
				smoothScrollTo(mCurrentMode == Mode.PULL_DOWN_TO_REFRESH ? -mHeaderHeight
						: mHeaderHeight);
			} else {
				smoothScrollTo(0);
			}
		}
	}

	protected final void smoothScrollTo(int y) {
		if (null != mCurrentSmoothScrollRunnable) {
			mCurrentSmoothScrollRunnable.stop();
		}

		if (getScrollY() != y) {
			mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(mHandler,
					getScrollY(), y);
//			mCurrentAverageScrollRunnable = new AverageScrollRunnable(mHandler,
//					getScrollY(), y);
			mHandler.post(mCurrentSmoothScrollRunnable);
		}
	}

	/**
	 * Updates the View State when the mode has been set. This does not do any
	 * checking that the mode is different to current state so always updates.
	 */
	protected void updateUIForMode() {
		// Remove Header, and then add Header Loading View again if needed
		if (this == mHeaderLayout.getParent()) {
			removeView(mHeaderLayout);
		}
		if (mMode.canPullDown()) {
			addView(mHeaderLayout, 0, new LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// Remove Footer, and then add Footer Loading View again if needed
		if (this == mFooterLayout.getParent()) {
			removeView(mFooterLayout);
		}
		if (mMode.canPullUp()) {
			addView(mFooterLayout, new LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));

		}

		// Hide Loading Views
		refreshLoadingViewsHeight();

		// If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
		// set it to pull down
		mCurrentMode = (mMode != Mode.BOTH) ? mMode : Mode.PULL_DOWN_TO_REFRESH;
	}

	private void init(Context context, AttributeSet attrs) {
		setOrientation(LinearLayout.VERTICAL);

		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();

		// Styleables from XML
//		TypedArray a = context.obtainStyledAttributes(attrs, ResourceLoader.getIdsByName(context, "styleable", "aliwx_PullToRefresh"));
//		handleStyledAttributes(a);
//
//		if (a.hasValue( R.styleable.aliwx_PullToRefresh_aliwx_ptrMode)) {
//			mMode = Mode.mapIntToMode(a.getInteger(
//					 R.styleable.aliwx_PullToRefresh_aliwx_ptrMode, 0));
//		}

		// Refreshable View
		// By passing the attrs, we can add ListView/GridView params via XML
		mRefreshableView = createRefreshableView(context, attrs);
		addRefreshableView(context, mRefreshableView);

		// We n eed to create now layouts now
		mHeaderLayout = new LoadingLayout(context, Mode.PULL_DOWN_TO_REFRESH, null);
		mFooterLayout = new LoadingLayout(context, Mode.PULL_UP_TO_REFRESH, null);

		// Add Header/Footer Views
		updateUIForMode();

		// Styleables from XML
//		if (a.hasValue( R.styleable.aliwx_PullToRefresh_aliwx_ptrHeaderBackground)) {
//			Drawable background = a
//					.getDrawable( R.styleable.aliwx_PullToRefresh_aliwx_ptrHeaderBackground);
//			if (null != background) {
//				setBackgroundDrawable(background);
//			}
//		}
//		if (a.hasValue( R.styleable.aliwx_PullToRefresh_aliwx_ptrAdapterViewBackground)) {
//			Drawable background = a
//					.getDrawable( R.styleable.aliwx_PullToRefresh_aliwx_ptrAdapterViewBackground);
//			if (null != background) {
//				mRefreshableView.setBackgroundDrawable(background);
//			}
//		}
//		a.recycle();
//		a = null;

		refreshToast = new Toast(context);
		View toastView = View.inflate(context, R.layout.aliwx_pull_down_refresh_toast, null);
		refreshToastIcon = (ImageView) toastView
				.findViewById( R.id.refresh_toast_icon);
		refreshToastHint = (TextView) toastView
				.findViewById( R.id.hint);
		refreshToast.setView(toastView);
		refreshToast.setGravity(Gravity.CENTER, 0, 0);
		refreshToast.setDuration(Toast.LENGTH_SHORT);
		mScroller = new Scroller(context, sInterpolator);
	}

	private boolean isReadyForPull() {
		switch (mMode) {
		case PULL_DOWN_TO_REFRESH:
			return isReadyForPullDown();
		case PULL_UP_TO_REFRESH:
			return isReadyForPullUp();
		case BOTH:
			return isReadyForPullUp() || isReadyForPullDown();
		}
		return false;
	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * Actions a Pull Event
	 *
	 * @return true if the Event has been handled, false if there has been no
	 *         change
	 */
	private void pullEvent() {

		final int newHeight;

		switch (mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			newHeight = Math.round(Math.max(mInitialMotionY - mLastMotionY, 0)
					/ FRICTION);
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			newHeight = Math.round(Math.min(mInitialMotionY - mLastMotionY, 0)
					/ FRICTION);
			break;
		}

		pullEvent(newHeight);

	}



	private void pullEvent(int newHeight) {
		setHeaderScroll(newHeight);
		if (newHeight != 0 && !mIsDisableRefresh) {
			float scale = Math.abs(newHeight) / (float) mHeaderHeight;
			switch (mCurrentMode) {
			case PULL_UP_TO_REFRESH:
				mFooterLayout.onPullY(scale);
				break;
			case PULL_DOWN_TO_REFRESH:
				mHeaderLayout.onPullY(scale);
				break;
			default:
				break;
			}
			if (mState == PULL_TO_REFRESH
					&& mHeaderHeight < Math.abs(newHeight)) {
				mState = RELEASE_TO_REFRESH;
				onReleaseToRefresh();

			} else if (mState == RELEASE_TO_REFRESH
					&& mHeaderHeight >= Math.abs(newHeight)) {
				mState = PULL_TO_REFRESH;
				onPullToRefresh();
			}
		}

	}

	/**
	 * Re-measure the Loading Views height, and adjust internal padding as
	 * necessary
	 */
	private void refreshLoadingViewsHeight() {
		if (mMode.canPullDown()) {
			measureView(mHeaderLayout);
			mHeaderHeight = mHeaderLayout.getMeasuredHeight();
		} else if (mMode.canPullUp()) {
			measureView(mFooterLayout);
			mHeaderHeight = mFooterLayout.getMeasuredHeight();
		}

		// Hide Loading Views
		switch (mMode) {
		case BOTH:
			setPadding(0, -mHeaderHeight, 0, -mHeaderHeight);
			break;
		case PULL_UP_TO_REFRESH:
			setPadding(0, 0, 0, -mHeaderHeight);
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			setPadding(0, -mHeaderHeight, 0, 0);
			break;
		}
	}

	public static enum Mode {
		/**
		 * Only allow the user to Pull Down from the top to refresh, this is the
		 * default.
		 */
		PULL_DOWN_TO_REFRESH(0x1),

		/**
		 * Only allow the user to Pull Up from the bottom to refresh.
		 */
		PULL_UP_TO_REFRESH(0x2),

		/**
		 * Allow the user to both Pull Down from the top, and Pull Up from the
		 * bottom to refresh.
		 */
		BOTH(0x3);

		/**
		 * Maps an int to a specific mode. This is needed when saving state, or
		 * inflating the view from XML where the mode is given through a attr
		 * int.
		 *
		 * @param modeInt
		 *            - int to map a Mode to
		 * @return Mode that modeInt maps to, or PULL_DOWN_TO_REFRESH by
		 *         default.
		 */
		public static Mode mapIntToMode(int modeInt) {
			switch (modeInt) {
			case 0x1:
			default:
				return PULL_DOWN_TO_REFRESH;
			case 0x2:
				return PULL_UP_TO_REFRESH;
			case 0x3:
				return BOTH;
			}
		}

		private int mIntValue;

		// The modeInt values need to match those from attrs.xml
		Mode(int modeInt) {
			mIntValue = modeInt;
		}

		/**
		 * @return true if this mode permits Pulling Down from the top
		 */
		boolean canPullDown() {
			return this == PULL_DOWN_TO_REFRESH || this == BOTH;
		}

		/**
		 * @return true if this mode permits Pulling Up from the bottom
		 */
		boolean canPullUp() {
			return this == PULL_UP_TO_REFRESH || this == BOTH;
		}

		int getIntValue() {
			return mIntValue;
		}

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * Simple Listener that allows you to be notified when the user has scrolled
	 * to the end of the AdapterView. See (
	 * {@link PullToRefreshAdapterViewBase#setOnLastItemVisibleListener}.
	 *
	 * @author Chris Banes
	 *
	 */
	static interface OnLastItemVisibleListener {

		/**
		 * Called when the user has scrolled to the end of the list
		 */
		public void onLastItemVisible();

	}

	/**
	 * Simple Listener to listen for any callbacks to Refresh.
	 *
	 * @author Chris Banes
	 */
	public static interface OnRefreshListener {

		/**
		 * onRefresh will be called for both Pull Down from top, and Pull Up
		 * from Bottom
		 */
		public void onRefresh();

	}

	/**
	 * An advanced version of the Listener to listen for callbacks to Refresh.
	 * This listener is different as it allows you to differentiate between Pull
	 * Ups, and Pull Downs.
	 *
	 * @author Chris Banes
	 */
	public static interface OnRefreshListener2 {

		/**
		 * onPullDownToRefresh will be called only when the user has Pulled Down
		 * from the top, and released.
		 */
		public void onPullDownToRefresh();

		/**
		 * onPullUpToRefresh will be called only when the user has Pulled Up
		 * from the bottom, and released.
		 */
		public void onPullUpToRefresh();

	}

	private final class SmoothScrollRunnable implements Runnable {

		private static final int ANIMATION_DURATION_MS = 190;
		private final Interpolator mInterpolator;
		private final int mScrollToY;
		private final int mScrollFromY;
		private final Handler mHandler;

		private boolean mContinueRunning = true;
		private long mStartTime = -1;
		private int mCurrentY = -1;

		private SmoothScrollRunnable(Handler handler, int fromY, int toY) {
			mHandler = handler;
			mScrollFromY = fromY;
			mScrollToY = toY;
			mInterpolator = new AccelerateDecelerateInterpolator();
		}

		@Override
		public void run() {

			/**
			 * Only set mStartTime if this is the first time we're starting,
			 * else actually calculate the Y delta
			 */
			if (mStartTime == -1) {
				mStartTime = System.currentTimeMillis();
			} else {

				/**
				 * We do do all calculations in long to reduce software float
				 * calculations. We use 1000 as it gives us good accuracy and
				 * small rounding errors
				 */
				long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime))
						/ ANIMATION_DURATION_MS;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((mScrollFromY - mScrollToY)
						* mInterpolator
								.getInterpolation(normalizedTime / 1000f));
				mCurrentY = mScrollFromY - deltaY;
				setHeaderScroll(mCurrentY);
			}

			// If we're not at the target Y, keep going...
			if (mContinueRunning && mScrollToY != mCurrentY) {
//				mHandler.postDelayed(this, ANIMATION_FPS);
				mHandler.post(this);
			}
		}

		private void stop() {
			mContinueRunning = false;
			mHandler.removeCallbacks(this);
		}
	}

	@Override
	public void computeScroll() {
		if (isInEditMode()) { return; }
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();

				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}

				// Keep on drawing until the animation has finished.
				invalidate();
				return;
			}
		}

		// Done with scroll, clean up state.
		completeScroll();
	}

	private void completeScroll() {
		boolean needPopulate = mScrolling;
		if (needPopulate) {
			// Done with scroll, no longer want to cache view drawing.
			setScrollingCacheEnabled(false);
			mScroller.abortAnimation();
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			if (oldX != x || oldY != y) {
				scrollTo(x, y);
			}
		}
		mScrolling = false;
	}

	private static final boolean USE_CACHE = true;

	private void setScrollingCacheEnabled(boolean enabled) {
		if (mScrollingCacheEnabled != enabled) {
			mScrollingCacheEnabled = enabled;
			if (USE_CACHE) {
				final int size = getChildCount();
				for (int i = 0; i < size; ++i) {
					final View child = getChildAt(i);
					if (child.getVisibility() != GONE) {
						child.setDrawingCacheEnabled(enabled);
					}
				}
			}
		}
	}


}
