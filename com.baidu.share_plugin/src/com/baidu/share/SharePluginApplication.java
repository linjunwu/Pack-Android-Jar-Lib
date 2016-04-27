package com.baidu.share;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.baidu.share.utils.LogUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * SharePluginApplication
 *
 * @author linjunwu
 * @since 2016/2/18
 */
public class SharePluginApplication extends Application {
    /**TAG*/
    protected static final String TAG = "SharePluginApplication";

    /** 全局的Context  */
    private static Context sContext;
    /**Handler*/
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        initImageLoader(this);
    }

    /**
     * @return the mContext
     */
    public static Context getAppContext() {
        if (sContext == null) {

            LogUtil.e(TAG, "Global context not set");

        }
        return sContext;
    }

    /**
     * 初始化imageloader
     * @param context
     *          context
     */
    private void initImageLoader(Context context) {
        DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .supportWebp(true)
                .handler(mHandler)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .discCacheExtraOptions(0, 0, Bitmap.CompressFormat.PNG, 100, null) // SUPPRESS CHECKSTYLE
//                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}
