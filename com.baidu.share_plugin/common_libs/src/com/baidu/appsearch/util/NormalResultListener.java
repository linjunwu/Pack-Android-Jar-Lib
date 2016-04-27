/**
 * 
 */
package com.baidu.appsearch.util;

/**
 * 通用的查看结果Listener
 * @author zhushiyu01
 * @since 2014年11月25日
 */

public abstract class NormalResultListener {

    /**
     * 成功了
     * @param result 结果数据
     */
    public void onSuccess(Object result) {
        // TODO Auto-generated method stub
        
    }

    /**
     * 失败了
     * @param msg 错误信息
     * @param errCode 错误码
     * @param extra 附加信息
     */
    public void onFailed(String msg, int errCode, Object extra) {
        // TODO Auto-generated method stub
        
    }
}
