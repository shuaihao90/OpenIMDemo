package com.taobao.openimui.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWChannel;
import com.alibaba.mobileim.YWConstants;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.cloud.contact.YWProfileInfo;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.fundamental.widget.YWAlertDialog;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.openIMUIDemo.R;
import com.taobao.openimui.common.Notification;
import com.taobao.openimui.sample.ChattingFragmentSample;
import com.taobao.openimui.sample.LoginSampleHelper;

import java.util.ArrayList;
import java.util.List;

public class FindContactFragement extends Fragment implements OnClickListener {

	private ProgressDialog mProgressView;
	private EditText textView;
	private Button searchBtn;
	private AlertDialog dialog;
	private volatile boolean isStop;

	private Handler handler = new Handler();
    private View view;
    private boolean isFinishing;
	private Handler mHandler=new Handler();

	private YWContactHeadLoadHelper mHelper;
	private YWProfileInfo mYWProfileInfo;
	private LinearLayout mBottomLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
        if(view!=null){
			ViewGroup parent = (ViewGroup)view.getParent();
			if (parent != null) {
				parent.removeView(view);
			}
            return view;
        }
        view = inflater.inflate(R.layout.demo_activity_find_contact, null);
        init();
        return view;
    }
    private void initTitle(){
        View titleView = view.findViewById(R.id.title_bar);
        titleView.setVisibility(View.VISIBLE);

		titleView.setBackgroundColor(Color.parseColor("#00b4ff"));
        TextView leftButton = (TextView) view.findViewById(R.id.left_button);
        leftButton.setText("返回");
		leftButton.setTextColor(Color.WHITE);
		leftButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.demo_common_back_btn_white, 0, 0, 0);

		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
        TextView title = (TextView) view.findViewById(R.id.title_self_title);
		title.setTextColor(Color.WHITE);
        title.setText("添加联系人");
    }

    private void init() {
        initTitle();
		initSearchView();
		initSearchResultView();
		initHelper();

	}

	private void initHelper() {
		mHelper = new YWContactHeadLoadHelper(this.getActivity(), null);
	}


	private void initSearchView() {
		textView = (EditText) view.findViewById(R.id.search_keyword);
		textView.setImeOptions(EditorInfo.IME_ACTION_DONE);
		searchBtn = (Button) view.findViewById(R.id.search_btn);
		searchBtn.setOnClickListener(this);
		searchBtn.setEnabled(false);

		textView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					searchBtn.setEnabled(true);
				} else {
					searchBtn.setEnabled(false);
				}

			}
		});
		textView.requestFocus();
		showKeyBoard();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_btn: {
			String keyword = textView.getText().toString();
			showSearchResult(false,null);
			if (!TextUtils.isEmpty(keyword)) {
				if (YWChannel.getInstance().getNetWorkState().isNetWorkNull()) {
					Notification.showToastMsg(this.getActivity(), this.getResources().getString(R.string.net_null));
				} else {
					String key = keyword.replace(" ", "");
					String userId = key;
                    ArrayList<String> userIds = new ArrayList<String>();
                    userIds.add(userId);
                    showProgress();
                    IYWContactService contactService = LoginSampleHelper.getInstance().getIMKit().getContactService();
                    contactService.fetchUserProfile(userIds, LoginSampleHelper.APP_KEY, new IWxCallback(){

						@Override
						public void onSuccess(final Object... result) {
                            if (result != null) {
                                List<YWProfileInfo> profileInfos = (List<YWProfileInfo>) (result[0]);
//                                 YWDBContactImpl mYWDBContactImpl =new YWDBContactImpl();
                                if (profileInfos == null || profileInfos.isEmpty()) {
                                    handleResult((List) result[0]);
                                    return;
                                }
                                YWProfileInfo mYWProfileInfo = profileInfos.get(0);
//                                mYWDBContactImpl.email = mYWProfileInfo.email;
//                                mYWDBContactImpl.icon = mYWProfileInfo.icon;
//                                mYWDBContactImpl.mobile = mYWProfileInfo.mobile;
//                                mYWDBContactImpl.nick = mYWProfileInfo.nick;
//                                mYWDBContactImpl.taobaoId = mYWProfileInfo.taobaoId;
//                                mYWDBContactImpl.userId = mYWProfileInfo.userId;
                                cancelProgress();
                                //修改hasContactAlready和contactsFromCache的Fragment生命周期缓存
                                hasContactAlready = checkIfHasContact(mYWProfileInfo);
                                showSearchResult(true, mYWProfileInfo);
                            } else {
                                handleResult((List) result[0]);
                            }
                        }

                        @Override
                        public void onError(int code, String info) {
                            handleResult(null);
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
					});
				}

			}

		}
		break;
		case R.id.bottom_btn: {
			addFriend(mYWProfileInfo);
		}
		break;

		case R.id.btn_send_message: {
			sendMessage(mYWProfileInfo);
		}
		break;


		}

	}




	private void showProgress() {
		if (mProgressView == null) {
			mProgressView = new ProgressDialog(this.getActivity());
			mProgressView.setMessage(getResources().getString(
					R.string.search_friend_processing));
			mProgressView.setIndeterminate(true);
			mProgressView.setCancelable(true);
			mProgressView.setCanceledOnTouchOutside(false);
		}
		mProgressView.show();
	}

	private void cancelProgress() {
		if (mProgressView != null && mProgressView.isShowing()) {
			mProgressView.dismiss();
		}
	}

	private void handleResult(final List profileInfos) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!isFinishing) {
					cancelProgress();
					if (profileInfos == null || profileInfos.isEmpty()) {
						showSearchResult(false,null);
						if (isStop) {
							return;
						}
						if (dialog == null) {
							dialog = new YWAlertDialog.Builder(
									FindContactFragement.this.getActivity())
									.setTitle(R.string.search_friend_not_found)
									.setMessage(
											R.string.search_friend_not_found_message)
									.setPositiveButton(
											R.string.confirm,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub

												}

											}).create();
						}
						dialog.show();
					}
				}
			}
		});

	}

	public void onBackPressed() {
        isFinishing=true;
		if (mProgressView != null && mProgressView.isShowing()) {
			mProgressView.dismiss();
			isStop = true;
			return;
		}
		hideKeyBoard();
        ((ContactsFragment)getParentFragment()).finish();
	}
	//--------------------------[搜索到的联系人的展示]相关实现

	private View parallaxView;
	private  Button mBottomButton;
	private Button mSendMessageBtn;
	private ImageView mHeadBgView;
	private ImageView mHeadView;
	private TextView mSelfDescview;
	private List<IYWDBContact> contactsFromCache;
	private boolean hasContactAlready;
	private void initSearchResultView() {
		parallaxView =view.findViewById(R.id.parallax_view);
		parallaxView.setVisibility(View.GONE);
		mHeadBgView = (ImageView) view.findViewById(R.id.block_bg);
		mHeadBgView.setImageResource(R.drawable.head_bg_0);
		mHeadView = (ImageView) view.findViewById(R.id.people_head);
		mSelfDescview = (TextView) view.findViewById(R.id.people_desc);
		mSelfDescview.setMaxLines(2); // 必须函数设置，否则无效

		mBottomLayout = (LinearLayout)view. findViewById(R.id.bottom_layout);
		mBottomButton = (Button) view.findViewById(R.id.bottom_btn);
		mBottomButton.setOnClickListener(this);
		mSendMessageBtn = (Button) view.findViewById(R.id.btn_send_message);
		mSendMessageBtn.setOnClickListener(this);
	}
	public  void clearSearchResult(){
		parallaxView.setVisibility(View.GONE);
	}
	public void showSearchResult(final boolean showOrHide, final YWProfileInfo lmYWProfileInfo){

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (showOrHide) {

					if(lmYWProfileInfo==null||TextUtils.isEmpty(lmYWProfileInfo.userId)){
						Notification.showToastMsg(FindContactFragement.this.getActivity(), "服务开小差，建议您重试搜索");
						return;
					}

					mYWProfileInfo=lmYWProfileInfo;
					setSearchResult(lmYWProfileInfo);
					setBottomView(lmYWProfileInfo);
					parallaxView.setVisibility(View.VISIBLE);
					hideKeyBoard();
				} else {
					clearSearchResult();
				}
			}
		});

	}
	public void setSearchResult(YWProfileInfo profileInfo) {
		if(profileInfo!=null){
			RelativeLayout useridLayout = (RelativeLayout) view.findViewById(R.id.userid_layout);
			if (TextUtils.isEmpty(profileInfo.userId)) {
				useridLayout.setVisibility(View.GONE);
			} else {
				useridLayout.setVisibility(View.VISIBLE);
				TextView textView = (TextView) view.findViewById(R.id.userid_text);
				textView.setText(new StringBuilder("  ").append(profileInfo.userId));
			}
			RelativeLayout settingRemarkNameLayout = (RelativeLayout) view.findViewById(R.id.remark_name_layout);
			if (TextUtils.isEmpty(profileInfo.nick)) {
				settingRemarkNameLayout.setVisibility(View.GONE);
			} else {
				settingRemarkNameLayout.setVisibility(View.VISIBLE);
				TextView textView = (TextView) view.findViewById(R.id.remark_name_text);
				textView.setText(new StringBuilder("  ").append(profileInfo.nick));
			}
//			if (TextUtils.isEmpty(profileInfo.icon)) {
//				mHeadView.setImageResource(R.drawable.aliwx_head_default);
//			} else if(!TextUtils.isEmpty(profileInfo.userId)&&!TextUtils.isEmpty(profileInfo.icon)) {
				mHelper.setHeadView(mHeadView, profileInfo.userId, LoginSampleHelper.APP_KEY, true);
//			}
		}
	}

	private void sendMessage(YWProfileInfo mYWProfileInfo) {
		if(mYWProfileInfo.userId.equals(LoginSampleHelper.getInstance().getIMKit().getIMCore().getLoginUserId())){
			Notification.showToastMsg(this.getActivity(), "这是您自己，无法发送消息");
			return;
		}
		if (LoginSampleHelper.APP_KEY.equals(YWConstants.YWSDKAppKey)) {
			//TB或千牛客的服账号
			EServiceContact eServiceContact = new EServiceContact(mYWProfileInfo.userId, 0);//
			final YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
			Intent intent = imKit.getChattingActivityIntent(eServiceContact);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this.getActivity(), ChattingFragmentSample.class);
			intent.putExtra(ChattingFragmentSample.TARGET_ID, mYWProfileInfo.userId);
			startActivity(intent);
		}
	}

	private void addFriend(YWProfileInfo mYWProfileInfo) {
		if(mYWProfileInfo.userId.equals(LoginSampleHelper.getInstance().getIMKit().getIMCore().getLoginUserId())){
			Notification.showToastMsg(this.getActivity(), "这是您自己，无法添加联系人");
			return;
		}
		if(hasContactAlready){
			Notification.showToastMsg(this.getActivity(), "已有该联系人");
		}else{
			YWDBContactImpl contact = new YWDBContactImpl(mYWProfileInfo.userId,mYWProfileInfo.nick,LoginSampleHelper.APP_KEY,mYWProfileInfo.icon);
			contactsFromCache.add(contact);
			IYWContactService contactService = LoginSampleHelper.getInstance().getIMKit().getContactService();
			contactService.asynchronousSyncContactsToCacheAndDB(contactsFromCache, new IWxCallback() {
				@Override
				public void onSuccess(Object... result) {
					Notification.showToastMsg(FindContactFragement.this.getActivity(), "添加联系人成功");
					onBackPressed();
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

	private boolean checkIfHasContact(YWProfileInfo mYWProfileInfo){
		 contactsFromCache = LoginSampleHelper.getInstance().getIMKit().getContactService().getContactsFromCache();
		for(IYWDBContact contact:contactsFromCache){
			if(contact.getUserId().equals(mYWProfileInfo.userId))
				return true;
		}
		return false;
	}
	private void setBottomView(YWProfileInfo lmYWProfileInfo){
		/* 对应账号以及好友关系所能够进行的操作*/


		if(mYWProfileInfo.userId.equals(LoginSampleHelper.getInstance().getIMKit().getIMCore().getLoginUserId())){
			mBottomLayout.setVisibility(View.GONE);
			Notification.showToastMsg(this.getActivity(), "这是您自己");
			return;
		}else if(hasContactAlready){
			mBottomLayout.setVisibility(View.VISIBLE);
			mBottomButton.setVisibility(View.GONE);
			mSendMessageBtn.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSendMessageBtn
					.getLayoutParams();
			layoutParams.width = getResources().getDimensionPixelSize(
					R.dimen.friend_info_btn_width);
			layoutParams.weight = 0;
			mSendMessageBtn.setLayoutParams(layoutParams);
//			Notification.showToastMsg(this.getActivity(), "已有该联系人");
		}else {
			mBottomLayout.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams bLayoutParams = (LinearLayout.LayoutParams) mBottomButton
					.getLayoutParams();
			bLayoutParams.width = 0;
			bLayoutParams.weight = 1;
			mBottomButton.setLayoutParams(bLayoutParams);
			mBottomButton.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSendMessageBtn
					.getLayoutParams();
			layoutParams.width = 0;
			layoutParams.weight = 1;
			mSendMessageBtn.setLayoutParams(layoutParams);
			mSendMessageBtn.setVisibility(View.VISIBLE);
		}

	}
	private void showKeyBoard() {
		View view = this.getActivity().getCurrentFocus();
		if (view != null) {
			((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
					.showSoftInput(view, 0);
		}
	}
	protected void hideKeyBoard() {
		View view = this.getActivity().getCurrentFocus();
		if (view != null) {
			((InputMethodManager)  this.getActivity().getSystemService(this.getActivity().INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	public class YWDBContactImpl extends YWProfileInfo implements IYWDBContact{

		public String appKey;

		public YWDBContactImpl(String userId, String nick, String appKey, String icon) {
			this.userId = userId;
			this.nick = nick;
			this.appKey = appKey;
			this.icon = icon;
		}

		public YWDBContactImpl() {
		}

		@Override
		public String[] getShortPinyins() {
			return new String[0];
		}

		@Override
		public String[] getPinyins() {
			return new String[0];
		}

		@Override
		public String getFirstChar() {
			return null;
		}

		@Override
		public boolean isFirstCharEnglish() {
			return false;
		}

		@Override
		public String getUserId() {
			return userId;
		}

		@Override
		public String getAppKey() {
			return appKey;
		}

		@Override
		public String getAvatarPath() {
			return null;
		}

		@Override
		public String getShowName() {
			return nick;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public void setShowName(String nick) {
			this.nick = nick;
		}

		public void setAppKey(String appKey) {
			this.appKey = appKey;
		}
	}


}
