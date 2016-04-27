/**
 * Copyright (c) 2013 Baidu Inc.
 * @date 2013-4-11
 */
package com.baidu.appsearch.statistic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.config.CommonConstants;

/**
 * 用户行为统计的接收广播，接收检查统计上传和时间改变
 * 
 * @author wangguanghui01
 * 
 */
public class UEStatisticReceiver extends BroadcastReceiver {

    /** TAG */
    private static final String TAG = "UEStatisticReceiver";
    /** DEBUG开关 */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DEBUG) {
            Log.d(TAG, "UEStatisticReceiver----------action = " + action);
        }
        if (TextUtils.equals(action, StatisticPoster.BROADCAST_CHECK_SEND_STATISTIC_DATA)
                || TextUtils.equals(action, Intent.ACTION_TIME_CHANGED)) {
            // 检查是否需要发送统计数据

            String postContent = StatisticPoster.getInstance(context).getPostContent();
            StatisticPoster.getInstance(context).checkSendStatisticData(TAG, postContent);
        }

    }

}
