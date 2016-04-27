/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2013-1-13
 */
package com.baidu.appsearch.util.uriext;

import android.text.TextUtils;


/**
 * 处理URi中的Fragment部分,目前没有相关需求。有可能会在fragment中添加参数。后续扩展使用
 */
public class UriFragment {
    /** uri中 fragment部分 */
    private StringBuffer mUriFragment = new StringBuffer();

    /**
     * 构造函数
     * 
     * @param fragment
     *            uri中的fragment
     */
    public UriFragment(String fragment) {
        if (!TextUtils.isEmpty(fragment)) {
            this.mUriFragment.append(fragment);
        }
    }

    /**
     * 返回Fragment内容
     * 
     * @return 返回Fragment内容
     */
    public String getFragment() {
        return mUriFragment.toString();
    }
}
