/**
 * 
 */
package com.baidu.appsearch.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认地址的Annotation
 * @author zhushiyu01
 * @since 2015年1月13日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Default {
    /** 地址内容 */
    String value();
}