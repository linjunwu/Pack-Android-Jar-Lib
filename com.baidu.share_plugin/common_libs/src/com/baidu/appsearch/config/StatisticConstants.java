package com.baidu.appsearch.config;

/**
 * CommonLib工程中，用到的用户行为统计
 */
public final class StatisticConstants {

    /**
     * constructor
     */
    private StatisticConstants() {
        // TODO Auto-generated constructor stub
    }
    
    /** Comon模块以后的前缀都是02 */
    public static final String PREFIX_COMMON_LIB = "02";
    
    /** {@value} requestor模块 */
    public static final String UE_REQUESTOR = "01";
    /** {@value} request 请求失败 */
    public static final String UEID_REQUEST_FAIL = "020101";

    // //////////////////////////////////////用户静态信息id分配
    // /////////////////////////////////////////////////////////

    // ******************************************* 类别 04
    // ************************************************
    /** {@value} 用户设置信息. */
    public static final String US_SETTINGS_STATIC_INFO = "010001";

    // //////////////////////////////////////浏览下载历史记录id分配
    // /////////////////////////////////////////////////////////

    // ******************************************* 类别 07
    // ************************************************
    /** {@value} 浏览下载历史记录. */
    public static final String US_DOWNLOAD_STATIC_INFO = "010001";

    /** {@value} 助手自身当天使用流量的统计*/
    public static final String UEID_019617 = "019617";
    /** 框当天使用流量的统计 */
    public static final String UEID_019618 = "019618";

    /** {@value} 增量更新覆盖率统计 */
    public static final String UEID_0112601 = "0112601";
    /** {@value} 无增量更新包的明细信息 */
    public static final String UEID_0112602 = "0112602";
    /** {@value} 上传本地APK的统计 */
    public static final String UEID_0112603 = "0112603";

    /** {@value} 主版 下载弹窗展示    */
    public static final String UEID_0116701 = "0116701";
    /** {@value} 主版 下载弹窗点击下载 */
    public static final String UEID_0116702 = "0116702";
    /** {@value} 主版 下载弹窗点击取消    */
    public static final String UEID_0116703 = "0116703";
    /** {@value} 主版 通过框下载第三方APK文件 */
    public static final String UEID_0116704 = "0116704";
    /** {@value} 主版 第三方调起type=14的下载，应用已经下载完成，直接调起安装页面    */
    public static final String UEID_0116706 = "0116706";

    /** {@value} 点击下载弹出下载方式选择弹窗 */
    public static final String UEID_012759 = "012759";
    /** {@value} 点击下载弹出下载方式选择弹窗，选择“继续下载” */
    public static final String UEID_012760 = "012760";
    /** {@value} 点击下载弹出下载方式选择弹窗，选择“预约下载” */
    public static final String UEID_012761 = "012761";
    /** {@value} 点击下载弹出下载方式选择弹窗，勾选了记住我的选择 */
    public static final String UEID_012762 = "012762";
    /** {@value} 点击下载弹出下载方式选择弹窗，不做任何选择 */
    public static final String UEID_012763 = "012763";
    /** {@value} 点击下载,无网弹出toast提示 */
    public static final String UEID_012765 = "012765";
    /** {@value} 新版详情页-分享到value:0:微信好友 1:微信朋友圈2:QQ好友3:QQ空间4:新浪微博5:短信6:复制链接7:其他 */
    public static final String UEID_0111556 = "0111556";
    /** {@value} TitleBar上分享按钮的点击后，各平台的点击 */
    public static final String UEID_017707 = "017707";
}
