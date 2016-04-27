package com.baidu.appsearch.config;

/**
 * 这里保存的是手机的信息，像电量，开屏状态，内存等
 * 现在这些信息还不全，后面遇到逐渐往里面补
 * 它是一个静态类，使用时直接用静态方法
 * Created by zhushiyu01 on 15-12-10.
 */
public final class DeviceInfo {

    /**
     * 构造函数
     */
    private DeviceInfo() {
        super();
    }

    /**
     * 初始化方法，目前这个方法没有使用，以后如果需要到context时，可以调用此方法传入context
     * @param context context
     */
//    public static void init(Context context) {
//
//    }

    /** 当前电池电量 */
    private static int mCurrBatteryLevel = 0;
    /** 当前电池状态 */
    private static int mCurBatteryState = 0;


    /**
     * 返回当前电量
     * @return 电量
     */
    public static int getCurrBatteryLevel() {
        return mCurrBatteryLevel;
    }

    /**
     * 当前电池电量
     *
     * @param currBatteryLevel 设置当前的电量
     */
    public static void setCurrBatteryLevel(int currBatteryLevel) {
        mCurrBatteryLevel = currBatteryLevel;
    }

    /**
     * 当前电池状态
     * @return 当前电池状态
     */
    public static int getCurBatteryState() {
        return mCurBatteryState;
    }

    /**
     * 当前电池状态
     * @param curBatteryState 当前电池状态
     */
    public static void setCurBatteryState(int curBatteryState) {
        mCurBatteryState = curBatteryState;
    }
}
