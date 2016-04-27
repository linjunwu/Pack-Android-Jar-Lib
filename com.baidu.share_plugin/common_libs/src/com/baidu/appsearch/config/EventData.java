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
 * 获取的客户端发往服务器的事件信息，类型为{@link #EVENT_TYPE}
 */
public class EventData extends Data {
    /**
     * 构造方法
     */
    public EventData() {
        super();
        setType(Data.EVENT_TYPE);
    }

}
