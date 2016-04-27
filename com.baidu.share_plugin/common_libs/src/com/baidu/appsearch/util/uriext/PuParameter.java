/**
 * Copyright (c) 2012 Baidu Inc.
 * 
 * @author Qingbiao Liu <liuqingbiao@baidu.com>
 * 
 * @date 2013-1-13
 */
package com.baidu.appsearch.util.uriext;

import java.util.HashMap;
import java.util.Iterator;

import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

/**
 * 统一处理pu参数。 pu参数中包含： cuid,cua,cut
 * ,osname,ctv,cfrom,pkname,csrc等参数，各个参数的value已经经过encode编码。
 * ，这里不再处理cuid@406F8CD13627D28AC0DA2CC8D4C78E77%7C3066F8C200000A,cua@aps_720_1280_android_2
 * .3
 * .5_a1,cut@XT928_4.0.4_15_motorola,osname@baiduappsearch,cfrom@1000561u,ctv@1
 * 
 * @return 返回的是Pu参数集合
 */
public class PuParameter {
    /** 参数key,value的分割符 */
    public static final String SEPARATOR = "@";
    /** debug tag. */
    private static final String TAG = PuParameter.class.getSimpleName();
    /** log 开关. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    // private String mCuid = "";
    // private String mCua = "";
    // private String mCut = "";
    // private String mOSName = "baiduappsearch";
    // private String mCfrom = "";
    // private String mCtv = "1";
    // private String mCsrc = "";
    /** 所有的pu参数 */
    private HashMap<String, String> mParams = new HashMap<String, String>();

    /**
     * 构造函数
     */
    public PuParameter() {
        mParams.put("cuid", "");
        mParams.put("cua", "");
        mParams.put("cut", "");
        mParams.put("osname", "baiduappsearch");
        mParams.put("cfrom", "");
        mParams.put("ctv", "1");
        // csrc不一定存在，所以不再添加
        // mParams.put("csrc", "");
    }

    /**
     * 设置cuid.
     * 
     * @param cuid
     *            cuid
     */
    public void setCuid(String cuid) {
        mParams.put("cuid", cuid);
    }

    /**
     * 设置cua.
     * 
     * @param cua
     *            cua
     */
    public void setCua(String cua) {
        mParams.put("cua", cua);
    }

    /**
     * 设置cut.
     * 
     * @param cut
     *            cut
     */
    public void setCut(String cut) {
        mParams.put("cut", cut);
    }

    /**
     * 设置osname.
     * 
     * @param osname
     *            默认是 baiduappsearch
     */
    public void setOsName(String osname) {
        mParams.put("osname", osname);
    }

    /**
     * 设置cfrom.
     * 
     * @param cfrom
     *            cfrom
     */
    public void setCfrom(String cfrom) {
        mParams.put("cfrom", cfrom);
    }

    /**
     * 设置ctv.
     * 
     * @param ctv
     *            ctv
     */
    public void setCtv(String ctv) {
        mParams.put("ctv", ctv);
    }

    /**
     * 设置csrc.搜索源
     * 
     * @param csrc
     *            csrc
     */
    public void setCsrc(String csrc) {
        mParams.put("csrc", csrc);
    }

    /**
     * 添加新的puvalue,如果之前存在，则替换，如果之前没有则添加新的。 对新的value会先进行一次decode.
     * 
     * @param values
     *            新的pu value，
     */
    public void parseValues(String values) {
        if (DEBUG) {
            Log.d(TAG, "before parse pu values:" + values);
        }
        values = UriHelper.getDecodedValue(values);
        // cuid@value,cua@value
        String[] params = values.split(",");
        for (int i = 0; i < params.length; i++) {
            String[] param = params[i].split(SEPARATOR);
            if (param.length == 2) {
                mParams.put(param[0], param[1]);
            } else if (param.length == 1) {
                mParams.put(param[0], "");
            }
        }
    }

    /**
     * 返回PU值，包含规定参数，除了csrc不一定返回，其他的都返回，如果csrc不为空，则也返回。 返回的值，未做encode
     * 
     * @return 未 encoded pu value
     */
    public String getPuValue() {
        StringBuffer puValue = new StringBuffer();
        Iterator<String> keys = mParams.keySet().iterator();
        // TODO value 是否有统一 encode
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.equals("csrc")) {
                if (!TextUtils.isEmpty(mParams.get(key))) {
                    // 如果搜索来源不为空，则添加,添加时encode
                    puValue.append(key).append(SEPARATOR)
                            .append(UriHelper.getEncodedValue(mParams.get(key))).append(",");
                }
            } else {
                puValue.append(key).append(SEPARATOR)
                        .append(UriHelper.getEncodedValue(mParams.get(key))).append(",");
            }
        }
        // 去掉最后的 逗号
        puValue.deleteCharAt(puValue.length() - 1);
        return puValue.toString();
    }
}
