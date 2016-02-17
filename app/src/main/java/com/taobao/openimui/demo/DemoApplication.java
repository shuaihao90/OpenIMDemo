package com.taobao.openimui.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.alibaba.wxlib.util.SysUtil;
import com.taobao.openimui.sample.InitHelper;

public class DemoApplication extends MultiDexApplication {
	//云旺OpenIM的DEMO用到的Application上下文实例
	private static Context sContext;
	public static Context getContext(){
		return sContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		//todo Application.onCreate中，首先执行这部分代码，以下代码固定在此处，不要改动，这里return是为了退出Application.onCreate！！！
		if(mustRunFirstInsideApplicationOnCreate()){
			//todo 如果在":TCMSSevice"进程中，无需进行openIM和app业务的初始化，以节省内存
			return;
		}

		//初始化云旺SDK
		InitHelper.initYWSDK(this);

	}

	private boolean mustRunFirstInsideApplicationOnCreate() {
		//必须的初始化
		SysUtil.setApplication(this);
		sContext = getApplicationContext();
		return SysUtil.isTCMSServiceProcess(sContext);
	}

}
