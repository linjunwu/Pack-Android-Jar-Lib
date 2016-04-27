/**
 * 
 */
package com.baidu.appsearch.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * prefUtils.
 * @author zhushiyu01
 *
 */
public final class PrefUtils {

    /**
     * 构造函数.
     */
    private PrefUtils() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 是否存在Key
     * @param context context
     * @param key key
     * @return 是否
     */
    public static boolean contains(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
    }
    
    /**
     * 是否存在Key
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @return 是否
     */
    public static boolean contains(String prefName, Context context, String key) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).contains(key);
    }
    
    /**
     * 获取Boolean
     * @param context context
     * @param key  key
     * @param defValue defValue
     * @return Boolean
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }
    
    /**
     * 获取Boolean
     * @param prefName preference Name
     * @param context context
     * @param key  key
     * @param defValue defValue
     * @return Boolean
     */
    public static boolean getBoolean(String prefName, Context context, String key, boolean defValue) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getBoolean(key, defValue);
    }
    
    /**
     * 设置Boolean
     * 
     * @param context context
     * @param key key
     * @param value value
     */
    public static void setBoolean(Context context, String key, boolean value) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            editor.putBoolean(key, value);
            try {
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
    /**
     * 设置Boolean
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param value value
     */
    public static void setBoolean(String prefName, Context context, String key, boolean value) {
        if (context == null || prefName == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            editor.putBoolean(key, value);
            try {
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
    /**
     * 获取一个preference中所有的记录
     * @param prefName preference name
     * @param context context
     * @return preference中的所有内容
     */
    public static Map<String, ?> getAll(String prefName, Context context) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getAll();
    }
    
    
    
    /**
     * 获取Float
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return Float
     */
    public static float getFloat(Context context, String key, float defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
    }
    
    /**
     * 获取Float
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return Float
     */
    public static float getFloat(String prefName, Context context, String key, float defValue) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getFloat(key, defValue);
    }
    
    /**
     * 设置Float
     * 
     * @param context context
     * @param key key
     * @param value value
     */
    public static void setFloat(Context context, String key, float value) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            editor.putFloat(key, value);
            try {
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 设置Float
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param value  value
     */
    public static void setFloat(String prefName, Context context, String key,
                                float value) {
        if (context == null || key == null || prefName == null) {
            return;
        }
        SharedPreferences sharepreference = context.getSharedPreferences(
                prefName, Context.MODE_PRIVATE);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            editor.putFloat(key, value);
            try {
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取Int
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return Long
     */
    public static int getInt(Context context, String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
    }
    
    /**
     * 获取Int
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return Long
     */
    public static int getInt(String prefName, Context context, String key, int defValue) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getInt(key, defValue);
    }
    
    /**
     * 设置int
     *
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setInt(Context context, String key, int value) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            editor.putInt(key, value);
            try {
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
    /**
     * 设置int
     *
     * @param prefName
     *            preference Name
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setInt(String prefName, Context context, String key,
            int value) {
        if (prefName == null || context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = context.getSharedPreferences(
                prefName, Context.MODE_PRIVATE);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            if (editor != null) {
                editor.putInt(key, value);
                try {
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
    
    /**
     * 获取Long
     * @param context context
     * @param key key
     * @param defValue  defValue
     * @return Long
     */
    public static long getLong(Context context, String key, long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
    }
    
    /**
     * 获取Long
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param defValue  defValue
     * @return Long
     */
    public static long getLong(String prefName, Context context, String key, long defValue) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getLong(key, defValue);
    }
    
    /**
     * 设置Long
     *
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setLong(Context context, String key, long value) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            if (editor != null) {
                editor.putLong(key, value);
                try {
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    /**
     * 设置Long
     *
     * @param prefName
     *            preference Name
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setLong(String prefName, Context context, String key,
            long value) {
        if (context == null) {
            return;
        }
        SharedPreferences sharepreference = context.getSharedPreferences(
                prefName, Context.MODE_PRIVATE);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            if (editor != null) {
                editor.putLong(key, value);
                try {
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
    
    /**
     * 设置String
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return String
     */
    public static String getString(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }
    
    /**
     * 设置String
     * @param prefName preference Name
     * @param context context
     * @param key key
     * @param defValue defValue
     * @return String
     */
    public static String getString(String prefName, Context context, String key, String defValue) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, defValue);
    }
    
    /**
     * 设置String
     *
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setString(Context context, String key, String value) {
        if (context == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            if (editor != null) {
                editor.putString(key, value);
                try {
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
    
    /**
     * 设置String
     *
     * @param prefName
     *            preference Name
     * @param context
     *            context
     * @param key
     *            key
     * @param value
     *            value
     */
    public static void setString(String prefName, Context context, String key,
            String value) {
        if (context == null || prefName == null || key == null) {
            return;
        }
        SharedPreferences sharepreference = context.getSharedPreferences(
                prefName, Context.MODE_PRIVATE);
        if (sharepreference != null) {
            Editor editor = sharepreference.edit();
            if (editor != null) {
                editor.putString(key, value);
                try {
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
    
    /**
     * 移除Key
     * @param context context
     * @param key key
     */
    public static void removeKey(Context context, String key) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(key);
        try {
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 移除Key
     * @param prefName preference Name
     * @param context context
     * @param key key
     */
    public static void removeKey(String prefName, Context context, String key) {
        Editor editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        editor.remove(key);
        editor.commit();
    }
}
