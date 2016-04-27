package com.baidu.appsearch.util;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * TTF字体读取管理
 * @author liyiyang
 */
public final class Typefaces {
    /** DEBUG 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG */
    private static final String TAG = "Typefaces";
    /** 字体cache的map  */
    private static final Hashtable<String, Typeface> CACHE = new Hashtable<String, Typeface>();
    /** HelveticaNeueLTPro TTF */
    public static final String FONT_HELVETICA_NEUEL_LTPRO = "fonts/HelveticaNeueLTPro.ttf";
    /** desktop_speedup_font TTF */
    public static final String FONT_DESKTOP_SPEEDUP_FONT = "fonts/desktop_speedup_font.ttf";
    
    /**
     * 构造方法
     */
    private Typefaces() {
    }
    
    /**
     * 返回所需的TTF字体
     * @param context context
     * @param name name
     * @return TTF字体
     */
    public static Typeface get(Context context, String name) {
        synchronized (CACHE) {
            if (!CACHE.containsKey(name)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(), name);
                    CACHE.put(name, t);
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "Could not get typeface '" + name + "' because " + e.getMessage());
                    }
                    return Typeface.DEFAULT;
                }
            }
            return CACHE.get(name);
        }
    }

}
