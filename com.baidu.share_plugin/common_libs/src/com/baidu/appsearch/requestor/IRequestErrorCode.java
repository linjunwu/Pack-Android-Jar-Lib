package com.baidu.appsearch.requestor;

/**
 * 内部的错误，不是服务器那边返回的错误标识
 * @author zhushiyu01
 *
 */
public interface IRequestErrorCode {    // SUPPRESS CHECKSTYLE

    /** 未知错误 */
    int ERROR_CODE_UNKNOW = -1;
    
    /** 没有请求的Url地址 */
    int ERROR_CODE_NO_URL = ERROR_CODE_UNKNOW - 1;
    
    /** 网络访问错误 */
    int ERROR_CODE_NET_FAILED = ERROR_CODE_NO_URL - 1;
    
    /** 获取到的String不是Json格式 */
    int ERROR_CODE_RESULT_IS_NOT_JSON_STYLE = ERROR_CODE_NET_FAILED - 1;
    
    /** 数据解析错误 */
    int ERROR_CODE_PARSE_DATA_ERROR = ERROR_CODE_RESULT_IS_NOT_JSON_STYLE - 1;
    
}
