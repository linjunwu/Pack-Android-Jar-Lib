package com.baidu.appsearch.util;

import java.util.List;

/**
 *  一个array工具类
 * @author 王佳
 *
 */
public final class ArrayUtils {

    /**
     * 私有构造器
     */
    private ArrayUtils() {
    }

    /**
     * 测试一个array是否为空;
     * @param array 待测试的array
     * @return 如果array为null或长度为0，则为真
     */
    public static boolean isEmpty(List<?> array) {
        return (null == array) || array.size() <= 0;
    }

}
