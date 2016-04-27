/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.config;

import com.baidu.appsearch.config.db.Data;

/**
 * 服务器下发开关类
 * 
 * @author wangguanghui01
 * 
 * @since 2013-03-11
 */
public class SwitchInfo extends Data {

    /** 用户行为统计开关,数据节点名称 */
    public static final String USREVT = "usrevt";

    /**
     * 构造方法
     */
    public SwitchInfo() {
        super();
        setType(Data.SETTING_TYPE);
    }

}
