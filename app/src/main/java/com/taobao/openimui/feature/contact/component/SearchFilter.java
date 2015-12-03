package com.taobao.openimui.feature.contact.component;

import android.text.TextUtils;
import android.widget.Filter;


import com.taobao.openimui.feature.contact.util.ComparatorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 搜索过滤器
 * 
 * @author shuheng
 *
 *
 * Filter是Android提供的异步字符序列筛选器
 */
public class SearchFilter extends Filter {

//	private List<ISearchable> mSearchList = new ArrayList<ISearchable>();
	private List<ISearchable> mFilterList;
	private List<List<? extends ISearchable>> mSearchableList=new ArrayList<List<? extends ISearchable>>();

	public SearchFilter(List<ISearchable> filterList) {
		mFilterList = filterList;
	}

	/**
	 * 增加需要搜索的列表
	 * 
	 * @param searchList
	 */
	public void addSearchList(List<? extends ISearchable> searchList,boolean needsort) {
		if (needsort) {
			//mergesort O(nlgn) shuheng
			Collections.sort(searchList, ComparatorUtils.searchComparator);
		}
		synchronized (mSearchableList) {
//			this.mSearchList.addAll(searchList);
			mSearchableList.add(searchList);
		}
	}
	
	public void addSearchList(List<? extends ISearchable> searchList) {
		addSearchList(searchList,true);
	}
	
	public void clear() {
		synchronized (mSearchableList) {
			mSearchableList.clear();
		}
	}

	public  List<List<? extends ISearchable>> getSearchableList(){
		return mSearchableList;
	}
	
	
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults result = new FilterResults();
		if (!TextUtils.isEmpty(constraint)) {
			String keyword = constraint.toString()
					.toLowerCase(Locale.getDefault()).trim();
			List<ISearchable> list = new ArrayList<ISearchable>();
			synchronized (mSearchableList) {
				for(List<? extends ISearchable> searchableList:mSearchableList){
					//筛选searchableList中字符串匹配规则的放到list中
					findMatched(list, searchableList, keyword);
				}
			}
			// ContactUtil.findMatchedPub(list, mPubList, keyword);
			if (list.size() > 0) {
				result.values = list;
				result.count = list.size();
			}
		}
		return result;
	}

	public static void findMatched(List<ISearchable> resultList,
			List<? extends ISearchable> searchList, String keyword) {
		if (searchList == null || resultList == null) {
			return;
		}
		if(TextUtils.isEmpty(keyword)||TextUtils.isEmpty(keyword.trim())){
			return;
		}		
		Set<Object> singleIdSet=new HashSet<Object>(); 
		for (ISearchable user : searchList) {
			if(singleIdSet.contains(user.getId())){
				continue;
			}
			singleIdSet.add(user.getId());
			String userName = "";
			if (user.getShowName() != null) {
				//用户名匹配
				userName = user.getShowName().toLowerCase(Locale.getDefault());
			}
			String[] shortNames = null;
			if (user.getShortPinyins() != null) {
				//拼音首字母组合缩写匹配
				shortNames = user.getShortPinyins();
			}
			String[] nameSpells = null;
			if (user.getPinyins() != null) {
				//用户名拼音匹配
				nameSpells = user.getPinyins();
			}
			// String userId = user.getId();
			if (userName.contains(keyword)) {
				resultList.add(user);
			} else {
				if (shortNames != null && shortNames.length > 0
						&& nameSpells != null
						&& shortNames.length == nameSpells.length) {
					int length = shortNames.length;
					for (int j = 0; j < length; j++) {
						if ((shortNames[j] != null && shortNames[j]
								.contains(keyword))
								|| (nameSpells[j] != null && nameSpells[j]
										.contains(keyword))) {
							resultList.add(user);
							break;
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		mFilterList.clear();
		if (results.values != null) {
			mFilterList.addAll((List<ISearchable>) results.values);
		}
	}
}
