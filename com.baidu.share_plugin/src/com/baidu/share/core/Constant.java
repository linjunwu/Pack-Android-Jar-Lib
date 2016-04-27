package com.baidu.share.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.appsearch.config.CommonConstants;

/**
 * Constant
 *
 * @author linjunwu
 * @since 2016/1/25
 */
public class Constant {

    public static final String WEIXIN_APP_ID = "wxba51387d0626313d";

    public class ShareContextKey {
        public static final String KEY_TITLE = "title";
        public static final String KEY_CONTENT = "content";
        public static final String KEY_IMAGE_URL = "image_url";
        public static final String KEY_BITMAP = "bitmap";
        public static final String KEY_LINK_URL = "link_url";
        public static final String KEY_SHARE_TYPE = "share_type";
        public static final String KEY_FORM = "form";
        public static final String KEY_FROM = "from";
        public static final String KEY_UPDATEDATABASE = "updatedatabase";
    }

    public class ShareConstant {

        public static final String SHARE_COMPLETE_WITH_JSONOBJECT = "complete_with_jsonobject";
        public static final String SHARE_COMPLETE_WITH_JSONARRAY = "share_complete_with_jsonarray";
        public static final String SHARE_COMPLETE_WITH_NULL = "share_complete_with_null";
        public static final String SHARE_CANCEL = "share_cancel";
        public static final String SHARE_ERROR = "share_error";
    }


}
