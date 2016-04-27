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
 * 获取的服务器的配置信息，类型为{@link #SETTING_TYPE}
 */
public class SettingData extends Data {
    /**
     * 构造方法
     */
    public SettingData() {
        super();
        setType(Data.SETTING_TYPE);
    }

}
