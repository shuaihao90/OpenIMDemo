package com.taobao.openimui.sample;

import com.alibaba.mobileim.ui.chat.widget.YWSmilyMgr;
import com.alibaba.openIMUIDemo.R;

import java.util.List;

/**
 * Created by zhaoxu on 2015/10/28.
 * 自定义表情示例
 * 自定义表情，SDK允许开发者替换（新增）现有的表情内容
 *
 * 先约定一下几个概念
 * 1.表情快捷键(shortCuts)，用于在协议中传输的表情符号
 * 2.表情含义(meanings)，表情中文含义，一般用于显示在通知栏中
 * 3.表情资源ID号(smilyRes)，这个好理解，就是Android的资源ID
 *
 * 开发者通过设置以上三个值来进行表情的功能的扩展，以上三个值的个数是一致的
 */
public class SmilyCustomSample {

    public static void init(){
        addSmilyMeanings();
        addShortCuts();
        addSmily();
    }

    private static void addSmilyMeanings(){
        List<String> list = YWSmilyMgr.getMeanings();
        list.add("测试表情");
        YWSmilyMgr.setMeanings(list);
    }

    private static void addShortCuts(){
        List<String> list = YWSmilyMgr.getShortCuts();
        list.add("<>:)-");
        YWSmilyMgr.setShortCuts(list);
    }

    private static void addSmily(){
        List<Integer> list = YWSmilyMgr.getSmilyRes();
        list.add(R.drawable.__leak_canary_icon);
        YWSmilyMgr.setSmilyRes(list);
    }
}
