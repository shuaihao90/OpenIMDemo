
package com.taobao.openimui.feature.contact.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.alibaba.openIMUIDemo.R;

@SuppressWarnings("deprecation")
public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

	private LoadingLayout mHeaderLoadingView;
	private LoadingLayout mFooterLoadingView;

	private FrameLayout mLvFooterLoadingFrame;
	private boolean isListNeedAutoSelection = true;
	
	public PullToRefreshListView(Context context) {
		super(context);
		setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, Mode mode) {
		super(context, mode);
		setDisableScrollingWhileRefreshing(false);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalListView) getRefreshableView()).getContextMenuInfo();
	}
	
	public void setPullLabel(String pullLabel, Mode mode) {
		super.setPullLabel(pullLabel, mode);

		if (null != mHeaderLoadingView && mode.canPullDown()) {
			mHeaderLoadingView.setPullLabel(pullLabel);
		}
		if (null != mFooterLoadingView && mode.canPullUp()) {
			mFooterLoadingView.setPullLabel(pullLabel);
		}
	}

	public void setRefreshingLabel(String refreshingLabel, Mode mode) {
		super.setRefreshingLabel(refreshingLabel, mode);

		if (null != mHeaderLoadingView && mode.canPullDown()) {
			mHeaderLoadingView.setRefreshingLabel(refreshingLabel);
		}
		if (null != mFooterLoadingView && mode.canPullUp()) {
			mFooterLoadingView.setRefreshingLabel(refreshingLabel);
		}
	}

	public void setReleaseLabel(String releaseLabel, Mode mode) {
		super.setReleaseLabel(releaseLabel, mode);

		if (null != mHeaderLoadingView && mode.canPullDown()) {
			mHeaderLoadingView.setReleaseLabel(releaseLabel);
		}
		if (null != mFooterLoadingView && mode.canPullUp()) {
			mFooterLoadingView.setReleaseLabel(releaseLabel);
		}
	}

	@Override
	protected final ListView createRefreshableView(Context context, AttributeSet attrs) {
		ListView lv = new InternalListView(context, attrs);
		// Get Styles from attrs
//		TypedArray a = context.obtainStyledAttributes(attrs, ResourceLoader.getIdsByName(context, "styleable", "aliwx_PullToRefresh"));

		// Create Loading Views ready for use later
		FrameLayout frame = new FrameLayout(context);
		mHeaderLoadingView = new LoadingLayout(context, Mode.PULL_DOWN_TO_REFRESH, null);
		frame.addView(mHeaderLoadingView, FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		mHeaderLoadingView.setVisibility(View.GONE);
		lv.addHeaderView(frame, null, false);

		mLvFooterLoadingFrame = new FrameLayout(context);
		mFooterLoadingView = new LoadingLayout(context, Mode.PULL_UP_TO_REFRESH, null);
		mLvFooterLoadingFrame.addView(mFooterLoadingView, FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		mFooterLoadingView.setVisibility(View.GONE);

//		a.recycle();

		// Set it to this so it can be used in ListActivity/ListFragment
//		lv.setId(android.R.id.list);
		lv.setId(R.id.PullToRefreshListViewID);
		return lv;
	}

	protected int getNumberInternalFooterViews() {
		return null != mFooterLoadingView ? 1 : 0;
	}

	protected int getNumberInternalHeaderViews() {
		return null != mHeaderLoadingView ? 1 : 0;
	}

	@Override
	protected void resetHeader() {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			isNeedAutoSelection = true;
			super.resetHeader();
			return;
		}

		LoadingLayout originalLoadingLayout;
		LoadingLayout listViewLoadingLayout;

		int scrollToHeight = getHeaderHeight();
		int selection;
		boolean scroll;

		switch (getCurrentMode()) {
			case PULL_UP_TO_REFRESH:
				originalLoadingLayout = getFooterLayout();
				listViewLoadingLayout = mFooterLoadingView;
				selection = mRefreshableView.getCount() - 1;
				scroll = mRefreshableView.getLastVisiblePosition() == selection;
				break;
			case PULL_DOWN_TO_REFRESH:
			default:
				originalLoadingLayout = getHeaderLayout();
				listViewLoadingLayout = mHeaderLoadingView;
				scrollToHeight *= -1;
				selection = 0;
				scroll = mRefreshableView.getFirstVisiblePosition() == selection;
				break;
		}

		// Set our Original View to Visible
		originalLoadingLayout.setVisibility(View.VISIBLE);

		/**
		 * Scroll so the View is at the same Y as the ListView header/footer,
		 * but only scroll if we've pulled to refresh and it's positioned
		 * correctly
		 */
		if (scroll && getState() != MANUAL_REFRESHING  && isNeedAutoSelection) {
			mRefreshableView.setSelection(selection);
			setHeaderScroll(scrollToHeight);
		}

		// Hide the ListView Header/Footer
		listViewLoadingLayout.setVisibility(View.GONE);
		isNeedAutoSelection = isListNeedAutoSelection;
		super.resetHeader();
	}
	
	@Override
	protected void setDisableLoadingLayout() {
		super.setDisableLoadingLayout();
		
		LoadingLayout originalLoadingLayout;
		LoadingLayout listViewLoadingLayout;

		switch (getCurrentMode()) {
		case PULL_UP_TO_REFRESH:
			originalLoadingLayout = getFooterLayout();
			listViewLoadingLayout = mFooterLoadingView;
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			originalLoadingLayout = getHeaderLayout();
			listViewLoadingLayout = mHeaderLoadingView;
			break;
		}
		originalLoadingLayout.setVisibility(View.INVISIBLE);
		listViewLoadingLayout.setVisibility(View.GONE);
		
	}

	@Override
	protected void setRefreshingInternal(boolean doScroll) {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			super.setRefreshingInternal(doScroll);
			return;
		}

		super.setRefreshingInternal(false);

		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int selection, scrollToY;

		switch (getCurrentMode()) {
			case PULL_UP_TO_REFRESH:
				originalLoadingLayout = getFooterLayout();
				listViewLoadingLayout = mFooterLoadingView;
				selection = mRefreshableView.getCount() - 1;
				scrollToY = getScrollY() - getHeaderHeight();
				break;
			case PULL_DOWN_TO_REFRESH:
			default:
				originalLoadingLayout = getHeaderLayout();
				listViewLoadingLayout = mHeaderLoadingView;
				selection = 0;
				scrollToY = getScrollY() + getHeaderHeight();
				break;
		}

		if (doScroll) {
			// We scroll slightly so that the ListView's header/footer is at the
			// same Y position as our normal header/footer
			setHeaderScroll(scrollToY);
		}

		// Hide our original Loading View
		originalLoadingLayout.setVisibility(View.INVISIBLE);

		// Show the ListView Loading View and set it to refresh
		listViewLoadingLayout.setVisibility(View.VISIBLE);
		listViewLoadingLayout.refreshing();

		if (doScroll) {
			// Make sure the ListView is scrolled to show the loading
			// header/footer
			mRefreshableView.setSelection(selection);

			// Smooth scroll as normal
			smoothScrollTo(0);
		}
	}

	private class InternalListView extends ListView implements EmptyViewMethodAccessor {

		public static final String TAG = "InternalListView";
		private ListAdapter mAdapter;
		private boolean mAddedLvFooter = false;
		
		private InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void draw(Canvas canvas) {
			/**
			 * This is a bit hacky, but ListView has got a bug in it when using
			 * Header/Footer Views and the list is empty. This masks the issue
			 * so that it doesn't cause an FC. See Issue #66.
			 */
			try {
				super.draw(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (NoSuchFieldError e){
				e.printStackTrace();
			}
		}

		
		
		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}

		@Override
		public void setAdapter(ListAdapter adapter) {
			// Add the Footer View at the last possible moment
			if (!mAddedLvFooter) {
				addFooterView(mLvFooterLoadingFrame, null, false);
				mAddedLvFooter = true;
			}

			super.setAdapter(adapter);
			this.mAdapter = adapter;
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

		@Override
		protected void layoutChildren() {
			try{
				super.layoutChildren();
			}catch(IllegalStateException e){
				if(mAdapter != null){
					throw new IllegalStateException("The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread in Adapter " + mAdapter);
				}else{
					throw e;
				}
			}
		}
	}
	
	public void setStartRefreshing(){
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter /*|| adapter.isEmpty()*/) {
			super.setRefreshingInternal(true);
			return;
		}
		super.setRefreshingInternal(false);
		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int selection, scrollToY;
		switch (getCurrentMode()) {
		case PULL_UP_TO_REFRESH:
			originalLoadingLayout = getFooterLayout();
			listViewLoadingLayout = mFooterLoadingView;
			selection = mRefreshableView.getCount() - 1;
			scrollToY = getScrollY() - getHeaderHeight();
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			originalLoadingLayout = getHeaderLayout();
			listViewLoadingLayout = mHeaderLoadingView;
			selection = 0;
			scrollToY = getScrollY() + getHeaderHeight();
			break;
		}
		setHeaderScroll(scrollToY);
		originalLoadingLayout.setVisibility(View.INVISIBLE);
		listViewLoadingLayout.setVisibility(View.VISIBLE);
		listViewLoadingLayout.refreshing();
		mRefreshableView.setSelection(selection);
		smoothScrollTo(0);
	}
	
	public void setStartRefreshingOver(){
		ListAdapter adapter = mRefreshableView.getAdapter();
		if (!getShowViewWhileRefreshing() || null == adapter /*|| adapter.isEmpty()*/) {
			super.resetHeader();
			return;
		}
		LoadingLayout originalLoadingLayout;
		LoadingLayout listViewLoadingLayout;
		int scrollToHeight = getHeaderHeight();
		int selection;
		boolean scroll;
		switch (getCurrentMode()) {
		case PULL_UP_TO_REFRESH:
			originalLoadingLayout = getFooterLayout();
			listViewLoadingLayout = mFooterLoadingView;
			selection = mRefreshableView.getCount() - 1;
			scroll = mRefreshableView.getLastVisiblePosition() == selection;
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			originalLoadingLayout = getHeaderLayout();
			listViewLoadingLayout = mHeaderLoadingView;
			scrollToHeight *= -1;
			selection = 0;
			scroll = mRefreshableView.getFirstVisiblePosition() == selection;
			break;
		}
		originalLoadingLayout.setVisibility(View.VISIBLE);
		if (scroll && getState() != MANUAL_REFRESHING && isNeedAutoSelection) {
			mRefreshableView.setSelection(selection);
			setHeaderScroll(scrollToHeight);
		}
		listViewLoadingLayout.setVisibility(View.GONE);
		super.resetHeader();
	}
	
	public void setNeedAutoSetSelection(boolean isNeedAutoSelection){
		this.isNeedAutoSelection = isNeedAutoSelection;
		this.isListNeedAutoSelection = isNeedAutoSelection;
	}
}
