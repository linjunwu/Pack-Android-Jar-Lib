package com.baidu.appsearch.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.content.Intent;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Xml;

import com.baidu.appsearch.common.RequestUrls;
import com.baidu.appsearch.config.db.Data;
import com.baidu.appsearch.logging.Log;
import com.baidu.appsearch.requestor.HttpURLRequest;
import com.baidu.appsearch.requestor.InputStreamResponseHandler;
import com.baidu.appsearch.requestor.RequestParams;
import com.baidu.appsearch.requestor.WebRequestTask.RequestType;
import com.baidu.appsearch.statistic.StatisticConfig;
import com.baidu.appsearch.statistic.StatisticFile;
import com.baidu.appsearch.statistic.StatisticPoster;
import com.baidu.appsearch.statistic.StatisticProcessor;
import com.baidu.appsearch.util.BaiduIdentityManager;
import com.baidu.appsearch.util.BaiduIdentityManager.Event;
import com.baidu.appsearch.util.NormalResultListener;
import com.baidu.appsearch.util.PrefUtils;
import com.baidu.appsearch.util.Utility;

/**
 * server action grabber. 获得server下发的action
 * 
 * @author fujiaxing liuqingbiao zhangjunguo wangguanghui01
 * 
 */
public class ServerCommandGrabber {
    /** DEBUG Switch.*/
    private static final boolean DEBUG = true & CommonConstants.DEBUG;
    /** TAG.*/
    private static final String TAG = ServerCommandGrabber.class.getSimpleName();
    /** server下发的commands.*/
    private ArrayList<Command> mCommands;
    /** {@link Context}. */
    private Context mContext;
    /** 数据下发时间（server时间戳）.*/
    private String mServerTime;
    /** 设置urls的action */
    private static final String SET_URLS_ACTION = "set_urls";
    /** 设置action */
    private static final String SET_SETTINGS_ACTION = "set_settings";
    /** 开关设置命令*/
    private static final String SET_SWITCH_INFO_COMMAND = "switch_info";
    /** 应用的events */
    private static final String APP_EVENTS_ACTION = "app_events";
    /** OEM设置命令*/
    private static final String SET_OEM_SETTINGS = "oem_settings";
    /** urls的数据版本key */
    private static final String URLS_DATASET_VERSION = "urls_dataset_version";
    /** settings的数据版本key */
    private static final String SETTINGS_DATASET_VERSION = "settings_dataset_version";
    /** 客户端发送请求的events的数据版本key */
    private static final String EVENTS_DATASET_VERSION = "events_dataset_version";
    /** 客户端活跃用户事件key */
    private static final String APP_EVENT_ACTIVE_USER = "event_active_user";
    /** 客户端有效安装事件key */
    private static final String APP_EVENT_ACTIVE_INSTALL_USER = "event_active_install_user";
    /** 客户端更新激活事件key */
    private static final String APP_EVENT_UPGRADE = "event_upgrade";
    /** 客户端激活事件key */
    private static final String APP_EVENT_ACTIVE = "event_active";
    /** 在server下发的配置xml中attribute的 属性名 */
    private static final String SERVER_CONFIG_XML_ATTRIBUTE_NAME = "name";
    /** 客户端是否是有效安装的后台请求 */
    private boolean mIsBackGroundRequest = false;
    /** BUFFER SIZE**/
    public static final int BUFFER_SIZE = 2048;
    /** server settings 拉取完毕的广播 */
    public static final String ACTION_SERVER_COMMAND_GRAB_SUCCESS = "com.baidu.appsearch.server_command_grab_success";

    
    /**
     * construct a ServerCommandGrabber.
     * 
     * @param context
     *            Context
     * @param isBackGroundRequest
     *            客户端是否是有效安装的后台请求
     */
    public ServerCommandGrabber(Context context, boolean isBackGroundRequest) {
        mContext = context;
        mIsBackGroundRequest = isBackGroundRequest;
        if (DEBUG) {
            Log.d(TAG, "is backGroundRequset:" + isBackGroundRequest);
        }
    }
    
    /**
     * 执行抓取、解析、执行命令操作.
     * @param url 服务端地址
     * @param resultListener 抓取结果监听
     */
    public void execute(String url, final NormalResultListener resultListener) {
        if (!TextUtils.isEmpty(TestConfiguration.getFixedUrl())) {
            url = TestConfiguration.getFixedUrl();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(url).append("&activetime=").append(BaiduIdentityManager.getInstance(mContext).getActiveTimeStamp());
        url = BaiduIdentityManager.getInstance(mContext).processUrl(sb.toString());
        url = BaiduIdentityManager.appendClientRequestIdToUrl(url,
                BaiduIdentityManager.generateClientRequestId(mContext));    // 添加客户端请求ID，方便QA跟跟踪
        if (DEBUG) {
            Log.d(TAG, "serverCommandUrl: " + url);
        }

        
        List<NameValuePair> params = getPostData();
        
        RequestParams requestParams = new RequestParams();
        requestParams.setUrl(url);
        requestParams.setRequestType(RequestType.POST);
        requestParams.setParams(params);
        requestParams.addHeader("Content-Type", "application/x-www-form-urlencoded");
        requestParams.addHeader("Accept-Encoding", "gzip");
        
        HttpURLRequest httpURLRequest = new HttpURLRequest(mContext, requestParams);
        httpURLRequest.request(new InputStreamResponseHandler() {
            
            @Override
            public void onSuccess(int responseCode, int contentLength, InputStream inputStream) throws IOException {
                try {
                    parseData(inputStream);
                } catch (XmlPullParserException e) {
                    if (DEBUG) {
                        e.printStackTrace();    
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        e.printStackTrace();    
                    }
                }
                
                executeCommand();
                if (null != resultListener) {
                    resultListener.onSuccess(responseCode);
                }
            }
            
            @Override
            public void onFail(int responseCode, String errorMessage) {
                if (null != resultListener) {
                    resultListener.onFailed(errorMessage, responseCode, null);
                }
            }
        });
        
        // 检查上传用户行为统计
        StatisticPoster.getInstance(mContext).setAlarmForStatisticData(0);
        
        
//        HttpPost httppost = new HttpPost(url);
//        httppost.setHeader("Accept-Encoding", "gzip");
//        httppost.setEntity(getPostData());
//        InputStream inputStream = null;
//        try {
//            mHttpClient = new ProxyHttpClient(mContext);
//            HttpResponse response = mHttpClient.execute(httppost);
//            HttpEntity resEntity = response.getEntity();
//            if (DEBUG) {
//                Log.d(TAG, "resEntity: " + resEntity);
//            }
//            inputStream = getGzipInputStream(resEntity);
//            if (inputStream == null) {
//                inputStream = resEntity.getContent();
//            }
//            if (DEBUG) {
//                Log.d(TAG, "inputStream: " + inputStream);
//            }
//
//            try {
//                parseData(inputStream);
//                executeCommand();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // 检查上传用户行为统计
//            StatisticPoster.getInstance(mContext).checkSendStatisticData(TAG);
//            if (mHttpClient != null) {
//                mHttpClient.close();
//            }
//        }
        
    }

    /**
     * server如果下发为gip，则获取gzip inputstream .
     * @param resEntity {@link HttpEntity}
     * @return gzip InputStream or null
     * @throws IOException {@link IOException}
     */
    private InputStream getGzipInputStream(HttpEntity resEntity) throws IOException {
        Header header = resEntity.getContentEncoding();
        String contentEncoding = null;
        InputStream inputStream = null;
        if (header != null) {
            contentEncoding = header.getValue();
            if (contentEncoding.toLowerCase().indexOf("gzip") != -1) {
                inputStream = new GZIPInputStream(resEntity.getContent());
            }
        }
        
        return inputStream;
    }
    
    /**
     * 获得上传数据.
     * @return 上传数据
     */
    private List<NameValuePair> getPostData() {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        String defaultVersion = Build.VERSION.SDK_INT + "_0";
        if (TestConfiguration.getTestSDKInt() > 0) {
            defaultVersion = TestConfiguration.getTestSDKInt() + "_0";
        }
        String urlVersion = PrefUtils.getString(mContext, URLS_DATASET_VERSION,
                /*Build.VERSION.RELEASE + */defaultVersion);
        // 用于测试的代码
        if (!TextUtils.isEmpty(TestConfiguration.getUrlActionVersion())) {
            urlVersion = TestConfiguration.getUrlActionVersion();
        }
        if (!urlVersion.startsWith(String.valueOf(Build.VERSION.SDK_INT))) {
            PrefUtils.setString(mContext, URLS_DATASET_VERSION, defaultVersion);
            urlVersion = defaultVersion;
        }
        String appEventVersion = PrefUtils.getString(mContext, EVENTS_DATASET_VERSION,
                /* Build.VERSION.RELEASE + */defaultVersion);
        // 用于测试的代码
        if (!TextUtils.isEmpty(TestConfiguration.getEventActionVersion())) {
            appEventVersion = TestConfiguration.getEventActionVersion();
        }
        if (!appEventVersion.startsWith(String.valueOf(Build.VERSION.SDK_INT))) {
            PrefUtils.setString(mContext, EVENTS_DATASET_VERSION, defaultVersion);
            appEventVersion = defaultVersion;
        }
        String settingVersion = PrefUtils.getString(mContext, SETTINGS_DATASET_VERSION,       
                /* Build.VERSION.RELEASE + */defaultVersion);
        // 用于测试的代码
        if (!TextUtils.isEmpty(TestConfiguration.getEventActionVersion())) {
            settingVersion = TestConfiguration.getEventActionVersion();
        }

        if (!settingVersion.startsWith(String.valueOf(Build.VERSION.SDK_INT))) {
            PrefUtils.setString(mContext, SETTINGS_DATASET_VERSION, defaultVersion);
            settingVersion = defaultVersion;
        }

        JSONObject jsonobj = new JSONObject();
        JSONArray events = new JSONArray();
        try {
            jsonobj.put("set_urls_version", urlVersion);
            jsonobj.put("set_settings_version", settingVersion);
            jsonobj.put("app_events_version", appEventVersion);

            String vn = "v" + BaiduIdentityManager.getInstance(mContext).getVersionName();
            jsonobj.put("app_version_name", vn);
            
            // 发送激活请求 仅当客户端在前台时发送
            if (!mContext.getSharedPreferences(BaiduIdentityManager.PREFS_NAME, 0).getBoolean(
                    BaiduIdentityManager.ACTIVE_KEY, false)
                    && !mIsBackGroundRequest) {
                JSONObject activeEvent = new JSONObject();
                activeEvent.put("event", Event.ACTIVE_EVENT.ordinal());
                activeEvent.put("vfrom", 1);
                activeEvent.put("name", "event_active");
                activeEvent.put("api", 1);
                activeEvent.put("parameters", BaiduIdentityManager.getInstance(mContext)
                        .processParameters(mContext, Event.ACTIVE_EVENT));
                events.put(activeEvent);
                // 重新设置最后一次更新时间
                BaiduIdentityManager.getInstance(mContext).resetLastUpdateTime(mContext);
            }
            // 发送更新激活
            if (CommonGloabalVar.getInstance(mContext).getUserType() == 2) {
                if (DEBUG) {
                    Log.d(TAG,
                            "send update active event,new lastUpdateTime="
                                    + Utility.getPacakgeLastUpdateTime(mContext, mContext.getPackageName()));
                }
                JSONObject upgradeActiveEvent = new JSONObject();
                upgradeActiveEvent.put("event", Event.UPDATE_ACTIVE_EVENT.ordinal());
                upgradeActiveEvent.put("name", "event_upgrade");
                upgradeActiveEvent.put("api", 1);
                upgradeActiveEvent.put("parameters", BaiduIdentityManager.getInstance(mContext)
                        .processParameters(mContext, Event.UPDATE_ACTIVE_EVENT));
                events.put(upgradeActiveEvent);
                // 重新设置最后一次更新时间
                BaiduIdentityManager.getInstance(mContext).resetLastUpdateTime(mContext);
            }

            // 发送有效安装用户统计
            if (mIsBackGroundRequest) {
                JSONObject activeInstallUserEvent = new JSONObject();
                activeInstallUserEvent.put("event", Event.ACTIVE_INSTALL_EVENT.ordinal());
                activeInstallUserEvent.put("name", "event_active_install_user");
                activeInstallUserEvent.put("api", 1);
                activeInstallUserEvent.put("parameters", BaiduIdentityManager.getInstance(mContext)
                        .processParameters(mContext, Event.ACTIVE_INSTALL_EVENT));
                events.put(activeInstallUserEvent);
            } else {
                // 发送活跃用户统计
                JSONObject activeUserEvent = new JSONObject();
                activeUserEvent.put("event", Event.ACTIVE_USER_EVENT.ordinal());
                activeUserEvent.put("name", "event_active_user");
                activeUserEvent.put("api", 1);
                activeUserEvent.put("parameters", BaiduIdentityManager.getInstance(mContext)
                        .processParameters(mContext, Event.ACTIVE_USER_EVENT));
                events.put(activeUserEvent);
            }
            jsonobj.put("events", events);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        list.add(new BasicNameValuePair("version", jsonobj.toString()));
        if (DEBUG) {
            Log.d(TAG,
                    "grab content data with key:"
                            + new BasicNameValuePair("version", jsonobj.toString()));
        }
//        UrlEncodedFormEntity entity = null;
//        try {
//            entity = new UrlEncodedFormEntity(list, "utf-8");
//            entity.setContentType("application/x-www-form-urlencoded");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return entity;
        
        return list;
    }
    
    /**
     * 解析server下发数据.
     * @param in 数据.
     * 下发数据格式：
     * <br/>xml格式：
     * <br/>     &lt;appcommand time=文件下发时间（精确到秒）&gt;
     * <br/>       &lt;do&gt;        //代表一个server下发的command
     * <br/>               &lt;action&gt;  XXXXXX  &lt;/action&gt;
     * <br/>               &lt;data_set version=数据版本&gt;       // version代表数据的版本
     * <br/>                 &lt;data attibute&gt;  value &lt;/data&gt;//action的数据
     * <br/>                  …………
     * <br/>               &lt;/data_set&gt;
     * <br/>       &lt;/do&gt;
     * <br/>        …………
     * <br/>     &lt;/appcommand&gt;
     * @throws XmlPullParserException 
     * @throws IOException 
     */
    private void parseData(InputStream in) throws XmlPullParserException, IOException {
        if (DEBUG) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
                baos.write(buffer, 0, len);
            }

            String str = new String(baos.toByteArray());
            Log.i(TAG, "xxx---" + str);

            in = new ByteArrayInputStream(baos.toByteArray());
        }

        XmlPullParser parser = Xml.newPullParser(); 
        parser.setInput(in, "UTF-8");
        int eventType = parser.getEventType(); 
        if (DEBUG) {
            Log.d(TAG, "parseData invoked eventtype:" + eventType);
        }
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) { 
                case XmlPullParser.START_DOCUMENT:
                    if (DEBUG) {
                        Log.d(TAG, "parse start");
                    }
                    mCommands = new ArrayList<Command>();
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName(); 
                    if (tagName.equalsIgnoreCase("do")) {
                        Command command = new Command();
                        parseCommandData(command, parser);
                        mCommands.add(command);
                    } else if (tagName.equalsIgnoreCase("appcommand")) {
                        // 下发的时间戳
                        mServerTime = parser.getAttributeValue(null, "time");
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String tagNameEnd = parser.getName();
                    if (tagNameEnd.equalsIgnoreCase("appcommand")) {
                        if (DEBUG) {
                            Log.d(TAG, "parse end");
                        }
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        in.close();
    }
    
    /**
     * 解析一个command.
     * @param command {@link Command}
     * @param parser {@link XmlPullParser}
     * @throws XmlPullParserException exception
     * @throws IOException exception
     */
    private void parseCommandData(Command command, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) { 
            case XmlPullParser.START_TAG:
                String tagName = parser.getName(); 
                if (tagName.equalsIgnoreCase("action")) {
                    command.setAction(parser.nextText());
                } else if (tagName.equalsIgnoreCase("data_set")) {
                    DataSet dataSet = new DataSet();
                    command.setDataSet(dataSet);
                    parseDataSet(dataSet, parser, command.getAction());
                }
                break;
            case XmlPullParser.END_TAG:
                String tagNameEnd = parser.getName(); 
                if (tagNameEnd.equalsIgnoreCase("do")) {
                    if (DEBUG) {
                        Log.d(TAG, "parseCommandData success.");
                    }
                    return;
                }
                break;
            default:
                break;
            }
            eventType = parser.next();
        }
    }
    
    /**
     * 解析一个command的数据集.
     * @param dataSet {@link DataSet}
     * @param parser {@link XmlPullParser}
     * @param commandName 对应command name
     * @throws XmlPullParserException exception
     * @throws IOException exception
     */
    private void parseDataSet(DataSet dataSet, XmlPullParser parser, String commandName)
            throws XmlPullParserException, IOException {
        
        dataSet.setVersion(parser.getAttributeValue(null, "version"));
        
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                String tagName = parser.getName();
                if (tagName.equalsIgnoreCase("data")) {
                    Data data = null;
                    if (TextUtils.equals(commandName, SET_URLS_ACTION)) {
                        data = new URLData();
                        parseUrlData(data, parser);
                    } else if (TextUtils.equals(commandName, SET_SETTINGS_ACTION)) {
                        data = new SettingData();
                        parseSettingData(data, parser);
                    } else if (TextUtils.equals(commandName, APP_EVENTS_ACTION)) {
                        data = new EventData();
                        parseEventData(data, parser);
                    } else if (TextUtils.equals(commandName, SET_SWITCH_INFO_COMMAND)) {
                        data = new SwitchInfo();
                        parseSwitchInfo((SwitchInfo) data, parser);
                    } else if (TextUtils.equals(commandName, SET_OEM_SETTINGS)) {
                        data = new SettingData();
                        data.setType(Data.OEM_SETTING_TYPE);
                        parseSettingData(data, parser);
                    }
                    dataSet.getDatas().add(data);
                }
                break;
            case XmlPullParser.END_TAG:
                String tagNameEnd = parser.getName();
                if (tagNameEnd.equalsIgnoreCase("data_set")) {
                    if (DEBUG) {
                        Log.d(TAG, "parseDataSet success.");
                    }
                    return;
                }
                break;
            default:
                break;
            }
            eventType = parser.next();
        }
    }

    /**
     * 封装服务器下发的url信息
     * 
     * @param data
     *            数据原型
     * @param parser
     *            xml的数据集
     * @throws XmlPullParserException
     *             xml解析异常
     * @throws IOException
     *             读写流异常
     */
    private void parseUrlData(Data data, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        String name = parser.getAttributeValue(null, SERVER_CONFIG_XML_ATTRIBUTE_NAME);
        if (TextUtils.isEmpty(name)) {
            if (DEBUG) {
                Log.i(TAG, "url data name is null.");
            }
            return;
        }
        data.setName(name);
        data.setValue(Utility.htmlSpecialcharsDecode(parser.nextText()));
    }

    /**
     * 封装服务器下发的事件信息
     * 
     * @param data
     *            数据原型
     * @param parser
     *            xml的数据集
     * @throws XmlPullParserException
     *             xml解析异常
     * @throws IOException
     *             读写流异常
     */
    private void parseEventData(Data data, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        String name = parser.getAttributeValue(null, SERVER_CONFIG_XML_ATTRIBUTE_NAME);
        if (TextUtils.isEmpty(name)) {
            if (DEBUG) {
                Log.i(TAG, "event data name is null.");
            }
            return;
        }
        data.setName(name);
        data.setValue(parser.nextText());
    }

    /**
     * 封装服务器下发的配置信息
     * 
     * @param data
     *            数据原型
     * @param parser
     *            xml的数据集
     * @throws XmlPullParserException
     *             xml解析异常
     * @throws IOException
     *             读写流异常
     */
    private void parseSettingData(Data data, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        String name = parser.getAttributeValue(null, SERVER_CONFIG_XML_ATTRIBUTE_NAME);
        if (TextUtils.isEmpty(name)) {
            if (DEBUG) {
                Log.i(TAG, "setting data name is null.");
            }
            return;
        }
        data.setName(name);
        data.setValue(parser.nextText());
    }

    /**
     * 解析开关信息.
     * @param data {@link SwitchInfo}
     * @param parser {@link XmlPullParser}
     * @throws XmlPullParserException 异常
     * @throws IOException 异常
     */
    private void parseSwitchInfo(SwitchInfo data, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, SERVER_CONFIG_XML_ATTRIBUTE_NAME);
        if (TextUtils.isEmpty(name)) {
            if (DEBUG) {
                Log.i(TAG, "event data name is null.");
            }
            return;
        }
        data.setName(name);
//        data.setUrl(parser.getAttributeValue(null, "url"));
        data.setValue(parser.nextText());
    }

    /**
     * 执行server下发的command.
     */
    private void executeCommand() {
        for (Command command : mCommands) {
            String action = command.getAction();
            if (TextUtils.equals(action, SET_URLS_ACTION)) {
                boolean suc = ServerConfigDBHelper.getInstance(mContext).updateServerConfig(
                        command.getDataSet().getDatas(), Data.URL_TYPE);
                if (suc) {
                    if (DEBUG) {
                        Log.d(TAG, "set url version:" + command.getDataSet().getVersion());
                    }
                    PrefUtils.setString(mContext, URLS_DATASET_VERSION, command.getDataSet().getVersion());
                }
            } else if (TextUtils.equals(action, SET_SETTINGS_ACTION)) {
                boolean suc = ServerConfigDBHelper.getInstance(mContext).updateServerConfig(
                        command.getDataSet().getDatas(), Data.SETTING_TYPE);
                if (suc) {
                    if (DEBUG) {
                        Log.d(TAG, "set settings version:" + command.getDataSet().getVersion());
                    }
                    PrefUtils.setString(mContext, SETTINGS_DATASET_VERSION, command.getDataSet().getVersion());
                }
            } else if (TextUtils.equals(action, APP_EVENTS_ACTION)) {
                processEventData(command.getDataSet().getDatas());
                if (DEBUG) {
                    Log.d(TAG, "set envents version:" + command.getDataSet().getVersion());
                }
                PrefUtils.setString(mContext, EVENTS_DATASET_VERSION, command.getDataSet().getVersion());
            } else if (TextUtils.equals(action, SET_SWITCH_INFO_COMMAND)) {
                boolean suc = ServerConfigDBHelper.getInstance(mContext).updateServerConfig(
                        command.getDataSet().getDatas(), Data.SETTING_TYPE);
                if (suc) {
                    if (DEBUG) {
                        Log.d(TAG, "set settings version:" + command.getDataSet().getVersion());
                    }
                    PrefUtils.setString(mContext, SETTINGS_DATASET_VERSION, command.getDataSet().getVersion());
                }
                setSwitchInfo(command);
            } else if (TextUtils.equals(action, SET_OEM_SETTINGS)) {
                ServerConfigDBHelper.getInstance(mContext).updateServerConfig(
                        command.getDataSet().getDatas(), Data.OEM_SETTING_TYPE);
            }
        }
    }

    /**
     * 解析服务器下发的事件信息
     * 
     * @param events
     *            服务器返回的事件集
     */
    private void processEventData(ArrayList<Data> events) {
        for (Data event : events) {
            if (TextUtils.equals(event.getName(), APP_EVENT_ACTIVE_USER)) {
                if (DEBUG) {
                    Log.d(TAG, "APP_EVENT_ACTIVE_USER");
                }
            } else if (TextUtils.equals(event.getName(), APP_EVENT_ACTIVE_INSTALL_USER)) {
                if (DEBUG) {
                    Log.d(TAG, "APP_EVENT_ACTIVE_INSTALL_USER");
                }
            } else if (TextUtils.equals(event.getName(), APP_EVENT_ACTIVE)) {
                if (!TextUtils.equals(event.getValue(), "0")) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences(
                            BaiduIdentityManager.PREFS_NAME, 0).edit();
                    editor.putBoolean(BaiduIdentityManager.ACTIVE_KEY, true);
                    editor.putString(BaiduIdentityManager.KEY_TIME, event.getValue());
                    editor.putLong(BaiduIdentityManager.ACTIVE_KEY_TIMESTAMP, System.currentTimeMillis());
                    editor.commit();
                    if (DEBUG) {
                        Log.d(TAG, "write active time: " + event.getValue());
                    }
                }
            } else if (TextUtils.equals(event.getName(), APP_EVENT_UPGRADE)) {

                    SharedPreferences.Editor editor =  mContext.getSharedPreferences(
                            BaiduIdentityManager.PREFS_NAME, 0).edit();
                    // 更新本地文件中存储的versioncode
                    editor.putInt(
                            BaiduIdentityManager.CURRENT_VERSIONCODE_KEY,
                            Utility.getPacakgeInfo(mContext, mContext.getPackageName()).versionCode);
                    editor.commit();
                    if (DEBUG) {
                        Log.d(TAG,
                                "write new update versioncode : "
                                        + Utility.getPacakgeInfo(mContext,
                                                mContext.getPackageName()).versionCode);
                    }
                }
        }
    }

    /**
     * set switch info command solve.
     * 
     * @param command
     *            {@link Command}
     */
    private void setSwitchInfo(Command command) {
        DataSet dataSet = command.getDataSet();
        ArrayList<Data> datas = dataSet.getDatas();

//      // 首先将用户行为统计恢复成默认关闭状态.
//      StatisticFile.getInstance(mContext).setUEStatisticEnabled(mContext,
//              false);
        
        
        for (Data data : datas) {
            SwitchInfo switchInfo = (SwitchInfo) data;
            String switchName = switchInfo.getName();
            // 用户行为统计开关.
            if (TextUtils.equals(switchName, SwitchInfo.USREVT)) {
                String ueConfig = switchInfo.getValue();
                if (DEBUG) {
                    Log.i(TAG, "statistic switch:" + ueConfig);
                }
                try {
                    JSONObject statisticSwitch = new JSONObject(ueConfig);
                    
                    String masterSwitch = statisticSwitch.getString(StatisticConfig.STATISTIC_MASTER_SWITCH);
                    if (TextUtils.isEmpty(masterSwitch) || masterSwitch.equals("0")) {
                        //关闭所有开关
                        StatisticFile statisticFile = StatisticFile.getInstance(mContext);
                        statisticFile.disableAllSubSwitch();
                        StatisticProcessor.getInstance(mContext).clearBuffer();
                        statisticFile.deleteUserBehaivorStatisticFiles();
                    } else {
                        //打开总开关，接收各分类开关
                        JSONObject subSwitchs = statisticSwitch.getJSONObject(
                                StatisticConfig.STATISTIC_SUB_SWITCH);
                        StatisticFile statisticFile = StatisticFile.getInstance(mContext);
                        for (String subSwitch : StatisticFile.SUB_SWITCHS) {
                            if (subSwitchs.has(subSwitch)) {
                                StatisticConfig.setSubStatisticEnabled(mContext,
                                        StatisticConfig.STATISTIC_SUB_PREFF + subSwitch, 
                                        subSwitchs.getString(subSwitch).equals("1"));
                            }
                        }
                        
                        if (subSwitchs.getString(StatisticConfig.STATISTIC_USER_BEHAVIOUR).equals("0")) {
                            StatisticProcessor.getInstance(mContext).clearBuffer();
                            statisticFile.deleteUserBehaivorStatisticFiles();
                        }
                        // 超时
                        double timeout = statisticSwitch.getDouble(StatisticConfig.STATISTIC_TIMEOUT);
                        if (timeout < StatisticConfig.STATISTIC_MIN_TIMEOUT
                                || timeout > StatisticConfig.STATISTIC_MAX_TIMEOUT) {
                            timeout = StatisticConfig.STATISTIC_DEFAULT_TIMEOUT;
                        }
                        StatisticConfig.setStatisticTimeout(mContext, (long) timeout);
                        // 阈值
                        double threshold = statisticSwitch.getDouble(StatisticConfig.STATISTIC_THRESHOLD);
                        if (threshold < StatisticConfig.STATISTIC_MIN_THRESHOLD 
                                || threshold > StatisticConfig.STATISTIC_MAX_THRESHOLD) {
                            threshold = StatisticConfig.STATISTIC_DEFAULT_THRESHOLD;
                        }
                        StatisticConfig.setStatisticThreshold(mContext, threshold);
                        // 强制上传间隔
                        double timeup = statisticSwitch.getDouble(StatisticConfig.STATISTIC_TIMEUP);
                        if (timeup < StatisticConfig.STATISTIC_MIN_TIMEUP
                                || timeup > StatisticConfig.STATISTIC_MAX_TIMEUP) {
                            timeup = StatisticConfig.STATISTIC_DEFAULT_TIMEUP;
                        }
                        StatisticConfig.setStatisticTimeup(mContext, (long) (timeup * StatisticConfig.ONEDAY));
                        
                        // 如果第一次进来，没有上次上传时间，以当前时间作为最后一次上传统计的时间
                        if (!StatisticConfig.containLastSendStatisticTime(mContext)) {
                            StatisticConfig.setLastSendStatisticTime(mContext, System.currentTimeMillis());
                        }
                    }
                } catch (JSONException e) {
                    if (DEBUG) {
                        Log.d(TAG, "json err:" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 获得开关boolean value，从server传的“1”，“0”转为boolean, 如果server不设置，返回true
     * @param switchInfo {@link SwitchInfo}
     * @return boolean
     */
    private boolean getSwitchValue(SwitchInfo switchInfo) {
        boolean value = true;
        if (TextUtils.equals(switchInfo.getValue(), "1")) {
            value = true;
        } else if (TextUtils.equals(switchInfo.getValue(), "0")) {
            value = false;
        } else {
            if (DEBUG) {
                Log.i(TAG, "getSwitchValue, server don't set switch");
            }
        }
        return value;
    }

    
    /**
     * 重置拉取接口的version。只给收到push消息，重置数据的时候用，其他时候慎重使用
     * 
     * @param ctx
     *            Context
     */
    public void resetInterfacesecVersion(Context ctx) {
        String defaultVersion = Build.VERSION.SDK_INT + "_0";
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ctx
                .getApplicationContext());
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(URLS_DATASET_VERSION, defaultVersion);
        editor.putString(SETTINGS_DATASET_VERSION, defaultVersion);
        editor.putString(EVENTS_DATASET_VERSION, defaultVersion);
        editor.commit();
    }

    /**
     * server下发命令.
     * @author fujiaxing
     *
     */
    public static class Command {
        /** command's name.*/
        private String action;
        /** command's data.*/
        private DataSet dataSet;

        /**
         * get action method.
         * @return action string
         */
        public String getAction() {
            return action;
        }

        /** 
         * set action method.
         * @param act action's string
         */
        public void setAction(String act) {
            this.action = act;
        }

        /**
         * get dateset method.
         * @return {@link DataSet}
         */
        public DataSet getDataSet() {
            return dataSet;
        }

        /**
         * set dataset method.
         * @param dataset {@link DataSet}
         */
        public void setDataSet(DataSet dataset) {
            this.dataSet = dataset;
        }
        
    }
    
    /**
     * 命令的数据集.
     * @author fujiaxing
     *
     */
    public static class DataSet {
        /** 数据集的版本号。如下发黑名单，则为下发黑名单的版本号.*/
        private String version;
        /** command的数据集.*/
        private ArrayList<Data> datas;

        /** construct method.*/
        public DataSet() {
            datas = new ArrayList<Data>();
        }
        
        /**
         * get version method.
         * @return version
         */
        public String getVersion() {
            return version;
        }

        /**
         * set version method.
         * @param v String
         */
        public void setVersion(String v) {
            this.version = v;
        }

        /**
         * get datas method.
         * @return ArrayList<ServerConfig>
         */
        public ArrayList<Data> getDatas() {
            return datas;
        }

        /**
         * set datas method.
         * @param d ArrayList<ServerConfig>
         */
        public void setDatas(ArrayList<Data> d) {
            this.datas = d;
        }
        
    }




    /**
     * 获取服务器配置
     *
     * @param context
     *            {@link Context}
     * @param isBackgroundActive 是否是后台活跃
     */
    public static void requestServerCommand(final Context context, boolean isBackgroundActive) {
        ServerCommandGrabber grabber = new ServerCommandGrabber(context, isBackgroundActive);
        try {
            grabber.execute(RequestUrls.getInstance(context).getUrl(RequestUrls.SERVER_COMMAND_URL),
                    new NormalResultListener() {

                @Override
                public void onSuccess(Object result) {
                    // 加载完成后，刷新AppSearchUrl
                    BaseConfigURL.ServerUrlsConf.getInstance(context).refresh();
                    CommonLibServerSettings.getInstance(context).refresh();
                    Intent offlineChannelRequestIntent = new Intent(ACTION_SERVER_COMMAND_GRAB_SUCCESS);
                    context.sendBroadcast(offlineChannelRequestIntent);
                    if (DEBUG) {
                        Log.d(TAG, "Server Command拉取完成，刷新数据并发送线下渠道广播");
                    }
                }

                @Override
                public void onFailed(String msg, int errCode, Object extra) {
                    if (DEBUG) {
                        Log.i(TAG, "拉取服务器配置失败, errorCode = " + errCode + "  errMsg = " + msg);
                    }
                }

            });
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, e.toString());
            }
        }
    }
    
    
}
