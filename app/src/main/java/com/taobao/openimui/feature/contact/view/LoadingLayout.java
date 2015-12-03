package com.taobao.openimui.feature.contact.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.openIMUIDemo.R;

class LoadingLayout extends FrameLayout {

	
	private static final int ROTATE_ARROW_ANIMATION_DURATION = 250;

	private  ImageView mHeaderImage ;
	private ImageView mPullArrowImage;

	private  TextView mHeaderText;
	private  TextView mSubHeaderText;

	private String mPullLabel;
	private String mRefreshingLabel;
	private String mReleaseLabel;

	private RotateAnimation flipAnimation;
	private RotateAnimation reverseFlipAnimation;
	private boolean bDisableLoading;

	@SuppressWarnings("deprecation") LoadingLayout(Context context, final PullToRefreshBase.Mode mode, TypedArray attrs) {
		super(context);
		if (isInEditMode()) { return; }
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate( R.layout.aliwx_pull_to_refresh_header, this);
		mHeaderText = (TextView) header.findViewById( R.id.pull_to_refresh_text);
		mSubHeaderText = (TextView) header
				.findViewById( R.id.pull_to_refresh_sub_text);
		mHeaderImage = (ImageView) header
				.findViewById( R.id.pull_to_refresh_image);
//		((Animatable)mHeaderImage.getDrawable()).start();
		mPullArrowImage = (ImageView) header
				.findViewById( R.id.pull_to_refresh_arrow);

		flipAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		flipAnimation.setInterpolator(new LinearInterpolator());
		flipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		flipAnimation.setFillAfter(true);
		reverseFlipAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseFlipAnimation.setInterpolator(new LinearInterpolator());
		reverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		reverseFlipAnimation.setFillAfter(true);

		switch (mode) {
		case PULL_UP_TO_REFRESH:
			Matrix mat = new Matrix();
			Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.aliwx_ic_pulltorefresh_arrow);
			mat.postRotate(180);
			Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0,
					bMap.getWidth(), bMap.getHeight(), mat, true);
			mPullArrowImage.setImageBitmap(bMapRotate);
			bMap.recycle();

			// Load in labels
			mPullLabel = context
					.getString( R.string.aliwx_pull_to_refresh_from_bottom_pull_label);
			mRefreshingLabel = context
					.getString( R.string.aliwx_pull_to_refresh_from_bottom_refreshing_label);
			mReleaseLabel = context
					.getString( R.string.aliwx_pull_to_refresh_from_bottom_release_label);

			break;

		case PULL_DOWN_TO_REFRESH:
		default:
			// Load in labels
			mPullLabel = context.getString( R.string.aliwx_pull_to_refresh_pull_label);
			mRefreshingLabel = context
					.getString( R.string.aliwx_pull_to_refresh_refreshing_label);
			mReleaseLabel = context
					.getString( R.string.aliwx_pull_to_refresh_release_label);
			break;
		}

//		if (attrs.hasValue( R.styleable.aliwx_PullToRefresh_ptrHeaderTextColor)) {
//			ColorStateList colors = attrs
//					.getColorStateList( R.styleable.aliwx_PullToRefresh_ptrHeaderTextColor);
//			setTextColor(null != colors ? colors : ColorStateList
//					.valueOf(0xFF000000));
//		}
//		if (attrs.hasValue( R.styleable.aliwx_PullToRefresh_ptrHeaderSubTextColor)) {
//			ColorStateList colors = attrs
//					.getColorStateList( R.styleable.aliwx_PullToRefresh_ptrHeaderSubTextColor);
//			setSubTextColor(null != colors ? colors : ColorStateList
//					.valueOf(0xFF000000));
//		}
//		if (attrs.hasValue( R.styleable.aliwx_PullToRefresh_ptrHeaderBackground)) {
//			Drawable background = attrs
//					.getDrawable( R.styleable.aliwx_PullToRefresh_ptrHeaderBackground);
//			if (null != background) {
//				setBackgroundDrawable(background);
//			}
//		}

		// Try and get defined drawable from Attrs

		reset();
	}

	private void disableLoading() {
		if (bDisableLoading) {
			mHeaderImage.setVisibility(View.GONE);
			mPullArrowImage.setVisibility(View.GONE);
			mHeaderText.setVisibility(View.GONE);
			mSubHeaderText.setVisibility(View.GONE);
		}
	}

	void reset() {
		mHeaderText.setText(Html.fromHtml(mPullLabel));
		mHeaderImage.setVisibility(View.GONE);
		mPullArrowImage.clearAnimation();
		mPullArrowImage.setVisibility(View.VISIBLE);

		if (TextUtils.isEmpty(mSubHeaderText.getText())) {
			mSubHeaderText.setVisibility(View.GONE);
		} else {
			mSubHeaderText.setVisibility(View.VISIBLE);
		}

		disableLoading();
	}

	

	void releaseToRefresh() {
		if (!bDisableLoading) {
			mPullArrowImage.setVisibility(View.VISIBLE);
			mHeaderImage.setVisibility(View.GONE);
			mPullArrowImage.clearAnimation();
			mPullArrowImage.startAnimation(flipAnimation);
			mHeaderText.setText(Html.fromHtml(mReleaseLabel));
		} else {
			mPullArrowImage.clearAnimation();
			mPullArrowImage.setVisibility(View.GONE);
		}

		disableLoading();
	}

	public void setPullLabel(String pullLabel) {
		mPullLabel = pullLabel;
	}

	void refreshing() {

		mPullArrowImage.setVisibility(View.INVISIBLE);
		mPullArrowImage.clearAnimation();
		mHeaderText.setVisibility(View.VISIBLE);
		mHeaderText.setText(Html.fromHtml(mRefreshingLabel));
		mHeaderImage.setVisibility(View.VISIBLE);
		mSubHeaderText.setVisibility(View.GONE);
	}

	public void setRefreshingLabel(String refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		mReleaseLabel = releaseLabel;
	}

	void pullToRefresh() {
		mPullArrowImage.setVisibility(View.VISIBLE);
		mHeaderImage.setVisibility(View.GONE);
		mPullArrowImage.clearAnimation();
		mPullArrowImage.startAnimation(reverseFlipAnimation);
		mHeaderText.setText(Html.fromHtml(mPullLabel));

		disableLoading();
	}

	public void setTextColor(ColorStateList color) {
		mHeaderText.setTextColor(color);
		mSubHeaderText.setTextColor(color);
	}

	public void setSubTextColor(ColorStateList color) {
		mSubHeaderText.setTextColor(color);
	}

	public void setTextColor(int color) {
		setTextColor(ColorStateList.valueOf(color));
	}

	public void setSubTextColor(int color) {
		setSubTextColor(ColorStateList.valueOf(color));
	}

	public void setSubHeaderText(CharSequence label) {
		if (TextUtils.isEmpty(label)) {
			mSubHeaderText.setVisibility(View.GONE);
		} else {
			mSubHeaderText.setText(label);
			mSubHeaderText.setVisibility(View.VISIBLE);
		}
		if (bDisableLoading) {
			mSubHeaderText.setVisibility(View.GONE);
		}
	}

	void onPullY(float scaleOfHeight) {

	}

	public boolean isDisableLoading() {
		return bDisableLoading;
	}

	public void setDisableLoading(boolean bDisableLoading) {
		this.bDisableLoading = bDisableLoading;
		if (bDisableLoading) {
			disableLoading();
		} else {

		}
	}
}
