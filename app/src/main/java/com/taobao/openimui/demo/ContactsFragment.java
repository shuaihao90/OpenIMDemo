package com.taobao.openimui.demo;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWConstants;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;

import com.alibaba.mobileim.channel.util.AccountUtils;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactProfileCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.alibaba.mobileim.contact.IYWOnlineContact;
import com.alibaba.mobileim.contact.YWContactManager;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.fundamental.widget.YWAlertDialog;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.lib.model.contact.Contact;
import com.alibaba.mobileim.lib.presenter.contact.ContactManager;
import com.alibaba.mobileim.lib.presenter.contact.IContactListListener;
import com.alibaba.mobileim.lib.presenter.contact.IContactManager;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.mobileim.utility.AccountInfoTools;
import com.alibaba.openIMUIDemo.R;
import com.alibaba.tcms.env.YWEnvManager;
import com.alibaba.tcms.env.YWEnvType;
import com.taobao.openimui.common.Notification;
import com.taobao.openimui.feature.contact.adapter.ContactsAdapter;
import com.taobao.openimui.feature.contact.adapter.SearchAdapter;
import com.taobao.openimui.feature.contact.component.ComparableContact;
import com.taobao.openimui.feature.contact.component.ISearchable;
import com.taobao.openimui.feature.contact.component.SearchFilter;
import com.taobao.openimui.feature.contact.view.DummyHeadListView;
import com.taobao.openimui.feature.contact.view.LetterListView;
import com.taobao.openimui.feature.contact.view.PullToRefreshBase;
import com.taobao.openimui.feature.contact.view.PullToRefreshDummyHeadListView;
import com.taobao.openimui.sample.ChattingFragmentSample;
import com.taobao.openimui.sample.ContactsAdapterSample.ContactImpl;
import com.taobao.openimui.sample.LoginSampleHelper;
import com.taobao.openimui.tribe.TribeConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ［联系人列表/联系人在线状态查询/黑名单操作/联系人首字母索引/联系人搜索/添加联系人] 的示例代码
 *
 *
 */
public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener ,View.OnClickListener,IContactListListener {

    private static final String TAG = "ContactsFragment";
    private static final int ISV = 1;
    private static final int TB = 2;

    public static final String SEND_CARD = "sendCard";

    private List<ContactImpl> mContacts = new ArrayList<ContactImpl>();
    //查询在线状态需要的map
    private Map<String,ComparableContact> mComparableContactsMap =new HashMap<String, ComparableContact>();


    private View mView;
    private YWIMKit mIMKit;
    private Activity mContext;
    private String mUserId;

    private boolean mIsTribeOP;
    private boolean mIsSendCard;

    private View mProgress;
    private PullToRefreshDummyHeadListView mPullToRefreshListView;
    private ContactsAdapter mContactsAdapter;
    private DummyHeadListView mListView;; // 列表视图
    private List<ComparableContact> mComparableContacts = new ArrayList<ComparableContact>();

    private View mHeadView;
    private RelativeLayout mListTop;
    private RelativeLayout mDummyListTop;



    private float density;
    private int headBarHeight;

    private Handler mHandler = new Handler();
    private int mMaxVisibleCount;
    //应用类型
    private int appType;

     //[搜索联系人的状态控制器]
    private SearcherStateManager  mSearcherStateManager;
    //[首字母索引联系人的状态控制器]
    private LetterIndexerStateManager  mLetterIndexerStateManager;


    public void initStateManagers(){
        mSearcherStateManager=new SearcherStateManager(this);
        mLetterIndexerStateManager=new LetterIndexerStateManager(this);
    }

    public ContactsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIMKit = LoginSampleHelper.getInstance().getIMKit();
        mUserId = mIMKit.getIMCore().getLoginUserId();
        if (TextUtils.isEmpty(mUserId)) {
            YWLog.i(TAG, "user not login");
        }

        mIsTribeOP = false;
        mIsSendCard = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            String tribeOP = (String) bundle.get(TribeConstants.TRIBE_OP);
            if (!TextUtils.isEmpty(tribeOP)) {
                mIsTribeOP = true;
            }
            String sendCard = (String) bundle.get(SEND_CARD);
            if (!TextUtils.isEmpty(sendCard)){
                mIsSendCard = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mView!= null){
            ViewGroup parent = (ViewGroup)mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
            return mView;
        }
        mContext = getActivity();
        mContext.getWindow().setWindowAnimations(0);
        mView = inflater.inflate(R.layout.demo_fragment_contacts, container, false);
        mProgress = mView.findViewById(R.id.progress);
        init();
        return mView;
    }
    @Override
    public void onResume() {
        super.onResume();

        addListeners();

        if (mIsTribeOP){
            initInviteTribeMemberList();

        }
        WxLog.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        removeListeners();
        Intent intent = getActivity().getIntent();
        if(intent != null && intent.getLongExtra(TribeConstants.TRIBE_ID, 0) != 0){
            intent.putExtra(TribeConstants.TRIBE_ID, 0);
            getActivity().setIntent(intent);
        }
        WxLog.i(TAG, "onPause");
    }

    protected void init() {
        //----判断应用类型后方便测试Demo，开发者不用关心此调用。-----
        judgeAppType();
        initStateManagers();
        //-----------------------------------------------------
        initAdapters();
        initView();
        IOGetContacts(false);

    }

    private void addListeners() {
        IYWContactService contactService = LoginSampleHelper.getInstance().getIMKit().getContactService();
        contactService.registerContactsListener(this);
    }
    private void removeListeners() {
        IYWContactService contactService = LoginSampleHelper.getInstance().getIMKit().getContactService();
        contactService.unRegisterContactsListener(this);
    }

    private void initView() {
        initTitle();
        //初始化下拉刷新
        initPullToRefreshListView();
        //初始化联系人列表
        initChildListView();
        //初始化搜索
        mSearcherStateManager.initSearchListView();
        //初始化字母listview，touch监听定位主listview
        mLetterIndexerStateManager.initLetterView();

    }

    //----------联系人在线状态查询/黑名单操作的示例代码-----------------------------------------------------
    /**
     * 同步联系人的“在线状态”的示例代码,建议的使用方式：暂时是轮询触发结合特定时机的触发。
     */
    private void syncContactsOnlineStatus() {
        YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
        IYWContactService contactService = imKit.getContactService();
        /**
         * 请求联系人在线状态
         * @param contacts 必须设置userid和appkey
         * @param result 注意：回调运行在主线程，即UI线程
         */
        contactService.syncContactsOnlineStatus((List<IYWContact>) (List) mComparableContacts, new IWxCallback() {
            //已经在UI线程
            @Override
            public void onSuccess(Object... result) {
                Map<String, IYWOnlineContact> contacts = (Map<String, IYWOnlineContact>) result[0];
                if (contacts != null) {
                    for (Map.Entry<String, IYWOnlineContact> entry : contacts
                            .entrySet()) {
                        IYWOnlineContact ct = entry
                                .getValue();
                        String uid = entry.getKey();
                        IYWContact contact = (IYWContact) getContactFromMap(uid);

                        if (contact != null && contact instanceof ComparableContact) {
                            ((ComparableContact) contact).setOnlineStatus(ct
                                    .getOnlineStatus());
                        }
                    }
                    mContactsAdapter.notifyDataSetChangedWithAsyncLoad();
                    mSearchAdapter.notifyDataSetChangedWithAsyncLoad();
                }

            }

            //已经在UI线程
            @Override
            public void onError(int code, String info) {
                mContactsAdapter.notifyDataSetChangedWithAsyncLoad();
                mSearchAdapter.notifyDataSetChangedWithAsyncLoad();
            }

            //已经在UI线程
            @Override
            public void onProgress(int progress) {

            }

        });
    }

    /**
     * 黑名单操作的示例代码
     * @param contact
     */
    public void handleOnUserLongClick(final ComparableContact contact){
        YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
        final IYWContactService contactService = imKit.getIMCore().getContactService();
        final String[] items = new String[1];
        boolean isBlocked = contactService.isBlackContact(contact.getUserId(), contact.getAppKey());
        if (isBlocked) {
            items[0] = "移除黑名单";
        } else {
            items[0] = "加入黑名单";
        }

        if(!YWContactManager.isBlackListEnable()) {
            YWContactManager.enableBlackList();
        }
        //此处为示例代码
        new YWAlertDialog.Builder(mContext)
                .setTitle(contact.getUserId())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("加入黑名单")) {
                            contactService.addBlackContact(contact.getUserId(), contact.getAppKey(), new IWxCallback() {
                                @Override
                                public void onSuccess(Object... result) {
                                    IYWContact iywContact = (IYWContact) result[0];
                                    YWLog.i(TAG, "加入黑名单成功, id = " + iywContact.getUserId() + ", appkey = " + iywContact.getAppKey());
                                    Notification.showToastMsg(mContext, "加入黑名单成功, id = " + iywContact.getUserId() + ", appkey = " + iywContact.getAppKey());
                                }

                                @Override
                                public void onError(int code, String info) {
                                    YWLog.i(TAG, "加入黑名单失败，code = " + code + ", info = " + info);
                                    Notification.showToastMsg(mContext, "加入黑名单失败，code = " + code + ", info = " + info);
                                }

                                @Override
                                public void onProgress(int progress) {

                                }
                            });
                        } else if (items[which].equals("移除黑名单")) {
                            contactService.removeBlackContact(contact.getUserId(), contact.getAppKey(), new IWxCallback() {
                                @Override
                                public void onSuccess(Object... result) {
                                    IYWContact iywContact = (IYWContact) result[0];
                                    YWLog.i(TAG, "移除黑名单成功,  id = " + iywContact.getUserId() + ", appkey = " + iywContact.getAppKey());
                                    Notification.showToastMsg(mContext, "移除黑名单成功,  id = " + iywContact.getUserId() + ", appkey = " + iywContact.getAppKey());
                                }

                                @Override
                                public void onError(int code, String info) {
                                    YWLog.i(TAG, "移除黑名单失败，code = " + code + ", info = " + info);
                                    Notification.showToastMsg(mContext, "移除黑名单失败，code = " + code + ", info = " + info);
                                }

                                @Override
                                public void onProgress(int progress) {

                                }
                            });
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create().show();

    }

    //------------------------------------------------------------------------------------------------------------------------------------




    private void initChildListView() {
        //实例化listview
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        if (Build.VERSION.SDK_INT >= 9) {
            mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        density = dm.density;
        headBarHeight = (int) (45 * density);

        //实例化headview，交给下拉刷新
        mHeadView = mContext.getLayoutInflater().inflate(
                R.layout.aliwx_contacts_header_layout, null);

        mSearchLayout = mHeadView.findViewById (R.id.aliwx_search_layout);
        mSearchLayout.setOnClickListener(this);
        mListTop = (RelativeLayout) mHeadView.findViewById (R.id.aliwx_list_top);
        mDummyListTop = (RelativeLayout) mView.findViewById(R.id.aliwx_dummy_list_top);
        mListTop.setVisibility(View.GONE);
        mDummyListTop.setVisibility(View.GONE);

        //fake，用于上拉的时候“始终”可见，交给listview
        //mListView.setDumyGroupView(mDummyListTop);
        if (!mIsTribeOP && !mIsSendCard) {
            mListView.addHeaderView(mHeadView);
        }
        mListView.setOnScrollListener(this);
        mListView.setAdapter(mContactsAdapter);
        mListView.setSelectionFromTop(0, -headBarHeight);
    }

    public void initAdapters(){
        //联系人的适配器
        mContactsAdapter = new ContactsAdapter(mContext, mComparableContacts,ContactsAdapter.CONTACTS);
        if (mIsTribeOP || mIsSendCard){
            mContactsAdapter.setShowCheckBox(true);
        }
        //搜索的适配器，同时实例化，方便切换
        mSearchAdapter = new SearchAdapter(mContext, mSearchContactList,SearchAdapter.CONTACTS);
        //搜索的筛选器
        mFilter = new SearchFilter(mSearchContactList);
    }

    private void initPullToRefreshListView() {
        mPullToRefreshListView = (PullToRefreshDummyHeadListView) mView.findViewById(R.id.aliwx_listview);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
        mPullToRefreshListView.setShowIndicator(false);
        mPullToRefreshListView.setDisableScrollingWhileRefreshing(false);
        mPullToRefreshListView.setPullLabel("同步联系人");
        mPullToRefreshListView.setRefreshingLabel("同步联系人");
        mPullToRefreshListView.setReleaseLabel("松开同步联系人");
        mPullToRefreshListView.resetHeadLayout();
        mPullToRefreshListView.setEnabled(true);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {
            @Override
            public void onPullDownToRefresh() {
                //netio刷新联系人
                //todo 备注:[由于添加联系人目前只同步数据到Cache和DB，因此这里只能本地同步刷新，否则会刷掉添加联系人产生的数据
                IOGetContacts(false);

            }

            @Override
            public void onPullUpToRefresh() {
            }
        });
    }


    public void onGetContactsSuccess(boolean forceViaNet, boolean isViaNet){
        initAlphaContacts(forceViaNet, isViaNet);
    }
    public void onGetContactsFail(){

    }
    private void initAlphaContacts(final boolean forceViaNet,final boolean isViaNet){
        List<ComparableContact> localComparableContacts = new ArrayList<ComparableContact>();
        for (ContactImpl contact : mContacts) {

            if(contact==null)continue;

            IYWContactProfileCallback callback =mIMKit.getContactService()
                    .getContactProfileCallback();
            IYWContact mCustomIYWContact = callback.onFetchContactInfo(contact.getUserId());
            ComparableContact mComparableContact;
            if(TextUtils.isEmpty(contact.getAvatarPath()) && mCustomIYWContact != null && !TextUtils.isEmpty(mCustomIYWContact.getAvatarPath())) {
                mComparableContact = new ComparableContact(contact.getShowName(), contact.getUserId(), mCustomIYWContact.getAvatarPath(), contact.getAppKey());
            } else {
                mComparableContact = new ComparableContact(contact.getShowName(), contact.getUserId(), contact.getAvatarPath(), contact.getAppKey());
            }

            if(TextUtils.isEmpty(contact.getShowName())){
                mComparableContact.setShowName(contact.getUserId());
            }
            //nick与userid相同则执行取自定义nick，否则就是认为在服务器设置好了,不设自定义nick
            if (callback != null && mCustomIYWContact != null&&contact.getUserId().equals(contact.getShowName())) {
                mComparableContact.setShowName(mCustomIYWContact.getShowName());
            }
            mComparableContact.generateSpell();
            localComparableContacts.add(mComparableContact);
        }
        if (localComparableContacts != null && localComparableContacts.size() > 0) {
            ComparableContact[] contacts = localComparableContacts
                    .toArray(new ComparableContact[localComparableContacts.size()]);
            Arrays.sort(contacts);
            localComparableContacts = Arrays.asList(contacts);
            final  List<ComparableContact> localContactList= localComparableContacts;

            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (localContactList != null) {
                        mComparableContacts.clear();
                        mComparableContacts.addAll(localContactList);
                    }

                    addContactsToMap();
                    //同步联系人在线状态
                    if (mComparableContacts.size() > 0) {
                        syncContactsOnlineStatus();
                    }
                    //如果是网络获取的，则异步地同步到DB
                    if(isViaNet)
                        asynchronousSyncNetToDB(mComparableContacts);
                    mFilter.clear();
                    //会用搜索的方式重新排列list，重排的结果不符合首字母索引的要求
                    mFilter.addSearchList(localContactList);
                    mContactsAdapter.updateIndexer();
                    mContactsAdapter.notifyDataSetChangedWithAsyncLoad();
                    mPullToRefreshListView.onRefreshComplete(forceViaNet, true);
                }
            });
        }
    }

    //网络获取联系人是异步回调方式，此为示例代码
    public List<ContactImpl>  netIOGetContacts(){
        List<ContactImpl> mContacts = new ArrayList<ContactImpl>();
        //客服账号请一定传该appkey，否则无法正确获取客服账号的在线状态
        mContacts.add(new ContactImpl("openim官方客服", "openim官方客服", "pic_1_cs", "", YWConstants.YWSDKAppKey));
        //普通账号请一定传该账号所属app的appkey，否则无法正确获取该账号的在线状态
        mContacts.add(new ContactImpl("百川开发者大会小秘书", "百川开发者大会小秘书", "pic_1_bc", "", LoginSampleHelper.APP_KEY));
        mContacts.add(new ContactImpl("云大旺", "云大旺", "pic_1_lg", "", LoginSampleHelper.APP_KEY));
        mContacts.add(new ContactImpl("云二旺", "云二旺", "pic_1_lg", "", LoginSampleHelper.APP_KEY));
        mContacts.add(new ContactImpl("云三旺", "云三旺", "pic_1_lg", "", LoginSampleHelper.APP_KEY));
        mContacts.add(new ContactImpl("云四旺", "云四旺", "pic_1_lg", "", LoginSampleHelper.APP_KEY));
        mContacts.add(new ContactImpl("云小旺", "云小旺", "pic_1_lg", "", LoginSampleHelper.APP_KEY));
        for (int i = 1; i < 11; i++) {
            if (YWEnvManager.getEnv(DemoApplication.getContext()) == YWEnvType.ONLINE){
                mContacts.add(new ContactImpl("", "uid" + i, "", "", LoginSampleHelper.APP_KEY));
            }else  if (YWEnvManager.getEnv(DemoApplication.getContext()) == YWEnvType.TEST){
                mContacts.add(new ContactImpl("", "测" + i, "", "", LoginSampleHelper.APP_KEY_TEST));
            }
        }
        return mContacts;
    }

    public void asynchronousSyncNetToDB(List<ComparableContact> comparableContacts){
        IWxCallback result = new IWxCallback() {

            @Override
            public void onSuccess(Object... result) {

            }

            @Override
            public void onError(int code, String info) {

            }

            @Override
            public void onProgress(int progress) {

            }
        };


        mIMKit.getContactService().asynchronousSyncContactsToCacheAndDB((List<IYWDBContact>) (List) comparableContacts,result);
    }

    public List<ContactImpl>  cacheIOOrDBIOGetContacts(){
        List<ContactImpl> mContacts = new ArrayList<ContactImpl>();
        List<IYWDBContact> contactsFromCache = (List<IYWDBContact>) (List) mIMKit.getContactService().getContactsFromCache();
        for(IYWDBContact DBContact:contactsFromCache){
            ContactImpl contact = new ContactImpl(DBContact.getShowName(), DBContact.getUserId(), DBContact.getAvatarPath(), "", DBContact.getAppKey());
            mContacts.add(contact);
        }
        return mContacts;
    }
    //DB或网络获取联系人是异步回调方式，此为示例代码,[注意：appType==TB部分是方便测试DEMO的代码，开发者不用关心]
    public void IOGetContacts(boolean forceViaNet){
        if(appType==ISV){
            boolean isViaNet = true;
            List<ContactImpl> localContacts=null;
            if(forceViaNet){
                localContacts = netIOGetContacts();
                if(localContacts.size()==0){
                    onGetContactsFail();
                    return;
                }
            }else{
                localContacts = cacheIOOrDBIOGetContacts();
                if(localContacts!=null&&localContacts.size()>0){
                        isViaNet=false;
                        //已通过CacheIO或DBIO获取到Contacts
                }else{
                    localContacts = netIOGetContacts();
                    if(localContacts.size()==0){
                        return;
                    }
                }
            }
            mContacts.clear();
            mContacts.addAll(localContacts);
            //IO返回结果成功后在回调中更新联系人
            onGetContactsSuccess(forceViaNet,isViaNet);
        }

        //－－－－－－判断应用类型后方便测试Demo，开发者不用关心这部分代码。－－－－－
        else if(appType== TB){
            List<ContactImpl> localContacts=getTBContacts();
            mContacts.clear();
            mContacts.addAll(localContacts);
            syncTBContactsInfo();
        }
        //－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－

    }


    private void initTitle() {
        RelativeLayout titleBar = (RelativeLayout) mView.findViewById(R.id.title_bar);
        TextView titleView = (TextView) mView.findViewById(R.id.title_self_title);
        TextView leftButton = (TextView) mView.findViewById(R.id.left_button);
        leftButton.setVisibility(View.GONE);

        titleBar.setBackgroundColor(Color.parseColor("#00b4ff"));
        titleView.setTextColor(Color.WHITE);
        titleView.setText("联系人");
        titleBar.setVisibility(View.VISIBLE);

        TextView rightButton = (TextView) mView.findViewById(R.id.right_button);
        rightButton.setTextColor(Color.WHITE);
        rightButton.setText("添加联系人");
        rightButton.setVisibility(View.VISIBLE);

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new FindContactFragement(),true);
            }
        });


        if (mIsTribeOP || mIsSendCard){
            titleBar.setVisibility(View.GONE);
            View shadow = mView.findViewById(R.id.title_bar_shadow_view);
            if (shadow != null) {
                shadow.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public void onClick(View v) {
        mSearcherStateManager.onClick(v);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (parent == mSearchListView) {
            if (position >= 0 && position < mSearchContactList.size()) {
                ISearchable iSearchable = mSearchContactList.get(position);
                if (iSearchable != null && iSearchable instanceof ComparableContact) {
                    ComparableContact userInfo = (ComparableContact) mSearchContactList.get(position);
                    if (userInfo != null) {
                        if (mContactsAdapter.getShowCheckBox()){
                            handleOnUserClick(view, userInfo);
                        } else {
                            handleOnUserClick(userInfo);
                        }
                    }
                }

            }
        }else if (parent == mListView) {
            int realPosition = position - mListView.getHeaderViewsCount();
            if (realPosition < 0) {
                return;
            }
            if (mComparableContacts != null && realPosition < mComparableContacts.size() && realPosition >= 0) {
                IYWContact mIYWContact = (IYWContact) mComparableContacts.get(realPosition);
                if (mIYWContact != null && mIYWContact instanceof  ComparableContact) {
                    ComparableContact userInfo = (ComparableContact) mComparableContacts.get(realPosition);
                    if (userInfo != null) {
                        if (mContactsAdapter.getShowCheckBox()){
                            handleOnUserClick(view, userInfo);
                        } else {
                            handleOnUserClick(userInfo);
                        }
                    }

                }
            }
        }
    }

    public void handleOnUserClick(View view, ComparableContact contact){
        mContactsAdapter.onItemClick(view, contact);
    }

    public void handleOnUserClick(ComparableContact contact){
        if (contact.getAppKey().equals(YWConstants.YWSDKAppKey)) {
            //TB或千牛客的服账号
            EServiceContact eServiceContact = new EServiceContact(contact.getUserId(), 0);//
            final YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
            Intent intent = imKit.getChattingActivityIntent(eServiceContact);
            startActivity(intent);
        } else {
            Intent intent = new Intent(mContext, ChattingFragmentSample.class);
            intent.putExtra(ChattingFragmentSample.TARGET_ID, contact.getUserId());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == mSearchListView) {
            if (position >= 0 && position < mSearchContactList.size()) {
                ISearchable iSearchable = mSearchContactList.get(position);
                if (iSearchable != null && iSearchable instanceof ComparableContact) {
                    ComparableContact userInfo = (ComparableContact) mSearchContactList.get(position);
                    if (userInfo != null) {
                        handleOnUserLongClick(userInfo);
                    }
                }

            }
        } else if (parent == mListView) {
            int realPosition = position - mListView.getHeaderViewsCount();
            if (realPosition < 0) {
                return true;
            }
            if (mComparableContacts != null && realPosition < mComparableContacts.size() && realPosition >= 0) {
                IYWContact mIYWContact = (IYWContact) mComparableContacts.get(realPosition);
                if (mIYWContact != null && mIYWContact instanceof ComparableContact) {
                    ComparableContact userInfo = (ComparableContact) mComparableContacts.get(realPosition);
                    if (userInfo != null) {
                        handleOnUserLongClick(userInfo);
                    }

                }
            }
        }
        return true;
    }




    public void addContactsToMap(){
        for (ComparableContact contact : mComparableContacts) {
            mComparableContactsMap.put(contact.getUserId(), contact);
        }
    }

    public IYWContact getContactFromMap(String uid){
           return mComparableContactsMap.get(uid);
    }


    //objIO:增加了群成员
    public void onNewContact(IYWContact[] contacts) {
        if (contacts != null && contacts.length > 0) {
            IOGetContacts(false);
        }
    }

    //objIO:删除了群成员
    @Override
    public void onDeleteContact(String[] ids) {
        if (ids != null && ids.length > 0) {
            IOGetContacts(false);

        }
    }

    //objIO:群成员信息变化
    @Override
    public void onChange(int type) {
        if (type != ContactManager.ONLY_CHANGE_PROFILE){
            if (mPullToRefreshListView != null
                    && mPullToRefreshListView.getVisibility() == View.VISIBLE) {
                IOGetContacts(false);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // WxLog.d("test", "getScrollY:"+mListView.getScrollY());
        if (scrollState == SCROLL_STATE_IDLE && mContactsAdapter != null) {
            mContactsAdapter.loadAsyncTask();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {


        mMaxVisibleCount = visibleItemCount > mMaxVisibleCount ? visibleItemCount
                : mMaxVisibleCount;
        if(mContactsAdapter!=null)
            mContactsAdapter.setMaxVisibleCount(mMaxVisibleCount);
    }

//-------------------------[搜索联系人]的主要方法和变量，无需该功能的开发者不用过于关心以下方法和变量的细节。-------------------------------------------------

    //搜索
    private ListView mSearchListView;
    private View mSearchContactsLayout;
    private View mSearchLayout;
    private SearchAdapter mSearchAdapter;
    private List<ISearchable> mSearchContactList = new ArrayList<ISearchable>();
    private EditText mSearchText;
    private SearchFilter mFilter;
    private Button mCancelBtn;

    public  class SearcherStateManager{

        private  ContactsFragment  context;
        public SearcherStateManager(ContactsFragment context) {
            this.context=context;

        }
        private void initSearchListView () {
            mSearchText = (EditText) mView.findViewById(R.id.aliwx_search_key);
            mSearchText.addTextChangedListener(new SearchTextChangeWatcher());

            mCancelBtn = (Button) mView.findViewById(R.id.aliwx_cancel_search);
            mCancelBtn.setVisibility(View.VISIBLE);
            mCancelBtn.setOnClickListener(context);

            mSearchContactsLayout = mView.findViewById(R.id.aliwx_search_contacts_layout);
            mSearchContactsLayout.setOnClickListener(context);
            if (mIsTribeOP || mIsSendCard){
                mSearchContactsLayout.setVisibility(View.GONE);
            }

            mSearchListView = (ListView) mView.findViewById(R.id.aliwx_search_contacts_listview);
            mSearchListView.setAdapter(mSearchAdapter);
            if (Build.VERSION.SDK_INT >= 9) {
                mSearchListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
            mSearchListView.setOnScrollListener(context);
            mSearchListView.setOnItemClickListener(context);
            mSearchListView.setOnItemLongClickListener(context);
            mSearchListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyBoard();

                    String text = mSearchText.getText().toString();
                    if (TextUtils.isEmpty(text)) {
                        hideSearch();
                        return true;
                    }
                    return false;
                }
            });
        }

        private class SearchTextChangeWatcher implements TextWatcher {

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (TextUtils.isEmpty(text)) {
                    mSearchContactsLayout.setBackgroundColor(getResources()
                            .getColor (R.color.aliwx_halftransparent));
                } else {
                    mSearchContactsLayout.setBackgroundColor(getResources()
                            .getColor (R.color.aliwx_common_bg_color));
                }
                searchFriends();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        }

        private void hideSearch() {
            mSearchContactsLayout.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.VISIBLE);
            hideKeyBoard();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLetterView.setVisibility(View.VISIBLE);
                }
            }, 100);

        }

        private void searchFriends() {
            if (mSearchText != null) {
                final String searchWord = mSearchText.getText().toString();
                //filter执行,搜索期间context被其持有
                mFilter.filter(searchWord, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
                        mSearchAdapter.notifyDataSetChangedWithAsyncLoad();
                    }
                });
            }
        }

        private void showKeyBoard() {
            View view = mContext.getCurrentFocus();
            if (view != null) {
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(view, 0);
            }
        }
        protected void hideKeyBoard() {
            View view = mContext.getCurrentFocus();
            if (view != null) {
                ((InputMethodManager)  mContext.getSystemService(mContext.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        public void onClick(View v) {
            int i = v.getId();
            if (i ==  R.id.aliwx_search_layout) {
                mListView.setSelectionFromTop(0, -headBarHeight);
                mSearchContactsLayout.setVisibility(View.VISIBLE);
                mSearchText.setText("");
                mSearchText.requestFocus();
                mSearchContactsLayout.invalidate();
                mSearchAdapter.notifyDataSetChanged();
                mSearchLayout.setVisibility(View.GONE);
                mLetterView.setVisibility(View.INVISIBLE);
                showKeyBoard();
            } else if (i ==  R.id.aliwx_cancel_search) {
                hideSearch();
            }
        }
    }

//-----------------------------------------------------

//-------------------------[首字母索引]的主要代码，无需该功能的开发者不用过于关心以下方法和变量的细节。-------------------------------------------------

    //首字母索引
    private LetterListView mLetterView;
    private TextView mOverlay;

    public  class LetterIndexerStateManager {

        private ContactsFragment context;

        public LetterIndexerStateManager(ContactsFragment context) {
            this.context = context;

        }
        private void initLetterView() {
            mLetterView = (LetterListView) mView.findViewById( R.id.aliwx_friends_letter);
            mLetterView.setVisibility(View.VISIBLE);
            mLetterView.setOnTouchingLetterChangedListener(new LetterListViewListener());
            mOverlay = (TextView) mView.findViewById( R.id.aliwx_friends_overlay);
        }
        private class LetterListViewListener implements
                LetterListView.OnTouchingLetterChangedListener {
            @Override
            public void onTouchingLetterChanged(final String s) {
                if (mContactsAdapter != null) {
                    if (TextUtils.isEmpty(s)) {
                        //setSelectionFromTop 定位到position，再做y的偏移
                        mListView.setSelectionFromTop(0, headBarHeight);
                    } else {
                        int section = mContactsAdapter.getSectionForAlpha(s);
                        if (section >= 0) {
                            int postion = mContactsAdapter
                                    .getPositionForSection(section);
                            if (postion >= 0) {
                                mListView.setSelectionFromTop(
                                        postion + mListView.getHeaderViewsCount(),
                                        headBarHeight);
                            }
                        }
                    }
                    mHandler.removeCallbacks(mLoadAvatar);
                    mHandler.postDelayed(mLoadAvatar, 200);
                    if (TextUtils.isEmpty(s)) {
                        mOverlay.setText (R.string.aliwx_friend_search);
                    } else {
                        mOverlay.setText(s);
                    }
                    mOverlay.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mOverlayGone);
                    mHandler.postDelayed(mOverlayGone, 1500);
                }
            }
        }
        private Runnable mOverlayGone = new Runnable() {

            @Override
            public void run() {
                mOverlay.setVisibility(View.GONE);
            }
        };

        private Runnable mLoadAvatar = new Runnable() {

            @Override
            public void run() {
                mContactsAdapter.loadAsyncTask();
            }
        };

    }

 //-----------------------------------------------------

//-------------------------判断应用类型后方便测试Demo，开发者不用关心以下方法和变量。-------------------------------------------------

    //判断应用类型后方便测试Demo，开发者不用关心此代码。
    private List<String> mTBContactUserids= new ArrayList<String>();

    //判断应用类型后方便测试Demo，开发者不用关心此代码。
    public void judgeAppType(){
        String appKey = mIMKit.getIMCore().getAppKey();
        String prefix = AccountInfoTools.getPrefix(appKey);
        if (TextUtils.isEmpty(prefix) || (!prefix.equals(AccountUtils.getHupanPrefix()) && !prefix.equals(AccountUtils.SITE_CNTAOBAO))){
            appType=ISV;
        }else{
            appType= TB;
        }
    }

    //判断应用类型后方便测试Demo，开发者不用关心此代码。
    private List<ContactImpl> getTBContacts(){
        List<ContactImpl> mContacts=new ArrayList<ContactImpl>();
        IContactManager contactManager = mIMKit.getIMCore().getWXContactManager();
        List <Contact> contacts = contactManager.getContacts(IContactManager.TYPE_FRIEND);
        List <Contact> blackContacts =  contactManager.getContacts(IContactManager.TYPE_BLACK);
        contacts.addAll(blackContacts);
        List<Contact> strangers = contactManager.getContacts(IContactManager.TYPE_STRANGE);
        contacts.addAll(strangers);
        mTBContactUserids.clear();
        if (contacts != null && contacts.size() > 0){
            for (Contact wxContact:contacts) {
                mContacts.add(new ContactImpl(wxContact.getShowName(), wxContact.getUserId(), wxContact.getAvatarPath(), "", ""));
                if (TextUtils.isEmpty(wxContact.getAvatarPath())){
                    mTBContactUserids.add(wxContact.getLid());
                }
            }
        }
        return mContacts;
    }
    //判断应用类型后方便测试Demo，开发者不用关心此代码。
    private void syncTBContactsInfo() {
        IContactManager contactManager = mIMKit.getIMCore().getWXContactManager();
        if (mTBContactUserids.size() > 0) {
            contactManager.syncContactsInfo(mTBContactUserids, new IWxCallback() {
                @Override
                public void onSuccess(Object... result) {

                    mContactsAdapter.notifyDataSetChangedWithAsyncLoad();
                }

                @Override
                public void onError(int code, String info) {

                }

                @Override
                public void onProgress(int progress) {

                }
            });
        }
    }


    //Fragment跳转相关

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.demo_fragment_contacts, fragment);
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();
    }

    public void finish() {
        getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


    public ContactsAdapter getContactsAdapter() {
        return mContactsAdapter;
    }


    //邀请成为群成员
    public void initInviteTribeMemberList(){
        long tribeId = getArguments().getLong(TribeConstants.TRIBE_ID);
        IYWTribeService tribeService = mIMKit.getTribeService();
        tribeService.getMembers(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                List<YWTribeMember> tribeMemberList = (List<YWTribeMember>) result[0];
                if (tribeMemberList != null && tribeMemberList.size() > 0){
                    List<ComparableContact> deleteList = new ArrayList<ComparableContact>();
                    for (YWTribeMember member : tribeMemberList){
                        for (ComparableContact contact : mComparableContacts){
                            if (contact.getUserId().equals(member.getUserId()) && contact.getAppKey().equals(member.getAppKey())){
                                deleteList.add(contact);
                            }
                        }
                    }
                    for (ComparableContact contact : deleteList){
                        mComparableContacts.remove(contact);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContactsAdapter.notifyDataSetChangedWithAsyncLoad();
                        }
                    });
                }
            }

            @Override
            public void onError(int code, String info) {

            }

            @Override
            public void onProgress(int progress) {

            }
        }, tribeId);
    }

}
