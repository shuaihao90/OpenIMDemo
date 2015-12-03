package com.taobao.openimui.feature.contact.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.kit.common.YWAsyncBaseAdapter;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.feature.contact.component.ComparableContact;
import com.taobao.openimui.feature.contact.component.ISearchable;

import java.util.List;

/**
 * 搜索adapter
 * 
 * @author shuheng
 * 
 */
public class SearchAdapter extends YWAsyncBaseAdapter implements
		OnClickListener {
	public static final int CONTACTS=1;
	private List<ISearchable> mContactlist;
	private Activity mContext;
	private LayoutInflater mlayoutInflater;
	private int maxVisibleCount;

	// private LayoutParams mParams;
	/**
	 * 头像缓存
	 */
	private YWContactHeadLoadHelper mHelper;

	private int type;
	public SearchAdapter(Activity context, List<ISearchable> contactlist,int type) {
		mContactlist = contactlist;
		mContext = context;
		this.type=type;
		mlayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mHelper = new YWContactHeadLoadHelper(context, this);

	}

	private class ViewHolder {
		ImageView headView;
		TextView selectName;
	}

	@Override
	public int getCount() {
		if (mContactlist != null) {
			return mContactlist.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mContactlist != null) {
			return mContactlist.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mlayoutInflater.inflate (R.layout.aliwx_member_item,
					parent, false);
			holder = new ViewHolder();
			holder.headView = (ImageView) convertView.findViewById (R.id.aliwx_head);
			holder.selectName = (TextView) convertView
					.findViewById (R.id.aliwx_select_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mContactlist != null) {
			ISearchable contact = mContactlist.get(position);
			String showname = contact.getShowName();
			holder.headView.setTag(R.id.aliwx_select_name, contact);
			if (contact != null && contact instanceof ComparableContact) {
				ComparableContact comparableContact = (ComparableContact) contact;
				holder.headView.setVisibility(View.VISIBLE);
				holder.headView.setTag(R.id.head, position);
				mHelper.setHeadView(holder.headView, comparableContact.getUserId(), comparableContact.getAppKey(), comparableContact.getOnlineStatus()==0);
				holder.selectName.setText(showname);
				holder.selectName.setVisibility(View.VISIBLE);
				holder.headView.setOnClickListener(this);

			}

		}
		return convertView;
	}

	public int getMaxVisibleCount() {
		return maxVisibleCount;
	}

	public void setMaxVisibleCount(int maxVisibleCount) {
		this.maxVisibleCount = maxVisibleCount;
		mHelper.setMaxVisible(maxVisibleCount);
	}

	@Override
	public void loadAsyncTask() {
		mHelper.loadAyncHead();
	}

	@Override
	public void onClick(View v) {
		Object tagObject = v.getTag (R.id.aliwx_select_name);
		int i = v.getId();
		if (i ==  R.id.aliwx_head) {
			if (v.getTag (R.id.aliwx_head) instanceof Integer) {
				int position = (Integer) v.getTag (R.id.aliwx_head);
			}

		}

	}

}