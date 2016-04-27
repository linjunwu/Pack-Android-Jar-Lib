/*
 * Copyright (C) 2012 Tapas Mobile Ltd.  All Rights Reserved.
 */

// CHECKSTYLE:OFF
package com.baidu.android.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.logging.Log;

public class SmsMessageCompat {
    private static final String TAG = "SmsMessageCompat";

    private static boolean DEBUG = CommonConstants.DEBUG;

    public static final String FORMAT_3GPP = "3gpp";
    public static final String FORMAT_3GPP2 = "3gpp2";

    private static Class<?> sSmsMessageBaseCls;
    private static Constructor<SmsMessage> sSmsMessageCons;
    private static Class<?> sSmsMessageGSMCls;
    private static Method sSmsMessageGSMCreateFromPdu;
    private static Class<?> sSmsMessageCDMACls;
    private static Method sSmsMessageCDMACreateFromPdu;

    private static Method sSendMessageGTI9100ICS;

    static {
        ClassLoader cl = ClassLoader.getSystemClassLoader();




        try {
            sSmsMessageBaseCls = cl.loadClass("com.android.internal.telephony.SmsMessageBase");
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "SmsMessageBase cls missing");
            }
            sSmsMessageBaseCls = null;
        }
        try {
            if (sSmsMessageBaseCls == null) {
                sSmsMessageCons = null;
            } else {
                sSmsMessageCons = SmsMessage.class.getDeclaredConstructor(sSmsMessageBaseCls);
                sSmsMessageCons.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            if (DEBUG) {
                Log.e(TAG, "SmsMessage cons missing");
            }
            sSmsMessageCons = null;
        }
        try {
            sSmsMessageGSMCls =
                    cl.loadClass("com.android.internal.telephony.gsm.SmsMessage");
            sSmsMessageGSMCreateFromPdu =
                    sSmsMessageGSMCls.getDeclaredMethod("createFromPdu", byte[].class);
            sSmsMessageGSMCreateFromPdu.setAccessible(true);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "gsm.SmsMessage missing", e);
            }
            sSmsMessageGSMCls = null;
            sSmsMessageGSMCreateFromPdu = null;
        }
        try {
            sSmsMessageCDMACls =
                    cl.loadClass("com.android.internal.telephony.cdma.SmsMessage");
            sSmsMessageCDMACreateFromPdu =
                    sSmsMessageCDMACls.getDeclaredMethod("createFromPdu", byte[].class);
            sSmsMessageCDMACreateFromPdu.setAccessible(true);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "cdma.SmsMessage missing", e);
            }
            sSmsMessageCDMACls = null;
            sSmsMessageCDMACreateFromPdu = null;
        }
        try {
            Class<?>[] arrayOfClass = new Class[] { String.class, String.class, ArrayList.class,
                    ArrayList.class, ArrayList.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE,
                    Integer.TYPE, };
            sSendMessageGTI9100ICS = SmsManager.class.getMethod("sendMultipartTextMessage", arrayOfClass);
        } catch (NoSuchMethodException localNoSuchMethodException) {
            sSendMessageGTI9100ICS = null;
        }
    }

    private static SmsMessage constructSmsMessage(Method mtd, byte[] pdu) {
        if ((sSmsMessageCons == null) || (mtd == null) || (pdu == null)) {
            return null;
        }
        try {
            Object smsbase;
            smsbase = mtd.invoke(null, pdu);
            if (smsbase == null) {
                return null;
            }
            return sSmsMessageCons.newInstance(smsbase);
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                Log.e(TAG, "constructSmsMessage", e);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "constructSmsMessage reflection", e);
            }
        }
        return null;
    }

    public static SmsMessage createFromPdu(byte[] pdu, String format) {
        SmsMessage ret = null;
        if (FORMAT_3GPP2.equals(format)) {
            // cdma
            ret = constructSmsMessage(sSmsMessageCDMACreateFromPdu, pdu);
            if (ret != null) {
                return ret;
            }
        } else if (FORMAT_3GPP.equals(format)) {
            // gsm
            ret = constructSmsMessage(sSmsMessageGSMCreateFromPdu, pdu);
            if (ret != null) {
                return ret;
            }
        } else {
            // no hint, maybe a dual card device?
            // try GSM
            ret = constructSmsMessage(sSmsMessageGSMCreateFromPdu, pdu);
            if (ret != null) {
                return ret;
            }
            // try CDMA
            ret = constructSmsMessage(sSmsMessageCDMACreateFromPdu, pdu);
            if (ret != null) {
                return ret;
            }
        }
        // Failed, try standard interface

        // if anything wrong, will be here
        return SmsMessage.createFromPdu(pdu);
    }

    /**
     * For fix a bug in I9100 4.0.3, it will send duplicate sms by 3rd party sms client
     *
     * @param number
     * @param body
     * @param sentIntent
     */
    public static void sendMessage(String number, String body, PendingIntent sentIntent) {
        SmsManager sm = SmsManager.getDefault();
        ArrayList<String> messages = sm.divideMessage(body);
        ArrayList<PendingIntent> sentIntents = null;
        if (sentIntent != null) {
            sentIntents = new ArrayList<PendingIntent>();
            sentIntents.add(sentIntent);
        }
        // if (sSendMessageGTI9100ICS != null) {
        // try {
        // if (DEBUG)
        // Log.d("SmsMessageCompat", "send for 9100 ics");
        // Method localMethod = sSendMessageGTI9100ICS;
        // Object[] arrayOfObject = new Object[] { number, null, messages,
        // sentIntents, null,
        // Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(0),
        // Integer.valueOf(0), };
        // localMethod.invoke(sm, arrayOfObject);
        // return;
        // } catch (IllegalAccessException localIllegalAccessException) {
        // // ignore this, will to the final
        // } catch (InvocationTargetException localInvocationTargetException) {
        // // ignore this, will to the final
        // }
        // }
        // sm.sendMultipartTextMessage(number, null, messages, sentIntents,
        // null);


        sm.sendTextMessage(number, null, body, sentIntent, null);
    }
}
// CHECKSTYLE:ON