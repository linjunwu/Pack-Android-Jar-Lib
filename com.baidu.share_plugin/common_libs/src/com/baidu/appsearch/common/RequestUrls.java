package com.baidu.appsearch.common;

import android.content.Context;

import com.baidu.appsearch.config.BaseConfigURL;
import com.baidu.appsearch.config.Default;

/**
 * Common模块的url获取控制
 * Created by zhushiyu01 on 15-11-10.
 */
public final class RequestUrls extends BaseConfigURL {

    /** 单例 */
    private static RequestUrls mInstance;

    /**
     * 获取单例
     * @param context context
     * @return RequestUrls
     */
    public static synchronized RequestUrls getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestUrls(context);
        }

        return mInstance;
    }

    /** 日志回传功能，上传日志的接口 */
    @Default("/appsrv?action=catchlog&native_api=1")
    public static final String LOG_TRACER_UPLOAD_URL = "log_tracer_upload";

    /** 服务端配置接口 */
    @Default("/appsrv?native_api=1&action=interfacesec")
    public static final String SERVER_COMMAND_URL = "server_command_url";

    /** 用户行为统计地址 */
    @Default("/appsrv?native_api=1&action=clientuserlog")
    public static final String USERLOG = "userlog";

    /**
     * 构造函数
     * @param context context
     */
    private RequestUrls(Context context) {
        super(context);
    }
}
