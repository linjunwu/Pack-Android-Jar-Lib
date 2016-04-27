/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */
package com.baidu.share.core;


import com.baidu.share.config.ShareConfigData;

/**
 * 幸运抽奖分享数据结构
 * @author fanjihuan
 * @since 2015年7月2日
 */
public class ShareLotteryData extends ShareConfigData {
    
    /**
     * 构造函数
     */
    public ShareLotteryData() {
        super();
        setEntry(ShareConfigData.SHARE_LOTTERY);
    }
}
