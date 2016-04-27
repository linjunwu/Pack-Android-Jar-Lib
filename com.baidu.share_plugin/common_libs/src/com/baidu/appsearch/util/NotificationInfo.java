/**
 * 
 */
package com.baidu.appsearch.util;

import android.app.Notification;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.common.R;
import com.baidu.appsearch.logging.Log;


/**
 * 
 * 获取系统通知栏信息
 * 
 * @author chenyangkun
 * 
 */
public final class NotificationInfo {

    /** TAG */
    private static final String TAG = "NotificationInfo";
    /** DEBUG 开关 */
    private static final boolean DEBUG = CommonConstants.DEBUG;

    /** 通知栏标题颜色: 默认白色 */
    private static Integer sNotiTitleColor = null;
    /**通知栏第二行颜色，默认灰色*/
    private static Integer sNotiTipContentColor = null;
    /** 测试用通知标题 */
    private static final String COLOR_SEARCH_RECURSE_TIP_TITLE = "SOME_SAMPLE_TEXT";
    /** 测试用通知内容 */
    private static final String COLOR_SEARCH_RECURSE_TIP_CONTENT = "SOME_SAMPLE_TEXT_CONTENT";

    /**
     * 从ViewGroup中查找颜色
     * @param gp ViewGroup
     * @return 是否查找成功
     */
    private static boolean recurseGroup(ViewGroup gp) {
        final int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            if (gp.getChildAt(i) instanceof TextView) {
                final TextView text = (TextView) gp.getChildAt(i);
                final String szText = text.getText().toString();
                if (COLOR_SEARCH_RECURSE_TIP_TITLE.equals(szText)) {
                    sNotiTitleColor = text.getTextColors().getDefaultColor();
                    if (DEBUG) {
                        Log.d(TAG, "--- Get notification tile color: " + sNotiTitleColor);
                    }
                }
                if (COLOR_SEARCH_RECURSE_TIP_CONTENT.equals(szText)) {
                    sNotiTipContentColor = text.getTextColors().getDefaultColor();
                    if (DEBUG) {
                        Log.d(TAG, "--- Get notification tile color: " + sNotiTitleColor);
                    }
                }
                
                if (sNotiTipContentColor != null && sNotiTitleColor != null) {
                    return true;
                }
            } else if (gp.getChildAt(i) instanceof ViewGroup) {
                boolean relt = recurseGroup((ViewGroup) gp.getChildAt(i));
                if (relt) {
                    return relt;
                }
            }
        }
        return false;
    }

    /**
     * 提取系统通知栏颜色
     * 
     * @param ctx
     *            上下文
     */
    @SuppressWarnings("deprecation")
    private static void extractColors(Context ctx) {

        try {
            Notification ntf = new Notification();
            ntf.setLatestEventInfo(ctx, COLOR_SEARCH_RECURSE_TIP_TITLE, COLOR_SEARCH_RECURSE_TIP_CONTENT, null);
            LinearLayout group = new LinearLayout(ctx);
            ViewGroup event = (ViewGroup) ntf.contentView.apply(ctx, group);
            recurseGroup(event);
            group.removeAllViews();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "*** Get noti color exp! ", e);
            }
        }
    }

    /**
     * 获取系统通知栏标题的颜色
     * 
     * @param ctx
     *            上下文
     * @return 颜色值
     */
    public static int getSystemNotiTitleColor(Context ctx) {
        if (sNotiTitleColor == null) {
            extractColors(ctx);
        }
        if (sNotiTitleColor != null) {
            return sNotiTitleColor.intValue();
        } else {
            return ctx.getResources().getColor(R.color.common_grey);
        }

    }
    
    /**
     * 获取系统通知栏第二行的颜色
     * 
     * @param ctx
     *            上下文
     * @return 颜色值
     */
    public static int getSystemNotiTipContentColor(Context ctx) {
        if (sNotiTipContentColor == null) {
            extractColors(ctx);
        }
        if (sNotiTipContentColor != null) {
            return sNotiTipContentColor.intValue();
        } else {
            return ctx.getResources().getColor(R.color.common_grey);
        }

    }

    /**
     * 不允许实例化
     */
    private NotificationInfo() {

    }

}
