package com.taobao.openimui.common;

public class IMUtil {

    public static final boolean isEnglishOnly(char c) {
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        return false;
    }
}