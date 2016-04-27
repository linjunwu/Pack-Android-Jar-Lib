/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author      Qingbiao Liu <liuqingbiao@baidu.com>,zhangjunguo <zhangjunguo@baidu.com>
 * 
 * @date 2012-7-3
 */
package com.baidu.share.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import com.baidu.share.SharePluginApplication;
import com.baidu.appsearch.config.BaseConfigURL;
import com.baidu.appsearch.config.CommonGloabalVar;
import com.baidu.appsearch.config.Configrations;
import com.baidu.appsearch.config.Default;
import com.baidu.appsearch.util.uriext.UriHelper;
import com.baidu.util.Base64Encoder;

/**
 * 统一管理AppSearch中用的Urls.
 */
public final class AppSearchUrl extends BaseConfigURL {

    /**
     * 带特型的搜索建议key
     */
    private static final String SUGURL_NEW = "sugurl_new";
    /**
     * 获取客户端首页所有二级TAB接口.key
     */
    private static final String ALL_TAB = "alltab";
    /**
     * 请求收藏更新的数据地址。 key
     */
    private static final String COLLECTIONUPDATE = "collection";

    /** 关于页地址key */
    private static final String ABOUT_URL = "about";
    /** 关于91页地址key */
    private static final String ABOUT_91_URL = "about91";
    /** 关于91页地址key */
    private static final String ABOUT_HIMARKET_URL = "abouthimarket";
    /** 帮助页地址key */
    private static final String HELP_URL = "help";
    /** 反馈页地址key */
    private static final String FEEDBACK_URL = "feedback";
    /** userid获取地址key */
    private static final String USERIDGRAB_URL = "useridgrab";
    /** 根据docid获取下载信息地址key */
    private static final String DOCID_URL = "docid_new";
    /** 高速下载请求浏览器下载目录时发送一个统计请求的地址key */
    private static final String HIGHDOWNLOAD_STATIS_DIR_URL = "highdownload_statis_dir";
    /** 获取主流浏览器的下载目录地址key */
    private static final String BROWSER_DOWNLOAD_URL = "browserdownloadurl";
    /** 获取新功能说明的地址 */
    private static final String INSTRUCTION_URL = "instruction";
    /** 安卓优化大师应用信息地址 */
    private static final String YOUHUADASHI_APP_INFO_URL = "youhuadashi_app_info_url";
    /** 91连接助手下载地址的key */
    private static final String CONNECT_PC_URL = "connect_pc_url_key";
    /** 获取卸载反馈的地址 */
    private static final String UNINSTALL_FEEDBACK_URL = "uninstall_feedback";
    /** 获取流量管理校准规则地址key */
    private static final String NETFLOW_CORRECT_RULE_URL = "netflowcorrectrule";
    /** 装机必备列表地址key */
    private static final String MUST_INSTALL_LIST_URL = "mustinstallurl";
    
    /** server 地址。 */
    private final String mServer = getServerAddress();

    /** 关于页地址 */
    private String mAboutUrl = mServer + "/appsrv?native_api=1&action=about";
    /** 关于91页地址 */
    private String mAbout91Url = mServer + "/appsrv?native_api=1&action=about91";
    /** 关于安卓市场页地址 */
    private String mAboutHiMarketUrl = mServer + "/appsrv?native_api=1&action=abouthimarket";
    /** 帮助页地址 */
    private String mHelpUrl = mServer + "/app?action=clienthelp";
    /** 反馈页地址 */

    /** 如果有改URL，则启动网页反馈，如果没有，则启动邮件反馈，在2.1版本是这样的逻辑。 */
    private String mFeedbackUrl = mServer + "/appsrv?action=feedback";
    /** 获取主流浏览器的下载目录 */
    private String mBroswerDownPathUrl = "http://wap.baidu.com/static/freeapp/broswer_down_path.cfg?v=1";
    /** 根据docid获取下载信息 */
    private String mDowninfoWithDocidUrl = mServer + "/appsrv?native_api=1&action=hdownloadentry";
    /** 高速下载请求浏览器下载目录时发送一个统计请求的地址 */
    private String mHighDownloadDirReqStaUrl = mServer + "/appsrv?native_api=1&action=highdownloadstatistic&type=1";

    /** 新的sug接口地址 */
    private String mNewSugSeverUrl = mServer + "/appsrv?native_api=1&action=sug&word=%s";
    
    /**
     * 请求收藏更新的数据地址。
     */
    private String mCollectionUpdateUrl = mServer
            + "/appsrv?action=collection&pu=osname@baiduappsearch";


    /** 记录页面中图标大小的值，详细参看 */
    // public static final String PSIZE = "psize=" + Constants.getPSize();

    /**
     * 客户端首页所有二级TAB获取接口.
     */
    private String mAllTabUrl = mServer + "/appsrv?native_api=1&action=alltabs";
    /** 获取userid地址 */
    private String mUserIdGrabUrl = mServer + "/app?pu=osname@baiduappsearch&action=useridgrab";
    /** 新功能说明地址地址 */
    private String mInstructionUrl = mServer + "/app?action=instruction";

    /** 安卓优化大师应用信息地址 */
    private String mYouhuadashiAppInfoUrl = "http://dxurl.cn/bd/yhds/baidusearch";
    /** 91连接助手下载地址 */
    private String mConnectPcDownloadUrl = 
            "http://gdown.baidu.com/data/wisegame/4362b9c46b958269/baidulianjiezhushou_387.apk";
    
    /** 卸载反馈的地址 */
    private String mUninstallFeedBackUrl = mServer + "/app?pu=osname@baiduappsearch&action=uninstall";
    
    /** 流量管理校准规则的地址 */
    private String mNetFlowCorrectRuleUrl = mServer + "/appsrv?native_api=1&action=netflowacupdate";
    // /-------------------------------------------------------------------------native新添加接口地址
    // BEGIN---
    
    /** 应用详情数据接口Key */
    private static final String APP_DETAIL_DATA_URL = "app_detail_data_url";
    /** 应用详情数据接口 */
    private String mAppDetailDataUrl = mServer + "/appsrv?native_api=1&action=detail";

    /** 线下运营活动地址 */
    private final String mOfflineOperationUrl = mServer + "/appsrv?native_api=1&action=support";
    
    /** 免流量地区接口key */
    private static final String CHECK_FREEDOWN_URL = "freedown_url";
    /** 免流量地区接口地址 */
    private String mFreeDownUrl = mServer + "/appsrv?native_api=1&action=checkfreedown";
    
    /** 首次启动的装机必备接口地址 */
    private String mMustInstallListUrl = mServer + "/appsrv?native_api=1&action=homemust&type=homemust";
    
    /** 下载无任务推荐下载地址 */
    private static final String DOWNLOAD_EMPTY_GUESS = "download_statistic_special";
    /** 下载无任务推荐下载地址 */
    private String mDownloadEmptyGuess = mServer + "/appsrv?native_api=1&action=downguess";

    /**
     * 获取应用详情数据接口
     * 
     * @return 应用详情数据接口
     */
    public String getAppDetailDataUr() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_DETAIL_DATA_URL))) {
            return getServerUrlsConf().get(APP_DETAIL_DATA_URL);
        }
        return mAppDetailDataUrl;
    }
    
    
    /** 百度浏览器极速上网数据接口Key */
    private static final String BAIDU_BROWSER_DATA_URL = "baidu_browser_data_url";
    /** 百度浏览器极速上网数据接口 */
    private String mBaiduBrowserDataUrl = mServer + "/appsrv?native_api=1&action=bindbrowser";

    /**
     * 获取百度浏览器极速上网数据接口
     * 
     * @return 百度浏览器极速上网数据接口
     */
    public String getBaiduBrowserDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BAIDU_BROWSER_DATA_URL))) {
            return getServerUrlsConf().get(BAIDU_BROWSER_DATA_URL);
        }
        return mBaiduBrowserDataUrl;
    }
    
    /** 通知栏动态入口数据接口Key */
    private static final String NOTIFICATION_ENTRANCE_URL_KEY = "notification_entrance_url_key";
    /** 通知栏动态入口数据接口地址 */
    private String mNotificationEntranceUrl = mServer + "/appsrv?native_api=1&action=noticeconf";

    /**
     * 获取通知栏动态入口数据接口地址
     * 
     * @return 通知栏动态入口数据接口地址
     */
    public String getNotificationEntranceUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(NOTIFICATION_ENTRANCE_URL_KEY))) {
            return getServerUrlsConf().get(NOTIFICATION_ENTRANCE_URL_KEY);
        }
        return mNotificationEntranceUrl;
    }

    /** 免流量免责声明接口Key */
    private static final String FREEDOWN_INTRODUCTION_URL = "introduce_webview_url";
    /** 免流量免责声明接口 */
    private String mfreedownIntroductionUrl = mServer + "/appsrv?native_api=1&action=disclaimer";

    /**
     * 获取免流量免责声明接口
     *
     * @return 免流量免责声明接口
     */
    public String getFreedownIntroductionUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(FREEDOWN_INTRODUCTION_URL))) {
            return getServerUrlsConf().get(FREEDOWN_INTRODUCTION_URL);
        }
        return mfreedownIntroductionUrl;
    }

    /** 应用搜索数据接口Key */
    private static final String SEARCH_REQUEST_URL = "search_request_url";
    /** 应用搜索数据接口 */
    private String mSearchRequestUrl = mServer + "/as?tn=native&pn=0&st=10a001&word=";
    
    /**
     * 获取应用搜索数据接口
     * 
     * @return 应用搜索数据接口
     */
    public String getSearchRequestUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(SEARCH_REQUEST_URL))) {
            return getServerUrlsConf().get(SEARCH_REQUEST_URL);
        }
        return mSearchRequestUrl;
    }

    /** 分类下的应用列表数据接口Key */
    private static final String CATEGORY_APP_LIST_DATA_URL = "category_app_list_data_url";
    /** 分类下的应用列表数据接口 */
    private String mCategoryAppListDataUrl = mServer + "/appsrv?action=catelist";

    /**
     * 获取 分类下的应用列表数据接口
     * 
     * @return 分类下的应用列表数据接口
     */
    public String getCategoryAppListDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(CATEGORY_APP_LIST_DATA_URL))) {
            return getServerUrlsConf().get(CATEGORY_APP_LIST_DATA_URL);
        }
        return mCategoryAppListDataUrl;
    }

    /** 专题下的应用列表数据接口Key */
    private static final String TOPIC_APP_LIST_DATA_URL = "topic_app_list_data_url";
    /** 专题下的应用列表数据接口 */
    private String mTopicAppListDataUrl = mServer + "/appsrv?action=topiclist&native_api=1";
    
    /**
     * 获取 分类下的应用列表数据接口
     * 
     * @return 分类下的应用列表数据接口
     */
    public String getTopicAppListDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(TOPIC_APP_LIST_DATA_URL))) {
            return getServerUrlsConf().get(TOPIC_APP_LIST_DATA_URL);
        }
        return mTopicAppListDataUrl;
    }
    
    /** 更多专题下的数据接口Key */
    private static final String MORE_TOPIC_LIST_DATA_URL = "more_topic_list_data_url";
    /** 专题下的应用列表数据接口 */
    private String mMoreTopicListDataUrl = mServer + "/appsrv?action=topiclistmanage&native_api=1";
    
    /**
     * 获取 更多专题数据接口
     * 
     * @return 分类下的应用列表数据接口
     */
    public String getMoreTopicListDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MORE_TOPIC_LIST_DATA_URL))) {
            return getServerUrlsConf().get(MORE_TOPIC_LIST_DATA_URL);
        }
        return mMoreTopicListDataUrl;
    }

    /** 评论列表数据接口Key */
    private static final String APP_COMMENT_LIST_DATA_URL = "app_comment_list_data_new_url";
    /** 评论列表数据接口 */
    private String mAppCommentListDataUrl = mServer + "/appsrv?native_api=1&action=getcommentlist";
    
    /**
     * 获取评论列表数据接口
     * 
     * @return 评论列表数据接口
     */
    public String getAppCommentListDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_LIST_DATA_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_LIST_DATA_URL);
        }
        return mAppCommentListDataUrl;
    }
    
    /** 添加评论数据接口Key */
    private static final String APP_COMMENT_ADD_DATA_URL = "app_comment_add_data_url";
    /** 添加评论数据接口 */
    private String mAppCommentAddDataUrl = mServer + "/appsrv?native_api=1&action=addcomment";
    
    /**
     * 获取添加评论数据接口
     * 
     * @return 添加评论数据接口
     */
    public String getAppCommentAddDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_ADD_DATA_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_ADD_DATA_URL);
        }
        return mAppCommentAddDataUrl;
    }
    
    /** 修改评论数据接口Key */
    private static final String APP_COMMENT_MODIFY_DATA_URL = "app_comment_modify_data_url";
    /** 修改评论数据接口 */
    private String mAppCommentModifyDataUrl = mServer + "/appsrv?native_api=1&action=modifycomment";
    
    /**
     * 获取修改评论数据接口
     * 
     * @return 修改评论数据接口
     */
    public String getAppCommentModifyDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_MODIFY_DATA_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_MODIFY_DATA_URL);
        }
        return mAppCommentModifyDataUrl;
    }
    
    /** 回复评论数据接口Key */
    private static final String APP_COMMENT_REPLY_URL = "app_comment_reply_url";
    /** 回复评论数据接口 */
    private String mAppCommentReplyDataUrl = mServer + "/appsrv?native_api=1&action=addreply";

    /**
     * 回复评论接口
     * 
     * @return 回复评论接口地址
     */
    public String getAppCommentReplyUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_REPLY_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_REPLY_URL);
        }
        return mAppCommentReplyDataUrl;
    }
    
    /** 回复列表数据接口Key */
    private static final String APP_COMMENT_REPLY_LIST_URL = "app_comment_reply_list_url";
    /** 回复列表数据接口 */
    private String mAppCommentReplyListDataUrl = mServer + "/appsrv?native_api=1&action=getreplylist";

    /**
     * 回复列表接口
     * 
     * @return 回复列表接口地址
     */
    public String getAppCommentReplyListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_REPLY_LIST_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_REPLY_LIST_URL);
        }
        return mAppCommentReplyListDataUrl;
    }

    /** 顶评论数据接口Key */
    private static final String APP_COMMENT_LIKE_URL = "app_comment_like_url";
    /** 顶评论数据接口 */
    private String mAppCommentLikeDataUrl = mServer + "/appsrv?native_api=1&action=likecomment";

    /**
     * 顶评论接口
     * 
     * @return 顶评论接口地址
     */
    public String getAppCommentLikeUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_COMMENT_LIKE_URL))) {
            return getServerUrlsConf().get(APP_COMMENT_LIKE_URL);
        }
        return mAppCommentLikeDataUrl;
    }

    /** 上传评论tag状态数据接口Key */
    private static final String LIKE_COMMENT_TAG_URL = "like_comment_tag_url";
    /** 上传评论tag状态数据接口 */
    private String mLikeCommentTagUrl = mServer + "/appsrv?native_api=1&action=likecommenttag";

    /**
     * 上传评论tag状态接口
     * 
     * @return 上传评论tag状态接口地址
     */
    public String getLikeCommentTagUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(LIKE_COMMENT_TAG_URL))) {
            return getServerUrlsConf().get(LIKE_COMMENT_TAG_URL);
        }
        return mLikeCommentTagUrl;
    }

    /** 展现统计上传接口的Key */
    private static final String SHOW_COUNT_UP_URL = "show_count_up_url";
    /** 展现统计上传接口 */
    private String mShowCountUpUrl = mServer + "/appsrv?native_api=1&action=showlog";

    /**
     * 获取展现统计上传接口
     * 
     * @return 展现统计上传接口
     */
    public String getShowCountUpUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(SHOW_COUNT_UP_URL))) {
            return getServerUrlsConf().get(SHOW_COUNT_UP_URL);
        }
        return mShowCountUpUrl;
    }

    /** 装机必备数据接口Key */
    private static final String MUST_INSTALL_APP_LIST_DATA_URL = "must_install_app_list_data_url";
    /** 装机必备数据接口 */
    private String mMustInstallAppListDataUrl = mServer + "/appsrv?action=must";
    
    /**
     * 装机必备评论数据接口
     * 
     * @return 装机必备数据接口
     */
    public String getMustInstallAppListDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MUST_INSTALL_APP_LIST_DATA_URL))) {
            return getServerUrlsConf().get(MUST_INSTALL_APP_LIST_DATA_URL);
        }
        return mMustInstallAppListDataUrl;
    }
    
    // /-------------------------------------------------------------------------native新添加接口地址
    // END---
    
    // /----------------------------------------------push绑定信息上传---------------BEGIN---------------
    /** push绑定信息上传地址Key */
    private static final String BINDINFO_REGISTER_URL = "bindinfo_register_url";
    /** push绑定信息上传接口 */
    private String mBindInfoRegisterUrl = mServer + "/appsrv?native_api=1&action=bindreg";

    /**
     * 获取push绑定信息上传地址
     * 
     * @return push绑定信息上传地址
     */
    public String getBindInfoRegisterUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BINDINFO_REGISTER_URL))) {
            return getServerUrlsConf().get(BINDINFO_REGISTER_URL);
        }
        return mBindInfoRegisterUrl;
    }

    // /----------------------------------------------push绑定信息上传---------------END---------------
    // /----------------------------------------------获取热词---------------BEGIN---------------
    /** 获取热词地址Key */
    private static final String HOT_WORD_URL = "hotkey";
    /** 获取热词接口 */
    private String mHotWordUrl = mServer + "/appsrv?native_api=1&action=hotkey";

    /**
     * 获取热词地址
     * 
     * @return 获取热词地址
     */
    public String getHotWordUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(HOT_WORD_URL))) {
            return getServerUrlsConf().get(HOT_WORD_URL);
        }
        return mHotWordUrl;
    }

    // /----------------------------------------------获取热词---------------END---------------
    // /----------------------------------------------推荐卸载app---------------BEGIN---------------
    /** app推荐卸载地址Key */
    private static final String APP_UNINSTALL_URL = "appuninstall";
    /** 获取app推荐卸载接口 */
    private String mAppUninstallUrl = mServer + "/appsrv?native_api=1&action=appuninstall";
    
    /**
     * 获取推荐卸载地址
     * 
     * @return 推荐卸载地址
     */
    public String getAppUninstallUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_UNINSTALL_URL))) {
            return getServerUrlsConf().get(APP_UNINSTALL_URL);
        }
        return mAppUninstallUrl;
    }

    // /----------------------------------------------推荐卸载app---------------END---------------

    /** 获取开发者链接地址Key */
    private static final String DEVELOPER_URL_KEY = "developer";
    /** 开发者连接页面地址 */
    private String mAppDeveloperInfoUrl = mServer + "/appsrv?native_api=1&action=developer";
    
    /**
     * 获取开发者链接页面
     * @return 开发者链接地址
     */
    public String getAppDeveloperInfoUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(DEVELOPER_URL_KEY))) {
            return getServerUrlsConf().get(DEVELOPER_URL_KEY);
        }
        return mAppDeveloperInfoUrl;
    }

    /** 单例 */
    private static AppSearchUrl instance = null;
    /** context */
    private Context mContext;

    /**
     * 获取AppSearchUrl实例
     * 
     * @param context
     *            非Activity Context
     * @return AppSearchUrl实例
     */
    public static synchronized AppSearchUrl getInstance(Context context) {
        if (instance == null) {
            instance = new AppSearchUrl(context);
        }
        return instance;
    }

    /**
     * 获取客户端首次拉取配置地址
     * 
     * @return 客户端拉取配置地址
     */
    public String getServerCommandUrl() {
        return getServerAddress() + "/appsrv?native_api=1&action=interfacesec";
    }

    /**
     * 释放本实例
     */
    public static synchronized void relaseInstance() {
        instance = null;
    }
    
    /**
     * 私有构造函数
     * 
     * @param context
     *            非Activity Context
     */
    private AppSearchUrl(Context context) {
        super(context);
        mContext = context.getApplicationContext();
    }



    /**
     * 获取客户端首页二级TAB获取地址
     * 
     * @return 客户端首页二级TAB获取地址
     */
    public String getAllTabUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(ALL_TAB))) {
            return getServerUrlsConf().get(ALL_TAB);
        }
        return mAllTabUrl;
    }

    /**
     * 获取收藏更新地址
     * 
     * @return 收藏更新地址
     */
    public String getCollectionUpdateUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(COLLECTIONUPDATE))) {
            return getServerUrlsConf().get(COLLECTIONUPDATE);
        }
        return mCollectionUpdateUrl;
    }

    /**
     * 获取搜索建议地址
     * 
     * @return 搜索建议地址
     */
    public String getNewSugSeverUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(SUGURL_NEW))) {
            return getServerUrlsConf().get(SUGURL_NEW);
        }
        return mNewSugSeverUrl;
    }

    /**
     * 获取新功能说明地址
     * 
     * @return 获取新功能说明地址
     */
    public String getInstructionUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(INSTRUCTION_URL))) {
            return getServerUrlsConf().get(INSTRUCTION_URL);
        }
        return mInstructionUrl;
    }

    


    /**
     * 下载失败统计地址Key
     */
    private static final String DOWNERROR_STATISTIC_URL = "downerror_statistic";

    /**
     * 更新下载完成统计的地址
     */
    private String mDownErrorStatisticUrl = mServer
            + "/appsrv?action=statistic&pu=osname@baiduappsearch&item=downerror&tj=";

    /**
     * 新增的，获取下载失败实时统计的url
     * 
     * @return 统计发送地址
     */
    public String getDownErrorStatisticUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(DOWNERROR_STATISTIC_URL))) {
            return getServerUrlsConf().get(DOWNERROR_STATISTIC_URL);
        }
        return mDownErrorStatisticUrl;
    }

    /**
     * 获取关于地址信息
     * 
     * @return 关于地址信息
     */
    public String getAboutUrl() {
        String packageName = mContext.getPackageName();
        if (TextUtils.equals(Configrations.APPSEARCH, packageName)) {
            if (!TextUtils.isEmpty(getServerUrlsConf().get(ABOUT_URL))) {
                return getServerUrlsConf().get(ABOUT_URL);
            }
            return mAboutUrl;
        } else if (TextUtils.equals(Configrations.ASSISTANT_91, packageName)) {
            if (!TextUtils.isEmpty(getServerUrlsConf().get(ABOUT_91_URL))) {
                return getServerUrlsConf().get(ABOUT_91_URL);
            }
            return mAbout91Url;
        } else if (TextUtils.equals(Configrations.HIMARKET, packageName)) {
            if (!TextUtils.isEmpty(getServerUrlsConf().get(ABOUT_HIMARKET_URL))) {
                return getServerUrlsConf().get(ABOUT_HIMARKET_URL);
            }
            return mAboutHiMarketUrl;
        }
        // 默认返回百度手机助手的关于页面
        return mAboutUrl;
    }

    /**
     * 获取帮助地址信息
     * 
     * @return 帮助地址信息
     */
    public String getHelpUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(HELP_URL))) {
            return getServerUrlsConf().get(HELP_URL);
        }
        return mHelpUrl;
    }

    /**
     * 获取反馈地址
     * 
     * @return 反馈地址
     */
    public String getFeedbackUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(FEEDBACK_URL))) {
            return getServerUrlsConf().get(FEEDBACK_URL);
        }
        return mFeedbackUrl;
    }
    
    /**
     * 获取userid地址
     * 
     * @return 获取userid的接口地址
     */
    public String getUserIdGrabberUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(USERIDGRAB_URL))) {
            return getServerUrlsConf().get(USERIDGRAB_URL);
        }
        return mUserIdGrabUrl;
    }

    /**
     * 获取主流浏览器默认下载目录
     * 
     * @return 主流浏览器的默认下载目录
     */
    public String getBroswerDownPathUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BROWSER_DOWNLOAD_URL))) {
            return getServerUrlsConf().get(BROWSER_DOWNLOAD_URL);
        }
        return mBroswerDownPathUrl;
    }

    /**
     * 通过docid获取下载信息
     * 
     * @return 下载信息的地址
     */
    public String getDowninfoWithDocidUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(DOCID_URL))) {
            return getServerUrlsConf().get(DOCID_URL);
        }
        return mDowninfoWithDocidUrl;
    }

    /**
     * 高速下载请求浏览器下载目录时发送一个统计请求的地址
     * 
     * @return 地址
     */
    public String getHighDownloadDirReqStaUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(HIGHDOWNLOAD_STATIS_DIR_URL))) {
            return getServerUrlsConf().get(HIGHDOWNLOAD_STATIS_DIR_URL);
        }
        return mHighDownloadDirReqStaUrl;
    }
    
    /**
     * 获取安卓优化大师下载地址
     * 
     * @return 下载地址
     */
    public String getYouhuadashiAppInfoUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(YOUHUADASHI_APP_INFO_URL))) {
            return getServerUrlsConf().get(YOUHUADASHI_APP_INFO_URL);
        }
        return mYouhuadashiAppInfoUrl;
    }

    /**
     * 91连接助手下载地址
     * 
     * @return 下载地址
     */
    public String getConnectpcDownloadUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(CONNECT_PC_URL))) {
            return getServerUrlsConf().get(CONNECT_PC_URL);
        }
        return mConnectPcDownloadUrl;
    }

    /**
     * 卸载反馈的地址
     * 
     * @return 获取新功能说明地址
     */
    public String getUninstallFeedBackUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(UNINSTALL_FEEDBACK_URL))) {
            return getServerUrlsConf().get(UNINSTALL_FEEDBACK_URL);
        }
        return mUninstallFeedBackUrl;
    }
    
    /**
     * 获取流量管理校准规则地址
     * 
     * @return 获取流量管理校准规则地址
     */
    public String getNetFlowCorrectRuleUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(NETFLOW_CORRECT_RULE_URL))) {
            return getServerUrlsConf().get(NETFLOW_CORRECT_RULE_URL);
        }
        return mNetFlowCorrectRuleUrl;
    }

    /**
     * 获取首次启动装机必备弹窗的接口地址
     * 
     * @return 首次启动装机必备弹窗的接口地址
     */
    public String getMustInstallListOnFirstLaunchUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MUST_INSTALL_LIST_URL))) {
            return getServerUrlsConf().get(MUST_INSTALL_LIST_URL);
        }
        return mMustInstallListUrl;
    }
    
    /**
     * 获取下载无任务推荐Url
     * 
     * @return 下载无任务推荐Url
     */
    public String getDownloadEmptyGuessUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(DOWNLOAD_EMPTY_GUESS))) {
            return getServerUrlsConf().get(DOWNLOAD_EMPTY_GUESS);
        }
        return mDownloadEmptyGuess;
    }

    /** 获取服务器下发6.6.7Beta(原计划6.6.3，因升级moplus临时改为6.6.7)版本后通用配置接口的地址 */
    private static final String COMMONCONF_URL = "commonconf_url";
    /**
     * 6.6.7Beta(原计划6.6.3，因升级moplus临时改为6.6.7)版本后的通用配置接口　对应原特殊配置的统一接口0。之前用请求类型（
     * reqtype）来区分，0：分享，1：启动画面，2：特殊运营广告，3：首页漂窗
     */
    private String mCommonConfUrl = mServer + "/appsrv?native_api=1&action=commonconf";

    /**
     * 获取服务器下发6.6.7Beta(原计划6.6.3，因升级moplus临时改为6.6.7)版本后通用配置接口的地址
     * 
     * @return 服务器下发6.6.7Beta(原计划6.6.3，因升级moplus临时改为6.6.7)版本后通用配置接口的地址
     */
    public String getCommonConfUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(COMMONCONF_URL))) {
            return getServerUrlsConf().get(COMMONCONF_URL);
        }
        return mCommonConfUrl;
    }
    
    /** 6.5版本 获取安讯配置分享的地址 */
    private static final String SHARE_CONF_URL = "share_conf_url";
    /** 支持分享入口：详情页、洗白白、幸运抽奖 */
    private String mShareConfUrl = mServer + "/appsrv?native_api=1&action=shareconf";

    /**
     * 获取服务器下发分享的Url
     * 
     * @return 服务器下发分享的Url
     */
    public String getShareConfUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(SHARE_CONF_URL))) {
            return getServerUrlsConf().get(SHARE_CONF_URL);
        }
        return mShareConfUrl;
    }
    
    /** 服务器下发首页开机启动运营画面的默认请求地址 */
    private String mLauncherInfoConfigFetchUrl = mServer + "/appsrv?native_api=1&action=specialconf&reqtype=4";
    /** 获取服务器下发首页开机启动运营画面的地址 */
    private static final String LAUNCHER_INFO_CONFIG_FETCH_URL = "launcher_info_config_fetch_url";
    
    
    /**
     * 获取服务器下发首页开机启动运营画面的Url
     * 存在活动页面，使用新的配置
     * 
     * @return 服务器下发首页启动运营画面的Url
     */
    public String getLauncherInfoConfigFetchUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(LAUNCHER_INFO_CONFIG_FETCH_URL))) {
            return getServerUrlsConf().get(LAUNCHER_INFO_CONFIG_FETCH_URL);
        }

        return mLauncherInfoConfigFetchUrl;
    }
    
    /** 服务器下发首页漂窗的默认请求地址 */
    private String mFloatingviewFetchUrl = mServer + "/appsrv?native_api=1&action=dialogconf";

    /** 获取服务器下发首页漂窗的地址,6.3.3版本开始因为飘窗配置url修改，更改名称，尾部增加new */
    private static final String FLOATINGVIEW_CONFIG_FETCH_URL_NEW = "floatingview_config_fetch_url_new";

    /**
     * 获取服务器下发首页漂窗的Url
     * 
     * @return 服务器下发首页漂窗的Url
     */
    public String getFloatingviewConfigFetchUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(FLOATINGVIEW_CONFIG_FETCH_URL_NEW))) {
            return getServerUrlsConf().get(FLOATINGVIEW_CONFIG_FETCH_URL_NEW);
        }
        
        return mFloatingviewFetchUrl;
    }
    
    /** 详情页话题列表请求地址 */
    private String mTalkSubjectListUrl = mServer + "/appsrv?native_api=1&action=tiebatopic";
    /** 获取服务器下发特殊运营广告的地址 */
    private static final String TALK_SUBJECT_LIST_URL = "talk_subject_list_url";

    /**
     * 获取详情页话题列表请求地址的Url
     * 
     * @return 详情页话题列表请求地址的Url
     */
    public String getTalkSubjectListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(TALK_SUBJECT_LIST_URL))) {
            return getServerUrlsConf().get(TALK_SUBJECT_LIST_URL);
        }
        return mTalkSubjectListUrl;
    }

    /** 详情页发现tab */
    @Default ("/appsrv?native_api=1&action=detaildis")
    public static final String FIND_SUBJECT_LIST = "find_subject_list";

    /** 礼包列表页接口key */
    private static final String GIFTBAG_LIST_URL = "giftbag_list_url";

    /** 礼包列表页接口地址 */
    private String mGiftBagListUrl = mServer + "/appsrv?native_api=1&action=homelist&type=privilege";

    /**
     * 获取礼包列表页地址
     * 
     * @return 礼包列表页的地址
     */
    public String getGiftBagListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GIFTBAG_LIST_URL))) {
            return getServerUrlsConf().get(GIFTBAG_LIST_URL);
        }
        return mGiftBagListUrl;
    }
    
    
    /** 金熊奖最新列表页接口key */
    private static final String GOLDENBEAR_NEWEST_LIST_URL = "goldenbear_newest_list_url";

    /** 金熊奖最新列表页接口地址 */
    private String mGoldenBearNewestListUrl = mServer + "/appsrv?native_api=1&action=goldenbear&type=newest";

    /**
     * 获取金熊奖最新列表页地址
     * 
     * @return 金熊奖最新列表页地址
     */
    public String getGoldenBearNewestListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GOLDENBEAR_NEWEST_LIST_URL))) {
            return getServerUrlsConf().get(GOLDENBEAR_NEWEST_LIST_URL);
        }
        return mGoldenBearNewestListUrl;
    }
    
    /** 金熊奖往期列表页接口key */
    private static final String GOLDENBEAR_PREVIOUS_LIST_URL = "goldenbear_previous_list_url";

    /** 金熊奖往期列表页接口地址 */
    private String mGoldenBearPreviousListUrl = mServer + "/appsrv?native_api=1&action=goldenbear&type=previous";

    /**
     * 获取金熊奖往期列表页地址
     * 
     * @return 金熊奖往期列表页地址
     */
    public String getGoldenBearPreviousListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GOLDENBEAR_PREVIOUS_LIST_URL))) {
            return getServerUrlsConf().get(GOLDENBEAR_PREVIOUS_LIST_URL);
        }
        return mGoldenBearPreviousListUrl;
    }

    /**
     * 获取线下运营活动接口
     * 
     * @return 线下运营活动的URL
     */
    public String getOfflineOperationUrl() {
        return mOfflineOperationUrl;
    }
    
    /**
     * 获取是否免流量专区接口地址
     * 
     * @return 是否免流量专区接口地址
     */
    public String getCheckFreeDownUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(CHECK_FREEDOWN_URL))) {
            return getServerUrlsConf().get(CHECK_FREEDOWN_URL);
        }
        return mFreeDownUrl;
    }
    
    /** 残留清理，内存清理，自启管理白名单 接口key */
    private static final String WHITELIST_CONFIG_URL = "whitelist_config_url";
    /** 残留清理，内存清理，自启管理白名单接口地址 */
    private String mWhitelistUrl = mServer + "/appsrv?native_api=1&action=whitelist";

    /**
     * 获取残留清理，内存清理，自启管理白名单接口地址
     * 
     * @return 残留清理，内存清理，自启管理白名单接口地址
     */
    public String getWhitelistCommandUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(WHITELIST_CONFIG_URL))) {
            return getServerUrlsConf().get(WHITELIST_CONFIG_URL);
        }
        return mWhitelistUrl;
    }

    /** 绑定app弹框统计接口key */
    private static final String BINDAPP_STATIC_URL = "bindapp_static_url";
    /** 绑定app列表接口地址 */
    private String mBindAppStaticUrl = mServer + "/appsrv?action=bindappstatic";
    
    /**
     * 获取绑定app统计相关的接口地址
     * 
     * @return 绑定app统计相关的接口地址
     */
    public String getBindAppStaticUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BINDAPP_STATIC_URL))) {
            return getServerUrlsConf().get(BINDAPP_STATIC_URL);
        }
        return mBindAppStaticUrl;
    }

    /** 静默下载列表配置的接口key */
    private static final String SILENT_DOWNLOAD_LIST_CONFIG_URL = "silent_download_list_config_url";
    /** 静默下载列表拉取的接口地址 */
    private String mSilentDownlistUrl = mServer + "/appsrv?action=silentdownloadlist";

    /**
     * 获取静默下载列表接口地址
     * 
     * @return 静默下载列表接口地址
     */
    public String getSilentDownllistCommandUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(SILENT_DOWNLOAD_LIST_CONFIG_URL))) {
            return getServerUrlsConf().get(SILENT_DOWNLOAD_LIST_CONFIG_URL);
        }
        return mSilentDownlistUrl;
    }

    /** 获取礼包内容接口key */
    private static final String GIFT_CODE_CONFIG_URL = "gift_code_config_url";
    /** 获取礼包内容的接口地址 */
    private String mGiftcodeRequestUrl = mServer + "/appsrv?native_api=1&action=giftcode";

    /**
     * 获取礼包内容接口地址
     * 
     * @return 礼包内容接口地址
     */
    public String getGiftContentCommandUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GIFT_CODE_CONFIG_URL))) {
            return getServerUrlsConf().get(GIFT_CODE_CONFIG_URL);
        }
        return mGiftcodeRequestUrl;
    }
    
    /** 获取礼包码接口key */
    private static final String GIFT_CODE_URL = "gift_code_url";
    /** 获取礼包码接口地址 */
    private String mNewGiftcodeUrl = mServer + "/appsrv?native_api=1&action=getgiftcode";

    /**
     * 获取礼包内容接口地址
     * 
     * @return 礼包内容接口地址
     */
    public String getNewGiftCodeUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GIFT_CODE_URL))) {
            return getServerUrlsConf().get(GIFT_CODE_URL);
        }
        return mNewGiftcodeUrl;
    }
    
    
    /** 获取礼包码接口地址 */
    @Default ("/appsrv?native_api=1&action=getaward&model=award")
    public static final String AWARD_LOTTERY_URL = "AWARD_LOTTERY_URL";
    
    /** 预定礼包内容接口key */
    private static final String GIFT_ORDER_CONFIG_URL = "gift_order_config_url";
    /** 预定礼包内容的接口地址 */
    private String mGiftOrderRequestUrl = mServer + "/appsrv?native_api=1&action=mygift";

    /**
     * 预定礼包内容接口地址
     * 
     * @return 预定礼包内容接口地址
     */
    public String getGiftOrderCommandUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GIFT_ORDER_CONFIG_URL))) {
            return getServerUrlsConf().get(GIFT_ORDER_CONFIG_URL);
        }
        return mGiftOrderRequestUrl;
    }
    
    /** 更多礼包接口key */
    private static final String MORE_GIFT_LIST_URL = "more_gift_list_url";
    /** 更多礼包接口地址 */
    private String mMoreGiftListRequestUrl = mServer + "/appsrv?native_api=1&action=moregiftlist";

    /**
     * 更多礼包接口地址
     * 
     * @return 更多礼包接口地址
     */
    public String getMoreGiftListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MORE_GIFT_LIST_URL))) {
            return getServerUrlsConf().get(MORE_GIFT_LIST_URL);
        }
        return mMoreGiftListRequestUrl;
    }

    /** 详情页评星 地址接口key */
    private static final String APP_DETAIL_RATING_URL = "addscore";
    /** 详情页评星 接口地址 */
    private String mAppDetailRatingUrl = mServer + "/appsrv?native_api=1&action=addscore";

    /**
     * 获取详情页评星的地址
     * @return 评星地址
     */
    public String getAppDetailRatingUrl() {

        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_DETAIL_RATING_URL))) {
            return getServerUrlsConf().get(APP_DETAIL_RATING_URL);
        }
        return mAppDetailRatingUrl;
    }

    /** 下载管理推荐 key */
    private static final String DOWNLOAD_RECOMMEND = "download_recommend";
    /** 下载管理推荐接口地址 */
    private String mDownloadRecommendUrl = mServer + "/appsrv?native_api=1&action=recinstall";

    /**
     * 获取 下载管理推荐接口地址
     * 
     * @return 下载管理推荐接口地址
     */
    public String getDownloadRecommendDataUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(DOWNLOAD_RECOMMEND))) {
            return getServerUrlsConf().get(DOWNLOAD_RECOMMEND);
        }
        return mDownloadRecommendUrl;
    }

    /** 消息中心-获取列表 key */
    private static final String MESSAGECENTER_GETLIST = "messagecenter_getlist";
    /** 消息中心-获取列表 接口 */
    private String mMessageCenterGetlistUrl = mServer + "/appsrv?native_api=1&action=mcgetlist";
    
    /**
     * 消息中心-获取列表接口地址
     * 
     * @return 消息中心-获取列表接口地址
     */
    public String getMessageCenterGetlistUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MESSAGECENTER_GETLIST))) {
            return getServerUrlsConf().get(MESSAGECENTER_GETLIST);
        }
        return mMessageCenterGetlistUrl;
    }
    
    /** 消息中心-更新状态  key*/
    private static final String MESSAGECENTER_UPDATE = "messagecenter_update";
    /** 消息中心-更新状态  接口*/
    private String mMessageCenterUpdateUrl = mServer + "/appsrv?native_api=1&action=mcupdate";
    
    /**
     * 消息中心更新状态接口地址
     * 
     * @return 消息中心-更新状态接口地址
     */
    public String getMessageCenterUpdateUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MESSAGECENTER_UPDATE))) {
            return getServerUrlsConf().get(MESSAGECENTER_UPDATE);
        }
        return mMessageCenterUpdateUrl;
    }
    
    /** 消息中心-获取未读数量  key*/
    private static final String MESSAGECENTER_GETNEWNUM = "messagecenter_getnewnum";
    /** 消息中心-获取未读数量 接口*/
    private String mMessageCenterGetNewNumUrl = mServer + "/appsrv?native_api=1&action=mcgetnewnum";
    
    /**
     * 消息中获取未读数量接口地址
     * 
     * @return 消息中心-获取未读数量接口地址
     */
    public String getMessageCenterGetNewNumUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MESSAGECENTER_GETNEWNUM))) {
            return getServerUrlsConf().get(MESSAGECENTER_GETNEWNUM);
        }
        return mMessageCenterGetNewNumUrl;
    }
    
    /** 应用流量统计数据接口Key */
    private static final String APP_TRAFFIC_STATISTICS_URL = "app_traffic_statistics_url";
    /** 应用流量统计数据接口url */
    private String mAppTrafficStatisticsUrl = mServer + "/appsrv?action=trafficstatistics&native_api=1";

    /**
     * 获取应用流量统计数据接口
     * 
     * @return 应用流量统计数据接口
     */
    public String getAppTrafficStatisticsUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_TRAFFIC_STATISTICS_URL))) {
            return getServerUrlsConf().get(APP_TRAFFIC_STATISTICS_URL);
        }
        return mAppTrafficStatisticsUrl;
    }
   
    /** 发号详情接口Key */
    private static final String GIFT_DETAIL_URL = "gift_detail_url";
    /** 发号详情接口 */
    private String mGiftDetailUrl = mServer + "/appsrv?native_api=1&action=gamegiftdetail";

    /**
     * 发号详情接口
     * 
     * @return 发号详情接口地址
     */
    public String getGiftDetailUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GIFT_DETAIL_URL))) {
            return getServerUrlsConf().get(GIFT_DETAIL_URL);
        }
        return mGiftDetailUrl;
    }
    
    /** 详情页应用反馈接口Key */
    private static final String APP_FEEDBACK_URL = "app_feedback_url";
    /** 详情页应用反馈接口
     * 老接口(弃用) : "http://feedback.ops.baidu.com/mbufeedback/api/showpage";
     * 测试环境　　 : "http://cp01-rdqa-dev153.cp01.baidu.com:8001/";
     * 正式环境 　　: "http://ufosdk.baidu.com/";
     */
    private String mAppFeedbackUrl = "http://ufosdk.baidu.com/";

    /**
     * 详情页应用反馈接口
     * 
     * @return 详情页应用反馈接口地址
     */
    public String getAppFeedbackUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_FEEDBACK_URL))) {
            return getServerUrlsConf().get(APP_FEEDBACK_URL);
        }
        return mAppFeedbackUrl;
    }

    /**
     * 在请求的url中增加省份信息
     *  
     * @param context 上下文
     * @return 省份信息
     */
    private String getProvince(Context context) {
        String province = CommonGloabalVar.getInstance(context).getCurrentProvince();
        if (!TextUtils.isEmpty(province)) {
            byte[] locArray = Base64Encoder.B64Encode(UriHelper.getEncodedValue(province).getBytes());
            if (locArray != null) {
                String encodedProvince = new String(locArray);
                
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("&province=").append(encodedProvince);
                return stringBuilder.toString();
            }
        }
        return "";
    }

    
    /** 耗电排行白名单 接口key */
    private static final String BATTERY_WHITELIST_URL = "powerusagewhitelist";
    /** 残留清理，内存清理，自启管理白名单接口地址 */
    private String mBatteryWhitelistUrl = mServer + "/appsrv?native_api=1&action=powerusagewhitelist";

    /**
     * 获取残留清理，内存清理，自启管理白名单接口地址
     * @return 残留清理，内存清理，自启管理白名单接口地址
     */
    public String getBatteryWhitelistUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BATTERY_WHITELIST_URL))) {
            return getServerUrlsConf().get(BATTERY_WHITELIST_URL);
        }
        return mBatteryWhitelistUrl;
    }
    
    /** pk头像列表接口Key */
    private static final String PK_AVATAR_LIST_URL = "pk_avatar_list_url";
    /** pk头像列表接口 */
    private String mPkAvatarListUrl = mServer + "/appsrv?native_api=1&action=avatarlist";

    /**
     * pk头像列表接口
     * 
     * @return pk头像列表地址
     */
    public String getPkAvatarListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(PK_AVATAR_LIST_URL))) {
            return getServerUrlsConf().get(PK_AVATAR_LIST_URL);
        }
        return mPkAvatarListUrl;
    }
    
    /** 发现列表接口Key */
    private static final String FIND_LIST_URL = "find_list_url";
    /** 发现列表接口 */
    private String mFindListUrl = mServer + "/appsrv?native_api=1&action=find";

    /**
     * 发现列表接口
     * 
     * @return 发现列表地址
     */
    public String getFindListUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(FIND_LIST_URL))) {
            return getServerUrlsConf().get(FIND_LIST_URL);
        }
        return mFindListUrl;
    }
        
    /** 插件列表查询 */
    private static final String PLUGIN_LIST_KEY = "pluginlist";
    /** 插件列表查询地址 */
    private String mPlugInList = mServer + "/appsrv?native_api=1&action=pluginlist";

    /**
     * 插件列表地址
     * 
     * @return url
     */
    public String getPlugInList() {
        String url;
        if (!TextUtils.isEmpty(getServerUrlsConf().get(PLUGIN_LIST_KEY))) {
            url = getServerUrlsConf().get(PLUGIN_LIST_KEY);
        } else {
            url = mPlugInList;
        }
        // 融合版特殊逻辑，如果是融合版，则增加请求字段
        if (Configrations.ASSISTANT_91.equals(mContext.getPackageName())) {
            url += "&apsver=2";
        }
        return url;
    }

    /** root信息推荐的key */
    private static final String ROOT_APPINFO_KEY = "rootappinfo";

    /** root推荐应用的URL */
    private String mRootAppinfoUrl = mServer + "/appsrv?native_api=1&action=rootappinfo";

    /**
     * 获取root推荐应用的url
     * 
     * @return url
     */
    public String getRootAppinfoUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(ROOT_APPINFO_KEY))) {
            return getServerUrlsConf().get(ROOT_APPINFO_KEY);
        }
        return mRootAppinfoUrl;
    }

    /** 免流量下载统计上传接口Key */
    private static final String USER_FREEDOWN_URL = "user_freedown_url";
    /** 免流量下载统计上传接口 */
    private String mUserFreeDownUrl = mServer + "/appsrv?native_api=1&action=userfreedown";

    /**
     * 免流量下载统计上传接口
     * 
     * @return 免流量下载统计上传接口
     */
    public String getUserFreeDownUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(USER_FREEDOWN_URL))) {
            return getServerUrlsConf().get(USER_FREEDOWN_URL);
        }
        return mUserFreeDownUrl;
    }
    
    /** 品专接口key */
    private static final String BRAND_AREA_URL = "brand_area_url";
    /** 品专接口 */
    private String mBrandAreaUrl = mServer + "/appsrv?native_api=1&action=brandarea";

    /**
     * 获取品专接口
     * 
     * @return 品专地址
     */
    public String getBrandAreaUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BRAND_AREA_URL))) {
            return getServerUrlsConf().get(BRAND_AREA_URL);
        }
        return mBrandAreaUrl;
    }
    
    /** 精品大图key */
    private static final String GREAT_GAME_URL = "great_game_url";
    /** 精品大图 */
    private String mGreatGameUrl = mServer + "/appsrv?native_api=1&action=greatgame";
    
    /**
     * 获取精品大图url
     * 
     * @return 精品大图url
     */
    public String getGreatGameUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(GREAT_GAME_URL))) {
            return getServerUrlsConf().get(GREAT_GAME_URL);
        }
        return mGreatGameUrl;
    }
    
    /** 单应用图集接口 */
    @Default ("/appsrv?native_api=1&action=allimages")
    public static final String APP_ALL_IMAGES = "app_all_images";

    /** 获取登陆领奖的抽奖Key */
    private static final String LOGIN_SPLASH_GIFT_URL = "login_splash_gift_url";
    /** 获取登陆领奖的抽奖Url */
    private String mLoginSplashGiftUrl =  "http://app.m.baidu.com/appweb/main/splash";
    
    /**
     *  获取登陆领奖的抽奖Url
     * 
     * @return 登陆领奖的抽奖Url
     */
    public String getSplashGiftUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(LOGIN_SPLASH_GIFT_URL))) {
            return getServerUrlsConf().get(LOGIN_SPLASH_GIFT_URL);
        }
        return mLoginSplashGiftUrl;
    }

    /** 线上自升级获取公钥的url地址 */
    @Default ("https://update.baidu.com/lcmanage/index.php?r=InterfaceAction&method=pub_key")
    public static final String CLIENT_UPDATE_ONLINE_PUBLIC_KEY_URL = "client_update_public_key_url";

    /** 线下自升级获取公钥的url地址 */
    @Default ("http://offline.update.baidu.com/lcmanage/index.php?r=InterfaceAction&method=pub_key")
    public static final String CLIENT_UPDATE_OFFLINE_PUBLIC_KEY_URL = "client_update_offline_public_key_url";


    /**
     * 获取自升级md5校验公钥的url地址
     * @return 公钥的url地址
     */
    public String getClientUpdatePublicKeyUrl() {
        ApplicationInfo applicationInfoInfo = mContext.getApplicationInfo();
        boolean isDebugable = (applicationInfoInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (isDebugable) {
            return getUrl(CLIENT_UPDATE_OFFLINE_PUBLIC_KEY_URL);
        } else {
            return getUrl(CLIENT_UPDATE_ONLINE_PUBLIC_KEY_URL);
        }
    }

    // ---------------------------------------------个人中心相关---------------------------------------BEGIN
    
    /** 抽奖规则的key */
    @Default ("/appsrv?native_api=1&action=lotteryrule")
    public static final String LOTTERY_RULE = "lotteryrule";
    
    /** 金币说明的key */
    @Default ("/appsrv?native_api=1&action=coinintro")
    public static final String COIN_INTRO_KEY = "coin_intro";

    /** 获取等级称口规则接口Key */
    @Default ("/appsrv?native_api=1&action=leveltitlerules")
    public static final String TITLE_RULE_URL = "TITLE_RULE_URL";
    
    /** 修改收货地址Key */
    @Default ("/appsrv?native_api=1&action=editaddress")
    public static final String CONSIGNEE_MODIFY_URL = "CONSIGNEE_MODIFY_URL";

    /** 提交订单的key */
    @Default ("/appsrv?native_api=1&action=consigneeinfo")
    public static final String CONSIGNEE_INFO_KEY = "consigneeinfo";
    
    /** 获取游戏任务的key */
    @Default ("/appsrv?native_api=1&action=gametask")
    public static final String GAME_TASK_LIST = "gametask";
    
    /** 获取单个游戏任务的key */
    @Default ("/appsrv?native_api=1&action=submitgametask")
    public static final String SUBMIT_GAME_TASK = "submitgametask";
    
    /** 个人中心兑换和抽奖分享连接url */
    @Default ("http://mobile.baidu.com/#/item?pid=825114773")
    public static final String PERSON_CENTER_SHARE_URL = "person_center_share_url";
    
    /** 任务列表 */
    @Default ("/appsrv?action=tasklist&native_api=1")
    public static final String MISSION_LIST_KEY = "mission_list";
    
    /** 向服务器提交任务完成请求 */
    @Default ("/appsrv?native_api=1&action=taskfinish")
    public static final String MISSION_COMPLETE_KEY = "mission_complete";
    
    /** 向服务器提交商品兑换请求 */
    @Default ("/appsrv?action=exchangegoods")
    public static final String BUY_COMMODITY_KEY = "buy_commodity";
    
    /** 向服务器提交幸运抽奖请求 */
    @Default ("/appsrv?action=lotterydraw")
    public static final String LOTTERY_KEY = "lottery";
    
    /** 向服务器提交幸运抽奖主题请求 */
    @Default ("/appsrv?action=lotterytheme")
    public static final String LOTTERY_THEME_KEY = "lotteryTheme";
    
    /** 商品列表 */
    @Default ("/appsrv?action=mall")
    public static final String COMMODITY_LIST_KEY = "commodity_list";
    
    /** 奖品列表 */
    @Default ("/appsrv?action=goodslist")
    public static final String AWARD_LIST_KEY = "award_list";

    /** 碎片列表 */
    @Default ("/appsrv?action=fragmentlist")
    public static final String FRAGMENT_LIST_KEY = "fragment_list_key";
    
    /** 获取个人基本信息Key */
    @Default ("/appsrv?native_api=1&action=usercenter")
    public static final String ACCOUNT_INFO_URL = "ACCOUNT_INFO_URL";
    
    /** 获取个人中心首页信息Key */
    @Default ("/appsrv?native_api=1&action=usercenter&type=mainpage")
    public static final String USER_CENTER_MAINPAGE_INFO_URL = "USER_CENTER_MAINPAGE_INFO_URL";
    
    /** 获取金币明细信息Key */
    @Default ("/appsrv?native_api=1&action=usercenter&type=detail")
    public static final String CASH_DETAIL_URL = "CASH_DETAIL_URL";
    

    /** 我的礼券包--全部地址*/
    @Default ("/appsrv?native_api=1&action=userawardlist&model=award&type=all")
    public static final String MY_GIFT_LOTTERY_ALL = "mygiftlottery_all";
    
    /** 我的礼券包--游戏礼包地址*/
    @Default ("/appsrv?native_api=1&action=userawardlist&model=award&type=gamegift")
    public static final String MY_GIFT_LOTTERY_GIFT = "mygiftlottery_gift";
    
    /** 我的礼券包--下载奖券地址*/
    @Default ("/appsrv?native_api=1&action=userawardlist&model=award&type=downloadlottery")
    public static final String MY_GIFT_LOTTERY_LOTTERY = "mygiftlottery_lottery";
    
    /** 推荐-有奖tab*/
    @Default ("/appsrv?native_api=1&action=privilege")
    public static final String RECOMMEND_PRIVILEGE = "recommend_privilege";
    
    /** 游戏-礼包Tab*/
    @Default ("/appsrv?native_api=1&action=giftlist&sorttype=game")
    public static final String GIFTLIST_LOTTERY = "gamelist_game";
    // ---------------------------------------------个人中心相关---------------------------------------END
    

    /** 软件-排行tab*/
    @Default ("/appsrv?native_api=1&action=softpage")
    public static final String SOFTWARE_TAB = "software_tab";
    
    /** 悬浮窗-最热APP*/
    @Default ("/appsrv?native_api=1&action=floatapp")
    public static final String FLOAT_HOTAPP = "float_hotapp";
    
    /** 垃圾清理-推荐APP*/
    @Default ("/appsrv?native_api=1&action=cleanrecommendapp")
    public static final String CLEAN_RECOMMONDAPP = "cleanrecommendapp";
    
    /** 更新管理推荐 key */
    private static final String UPDATE_RECOMMEND = "updaterecommend";
    /** 更新管理推荐接口地址 */
    private String mUpdateRecommendUrl = mServer + "/appsrv?native_api=1&action=updaterecommend";

    /**
     * 获取 更新管理推荐接口地址
     * 
     * @return 更新管理推荐接口地址
     */
    public String getUpdateRecommendUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(UPDATE_RECOMMEND))) {
            return getServerUrlsConf().get(UPDATE_RECOMMEND);
        }
        return mUpdateRecommendUrl;
    }
    

    /** 管理首页场景化接口key */
    private static final String MANAGEMENT_SCENARIZED = "managementscenarized";
    /** 管理首页场景化接口地址 */
    private String mManagementScenarizedUrl = mServer + "/appsrv?native_api=1&action=managementscenarizedcard";

    /** 下载管理必备推荐 key */
    private static final String MUST_RECOMMEND = "mustrecommend";
    /** 下载管理必备接口地址 */
    private String mMustRecommendUrl = mServer + "/appsrv?native_api=1&action=recmust";

    /**
     * 获取 更新管理推荐接口地址
     * 
     * @return 更新管理推荐接口地址
     */
    public String getManagementScenarizedUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MANAGEMENT_SCENARIZED))) {
            return getServerUrlsConf().get(MANAGEMENT_SCENARIZED);
        }
        return mManagementScenarizedUrl;
    }
    
    /**
     * 获取下载管理必备推荐接口地址
     * 
     * @return 下载管理必备推荐接口地址
     */
    public String getMustRecommendUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(MUST_RECOMMEND))) {
            return getServerUrlsConf().get(MUST_RECOMMEND);
        }
        return mMustRecommendUrl;
    }
    
    /** 弱BDUSS取用户信息接口 */
    private static final String BDUSS_INFO = "bdussinfo";
    /** 弱BDUSS取用户信息接口地址 */
    private String mBdussInfoUrl = mServer + "/appsrv?native_api=1&action=bdussinfo";
    
    /**
     * 获取 弱BDUSS用户信息接口地址
     * 
     * @return 弱BDUSS用户信息接口地址
     */
    public String getBdussInfoUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(BDUSS_INFO))) {
            return getServerUrlsConf().get(BDUSS_INFO);
        }
        return mBdussInfoUrl;
    }
    
    
    /** 首页热词 key */
    private static final String HOT_WORD_ON_SEARCHED = "hotwordonsearched";
    /** 首页热词接口地址 */
    private String mHotWordOnSearched = mServer + "/appsrv?native_api=1&action=homepagehotword";

    /**
     * 获取首页热词接口地址
     * @return 首页热词接口地址
     */
    public String getHotWordOnSearchedUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(HOT_WORD_ON_SEARCHED))) {
            return getServerUrlsConf().get(HOT_WORD_ON_SEARCHED);
        }
        return mHotWordOnSearched;
    }
    
    /** 91用户数据迁移的KEY*/
    private static final String USER_DATA_MIGRATION_KEY = "user_data_migration";
    /** 91用户数据迁移的接口 */
    private final String mUserDataMigrationUrl = mServer + "/appsrv?action=import91databy91id";
    
    /**
     * 获取91用户数据迁移的接口
     * @return 91用户数据迁移的接口
     */
    public String getUserDataMigrationUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(USER_DATA_MIGRATION_KEY))) {
            return getServerUrlsConf().get(USER_DATA_MIGRATION_KEY);
        }
        
        return mUserDataMigrationUrl;
    }
    
    /** 火星计划推荐卡片 key */
    private static final String APP_RECOMMEND = "apprecommend";
    /** 火星计划推荐卡片接口地址 */
    private String mRecommendAppsUrl = mServer + "/appsrv?native_api=1&action=apprecommend";

    /**
     * 获取 火星计划推荐卡片接口地址
     * 
     * @return 火星计划推荐卡片接口地址
     */
    public String getRecommendAppsUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(APP_RECOMMEND))) {
            return getServerUrlsConf().get(APP_RECOMMEND);
        }
        return mRecommendAppsUrl;
    }
    
    
    /** 搜索结果页装了又装*/
    @Default ("/appsrv?native_api=1&action=installedCard")
    public static final String SEARCH_RECOMMEND_APPS = "SEARCH_RECOMEND_APPS";
    
    /** 搜索结果页装了又装点击更多*/
    @Default ("/appsrv?native_api=1&action=installedlist")
    public static final String SEARCH_RECOMMEND_APPS_MORE = "SEARCH_RECOMEND_APPS_MORE";
    
    /** 优惠信息请求的key */
    private static final String PREFERENTIAL_INFO = "getact";
    /** 优惠信息请求的url */
    private String mPreferentialInfoUrl = mServer + "/appsrv?native_api=1&action=getact&model=award";
    
    /**
     * 获取优惠信息请求接口地址
     * @return 优惠信息请求接口地址
     */
    public String getPreferentialInfoUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(PREFERENTIAL_INFO))) {
            return getServerUrlsConf().get(PREFERENTIAL_INFO);
        }
        return mPreferentialInfoUrl;
    }

    /** 金熊掌跳转页面url */
    public static final String GOLDEN_BEAR_URL = "http://app.baidu.com/index/golden";

    /** 静默唤醒其他app */
    @Default ("/appsrv?native_api=1&action=appwakeup")
    public static final String SILENT_WEAKUP_UP = "silent_weakup";
    
    /** 游戏图库 */
    @Default ("/appsrv?native_api=1&action=gamegallery")
    public static final String GAME_HOT_GALLERY = "gamegallery";

    /** 酷应用详情页key */
    private static final String COOL_APP_URL = "cool_app_url";
    /** 酷应用详情页地址 */
    private String mCoolAppUrl = mServer + "/appsrv?native_api=1&action=coolappdetail";

    /**
     * 获取酷应用详情页
     *
     * @return 酷应用详情页url
     */
    public String getCoolAppDetailUrl() {
        if (!TextUtils.isEmpty(getServerUrlsConf().get(COOL_APP_URL))) {
            return getServerUrlsConf().get(COOL_APP_URL);
        }
        return mCoolAppUrl;
    }

    /** 监控列表数据的请求地址 */
    @Default ("/appsrv?native_api=1&action=threadmonitor")
    public static final String MONITOR_LIST_URL = "monitor_list_url";

    /** 监控列表数据的上报服务器地址 */
    @Default ("http://app.navi.baidu.com/competingInfo/sendCompet")
    public static final String MONITOR_UPLOAD_URL = "thread_monitor_upload_server";

    /**
     * 获取Url
     *
     * @param key key
     * @return 请求地址
     */
    public static String obtainUrl(String key) {
        AppSearchUrl appSearchUrl = AppSearchUrl.getInstance(SharePluginApplication.getAppContext());
        return appSearchUrl.getUrl(key);
    }

    /** 管理页的所有入口的列表 */
    @Default("/appsrv?action=adminpage")
    public static final String MANAGEMENT_ENTRY_LIST = "management_entry_list";

    /** 获取线下渠道功能开关的地址 */
    @Default("/appsrv?action=offlineChannelSet")
    public static final String OFFLINE_CHANNEL_SETTINGS = "offlineChannelSet";

    /** 特型tab信息请求地址 */
    @Default("/appsrv?action=specialtabs")
    public static final String MAIN_PAGE_SPECIAL_TABS = "main_page_special_tabs";

}
