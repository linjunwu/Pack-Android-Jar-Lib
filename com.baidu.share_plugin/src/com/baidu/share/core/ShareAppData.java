/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */
package com.baidu.share.core;


import com.baidu.share.config.ShareConfigData;

/**
 * 详情页分享数据结构
 * @author fanjihuan
 * @since 2015年7月2日
 */
public class ShareAppData extends ShareConfigData {
    
    /**
     * 构造函数
     */
    public ShareAppData() {
        super();
        setEntry(ShareConfigData.SHARE_APP);
    }
}