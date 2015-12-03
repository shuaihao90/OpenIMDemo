package com.taobao.openimui.feature.contact.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.alibaba.mobileim.kit.common.YWAsyncBaseAdapter;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.feature.contact.component.ComparableContact;
import com.taobao.openimui.feature.contact.component.ModelIndexer;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends YWAsyncBaseAdapter implements
		SectionIndexer, OnClickListener {

	public static final int CONTACTS=1;
	/**
	 * 联系人首字母分组，用mContactsList初始化
	 */
	private ModelIndexer shortNameIndexer;
	private List<ComparableContact> mContactlist;
	private Activity mContext;
	private LayoutInflater mlayoutInflater;
	private int maxVisibleCount;
	private YWContactHeadLoadHelper mHelper;
	private int type;

    private boolean mShowCheckBox;
    private List<String> mSelectedList;

	public ContactsAdapter(Activity context, List<ComparableContact> contactlist, int type) {
		this.mContactlist = contactlist;
		mContext = context;
		this.type=type;
		shortNameIndexer = new ModelIndexer(contactlist);
		mlayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mHelper = new YWContactHeadLoadHelper(context, this);

        mShowCheckBox = false;
        mSelectedList = new ArrayList<String>();
	}

    public void setShowCheckBox(boolean isShow){
        mShowCheckBox = isShow;
    }

    public boolean getShowCheckBox(){
        return mShowCheckBox;
    }

	public void updateIndexer() {
		shortNameIndexer.updateIndexer();
	}

	@Override
	public int getPositionForSection(int section) {
		return shortNameIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return shortNameIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return shortNameIndexer.getSections();
	}

	public int getSectionForAlpha(String alpha) {
		return shortNameIndexer.getSectionForItem(alpha);
	}

	private class ViewHolder {
        CheckBox checkBox;
		ImageView headView;
		TextView title;
		TextView nameInSelect;
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
					null);
			holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.aliwx_select_box);
			holder.headView = (ImageView) convertView.findViewById (R.id.aliwx_head);
			holder.headView.setOnClickListener(this);
			holder.title = (TextView) convertView.findViewById(R.id.aliwx_title);
			holder.nameInSelect = (TextView) convertView
					.findViewById(R.id.aliwx_select_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mContactlist != null) {
			ComparableContact user = mContactlist.get(position);
			if (user != null) {
				String showname = user.getShowName();
				holder.nameInSelect.setText(showname);
				holder.nameInSelect.setVisibility(View.VISIBLE);
				holder.headView.setTag(R.id.aliwx_head, position);
				mHelper.setHeadView(holder.headView, user.getUserId(), user.getAppKey(), user.getOnlineStatus() == 0);
				//list里存了首个有alpha字母的联系人的位置，所以只有这些position 的要出现字母栏，其他visibility.gone
				int section = getSectionForPosition(position);
				if (getPositionForSection(section) == position) {
					String title = (String) shortNameIndexer.getSections()[section];
					holder.title.setText(title);
					holder.title.setVisibility(View.VISIBLE);
				} else {
					holder.title.setVisibility(View.GONE);
				}
                if (mShowCheckBox){
                    holder.checkBox.setVisibility(View.VISIBLE);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                }
			}

		}
		return convertView;
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.aliwx_head) {
			if (v.getTag (R.id.aliwx_head) instanceof Integer) {
				int position = (Integer) v.getTag (R.id.aliwx_head);
			}

		}
	}

	public int getMaxVisibleCount() {
		return maxVisibleCount;
	}

	public void setMaxVisibleCount(int maxVisibleCount) {
		this.maxVisibleCount = maxVisibleCount;
	}

	@Override
	public void loadAsyncTask() {
		mHelper.setMaxVisible(maxVisibleCount);
		mHelper.loadAyncHead();
//		mHelper.loadLazyImage();
	}

    public List<String> getSelectedList(){
        return mSelectedList;
    }

    public void onItemClick(View view, ComparableContact contact){
        ViewHolder holder = (ViewHolder) view.getTag();
        CheckBox checkBox = holder.checkBox;
        boolean isChecked = checkBox.isChecked();
        if (isChecked){
            mSelectedList.remove(contact.getUserId());
        }else {
            mSelectedList.add(contact.getUserId());
        }
        checkBox.setChecked(!isChecked);
    }

}