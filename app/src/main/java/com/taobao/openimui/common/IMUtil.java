package com.taobao.openimui.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.YWChannel;
import com.alibaba.mobileim.channel.HttpChannel;
import com.alibaba.mobileim.channel.IMChannel;
import com.alibaba.mobileim.channel.constant.Domains;
import com.alibaba.mobileim.channel.constant.WXConstant;
import com.alibaba.mobileim.channel.itf.JsonPacker;
import com.alibaba.mobileim.channel.message.profilecard.ProfileCardMessagePacker;
import com.alibaba.mobileim.channel.message.share.ShareMsgPacker;
import com.alibaba.mobileim.channel.util.AccountUtils;
import com.alibaba.mobileim.channel.util.WXThreadPoolMgr;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.lib.model.httpmodel.NetWorkState;
import com.alibaba.mobileim.lib.model.message.ProfileCardMessage;
import com.alibaba.mobileim.lib.model.message.ShareMessage;
import com.alibaba.mobileim.lib.model.message.TemplateMessage;
import com.alibaba.mobileim.lib.presenter.account.IIMConfig;
import com.alibaba.mobileim.utility.IMConstants;
import com.alibaba.mobileim.utility.TicketConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
