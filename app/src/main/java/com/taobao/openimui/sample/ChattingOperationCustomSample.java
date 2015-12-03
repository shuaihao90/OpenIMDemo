package com.taobao.openimui.sample;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.WXAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingPageOperateion;
import com.alibaba.mobileim.aop.model.GoodsInfo;
import com.alibaba.mobileim.aop.model.ReplyBarItem;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactProfileCallback;
import com.alibaba.mobileim.contact.IYWCrossContactProfileCallback;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWGeoMessageBody;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageBody;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.alibaba.mobileim.conversation.YWTribeConversationBody;
import com.alibaba.mobileim.fundamental.widget.YWAlertDialog;
import com.alibaba.mobileim.kit.common.IMUtility;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.demo.DemoApplication;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 聊天界面(单聊和群聊界面)的定制点(根据需要实现相应的接口来达到自定义聊天界面)，不设置则使用openIM默认的实现 1.
 * CustomChattingTitleAdvice 自定义聊天窗口标题 2. OnUrlClickChattingAdvice 自定义聊天窗口中
 * 当消息是url是点击的回调。用于isv处理url的打开处理。不处理则用第三方浏览器打开 如果需要定制更多功能，需要实现更多开放的接口
 * 需要.继承BaseAdvice .实现相应的接口
 * <p/>
 * 另外需要在Application中绑定
 * AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT,
 * DemoChattingCustomTitleAdvice.class);
 *
 * @author jing.huai
 */
public class ChattingOperationCustomSample extends IMChattingPageOperateion {

    // 默认写法
    public ChattingOperationCustomSample(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 单聊ui界面，点击url的事件拦截 返回true;表示自定义处理，返回false，由默认处理
     *
     * @param fragment 可以通过 fragment.getActivity拿到Context
     * @param message  点击的url所属的message
     * @param url      点击的url
     */
    @Override
    public boolean onUrlClick(Fragment fragment, YWMessage message, String url,
                              YWConversation conversation) {
        // 仅处理单聊
        if (!isP2PChat(conversation)&&!isShopChat(conversation)) {
            return false;
        }

        Toast.makeText(fragment.getActivity(), "用户点击了url:" + url,
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        fragment.startActivity(intent);

        return false;
    }

    private static int ITEM_ID_1 = 0x1;
    private static int ITEM_ID_2 = 0x2;

    /**
     * 用于增加聊天窗口 下方回复栏的操作区的item ReplyBarItem itemId:唯一标识 建议从1开始
     * ItemImageRes：显示的图片 ItemLabel：文字 label YWConversation
     * conversation：当前会话，通过conversation.getConversationType() 区分个人单聊，与群聊天
     */
    @Override
    public List<ReplyBarItem> getReplybarItems(Fragment pointcut,
                                               YWConversation conversation) {
        List<ReplyBarItem> replyBarItems = new ArrayList<ReplyBarItem>();
        if (conversation.getConversationType() == YWConversationType.P2P) {
            ReplyBarItem replyBarItem = new ReplyBarItem();
            replyBarItem.setItemId(ITEM_ID_1);
            replyBarItem.setItemImageRes(R.drawable.demo_reply_bar_location_nor);
            replyBarItem.setItemLabel("位置");
            ReplyBarItem replyBarItem2 = new ReplyBarItem();
            replyBarItem2.setItemId(ITEM_ID_2);
            replyBarItem2.setItemImageRes(R.drawable.demo_input_plug_ico_hi_nor);
            replyBarItem2.setItemLabel("名片");
            replyBarItems.add(replyBarItem);
            replyBarItems.add(replyBarItem2);

        } else if (conversation.getConversationType() == YWConversationType.Tribe) {
            ReplyBarItem replyBarItem = new ReplyBarItem();
            replyBarItem.setItemId(ITEM_ID_1);
            replyBarItem.setItemImageRes(R.drawable.demo_input_plug_ico_hi_nor);
            replyBarItem.setItemLabel("Say-Hi");
            replyBarItems.add(replyBarItem);
        }
        return replyBarItems;

    }

    private static YWConversation mConversation;

    /**
     * 当自定义的item点击时的回调
     */
    @Override
    public void onReplyBarItemClick(Fragment pointcut, ReplyBarItem item,
                                    YWConversation conversation) {
        if (conversation.getConversationType() == YWConversationType.P2P) {
            if (item.getItemId() == ITEM_ID_1) {
                sendGeoMessage(conversation);

            } else if (item.getItemId() == ITEM_ID_2) {
                Activity context = pointcut.getActivity();
                Intent intent = new Intent(context, SelectContactToSendCardActivity.class);
                context.startActivity(intent);
                mConversation = conversation;
            }
        } else if (conversation.getConversationType() == YWConversationType.Tribe) {
            if (item.getItemId() == ITEM_ID_1) {
                sendTribeCustomMessage(conversation);
            }

        }
    }

    public static ISelectContactListener selectContactListener = new ISelectContactListener() {
        @Override
        public void onSelectCompleted(List<String> contacts) {
            if (contacts != null && contacts.size() > 0){
                for (String userId : contacts){
                    sendP2PCustomMessage(userId);
                }
            }
        }
    };

    @Override
    public int getFastReplyResId(YWConversation conversation) {
        return R.drawable.aliwx_reply_bar_face_bg;
    }

    @Override
    public boolean onFastReplyClick(Fragment pointcut, YWConversation ywConversation) {
        return false;
    }

    @Override
    public int getRecordResId(YWConversation conversation) {
        return 0;
    }

    @Override
    public boolean onRecordItemClick(Fragment pointcut, YWConversation ywConversation) {
        return false;
    }

    private boolean isP2PChat(YWConversation conversation) {
        return conversation.getConversationType() == YWConversationType.P2P;
    }
    private boolean isShopChat(YWConversation conversation) {
        return conversation.getConversationType() == YWConversationType.SHOP;
    }

    public static int count = 1;

    /**
     * 创建群自定义消息
     *
     * @return
     */
    public static YWMessage createTribeCustomMessage() {
        // 发送自定义消息
        YWMessageBody messageBody = new YWMessageBody();

        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", "Greeting");
        } catch (JSONException e) {
        }

        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("您收到一个招呼");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是群自定义消息
        return YWMessageChannel.createTribeCustomMessage(messageBody);
    }

    /**
     * 发送群自定义消息
     */
    public static void sendTribeCustomMessage(YWConversation conversation) {
        YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
        conversation.getMessageSender().sendMessage(createTribeCustomMessage(),
                120, null);
    }

    public static void sendP2PCustomMessage(String userId) {
        YWMessageBody messageBody = new YWMessageBody();
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", "CallingCard");
            object.put("personId", userId);
        } catch (JSONException e) {

        }

        messageBody.setContent(object.toString());
        messageBody.setSummary("[名片]");
        YWMessage message = YWMessageChannel.createCustomMessage(messageBody);
        mConversation.getMessageSender().sendMessage(message, 120, null);
    }

    /**
     * 发送单聊地理位置消息
     */
    public static void sendGeoMessage(YWConversation conversation) {
        conversation.getMessageSender().sendMessage(
                YWMessageChannel.createGeoMessage(30.2743790000,
                        120.1422530000, "浙江省杭州市西湖区"), 120, null);
    }

    @Override
    public View getCustomGeoMessageView(Fragment fragment, YWMessage message) {

        YWGeoMessageBody messageBody = (YWGeoMessageBody)message.getMessageBody();
        LinearLayout layout = (LinearLayout) View.inflate(
                DemoApplication.getContext(),
                R.layout.demo_geo_message_layout, null);
        TextView textView = (TextView) layout.findViewById(R.id.content);
        textView.setText(messageBody.getAddress());
        return layout;
    }

    @Override
    public View getCustomMessageView(Fragment fragment, YWMessage msg) {
        String msgType = null;
        try {
            String content = msg.getMessageBody().getContent();
            JSONObject object = new JSONObject(content);
            msgType = object.getString("customizeMessageType");
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(msgType)){
            return null;
        }
        if (msgType.equals("Greeting")) {
            TextView greeting = new TextView(fragment.getActivity());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            greeting.setLayoutParams(params);
            greeting.setBackgroundResource(R.drawable.demo_greeting_message);
            return greeting;
        }
        return null;
    }

    /**
     * 不显示头像的自定义消息View
     *
     * @param fragment
     * @param message
     * @param conversation
     * @return
     */
    @Override
    public View getCustomMessageViewWithoutHead(Fragment fragment, YWMessage message, YWConversation conversation) {
        String msgType = null;
        String userId = null;
        try {
            String content = message.getMessageBody().getContent();
            JSONObject object = new JSONObject(content);
            msgType = object.getString("customizeMessageType");
            userId = object.getString("personId");
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(msgType)){
            return null;
        }
        if (msgType.equals("CallingCard") && !TextUtils.isEmpty(userId)) {
            RelativeLayout layout = (RelativeLayout) View.inflate(fragment.getActivity(), R.layout.demo_custom_msg_layout_without_head, null);
            ImageView head = (ImageView) layout.findViewById(R.id.head);
            TextView tv = (TextView) layout.findViewById(R.id.name);
            tv.setText(userId);

            IYWContact contact = IMUtility.getContactProfileInfo(userId, LoginSampleHelper.getInstance().getIMKit().getIMCore().getAppKey());
            if (contact != null){
                String nick = contact.getShowName();
                if (!TextUtils.isEmpty(nick)){
                    tv.setText(nick);
                }
                String avatarPath = contact.getAvatarPath();
                if (avatarPath != null){
                    YWContactHeadLoadHelper helper = new YWContactHeadLoadHelper(fragment.getActivity(), null);
                    helper.setCustomHeadView(head, R.drawable.aliwx_head_default, avatarPath);
                }
            }
            return layout;
        }
        return null;
    }

    @Override
    public void onCustomMessageClick(Fragment fragment, YWMessage message) {
        Toast.makeText(DemoApplication.getContext(), "你点击了自定义消息",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCustomMessageLongClick(Fragment fragment, YWMessage message) {
        Toast.makeText(DemoApplication.getContext(), "你长按了自定义消息",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeoMessageClick(Fragment fragment, YWMessage message) {
        Toast.makeText(DemoApplication.getContext(), "你点击了地理位置消息消息",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeoMessageLongClick(Fragment fragment, YWMessage message) {
        Toast.makeText(DemoApplication.getContext(), "你长按了地理位置消息消息",
                Toast.LENGTH_SHORT).show();
    }



    @Override
    public boolean onMessageClick(Fragment fragment, YWMessage ywMessage) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(final Fragment fragment, final YWMessage ywMessage) {
        if (ywMessage.getSubType() == YWMessage.SUB_MSG_TYPE.IM_IMAGE || //自定义图片长按的事件处理，无复制选项。
                ywMessage.getSubType() == YWMessage.SUB_MSG_TYPE.IM_GIF
                ) {

            new YWAlertDialog.Builder(fragment.getActivity()).setTitle(getShowName(WXAPI.getInstance().getConversationManager().getConversationByConversationId(ywMessage.getConversationId()))).setItems(new String[]{"删除"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WXAPI.getInstance().getConversationManager().getConversationByConversationId(ywMessage.getConversationId()).getMessageLoader().deleteMessage(ywMessage);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();
            return true;
        } else if (ywMessage.getSubType() == YWMessage.SUB_MSG_TYPE.IM_AUDIO) {
            String[] items = new String[1];
            if (mUserInCallMode){ //当前为听筒模式
                items[0] = "使用扬声器模式";
            } else { //当前为扬声器模式
                items[0] = "使用听筒模式";
            }
            new YWAlertDialog.Builder(fragment.getActivity()).setTitle(getShowName(WXAPI.getInstance().getConversationManager().getConversationByConversationId(ywMessage.getConversationId()))).setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mUserInCallMode) {
                        mUserInCallMode = false;
                    } else {
                        mUserInCallMode = true;
                    }
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();
            return true;
        }

        return false;
    }

    public String getShowName(YWConversation conversation) {
        String conversationName;
        //added by 照虚  2015-3-26,有点无奈
        if (conversation == null) {
            return "";
        }

        if (conversation.getConversationType() == YWConversationType.Tribe) {
            conversationName = ((YWTribeConversationBody) conversation.getConversationBody()).getTribe().getTribeName();
        } else {
            IYWContact contact = ((YWP2PConversationBody) conversation.getConversationBody()).getContact();
            String userId = contact.getUserId();
            String appkey = contact.getAppKey();
            conversationName = userId;
            IYWCrossContactProfileCallback callback = WXAPI.getInstance().getCrossContactProfileCallback();
            if (callback != null) {
                IYWContact iContact = callback.onFetchContactInfo(userId, appkey);
                if (iContact != null && !TextUtils.isEmpty(iContact.getShowName())) {
                    conversationName = iContact.getShowName();
                    return conversationName;
                }
            } else {
                IYWContactProfileCallback profileCallback = WXAPI.getInstance().getContactProfileCallback();
                if (profileCallback != null) {
                    IYWContact iContact = profileCallback.onFetchContactInfo(userId);
                    if (iContact != null && !TextUtils.isEmpty(iContact.getShowName())) {
                        conversationName = iContact.getShowName();
                        return conversationName;
                    }
                }
            }
            IYWContact iContact = WXAPI.getInstance().getWXIMContact(userId);
            if (iContact != null && !TextUtils.isEmpty(iContact.getShowName())) {
                conversationName = iContact.getShowName();
            }
        }
        return conversationName;
    }

    @Override
    public GoodsInfo getGoodsInfoFromUrl(Fragment fragment, YWMessage message, String url, YWConversation ywConversation) {
        if (url.equals("https://www.taobao.com/ ")) {
            Bitmap bitmap = BitmapFactory.decodeResource(fragment.getResources(), R.drawable.pic_1_18);
            GoodsInfo info = new GoodsInfo("我的淘宝宝贝", "60.03", "86.25", "8.00", bitmap);
            return info;
        }
        return null;
    }

    @Override
    public View getCustomUrlView(Fragment fragment, YWMessage message, String url, YWConversation ywConversation) {
        if (url.equals("https://www.baidu.com/ ")) {
            LinearLayout layout = (LinearLayout) View.inflate(
                    DemoApplication.getContext(),
                    R.layout.demo_custom_tribe_msg_layout, null);
            TextView textView = (TextView) layout.findViewById(R.id.msg_content);
            textView.setText("I'm from getCustomUrlView!");
            return layout;
        }
        return null;
    }

    /**
     * 开发者可以根据用户操作设置该值
     */
    private static boolean mUserInCallMode = false;

    /**
     * 是否使用听筒模式播放语音消息
     * @param fragment
     * @param message
     * @return true：使用听筒模式， false：使用扬声器模式
     */
    @Override
    public boolean useInCallMode(Fragment fragment, YWMessage message) {
        return mUserInCallMode;
    }

    /**
     * 当打开聊天窗口时，自动发送该字符串给对方
     * @return 自动发送的内容（注意，内容empty则不自动发送）
     */
    @Override
    public String messageToSendWhenOpenChatting(Fragment fragment, YWConversation conversation) {
        //p2p、客服和店铺会话处理，否则不处理，
        int mCvsType=conversation.getConversationType().getValue();
        if(mCvsType==YWConversationType.P2P.getValue()||mCvsType==YWConversationType.SHOP.getValue()){
//            return "你好";
            return null;
        }
        else {
            return null;
        }

    }

    @Override
    public YWMessage ywMessageToSendWhenOpenChatting(Fragment fragment, YWConversation conversation) {
//        YWMessageBody messageBody = new YWMessageBody();
//        messageBody.setSummary("WithoutHead");
//        messageBody.setContent("hi，我是单聊自定义消息之好友名片");
//        YWMessage message = YWMessageChannel.createCustomMessage(messageBody);
//        return message;
        return null;
    }
}
