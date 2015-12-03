package com.taobao.openimui.demo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.gingko.presenter.tribe.IYWTribeChangeListener;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.sample.CustomConversationHelper;
import com.taobao.openimui.sample.LoginSampleHelper;
import com.taobao.openimui.tribe.TribeConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mayongge on 15-9-22.
 */
public class FragmentTabs extends FragmentActivity {

    public static final String TAG = "FragmentTabs";
    public static final String LOGIN_SUCCESS = "loginSuccess";

    public static final String TAB_MESSAGE = "message";
    public static final String TAB_CONTACT = "contact";
    public static final String TAB_TRIBE = "tribe";
    public static final String TAB_MORE = "more";

    private TextView mMessageTab;
    private TextView mContactTab;
    private TextView mTribeTab;
    private TextView mMoreTab;
    private TextView mUnread;

    private Drawable mMessagePressed;
    private Drawable mMessageNormal;
    private Drawable mFriendPressed;
    private Drawable mFriendNormal;
    private Drawable mTribePressed;
    private Drawable mTribeNormal;
    private Drawable mMorePressed;
    private Drawable mMoreNormal;

    private FragmentTabHost mTabHost;
    private YWIMKit mIMKit;
    private IYWConversationService mConversationService;
    private IYWConversationUnreadChangeListener mConversationUnreadChangeListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IYWTribeChangeListener mTribeChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIMKit = LoginSampleHelper.getInstance().getIMKit();
        if (mIMKit == null) {
            return;
        }
        mConversationService = mIMKit.getConversationService();
        initConversationServiceAndListener();
        addTribeChangeListener();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.demo_fragment_tabs);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        CustomConversationHelper.addCustomConversation("myconversation", null);
        CustomConversationHelper.addCustomViewConversation("myConversation","这个会话的展示布局可以自定义");
        View indicator = getIndicatorView(TAB_MESSAGE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MESSAGE).setIndicator(indicator), mIMKit.getConversationFragmentClass(), null);

        indicator = getIndicatorView(TAB_CONTACT);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_CONTACT).setIndicator(indicator), ContactsFragment.class, null);

        indicator = getIndicatorView(TAB_TRIBE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_TRIBE).setIndicator(indicator), TribeFragment.class, null);

        indicator = getIndicatorView(TAB_MORE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MORE).setIndicator(indicator), MoreFragment.class, null);

        mUnread = (TextView) findViewById(R.id.unread);

        mTabHost.setOnTabChangedListener(listener);
        listener.onTabChanged(TAB_MESSAGE);

    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (TAB_MESSAGE.equals(tabId)) {
                setMessageText(true);
                setContactText(false);
                setTribeText(false);
                setMoreText(false);
                return;
            }
            if (TAB_CONTACT.equals(tabId)) {
                setMessageText(false);
                setContactText(true);
                setTribeText(false);
                setMoreText(false);
                return;
            }
            if (TAB_TRIBE.equals(tabId)) {
                setMessageText(false);
                setContactText(false);
                setTribeText(true);
                setMoreText(false);
                return;
            }
            if (TAB_MORE.equals(tabId)) {
                setMessageText(false);
                setContactText(false);
                setTribeText(false);
                setMoreText(true);
                return;
            }
        }
    };

    private View getIndicatorView(String tab) {
        View tabView = View.inflate(this, R.layout.demo_tab_item, null);
        TextView indicator = (TextView) tabView.findViewById(R.id.tab_text);
        Drawable drawable;

        if (tab.equals(TAB_MESSAGE)) {
            indicator.setText("消息");
            drawable = getResources().getDrawable(R.drawable.demo_tab_icon_message_normal);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mMessageTab = indicator;
        } else if (tab.equals(TAB_CONTACT)) {
            indicator.setText("联系人");
            drawable = getResources().getDrawable(R.drawable.demo_tab_icon_contact_normal);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mContactTab = indicator;
        } else if (tab.equals(TAB_TRIBE)) {
            indicator.setText("群组");
            drawable = getResources().getDrawable(R.drawable.demo_tab_icon_tribe_normal);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mTribeTab = indicator;
        } else if (tab.equals(TAB_MORE)) {
            indicator.setText("更多");
            drawable = getResources().getDrawable(R.drawable.demo_tab_icon_setting_normal);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mMoreTab = indicator;
        }
        return tabView;
    }

    private void initConversationServiceAndListener() {
        mConversationUnreadChangeListener = new IYWConversationUnreadChangeListener() {

            @Override
            public void onUnreadChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoginSampleHelper loginHelper = LoginSampleHelper.getInstance();
                        final YWIMKit imKit = loginHelper.getIMKit();
                        mConversationService = imKit.getConversationService();
                        int unReadCount = mConversationService.getAllUnreadCount();
                        if (unReadCount > 0) {
                            mUnread.setVisibility(View.VISIBLE);
                            if (unReadCount < 100) {
                                mUnread.setText(unReadCount + "");
                            } else {
                                mUnread.setText("99+");
                            }
                        } else {
                            mUnread.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        };
    }

    private void addTribeChangeListener(){
        mTribeChangedListener = new IYWTribeChangeListener() {
            @Override
            public void onInvite(YWTribe tribe, YWTribeMember user) {
                Map<YWTribe, YWTribeMember> map = new HashMap<YWTribe, YWTribeMember>();
                map.put(tribe, user);
                LoginSampleHelper.getInstance().getTribeInviteMessages().add(map);
                String userName = user.getShowName();
                if (TextUtils.isEmpty(userName)){
                    userName = user.getUserId();
                }
                WxLog.i(TAG, "onInvite");
            }

            @Override
            public void onUserJoin(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onUserQuit(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onUserRemoved(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onTribeDestroyed(YWTribe tribe) {

            }

            @Override
            public void onTribeInfoUpdated(YWTribe tribe) {

            }

            @Override
            public void onTribeManagerChanged(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onTribeRoleChanged(YWTribe tribe, YWTribeMember user) {

            }
        };
        mIMKit.getTribeService().addTribeListener(mTribeChangedListener);
    }

    private void setMessageText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mMessageTab.setTextColor(getResources().getColor(
                    R.color.tab_pressed_color));
            if (mMessagePressed == null) {
                mMessagePressed = getResources().getDrawable(
                        R.drawable.demo_tab_icon_message_pressed);
            }
            drawable = mMessagePressed;
        } else {
            mMessageTab.setTextColor(getResources().getColor(
                    R.color.tab_normal_color));
            if (mMessageNormal == null) {
                mMessageNormal = getResources().getDrawable(
                        R.drawable.demo_tab_icon_message_normal);
            }
            drawable = mMessageNormal;
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            mMessageTab.setCompoundDrawables(null, drawable, null, null);
        }
    }


    private void setContactText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mContactTab.setTextColor(getResources().getColor(
                    R.color.tab_pressed_color));
            if (mFriendPressed == null) {
                mFriendPressed = getResources().getDrawable(
                        R.drawable.demo_tab_icon_contact_pressed);
            }
            drawable = mFriendPressed;
        } else {
            mContactTab.setTextColor(getResources().getColor(
                    R.color.tab_normal_color));
            if (mFriendNormal == null) {
                mFriendNormal = getResources().getDrawable(
                        R.drawable.demo_tab_icon_contact_normal);
            }
            drawable = mFriendNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            mContactTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    private void setTribeText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mTribeTab.setTextColor(getResources().getColor(
                    R.color.tab_pressed_color));
            if (mTribePressed == null) {
                mTribePressed = getResources().getDrawable(
                        R.drawable.demo_tab_icon_tribe_pressed);
            }
            drawable = mTribePressed;
        } else {
            mTribeTab.setTextColor(getResources().getColor(
                    R.color.tab_normal_color));
            if (mTribeNormal == null) {
                mTribeNormal = getResources().getDrawable(
                        R.drawable.demo_tab_icon_tribe_normal);
            }
            drawable = mTribeNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            mTribeTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    private void setMoreText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mMoreTab.setTextColor(getResources().getColor(
                    R.color.tab_pressed_color));
            if (mMorePressed == null) {
                mMorePressed = getResources().getDrawable(
                        R.drawable.demo_tab_icon_setting_pressed);
            }
            drawable = mMorePressed;
        } else {
            mMoreTab.setTextColor(getResources().getColor(
                    R.color.tab_normal_color));
            if (mMoreNormal == null) {
                mMoreNormal = getResources().getDrawable(
                        R.drawable.demo_tab_icon_setting_normal);
            }
            drawable = mMoreNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            mMoreTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //在Tab栏删除会话未读消息变化的全局监听器
        mConversationService.removeTotalUnreadChangeListener(mConversationUnreadChangeListener);
        mIMKit.getTribeService().removeTribeListener(mTribeChangedListener);
        YWLog.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginSampleHelper loginHelper = LoginSampleHelper.getInstance();
        final YWIMKit imKit = loginHelper.getIMKit();
        mConversationService = imKit.getConversationService();

        //resume时需要检查全局未读消息数并做处理，因为离开此界面时删除了全局消息监听器
        mConversationUnreadChangeListener.onUnreadChange();

        //在Tab栏增加会话未读消息变化的全局监听器
        mConversationService.addTotalUnreadChangeListener(mConversationUnreadChangeListener);
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(LOGIN_SUCCESS) != null){
            listener.onTabChanged(TAB_MESSAGE);
            getIntent().removeExtra(LOGIN_SUCCESS);
        }

        IYWTribeService tribeService = imKit.getTribeService();
        tribeService.addTribeListener(mTribeChangedListener);
        YWLog.i(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        YWLog.i(TAG, "onDestroy");
        super.onDestroy();

        if (mMessageNormal != null) {
            mMessageNormal.setCallback(null);
        }
        if (mMessagePressed != null) {
            mMessagePressed.setCallback(null);
        }
        if (mFriendNormal != null) {
            mFriendNormal.setCallback(null);
        }
        if (mFriendPressed != null) {
            mFriendPressed.setCallback(null);
        }

        if (mTribeNormal != null) {
            mTribeNormal.setCallback(null);
        }
        if (mTribePressed != null) {
            mTribePressed.setCallback(null);
        }
        if (mMoreNormal != null) {
            mMoreNormal.setCallback(null);
        }
        if (mMorePressed != null) {
            mMorePressed.setCallback(null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        long tribeId = intent.getLongExtra(TribeConstants.TRIBE_ID, 0);
        if (tribeId != 0){
            setIntent(intent);
            mTabHost.setCurrentTab(1);
        }

        String tribeOp = intent.getStringExtra(TribeConstants.TRIBE_OP);
        if (!TextUtils.isEmpty(tribeOp)){
            setIntent(intent);
            mTabHost.setCurrentTab(2);
        }
        YWLog.i(TAG, "onNewIntent");
    }


}
