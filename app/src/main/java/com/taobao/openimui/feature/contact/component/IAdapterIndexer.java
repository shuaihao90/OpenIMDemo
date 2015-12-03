package com.taobao.openimui.feature.contact.component;

import android.widget.SectionIndexer;

/**
 * adapter 中indexer相关的操作借口
 * @author shuheng
 * @param <T>
 * @param <T>
 *
 * @param <T>
 */
public interface IAdapterIndexer<T> extends SectionIndexer{
	
	/**
	 * 刷新
	 */
	void updateIndexer();
	
	/**
	 * 清空indexer
	 */
	void clearIndexr();
	
	/**
	 * 用制定jiegout
	 * @param model
	 */
	void updateIndexer(T model);
}
