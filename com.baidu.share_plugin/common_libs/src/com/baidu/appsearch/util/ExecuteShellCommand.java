/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.baidu.appsearch.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baidu.appsearch.config.CommonConstants;
import com.baidu.appsearch.config.CommonParams;
import com.baidu.appsearch.logging.Log;

/**
 * 执行shell命令
 * 
 * @author chenzhiqin
 * @since 2013-6-9
 */
public final class ExecuteShellCommand {

    /** Log TAG */
    private static final String TAG = ExecuteShellCommand.class.getSimpleName();

    /** if enabled, logcat will output the log. */
    private static final boolean DEBUG = true & CommonConstants.DEBUG;

    /**
     * ExecuteShellCommand 的单例
     */
    private static ExecuteShellCommand instance;

    /**
     * 通过root权限，执行cmd命令，执行失败，返回0
     */
    public static final String EXE_SU_CMD_RS_FAIL = "fail";
    /**
     * 通过root权限，执行cmd命令，执行成功，返回1
     */
    public static final String EXE_SU_CMD_RS_SUCC = "success";
    /**
     * 通过root权限，执行cmd命令，root权限被拒绝，返回-1
     */
    public static final int EXE_SU_CMD_RS_REFUSED = -1;

    /**
     * 请求执行shell语句的通道
     */

    java.lang.Process process = null;

    /**
     * 是否获取到root权限
     */
    private boolean hasRootPermission = false;

    /**
     * 输出流
     */
    private DataOutputStream dos;
    /**
     * 输入流
     */
    private DataInputStream dis;

    /**
     * Context
     */
    private Context mContext;

    /**
     * 上一次root失败的时间
     */
    private static final String LAST_ROOT_FAIL_TIME = "lastRootFailTime";

    /**
     * root 请求失败，超时时间6小时
     */
    private static final long ROOT_FAIL_TIME_OUT = 6 * 60 * 60 * 1000;

    /**
     * 错误前缀
     */
    private static final String ERROR_PREFIX = "ERROR_";


    /**
     * 私有化构造方法
     * 
     * @param context
     *            上下文
     */
    private ExecuteShellCommand(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 获取实例
     * 
     * @param context
     *            上下文
     * @return 返回实例
     */
    public static synchronized ExecuteShellCommand getInstance(Context context) {
        if (instance == null) {
            instance = new ExecuteShellCommand(context);
        }

        return instance;
    }

    /**
     * 请求root权限
     * 
     * @return 返回是否成功
     */
    public synchronized boolean requestRootPermission() {
        if (isHasRootPermission()) {
            return true;
        }

        try {
            if (dos != null) {
                try {
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dos = null;
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dis = null;
            }
            process = RootEngineManager.getInstance(mContext).execSuScript();

            OutputStream os = process.getOutputStream();
            InputStream is = process.getInputStream();

            if (os != null && is != null) {

                dos = new DataOutputStream(os);
                dis = new DataInputStream(is);

                dos.writeBytes("id\n");
                dos.flush();

                String result = "";
                hasRootPermission = false;
                while ((result = dis.readLine()) != null) {
                    if (DEBUG) {
//                        if (result != null) {
                        Log.i(TAG, "request result = " + result);
//                        }
                    }
                    if (result.toLowerCase().contains("uid=0")) {
                        hasRootPermission = true;
                        break;
                    } else if (!TextUtils.isEmpty(result)) {
                        hasRootPermission = false;
                        break;
                    }
                }
                if (hasRootPermission) {
                    // 在个别root过的设备上，su后pm命令无法执行（exitValue=139，在shell运行pm会
                    // segmentation fault），原因可能是缺少LD_LIBRARY_PATH，这里加以规避
                    dos.writeBytes("export LD_LIBRARY_PATH=/vendor/lib:/system/lib" + "\n");
                    dos.flush();
                }
            }
        } catch (Exception e) {
            hasRootPermission = false;
            e.printStackTrace();
        }

        if (!hasRootPermission) {
            // 获取root权限失败，释放资源
            release();
            // 记录失败的时间
            updateLastRootFailTime(System.currentTimeMillis());
        } else {
            updateLastRootFailTime(0);
        }

        return hasRootPermission;
    }

    /**
     * 执行shell命令
     * 
     * @param cmd
     *            需要执行的命令语句
     * @return 返回是否执行成功
     */
    public synchronized String executeSuCommand(String cmd) {
        String rs = EXE_SU_CMD_RS_FAIL;
        if (process == null || !hasRootPermission) {
            return rs;
        }
        try {
            if (dos == null) {
                dos = new DataOutputStream(process.getOutputStream());
            }
            if (dos != null) {
                if (DEBUG) {
                    Log.i(TAG, "cmd:" + cmd);
                }
                // 将文件路径用单引号括起来。
                byte[] command = (cmd + "\n").getBytes("utf-8");
                dos.write(command);
                dos.flush();

                if (dis == null) {
                    dis = new DataInputStream(process.getInputStream());
                }
                String line;
                while ((line = dis.readLine()) != null) {
                    if (DEBUG) {
                        Log.i(TAG, "result:" + line);
                    }
                    if (line.toLowerCase().contains("success")) {
                        rs = EXE_SU_CMD_RS_SUCC;
                        break;
                    } else if (line.toLowerCase().contains("failure")) {
                        String s = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                        rs = ERROR_PREFIX + s;
                        break;
                    } else if (!TextUtils.isEmpty(line) && line.toLowerCase().contains("fault")) {
                        // root 执行失败，一般是手机root有问题
                        rs = EXE_SU_CMD_RS_FAIL;
                        break;
                    } else if (!TextUtils.isEmpty(line) && line.toLowerCase().contains("no such file or directory")) {
                        // root 执行失败，一般是手机root有问题。
                        rs = EXE_SU_CMD_RS_FAIL;
                        break;
                    }

                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            rs = EXE_SU_CMD_RS_FAIL;
        }
        if (!rs.equals(EXE_SU_CMD_RS_SUCC) && rs.indexOf(ERROR_PREFIX) < 0) {
            release();
            updateLastRootFailTime(System.currentTimeMillis());
        }
        if (DEBUG) {
            Log.i(TAG, "rs = " + rs);
        }
        return rs;
    }

    /**
     * @return the hasRootPermission
     */
    public boolean isHasRootPermission() {
        if (process == null) {
            hasRootPermission = false;
        }
        return hasRootPermission;
    }

    /**
     * 释放资源
     */
    public synchronized void release() {
        try {
            if (dos != null) {
                dos.close();
                dos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (dis != null) {
                dis.close();
                dis = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process != null) {
            // process运行时马上调用destroy是杀不掉进程的，因为此时进程还在锁定中
            try {
                Thread.sleep(3000);     // SUPPRESS CHECKSTYLE
                process.destroy();
                process = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 记录上一次root失败的时间
     * 
     * @param time
     *            失败的时间
     */
    public void updateLastRootFailTime(long time) {
        SharedPreferences prefs = mContext.getSharedPreferences(CommonParams.WEBSUITE_SHARE_FILE, 0);
        prefs.edit().putLong(LAST_ROOT_FAIL_TIME, time).commit();
    }

    /**
     * 判断是否可以请求root
     * 
     * @return 返回是否可以请求root
     */
    public boolean canRequestRoot() {
        long lastTime = mContext.getSharedPreferences(CommonParams.WEBSUITE_SHARE_FILE, 0).getLong(LAST_ROOT_FAIL_TIME,
                0L);
        boolean b = false;
        if (lastTime == 0) {
            b = true;
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= ROOT_FAIL_TIME_OUT) {
                b = true;
            } else {
                b = false;
            }
        }
//        b = false;
        return b;
    }
    
    /**
     * shell命令执行结果
     * @author zhushiyu01
     */
    public static class CommandResult {

        /** 执行结果 */
        public String result = "";
        /** 执行错误信息 */
        public String error = "";
    }
    
    /**
     *执行Root命令
     * @param isRunOnSu 是否Root执行
     * @param commands 命令集
     * @return 执行结果
     */
    public static CommandResult runShellCommand(boolean isRunOnSu, Object... commands) {
        
        Process process = null;
        DataOutputStream os = null;
        DataInputStream stdout = null;
        DataInputStream stderr = null;
        CommandResult ret = new CommandResult();
        try {
            StringBuffer output = new StringBuffer();
            StringBuffer error = new StringBuffer();
            if (isRunOnSu) {
                process = Runtime.getRuntime().exec("su");
            } else {
                process = Runtime.getRuntime().exec("sh");
            }
            os = new DataOutputStream(process.getOutputStream());
            for (Object command : commands) {
                String cmd = command.toString();
                if (DEBUG) {
                    Log.d(TAG, cmd);
                }
                // mount 特殊处理
                if (cmd.startsWith("mount ")) {
                    Thread.sleep(500L); // SUPPRESS CHECKSTYLE
                    os.writeBytes(cmd + "\n");
                    Thread.sleep(500L); // SUPPRESS CHECKSTYLE
                } else {
                    os.writeBytes(cmd + "\n");
                }
            }
            os.writeBytes("exit\n");
            os.flush();
            stdout = new DataInputStream(process.getInputStream());
            String line;
            while ((line = stdout.readLine()) != null) {
                output.append(line).append('\n');
            }
            stderr = new DataInputStream(process.getErrorStream());
            while ((line = stderr.readLine()) != null) {
                error.append(line).append('\n');
            }
            process.waitFor();
            ret.result = output.toString().trim();
            ret.error = error.toString().trim();
        } catch (Exception e) {
            ret.result = "";
            ret.error = e.getMessage();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (stdout != null) {
                    stdout.close();
                }
                if (stderr != null) {
                    stderr.close();
                }
                process.destroy();
            } catch (Exception e) {
                ret.result = "";
                ret.error = e.getMessage();
            }
        }
        return ret;
    }
}
