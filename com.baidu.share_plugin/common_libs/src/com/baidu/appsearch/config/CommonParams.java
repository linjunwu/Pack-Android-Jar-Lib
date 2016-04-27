/**
 * 
 */
package com.baidu.appsearch.config;

/**
 * 常参数名称 和值
 * @author zhushiyu01
 * @since 2013-05-28
 */
public final class CommonParams {
    
    /**
     * 私有构造函数
     */
    private CommonParams() {
        
    }

    /**二维码参数中connent的参数名称 */
    public static final String PARAM_CONTENT = "_w-m_";
    
    /** 请求参数Cid */
    public static final String CID = "cid";
    /** Jsonp回调函数 */
    public static final String JSON_CALLBACK = "jsoncallback";
    /** call_id */
    public static final String CALL_ID = "call_id";
    /** app_id */
    public static final String APP_ID = "app_id";
    /** app_name */
    public static final String APP_NAME = "app_name";
    /** func参数名称 */
    public static final String PARAM_NAME_FUNC = "func";
    /** 参数名称 参数 */
    public static final String PARAM_NAME_ARGS = "args";
    /** 参数名称 前一次成功获取push信息的 index */
    public static final String PARAM_LAST_INDEX = "lastIndex";
    
    
    /** 状态 */
    public static final String JSON_KEY_STATUS = "status";
    /** 是否是最后一包 */
    public static final String JSON_KEY_LAST = "last";
    /** 类型 */
    public static final String JSON_KEY_TYPE = "type";
    /** 类型 */
    public static final String JSON_KEY_ARGS = "args";
    /** task id */
    public static final String JSON_KEY_TASK_ID = "taskid";
    /** 类型_result */
    public static final String JSON_VALUE_TYPE_RESULT = "result";
    /** 类型_event */
    public static final String JSON_VALUE_TYPE_EVENT = "event";
    /** 类型 message */
    public static final String JSON_VALUE_TYPE_MESSAGE = "message";
    /** 类型error_message */
    public static final String JSON_VALUE_TYPE_ERROR_MESSAGE = "error_message";
    /** 类型_event */
    public static final String JSON_VALUE_EVENT_NAME = "event_name";
    /** 类型_数据 */
    public static final String JSON_VALUE_DATA = "data";
    /** 类型_file info */
    public static final String JSON_VALUE_FILE_INFO = "file_info";
    
    /** 类型——超时 */
    public static final String JSON_VALUE_TYPE_TIMEOUT = "timeout";
    
    /** event 名称 之 DPROCESS */
    public static final String EVENT_NAME_DPROCESS = "DPROCESS";
    
    /** event 名称 之 APPINSTALL，安装 */
    public static final String EVENT_NAME_APPINSTALL = "APPINSTALL";
    
    /** event 名称 之客户端闭会连接 */
    public static final String EVENT_NAME_CLIENT_CLOSE = "CLIENT_CLOSE";
    
    /** event 名称 之 APPUNINSTALL，卸载 */
    public static final String EVENT_NAME_APPUNINSTALL = "APPUNINSTALL";
    
    /** event 名称 之 APPUNINSTALL，卸载 */
    public static final String EVENT_NAME_APPMOVE = "APPMOVE";
    /** event 名称 之 PUSHFILE */
    public static final String EVENT_NAME_PUSH_FILE = "PUSHFILE";
    
    /** event 名称 之  发送docid给前端 */
    public static final String EVENT_NAME_SEND_APP_DOCID = "RECEIVER_APP_DOCID";
    
    /** 状态 成功 */
    public static final String STATUS_SUCCESS = "success";
    
    /** 状态 正在进行  */
    public static final String STATUS_WORK = "working";
    
    /** 状态失败 */
    public static final String STATUS_FAILED = "fail";
    
    /** 公共JSON关键字：包名 */
    public static final String KEY_PACKAGE_NAME = "package";
    
    /** 公共JSON关键字: 参数 */
    public static final String KEY_ARGUMENTS = "args";

    /** 公共JSON关键字：APK的安装位置 */
    public static final String JSON_KEY_INS_POSI = "ins_posi";
    
    /** task 下载 event 状态*/
    public static final String KEY_EVENT_SETUP = "setup";
    /** 开始状态 */
    public static final String KEY_EVENT_SETUP_VALUE_START = "start";
    /** 等待状态 */
    public static final String KEY_EVENT_SETUP_VALUE_WAIT = "wait";
    /** 运行状态 */
    public static final String KEY_EVENT_SETUP_VALUE_RUNNING = "running";
    /** 运行结束 */
    public static final String KEY_EVENT_SETUP_VALUE_END = "end";
    
    /** task 过程状态,正在root */
    public static final String KEY_EVENT_SETUP_VALUE_ROOT = "root";

    /** task 过程状态,系统操作 */
    public static final String KEY_EVENT_SETUP_VALUE_SYSTEM = "system";

    /** task 执行结构，等待 */
    public static final String TASK_STATUS_WAIT = "wait";

    /** task 执行结果 */
    public static final String TASK_STATUS_ERROR = "ERROR";

    /** task 执行结果,未分类错误 */
    public static final String TASK_STATUS_ERROR_DEFAULT = "ERROR_DEFAULT";
    
    /** task 下载 event 速度*/
    public static final String KEY_EVENT_SPEED = "speed";
    
    /** task 下载 event 进度*/
    public static final String KEY_EVENT_PROCESS = "process";
    /** task 下载Path */
    public static final String KEY_EVENT_PATH = "path";
    
    /** 来源App */
    public static final String PARAM_NAME_FROM_APP = "from_app";
    /** Message发送到的App */
    public static final String PARAM_NAME_APP_NAME = "app_name";
    /** message 内容 */
    public static final String PARAM_NAME_MESSAGE_BODY = "msg_body";
    
    /** websuite SharedPreferences 的文件 */
    public static final String WEBSUITE_SHARE_FILE = "websuite_preference";
    
    /** 类型error_message */
    public static final String JSON_VALUE_ERROR_CODE = "error_code";
    
    /** app index */
    public static final String JSON_KEY_INDEX = "index";
}
