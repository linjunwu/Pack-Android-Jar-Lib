/**
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.baidu.share.wxapi;
//CHECKSTYLE:OFF 

import com.baidu.cloudsdk.social.share.handler.WeixinShareActivity;


/**
 * 处理微信回调
 * @author chenhetong(chenhetong@baidu.com)
 *
 */
public class WXEntryActivity extends WeixinShareActivity {

    @Override
    protected boolean handleIntent() {
        if (super.handleIntent()) {
            return true;
        } else {
            return false;
        }
    }
    
}


//CHECKSTYLE:ON
