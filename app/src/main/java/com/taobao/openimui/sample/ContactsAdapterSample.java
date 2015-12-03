package com.taobao.openimui.sample;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.WXAPI;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactProfileCallback;
import com.alibaba.mobileim.contact.YWOnlineContact;
import com.alibaba.mobileim.kit.common.YWAsyncBaseAdapter;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.mobileim.lib.presenter.contact.IIMContact;
import com.alibaba.openIMUIDemo.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapterSample extends YWAsyncBaseAdapter {
    private static final String TAG = "ContactsAdapterSample";
    private Context context;
    private int max_visible_item_count;
    private LayoutInflater inflater;
    private YWContactHeadLoadHelper mContactHeadLoadHelper;
    private List<ContactImpl> mList;
    private boolean mShowCheckBox;
    private List<ContactImpl> mSelectedList;


    public ContactsAdapterSample(Activity context, List<ContactImpl> list) {
        this.context = context;
        this.mList = list;
        mContactHeadLoadHelper = new YWContactHeadLoadHelper(context, this);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mShowCheckBox = false;
        mSelectedList = new ArrayList<ContactImpl>();
    }

    private class ViewHolder {
        CheckBox checkBox;
        ImageView headView;
        TextView nameInSelect;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < mList.size()) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void loadAsyncTask() {
        mContactHeadLoadHelper.setMaxVisible(max_visible_item_count);
        mContactHeadLoadHelper.loadAyncHead();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.demo_contacts_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.FILL_PARENT, context.getResources()
                    .getDimensionPixelSize(R.dimen.aliwx_message_item_height)));
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
            holder.headView = (ImageView) convertView.findViewById(R.id.head);
            holder.nameInSelect = (TextView) convertView
                    .findViewById(R.id.select_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mList != null) {
            ContactImpl user = mList.get(position);
            if (user != null) {
                String name = user.getShowName();
                holder.headView.setTag(R.id.head, position);
                IYWContactProfileCallback callback = WXAPI.getInstance()
                        .getContactProfileCallback();
                if (callback != null && callback.onFetchContactInfo(user.getUserId()) != null) {
                    IYWContact contact = callback.onFetchContactInfo(user.getUserId());
                    if (contact != null) {
                        mContactHeadLoadHelper.setHeadView(holder.headView, user.getUserId(),user.getAppKey(), user.getOnlineStatus() == 0);
                        if (TextUtils.isEmpty(name)) { //如果之前自定义了SHOW_NAME，那么优先使用之前定义的。
                            holder.nameInSelect.setText(contact.getShowName());
                        } else {
                            holder.nameInSelect.setText(name);
                        }
                    }
                } else {
                    IIMContact  contact = (IIMContact)WXAPI.getInstance().getWXIMContact(user.getUserId());
                    mContactHeadLoadHelper.setHeadView(holder.headView, user.getUserId(),user.getAppKey(), user.getOnlineStatus() == 0);
                    holder.nameInSelect.setText(name);
                }
                holder.nameInSelect.setVisibility(View.VISIBLE);
            }
            if (mShowCheckBox){
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    public void setMax_visible_item_count(int max_visible_item_count) {
        this.max_visible_item_count = max_visible_item_count;
    }


    public static class ContactImpl implements IYWContact {
        private String userid = "", appKey = "", avatarPath = "", showName = "";
        private int status= YWOnlineContact.ONLINESTATUS_ONLINE;

        public ContactImpl(String showName, String userid, String avatarPath, String signatures, String appKey) {
            this.showName = showName;
            this.userid = userid;
            this.avatarPath = avatarPath;
            this.appKey = appKey;
        }
        public void setOnlineStatus(int status){
            this.status=status;
        }
        @Override
        public String getUserId() {
            return userid;
        }

        @Override
        public String getAppKey() {
            return appKey;
        }

        @Override
        public String getAvatarPath() {
            return avatarPath;
        }

        @Override
        public String getShowName() {
            return showName;
        }

        public int getOnlineStatus() {
            return status;
        }

    }

}
