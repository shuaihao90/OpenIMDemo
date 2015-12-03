package com.taobao.openimui.feature.contact.component;

import android.text.TextUtils;

import com.alibaba.mobileim.contact.IYWDBContact;
import com.alibaba.mobileim.utility.PinYinUtil;
import com.taobao.openimui.common.IMUtil;

import java.text.Collator;
import java.util.ArrayList;

public class ComparableContact implements IYWDBContact,Comparable<ComparableContact>,ISearchable<String> {
    //Collator 字符串大小比较 ，联合Quiksort排序pinyin人名  shuehng
    public static final String TAG=ComparableContact.class.getSimpleName();
    private static final Collator collator = Collator.getInstance();
    protected transient String[] shortNames;
    public static final String SPELL_SPLIT = "\r";
    public String shortname; // 姓名简拼
    private String firstChar = "";
    private boolean isFirstCharEnglish;
    private String[] nameSpells;
    private String nameSpell;
    public void setShowName(String showName) {
        this.showName = showName;
    }

    private boolean isFirstCharChinese;
    @Override
    public int compareTo(ComparableContact another) {

        if (another == null) {
            return -1;
        } else {
            boolean isEng1 = isFirstCharEnglish();
            boolean isEng2 = another.isFirstCharEnglish();
            if (!isEng1) {
                if (isEng2) {
                    return 1;
                }
            } else {
                if (!isEng2) {
                    return -1;
                }
            }
            int code = collator.compare(getFirstChar(), another.getFirstChar());
            if (code == 0) {
                if (getShowName() == null) {
                    if (another.getShowName() == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (another.getShowName() == null) {
                        return -1;
                    } else {
                        return collator.compare(getShowName(),
                                another.getShowName());

                    }
                }
            }
            return code;
        }

    }

    public String[] getShortPinyins() {

        return shortNames;
    }

    @Override
    public String[] getPinyins() {
        return nameSpells;
    }

    @Override
    public boolean isFirstCharChinese() {
        return isFirstCharChinese;
    }

    @Override
    public String getIcon() {
        return avatarPath;
    }
    @Override
    public String getId() {
        return userid;
    }


    public String getFirstChar() {
        return firstChar;
    }

    public boolean isFirstCharEnglish() {
        return isFirstCharEnglish;
    }

    private String userid = "", appKey = "", avatarPath = "", showName = "";

    public ComparableContact(String showName, String userid, String avatarPath, String appKey) {
        this.showName = showName;
        this.userid = userid;
        this.avatarPath = avatarPath;
        this.appKey = appKey;
    }

    @Override
    public String getUserId() {
        return userid;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public String getAvatarPath() {
        return avatarPath;
    }


    @Override
    public String getShowName() {
        return showName;
    }


    public void generateSpell() {
        //在显示名和用户名间添加字符“ ”分隔开，解决拼音首字符反序查找的问题
        String name = getShowName() + " " + getUserId();
        if (!TextUtils.isEmpty(name)) {
            ArrayList<String> shortNamesList = new ArrayList<String>();
            ArrayList<String> nameSpellsList = new ArrayList<String>();
            int length = name.length();
            for (int i = 0; i < length; i++) {
                java.util.ArrayList<String> pinyins = PinYinUtil
                        .findPinyin(name.charAt(i));
                if (pinyins != null && pinyins.size() > 0) {
                    int snSize = shortNamesList.size();
                    ArrayList<String> shortNamescache = new ArrayList<String>();
                    ArrayList<String> nameSpellscache = new ArrayList<String>();
                    for (int j = 0, size = pinyins.size(); j < size; j++) {
                        String pinyin = pinyins.get(j);

                        if (pinyin != null) {
                            if (snSize == 0) {
                                shortNamescache.add(String.valueOf(pinyin
                                        .charAt(0)));
                                nameSpellscache.add(pinyin);
                            } else {
                                for (int k = 0; k < snSize; k++) {
                                    try {
                                        shortNamescache.add(shortNamesList
                                                .get(k)
                                                + String.valueOf(pinyin
                                                .charAt(0)));
                                        nameSpellscache.add(nameSpellsList
                                                .get(k) + pinyin);
                                    } catch (OutOfMemoryError e) {
                                    }
                                }
                            }
                        }
                    }

                    if (shortNamescache.size() > 0) {
                        shortNamesList.clear();
                        shortNamesList.addAll(shortNamescache);
                        nameSpellsList.clear();
                        nameSpellsList.addAll(nameSpellscache);
                    }
                }
            }
            nameSpells = nameSpellsList.toArray(new String[]{});
            try {
                nameSpell = TextUtils.join(SPELL_SPLIT, nameSpells);
            } catch (OutOfMemoryError e) {
            }
            shortNames = shortNamesList.toArray(new String[]{});
            try {
                shortname = TextUtils.join(SPELL_SPLIT, shortNames);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            if (shortNames.length > 0 && !TextUtils.isEmpty(this.shortNames[0])) {
                char c = this.shortNames[0].charAt(0);
                isFirstCharEnglish = IMUtil.isEnglishOnly(c);
                firstChar = String.valueOf(c);
            }
        }
    }

    private int status= ONLINESTATUS_ONLINE_YW;

    public int getOnlineStatus() {
        return status;
    }
    public void setOnlineStatus(int status){
        this.status=status;
    }

}
