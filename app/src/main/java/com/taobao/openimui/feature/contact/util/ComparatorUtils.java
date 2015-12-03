package com.taobao.openimui.feature.contact.util;


import com.taobao.openimui.feature.contact.component.ISearchable;
import com.taobao.openimui.feature.contact.component.SearchSpellComparator;

import java.text.Collator;
import java.util.Comparator;

public class ComparatorUtils {

	/**
	 * 比较接口实现，按拼音排序，#开头的在最后，内部排序用中文的编码排序
	 */
	public static final Comparator<ISearchable> searchComparator = new SearchSpellComparator();



}
