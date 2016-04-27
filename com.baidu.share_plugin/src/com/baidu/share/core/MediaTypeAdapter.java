package com.baidu.share.core;

import com.baidu.cloudsdk.social.core.MediaType;

/**
 * MediaTypeAdapter
 *
 * @author linjunwu
 * @since 2016/2/1
 */
interface MediaTypeAdapter {
    public MediaType getMediaType(int pos);
}
