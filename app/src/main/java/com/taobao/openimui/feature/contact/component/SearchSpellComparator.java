package com.taobao.openimui.feature.contact.component;

import android.text.TextUtils;


import java.text.Collator;
import java.util.Comparator;

public class SearchSpellComparator implements Comparator<ISearchable> {
	//Collator 字符串大小比较 ，联合Collections.sort排序pinyin人名  shuehng
	private final Collator collator = Collator
			.getInstance(java.util.Locale.CHINA);

	@Override
	public int compare(ISearchable obj1, ISearchable obj2) {
		if (obj1 == null) {
			if (obj2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (obj2 == null) {
				return -1;
			} else {
				if (TextUtils.isEmpty(obj1.getShowName())) {
					if (TextUtils.isEmpty(obj2.getShowName())) {
						return 0;
					} else {
						return 1;
					}
				} else {
					if (TextUtils.isEmpty(obj2.getShowName())) {
						return -1;
					} else {
						boolean first = obj1.isFirstCharChinese();
						boolean second = obj2.isFirstCharChinese();
						if (first && !second) {
							return 1;
						}

						if (!first && second) {
							return -1;
						}
						return collator.compare(obj1.getShowName(),
								obj2.getShowName());

					}
				}
			}
		}
	}

}