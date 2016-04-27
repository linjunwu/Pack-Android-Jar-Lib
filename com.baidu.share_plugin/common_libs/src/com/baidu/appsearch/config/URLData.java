/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>,zhangjunguo <zhangjunguo@baidu.com>
 * 
 * @date 2012-7-3
 */
package com.baidu.appsearch.config;

import com.baidu.appsearch.config.db.Data;

/**
 * 获取的客户端请求服务器的url，类型为{@link #URL_TYPE}
 */
public class URLData extends Data {
    /**
     * 构造方法
     */
    public URLData() {
        super();
        setType(Data.URL_TYPE);
    }

}
