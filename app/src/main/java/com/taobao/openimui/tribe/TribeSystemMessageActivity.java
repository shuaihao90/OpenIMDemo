package com.taobao.openimui.tribe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.lib.model.message.SystemMessage;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.sample.LoginSampleHelper;

import java.util.ArrayList;
import java.util.List;

public class TribeSystemMessageActivity extends Activity {

    private TribeSystemMessageAdapter mAdapter;
    private ListView mListView;
    private IYWTribeService mTribeService;
    private List<YWMessage> mList = new ArrayList<YWMessage>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_system_message);
        init();
    }

    private void init(){
        mTribeService = LoginSampleHelper.getInstance().getIMKit().getTribeService();
        initTitle();
        mListView = (ListView) findViewById(R.id.message_list);
        mAdapter = new TribeSystemMessageAdapter(this, mList);
        mListView.setAdapter(mAdapter);
        loadSystemMessages();
    }

    private void initTitle() {
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.title_bar);
        titleBar.setBackgroundColor(Color.parseColor("#00b4ff"));
        titleBar.setVisibility(View.VISIBLE);

        TextView titleView = (TextView) findViewById(R.id.title_self_title);
        TextView leftButton = (TextView) findViewById(R.id.left_button);
        leftButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.demo_common_back_btn_white, 0, 0, 0);
        leftButton.setTextColor(Color.WHITE);
        leftButton.setText("返回");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleView.setTextColor(Color.WHITE);
        titleView.setText("群系统消息");


        TextView rightButton = (TextView) findViewById(R.id.right_button);
        rightButton.setText("清空");
        rightButton.setTextColor(Color.WHITE);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTribeService.clearTribeSystemMessages();
                loadSystemMessages();
            }
        });
    }

    private void refreshAdapter(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.refreshData(mList);
            }
        });
    }

    private void loadSystemMessages(){
        IYWTribeService tribeService = LoginSampleHelper.getInstance().getIMKit().getTribeService();
        tribeService.getTribeSystemMessages(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                if (result != null && result.length > 0) {
                    mList = (List<YWMessage>) result[0];
                    refreshAdapter();
                }
            }

            @Override
            public void onError(int code, String info) {
                WxLog.i("test", "result");
            }

            @Override
            public void onProgress(int progress) {

            }
        });
    }

    public void acceptToJoinTribe(final YWMessage message) {
        final SystemMessage msg = (SystemMessage) message;
        YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
        if (imKit != null) {
            IYWTribeService tribeService = imKit.getTribeService();
            if (tribeService != null) {
                tribeService.accept(new IWxCallback() {
                    @Override
                    public void onSuccess(Object... result) {
                        Boolean isSuccess = (Boolean) result[0];
                        if (isSuccess) {
                            msg.setSubType(SystemMessage.SYSMSG_TYPE_AGREE);
                            refreshAdapter();
                            mTribeService.updateTribeSystemMessage(msg);
                        }
                    }

                    @Override
                    public void onError(int code, String info) {

                    }

                    @Override
                    public void onProgress(int progress) {

                    }
                }, Long.valueOf(msg.getAuthorId()), msg.getRecommender());
            }
        }
    }

    public void refuseToJoinTribe(YWMessage message) {
        final SystemMessage msg = (SystemMessage) message;
        msg.setSubType(SystemMessage.SYSMSG_TYPE_IGNORE);
        refreshAdapter();
        mTribeService.updateTribeSystemMessage(msg);
    }
}