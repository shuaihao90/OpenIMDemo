package com.taobao.openimui.feature.contact.component;

import android.text.TextUtils;


import com.taobao.openimui.common.IMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModelIndexer implements IAdapterIndexer<List<ComparableContact>> {
	//不同分组
	private List<String> mSections;
	//不同分组首个联系人的mContactList中postion（mContactList已按首字母排序好）shuheng@2015.9
	private List<Integer> mPositions;
	private int mCount;
	private List<ComparableContact> mContactList;

	public ModelIndexer(List<ComparableContact> list) {
		mSections = new ArrayList<String>();
		mPositions = new ArrayList<Integer>();
		this.mContactList = list;
		init();
	}
	//初始化alpha分组
	private void init(){
		if(mContactList != null){
			mCount = mContactList.size();
			String lastString = null;
			//收集拼音，供分组排序,不同的首字母加到不同的section
			for(int i = 0; i < mCount; i++){

				String[] names = mContactList.get(i).getShortPinyins();
				if (names != null) {
					String thisString = null;
					if (names.length == 0 || TextUtils.isEmpty(names[0]) || !IMUtil.isEnglishOnly(names[0].charAt(0))) {
						thisString = "#";
					} else {
						thisString = names[0].substring(0, 1).toUpperCase(Locale.getDefault());
					}
					if (!thisString.equals(lastString)) {
						mSections.add(thisString);
						mPositions.add(i);
						lastString = thisString;
					}
				}else{
					String thisString = "#";
					if (!thisString.equals(lastString)) {
						mSections.add(thisString);
						mPositions.add(i);
						lastString = thisString;
					}
				}
			}
		}
		
	}

	/**
	 * @param alpha
	 * @return 字母所处的section list位置
	 */
	public int getSectionForItem(String alpha){
		return mSections.indexOf(alpha);
	}

	/**
	 * @param section
	 * @return 首个联系人mcontactsList位置
	 */
	@Override
	public int getPositionForSection(int section) {
		if (section < 0 || section >= mSections.size()) {
			return -1;
		}

		return mPositions.get(section);
	}

	/**
	 * @param position
	 * @return 联系人位置所处的sectionlist位置
	 */
	@Override
	public int getSectionForPosition(int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		int index = mPositions.indexOf(position);

		return index >= 0 ? index : -index - 2;
	}

	@Override
	public Object[] getSections() {
		return mSections.toArray();
	}

	@Override
	public void updateIndexer() {
		mSections = new ArrayList<String>();
		mPositions = new ArrayList<Integer>();
		mCount = 0;
		init();
	}
	

	@Override
	public void clearIndexr() {
		mSections.clear();
		mPositions.clear();
		mCount = 0;
	}

	@Override
	public void updateIndexer(List<ComparableContact> model) {
		mContactList = model;
		updateIndexer();
	}

}