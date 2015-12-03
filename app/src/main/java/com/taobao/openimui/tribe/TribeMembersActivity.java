package com.taobao.openimui.tribe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.fundamental.widget.YWAlertDialog;
import com.alibaba.mobileim.fundamental.widget.refreshlist.PullToRefreshListView;
import com.alibaba.mobileim.fundamental.widget.refreshlist.YWPullToRefreshBase;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeType;
import com.alibaba.mobileim.gingko.presenter.tribe.IYWTribeChangeListener;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.common.Notification;
import com.taobao.openimui.demo.FragmentTabs;
import com.taobao.openimui.sample.LoginSampleHelper;

import java.util.ArrayList;
import java.util.List;

public class TribeMembersActivity extends Activity implements AdapterView.OnItemLongClickListener {

    private static final int REQUEST_CODE = 1;
    private YWIMKit mIMKit;
    private IYWTribeService mTribeService;
    private IYWTribeChangeListener mTribeChangedListener;

    private long mTribeId;
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;

    private List<YWTribeMember> mList;
    private TribeMembersAdapterSample mAdapter;
    private TextView mAddTribeMembers;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_tribe_members);
        init();
    }

    private void init() {
        initTitle();

        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.tribe_members_list);
        mPullToRefreshListView.setMode(YWPullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
        mPullToRefreshListView.setShowIndicator(false);
        mPullToRefreshListView.setDisableScrollingWhileRefreshing(false);
        mPullToRefreshListView.setRefreshingLabel("同步群成员列表");
        mPullToRefreshListView.setReleaseLabel("松开同步群成员列表");
        mPullToRefreshListView.setDisableRefresh(false);
        mPullToRefreshListView.setOnRefreshListener(new YWPullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTribeMembersFormLocal();
            }
        });
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setOnItemLongClickListener(this);

        mList = new ArrayList<YWTribeMember>();
        mAdapter = new TribeMembersAdapterSample(this, mList);
        mListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        mTribeId = intent.getLongExtra(TribeConstants.TRIBE_ID, 0);
        mIMKit = LoginSampleHelper.getInstance().getIMKit();
        mTribeService = mIMKit.getTribeService();

        getTribeMembersFormLocal();

        mAddTribeMembers = (TextView) findViewById(R.id.add_tribe_members);
        mAddTribeMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TribeMembersActivity.this, InviteTribeMemberActivity.class);
                intent.putExtra(TribeConstants.TRIBE_ID, mTribeId);
                startActivity(intent);
            }
        });
        mAddTribeMembers.setVisibility(View.GONE);

        initTribeChangedListener();
        mTribeService.addTribeListener(mTribeChangedListener);
    }

    private void initTitle() {
        View view = findViewById(R.id.title_bar);
        view.setVisibility(View.VISIBLE);
        view.setBackgroundColor(Color.parseColor("#00b4ff"));
        TextView leftButton = (TextView) view.findViewById(R.id.left_button);
        TextView titleView = (TextView) view.findViewById(R.id.title_self_title);
        TextView rightButton = (TextView) view.findViewById(R.id.right_button);

        leftButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.demo_common_back_btn_white, 0, 0, 0);
        leftButton.setText("返回");
        leftButton.setTextColor(Color.WHITE);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleView.setText("群成员列表");
        titleView.setTextColor(Color.WHITE);

        rightButton.setText("管理");
        rightButton.setTextColor(Color.WHITE);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YWTribe tribe = mTribeService.getTribe(mTribeId);
                //群的普通成员没有加入权限，所以因此加入view
                if (tribe.getTribeType() == YWTribeType.CHATTING_TRIBE && getLoginUserRole() == YWTribeMember.ROLE_NORMAL) {
                    Notification.showToastMsg(TribeMembersActivity.this, "您不是群管理员，没有管理权限~");
                } else {
                    mAddTribeMembers.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getTribeMembersFormLocal() {
        mTribeService.getMembers(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                mList.clear();
                mList.addAll((List<YWTribeMember>) result[0]);
                refreshAdapter();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshListView.onRefreshComplete(false, true);
                    }
                });
            }

            @Override
            public void onError(int code, String info) {
                Notification.showToastMsg(TribeMembersActivity.this, "error, code = " + code + ", info = " + info);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshListView.onRefreshComplete(false, false);
                    }
                });
            }

            @Override
            public void onProgress(int progress) {

            }
        }, mTribeId);
    }

    /**
     * 刷新当前列表
     */
    private void refreshAdapter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.refreshData(mList);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTribeService.removeTribeListener(mTribeChangedListener);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position - mListView.getHeaderViewsCount();
        final YWTribeMember member = mList.get(pos);
        YWTribe tribe = mTribeService.getTribe(mTribeId);
        final String[] items = getItems(tribe, member);
        if (items == null) {
            return true;
        }
        new YWAlertDialog.Builder(this)
                .setTitle("群成员管理")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals(TribeConstants.TRIBE_SET_MANAGER)){
                            mTribeService.setTribeManager(null, mTribeId, member.getUserId());
                        } else if (items[which].equals(TribeConstants.TRIBE_CANCEL_MANAGER)){
                            mTribeService.cancelTribeManager(null, mTribeId, member.getUserId());
                        } else if (items[which].equals(TribeConstants.TRIBE_EXPEL_MEMBER)){
                            mTribeService.expelMember(new IWxCallback() {
                                @Override
                                public void onSuccess(Object... result) {
                                    Notification.showToastMsg(TribeMembersActivity.this, "踢人成功！");
                                    mList.remove(member);
                                    refreshAdapter();
                                }

                                @Override
                                public void onError(int code, String info) {

                                }

                                @Override
                                public void onProgress(int progress) {

                                }
                            }, mTribeId, member.getUserId());
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
        return true;
    }

    /**
     * 判断当前登录用户在群组中的身份
     *
     * @return
     */
    private int getLoginUserRole() {
        int role = YWTribeMember.ROLE_NORMAL;
        String loginUser = mIMKit.getIMCore().getLoginUserId();
        for (YWTribeMember member : mList) {
            if (member.getUserId().equals(loginUser)) {
                role = member.getTribeRole();
            }
        }
        return role;
    }

    private String[] getItems(YWTribe tribe, YWTribeMember member) {
        String[] items = null;
        if (tribe.getTribeType() == YWTribeType.CHATTING_TRIBE && getLoginUserRole() == YWTribeMember.ROLE_HOST) {
            if (member.getTribeRole() == YWTribeMember.ROLE_NORMAL) {
                items = new String[]{TribeConstants.TRIBE_SET_MANAGER, TribeConstants.TRIBE_EXPEL_MEMBER};
            } else if (member.getTribeRole() == YWTribeMember.ROLE_MANAGER) {
                items = new String[]{TribeConstants.TRIBE_CANCEL_MANAGER, TribeConstants.TRIBE_EXPEL_MEMBER};
            }
        } else if (tribe.getTribeType() == YWTribeType.CHATTING_GROUP && getLoginUserRole() == YWTribeMember.ROLE_HOST && member.getTribeRole() != YWTribeMember.ROLE_HOST) {
            items = new String[]{TribeConstants.TRIBE_EXPEL_MEMBER};
        }
        return items;
    }

    private void initTribeChangedListener(){
        mTribeChangedListener = new IYWTribeChangeListener() {
            @Override
            public void onInvite(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onUserJoin(YWTribe tribe, YWTribeMember user) {
                mList.add(user);
                refreshAdapter();
            }

            @Override
            public void onUserQuit(YWTribe tribe, YWTribeMember user) {
                mList.remove(user);
                refreshAdapter();
            }

            @Override
            public void onUserRemoved(YWTribe tribe, YWTribeMember user) {
                //只有被踢出群的用户会收到该回调，即如果收到该回调表示自己被踢出群了
                openTribeListFragment();
            }

            @Override
            public void onTribeDestroyed(YWTribe tribe) {
                openTribeListFragment();
            }

            @Override
            public void onTribeInfoUpdated(YWTribe tribe) {

            }

            @Override
            public void onTribeManagerChanged(YWTribe tribe, YWTribeMember user) {
                for (YWTribeMember member : mList){
                    if (member.getUserId().equals(user.getUserId()) && member.getAppKey().equals(user.getAppKey())){
                        mList.remove(member);
                        mList.add(user);
                        refreshAdapter();
                        break;
                    }
                }
            }

            @Override
            public void onTribeRoleChanged(YWTribe tribe, YWTribeMember user) {
                for (YWTribeMember member : mList){
                    if (member.getUserId().equals(user.getUserId()) && member.getAppKey().equals(user.getAppKey())){
                        mList.remove(member);
                        mList.add(user);
                        refreshAdapter();
                        break;
                    }
                }
            }
        };
    }

    private void openTribeListFragment() {
        Intent intent = new Intent(this, FragmentTabs.class);
        intent.putExtra(TribeConstants.TRIBE_OP, TribeConstants.TRIBE_OP);
        startActivity(intent);
        finish();
    }

}
