package com.taobao.openimui.feature.contact.component;

import java.util.regex.Pattern;

/**
 * 可搜索接口
 * @author shuheng
 *
 */
public interface ISearchable<T> {
	
//	public static final String SPELL_SPLIT = "\r";
Pattern chinesePattern = Pattern.compile("[\u4e00-\u9fa5]"); // 中文字符匹配

	
	/**
	 * 唯一id
	 * @return
	 */
	T getId();
	
	/**
	 * 显示名
	 * @return
	 */
	String getShowName();
	
	String[] getShortPinyins();

	String[] getPinyins();
	
//	public String getFirstChar();

	boolean isFirstCharChinese();
	
	/**
	 * 图标
	 * @return
	 */
	String getIcon();
	
}