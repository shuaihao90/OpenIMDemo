package com.taobao.openimui.feature.contact.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import com.alibaba.openIMUIDemo.R;

public class PullToRefreshDummyHeadListView extends
		PullToRefreshAdapterViewBase<DummyHeadListView> {
	public PullToRefreshDummyHeadListView(Context context) {
		super(context);
	}

	public PullToRefreshDummyHeadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshDummyHeadListView(Context context, Mode mode) {
		super(context, mode);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalExpandableListView) getRefreshableView())
				.getContextMenuInfo();
	}

	@Override
	protected final DummyHeadListView createRefreshableView(Context context,
			AttributeSet attrs) {
		DummyHeadListView lv = new InternalExpandableListView(context, attrs);

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId( R.id.PullToRefreshExpandableListViewID);
		return lv;
	}

	private class InternalExpandableListView extends DummyHeadListView
			implements EmptyViewMethodAccessor {

		private InternalExpandableListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshDummyHeadListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}
	}
}
