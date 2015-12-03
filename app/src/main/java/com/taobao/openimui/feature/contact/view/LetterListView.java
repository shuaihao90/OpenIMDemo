package com.taobao.openimui.feature.contact.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.openIMUIDemo.R;


public class LetterListView extends View {

	private  Context mContext;
	OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	String[] b = {"A","B","C","D","E","F","G","H","I","J","K","L"
			,"M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","#"};
	int choose = -1;
	Paint paint = new Paint();
	boolean showBkg = false;
	private Bitmap mSearchDrawable;
	private Bitmap mSearchPressed;

	public LetterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		mSearchDrawable = BitmapFactory.decodeResource(context.getResources(), R.drawable.aliwx_friends_search_icon);
		mSearchPressed = BitmapFactory.decodeResource(context.getResources(),  R.drawable.aliwx_friends_search_icon_pressed);
	}

	public LetterListView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		mContext=context;
	}

	public LetterListView(Context context) {
		this(context,null);
		mContext=context;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(showBkg){
		    canvas.drawColor(Color.parseColor("#40000000"));
		}
		
	    int height = getHeight();
	    int width = getWidth();
	    int singleHeight = height / (b.length+1);
	    for(int i=0;i<b.length;i++){
	       paint.setColor(0xFF999999);
	       paint.setTypeface(Typeface.DEFAULT_BOLD);
	       paint.setAntiAlias(true);
	       paint.setTextSize(getContext().getResources().getDimensionPixelSize (R.dimen.aliwx_friend_letter_size));
	       if(i+1 == choose){
	    	   paint.setColor(Color.parseColor("#3399ff"));
	    	   paint.setFakeBoldText(true);
	       }
	       float xPos = width/2  - paint.measureText(b[i])/2;
	       float yPos = singleHeight * (i+1) + singleHeight;
	       canvas.drawText(b[i], xPos, yPos, paint);
	       paint.reset();
	    }
	    if(choose == 0){
	    	canvas.drawBitmap(mSearchPressed, width/2-mSearchPressed.getWidth()/2, singleHeight/2, null);
	    }else{
	    	canvas.drawBitmap(mSearchDrawable, width/2-mSearchPressed.getWidth()/2, singleHeight/2, null);
	    }
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
	    final float y = event.getY();
	    final int oldChoose = choose;
	    final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
	    final int c = (int) (y/getHeight()*b.length);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				showBkg = true;
				if(oldChoose != c && listener != null){
					if(c >= 0 && c< b.length+1){
						if(c== 0){
							listener.onTouchingLetterChanged("");
						}else{
							listener.onTouchingLetterChanged(b[c-1]);
						}
						choose = c;
						invalidate();
					}
				}
				
				break;
			case MotionEvent.ACTION_MOVE:
				if(oldChoose != c && listener != null){
					if(c >= 0 && c< b.length+1){
						if(c== 0){
							listener.onTouchingLetterChanged("");
						}else{
							listener.onTouchingLetterChanged(b[c-1]);
						}
						choose = c;
						invalidate();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				showBkg = false;
				choose = -1;
				invalidate();
				break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener{
		void onTouchingLetterChanged(String s);
	}
	
}
