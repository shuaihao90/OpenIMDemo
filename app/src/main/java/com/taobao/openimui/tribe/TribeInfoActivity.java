package com.taobao.openimui.tribe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.gingko.presenter.tribe.IYWTribeChangeListener;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.common.Notification;
import com.taobao.openimui.demo.FragmentTabs;
import com.taobao.openimui.sample.LoginSampleHelper;

import java.util.ArrayList;
import java.util.List;

public class TribeInfoActivity extends Activity {

    private static final String TAG = "TribeInfoActivity";

    private YWIMKit mIMKit;
    private IYWTribeService mTribeService;
    private YWTribe mTribe;
    private long mTribeId;
    private String mTribeOp;
    private int mTribeMemberCount;
    List<YWTribeMember> mList = new ArrayList<YWTribeMember>();

    private IYWTribeChangeListener mTribeChangedListener;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private TextView mTribeName;
    private TextView mTribeDesc;
    private TextView mMemberCount;
    private TextView mQuiteTribe;
    private TextView mMangeTribeMembers;
    private RelativeLayout mMangeTribeMembersLayout;
    private RelativeLayout mEditTribeInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_tribe_info);

        Intent intent = getIntent();
        mTribeId = intent.getLongExtra(TribeConstants.TRIBE_ID, 0);
        mTribeOp = intent.getStringExtra(TribeConstants.TRIBE_OP);

        initTribeChangedListener();
        initView();
        initTribeInfo();

    }

    private void initTitle() {
        View titleView = findViewById(R.id.title_bar);
        titleView.setVisibility(View.VISIBLE);
        titleView.setBackgroundColor(Color.parseColor("#00b4ff"));

        TextView leftButton = (TextView) findViewById(R.id.left_button);
        leftButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.demo_common_back_btn_white, 0, 0, 0);
        leftButton.setText("返回");
        leftButton.setTextColor(Color.WHITE);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView title = (TextView) findViewById(R.id.title_self_title);
        title.setText("群资料");
        title.setTextColor(Color.WHITE);
    }

    private void initView() {
        initTitle();
        mTribeName = (TextView) findViewById(R.id.tribe_name);
        TextView tribeId = (TextView) findViewById(R.id.tribe_id);
        tribeId.setText("群号 " + mTribeId);

        mTribeDesc = (TextView) findViewById(R.id.tribe_description);
        mMemberCount = (TextView) findViewById(R.id.member_count);

        mMangeTribeMembers = (TextView) findViewById(R.id.manage_tribe_members);
        mMangeTribeMembersLayout = (RelativeLayout) findViewById(R.id.manage_tribe_members_layout);
        mMangeTribeMembersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TribeInfoActivity.this, TribeMembersActivity.class);
                intent.putExtra(TribeConstants.TRIBE_ID, mTribeId);
                startActivity(intent);
            }
        });

        mEditTribeInfoLayout = (RelativeLayout) findViewById(R.id.edit_tribe_info_layout);
        mEditTribeInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TribeInfoActivity.this, EditTribeInfoActivity.class);
                intent.putExtra(TribeConstants.TRIBE_ID, mTribeId);
                intent.putExtra(TribeConstants.TRIBE_OP, TribeConstants.TRIBE_EDIT);
                startActivity(intent);
            }
        });

        mQuiteTribe = (TextView) findViewById(R.id.quite_tribe);
    }

    private void updateView() {
        mTribeName.setText(mTribe.getTribeName());
        mTribeDesc.setText(mTribe.getTribeNotice());
        if (mTribeMemberCount > 0) {
            mMemberCount.setText(mTribeMemberCount + "人");
        }

        if (getLoginUserRole() == YWTribeMember.ROLE_HOST) {
            mMangeTribeMembers.setText("群成员管理");
            mEditTribeInfoLayout.setVisibility(View.VISIBLE);
            mQuiteTribe.setText("解散群");
            mQuiteTribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTribeService.disbandTribe(new IWxCallback() {
                        @Override
                        public void onSuccess(Object... result) {
                            YWLog.i(TAG, "解散群成功！");
                            Notification.showToastMsg(TribeInfoActivity.this, "解散群成功！");
                            openTribeListFragment();
                        }

                        @Override
                        public void onError(int code, String info) {
                            YWLog.i(TAG, "解散群失败， code = " + code + ", info = " + info);
                            Notification.showToastMsg(TribeInfoActivity.this, "解散群失败, code = " + code + ", info = " + info);
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
                    }, mTribeId);
                }
            });
        } else {
            mMangeTribeMembers.setText("群成员列表");
            mEditTribeInfoLayout.setVisibility(View.GONE);
            mQuiteTribe.setText("退出群");
            mQuiteTribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTribeService.exitFromTribe(new IWxCallback() {
                        @Override
                        public void onSuccess(Object... result) {
                            YWLog.i(TAG, "退出群成功！");
                            Notification.showToastMsg(TribeInfoActivity.this, "退出群成功！");
                            openTribeListFragment();
                        }

                        @Override
                        public void onError(int code, String info) {
                            YWLog.i(TAG, "退出群失败， code = " + code + ", info = " + info);
                            Notification.showToastMsg(TribeInfoActivity.this, "退出群失败, code = " + code + ", info = " + info);
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
                    }, mTribeId);
                }
            });
        }

        if (!TextUtils.isEmpty(mTribeOp)) {
            mMangeTribeMembersLayout.setVisibility(View.GONE);
            mEditTribeInfoLayout.setVisibility(View.GONE);
            mQuiteTribe.setText("加入群");
            mQuiteTribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTribeService.joinTribe(new IWxCallback() {
                        @Override
                        public void onSuccess(Object... result) {
                            if (result != null && result.length > 0) {
                                Integer retCode = (Integer) result[0];
                                if (retCode == 0) {
                                    YWLog.i(TAG, "加入群成功！");
                                    Notification.showToastMsg(TribeInfoActivity.this, "加入群成功！");
                                    mTribeOp = null;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateView();
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onError(int code, String info) {
                            YWLog.i(TAG, "加入群失败， code = " + code + ", info = " + info);
                            Notification.showToastMsg(TribeInfoActivity.this, "加入群失败, code = " + code + ", info = " + info);
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
                    }, mTribeId);
                }
            });
        } else {
            if (getLoginUserRole() == YWTribeMember.ROLE_NORMAL) {
                mMangeTribeMembersLayout.setVisibility(View.VISIBLE);
                mEditTribeInfoLayout.setVisibility(View.GONE);
            } else {
                mMangeTribeMembersLayout.setVisibility(View.VISIBLE);
                mEditTribeInfoLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateTribeMemberCount(){
        if (mTribeMemberCount > 0) {
            mMemberCount.setText(mTribeMemberCount + "人");
        }
    }

    private void openTribeListFragment() {
        Intent intent = new Intent(this, FragmentTabs.class);
        intent.putExtra(TribeConstants.TRIBE_OP, TribeConstants.TRIBE_OP);
        startActivity(intent);
        finish();
    }

    private void initTribeInfo() {
        mIMKit = LoginSampleHelper.getInstance().getIMKit();
        mTribeService = mIMKit.getTribeService();
        mTribe = mTribeService.getTribe(mTribeId);
        mTribeService.addTribeListener(mTribeChangedListener);
        initTribeMemberList();
        getTribeInfoFromServer();
    }

    private void getTribeInfoFromServer() {
        mTribeService.getTribeFromServer(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }

            @Override
            public void onError(int code, String info) {

            }

            @Override
            public void onProgress(int progress) {

            }
        }, mTribeId);
    }

    private void initTribeMemberList() {
        getTribeMembersFromLocal();
        getTribeMembersFromServer();
    }

    private void getTribeMembersFromLocal() {
        mTribeService.getMembers(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                mList.clear();
                mList.addAll((List<YWTribeMember>) result[0]);
                if (mList != null || mList.size() > 0) {
                    mTribeMemberCount = mList.size();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateView();
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
        }, mTribeId);
    }

    private void getTribeMembersFromServer() {
        mTribeService.getMembersFromServer(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                mList.clear();
                mList.addAll((List<YWTribeMember>) result[0]);
                if (mList != null || mList.size() > 0) {
                    mTribeMemberCount = mList.size();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateView();
                        }
                    });
                }
            }

            @Override
            public void onError(int code, String info) {
                Notification.showToastMsg(TribeInfoActivity.this, "error, code = " + code + ", info = " + info);
            }

            @Override
            public void onProgress(int progress) {

            }
        }, mTribeId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTribeService.removeTribeListener(mTribeChangedListener);
    }

    private void initTribeChangedListener() {
        mTribeChangedListener = new IYWTribeChangeListener() {
            @Override
            public void onInvite(YWTribe tribe, YWTribeMember user) {

            }

            @Override
            public void onUserJoin(YWTribe tribe, YWTribeMember user) {
                mTribeMemberCount += 1;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateTribeMemberCount();
                    }
                });
            }

            @Override
            public void onUserQuit(YWTribe tribe, YWTribeMember user) {
                mTribeMemberCount -= 1;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateTribeMemberCount();
                    }
                });
            }

            @Override
            public void onUserRemoved(YWTribe tribe, YWTribeMember user) {
                openTribeListFragment();
            }

            @Override
            public void onTribeDestroyed(YWTribe tribe) {
                openTribeListFragment();
            }

            @Override
            public void onTribeInfoUpdated(YWTribe tribe) {
                mTribe = tribe;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }

            @Override
            public void onTribeManagerChanged(YWTribe tribe, YWTribeMember user) {
                String loginUser = mIMKit.getIMCore().getLoginUserId();
                if (loginUser.equals(user.getUserId())) {
                    for (YWTribeMember member : mList) {
                        if (member.getUserId().equals(loginUser)) {
                            mList.remove(member);
                            mList.add(user);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateView();
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onTribeRoleChanged(YWTribe tribe, YWTribeMember user) {
                String loginUser = mIMKit.getIMCore().getLoginUserId();
                if (loginUser.equals(user.getUserId())) {
                    for (YWTribeMember member : mList) {
                        if (member.getUserId().equals(loginUser)) {
                            mList.remove(member);
                            mList.add(user);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateView();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        };
    }
}
