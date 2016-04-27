package com.baidu.share.utils;

import android.os.IBinder;
import android.os.RemoteException;

import com.baidu.android.gporter.rmi.Naming;
import com.baidu.sharecallback.IShareCallback;

/**
 * PluginTransferDataUtils
 *
 * @author linjunwu
 * @since 2016/2/18
 */
public class PluginTransferDataUtils {
    private static final String TAG = "PluginTransferDataUtils";

    public static void callback(String resultType) {
        LogUtil.d(TAG, "callback");
        IBinder binder = Naming.lookupHost("com.baidu.sharecallback.ShareCallbackImpl");

        IShareCallback shareCallback = IShareCallback.Stub.asInterface(binder);

        try {
            if (shareCallback != null) {
                shareCallback.callback(resultType);
            }
//            Toast.makeText(this, "result from host : " + result, Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
