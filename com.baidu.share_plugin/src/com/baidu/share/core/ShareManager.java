/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */
package com.baidu.share.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Toast;

import com.baidu.android.gporter.hostapi.HostUtil;
import com.baidu.appsearch.requestor.AbstractRequestor;
import com.baidu.appsearch.util.ImageCacheUtils;
import com.baidu.appsearch.util.SysMethodUtils;
import com.baidu.appsearch.util.Utility;
import com.baidu.cloudsdk.BaiduException;
import com.baidu.cloudsdk.IBaiduListener;
import com.baidu.cloudsdk.social.core.MediaType;
import com.baidu.cloudsdk.social.core.util.MobileQQ;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.cloudsdk.social.share.SocialShare;
import com.baidu.cloudsdk.social.share.handler.WXMediaMessage;
import com.baidu.share.config.ShareConfigDbManager;
import com.baidu.share.requestor.ShareConfigRequestor;
import com.baidu.share.ui.BDProgressDialog;
import com.baidu.share.utils.AppUtils;
import com.baidu.share.utils.PluginTransferDataUtils;
import com.baidu.shareplugin.R;
import com.baidu.share.utils.LogUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public final class ShareManager {

    private static final String TAG = "ShareManager";
    /** 两次联网拉取的最小时间间隔1d */
    private static final long INTERVAL_TIME = DateUtils.DAY_IN_MILLIS;
    /**QQ空间分享图片限制200*200，最小宽度/高度*/
    private static final int MIN_LINE = 250;

    /**单例实例*/
    private static volatile ShareManager mInstance;
    /** Context */
    private Context mContext = null;
    /**图片回收*/
    private Bitmap mBitmap = null;
    /** 加载进度对话框*/
    private BDProgressDialog mProgressDialog = null;
    /**ShareConfigDbManager*/
    private ShareConfigDbManager mShareConfigDBHelper;
    private String mShareType = "";
    private String mForm = "";
    private int mFrom;
    private boolean mUpdateDatabase = false;
    /** 除QQ、微信以外的分享媒体*/
    private Boolean mOtherForm = false;



    /** 详情页分享 */
    public static final String SHARE_TYPE_APP = "appdetail";
    /** 抽奖分享 */
    public static final String SHARE_TYPE_LOTTERY = "lottery";
    /** 洗白白分享*/
    public static final String SHARE_TYPE_WASH = "wash";
    /** 酷应用分享*/
    public static final String SHARE_TYPE_COOL = "coolapp";
    /** 专题详情页分享*/
    public static final String SHARE_TYPE_TOPIC = "topicdetail";
    
    /**
     * @param context
     *            Context
     */
    private ShareManager(Context context) {
        mContext = context.getApplicationContext();
        mShareConfigDBHelper = ShareConfigDbManager.getInstance(mContext);
    }

    /**
     * 获取分享管理器实例
     * @param context
     * Context
     * @return
     * ShareManager
     */
    public static synchronized ShareManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ShareManager(context);
        }
        return mInstance;
    }

    /**
     * 分享
     * @param activity
     * @param shareContent 分享内容
     */
    public void showShareContent(final Activity activity, ShareContent shareContent, String shareType,
                                 String form, int from, boolean updateDatabase) {
        if (activity == null || activity.isFinishing()) {
            LogUtil.d(TAG, "context.isFinishing() == true");
            return;
        }



        try {
            synchronized (this) {


                mShareType = shareType;
                mForm = form;
                mFrom = from;
                mUpdateDatabase = updateDatabase;
                if (!mUpdateDatabase) {
                    showShareWindow(activity, shareContent);
                } else {
                    executeRequestor(activity, shareContent);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, e);
            Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
                    mContext.getText(R.string.bdsocialshare_fail), Toast.LENGTH_LONG).show();
        }

    }

    private synchronized void showShareWindow(Activity activity, ShareContent shareContent) {
        // 因为是大图所以尽可能在不使用是释放掉
        mBitmap = shareContent.getImageData();
        try {
            ShareMenu shareMenu = new ShareMenu(activity, shareContent, mIBaiduListener, mShareType,
                    mFrom);
            shareMenu.show(activity.getWindow().getDecorView(), shareContent);
        } catch (IllegalArgumentException e) {
            Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
                    mContext.getText(R.string.bdsocialshare_success), Toast.LENGTH_SHORT).show();
        }

    }

    public IBaiduListener getIBaiduListener() {
        return mIBaiduListener;
    }
    
    /**
     * 分享结束
     *
     */
    private void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;            
        }
    }
    
    /** 
     * shareFinish
     */
    private void shareFinish() {

        recycle();
        SocialShare.clean();

    }

    /**
     * 分享成功
     */
    private void shareComplete() {
        LogUtil.d(TAG, "shareComplete");
//        Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
//                mContext.getText(R.string.bdsocialshare_success), Toast.LENGTH_SHORT).show();
        shareFinish();
    }

    /**
     * 分享失败
     */
    private void shareFail() {

        LogUtil.d(TAG, "SocialShare:IBaiduListener:shareFail()");
//        Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
//                mContext.getText(R.string.bdsocialshare_fail), Toast.LENGTH_SHORT).show();
        shareFinish();
    }


    /**
     * 从服务器获取数据
     * @param activity Context
     */
    private void executeRequestor(final Activity activity, final ShareContent shareContent) {
        ShareConfigRequestor requestor = new ShareConfigRequestor(mContext);
        LogUtil.d(TAG, "ShareConfigRequestor");
        if (System.currentTimeMillis() - AppUtils.getSericeConfigShareTime(mContext) > INTERVAL_TIME) {
            // 当快速点击时，进度条会出现多次，所以当进度条已显示时，不再创建进度条
            if (mProgressDialog == null || !mProgressDialog.isShowing()) {

                if (activity == null || activity.isFinishing()) {
                    LogUtil.d(TAG, "context.isFinishing() == true 1111");
                    return;
                } else if ((mShareType == SHARE_TYPE_APP || mShareType == SHARE_TYPE_WASH)
                        && !activity.hasWindowFocus()) {
                    LogUtil.d(TAG, "context.hasWindowFocus() == false");
                    return;
                }
                showProgressDialog(activity);
            }
            requestor.request(new AbstractRequestor.OnRequestListener() {

                @Override
                public void onSuccess(AbstractRequestor requestor) {
                    LogUtil.d(TAG, "ShareConfigRequestor:success");
                    dismissProgressDialog();
                    showShareWindow(activity, shareContent);
                }

                @Override
                public void onFailed(AbstractRequestor requestor, int errorCode) {
                    LogUtil.d(TAG, "ShareConfigRequestor:failed:" + errorCode);
                    dismissProgressDialog();
                    showShareWindow(activity, shareContent);
                }

            });
        } else {
            showShareWindow(activity, shareContent);
        }
    }

    /**
     * 更新指定第三方分享内容
     * @param shareContent 分享内容
     * @param media 分享第三方媒体
     * @return 更新后的分享内容
     * */

    public ShareContent shareSpecialMedia(ShareContent shareContent, String media) {
        if (!mUpdateDatabase) {
            return shareContent;
        }
        ShareContent content;
        if (media.equalsIgnoreCase(MediaType.WEIXIN_FRIEND.toString())) {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "weixin_friend");
        } else  if (media.equalsIgnoreCase(MediaType.WEIXIN_FRIEND.toString())) {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "weixin_timeline");
        } else  if (media.equalsIgnoreCase(MediaType.QQFRIEND.toString())) {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "qqfriend");
        } else  if (media.equalsIgnoreCase(MediaType.QZONE.toString())) {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "qqdenglu");
        } else if (media.equalsIgnoreCase(MediaType.SINAWEIBO.toString())) {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "weibo");
            mOtherForm = true;
        } else {
            content = mShareConfigDBHelper.queryServerConfigByType(mShareType, "all");
            mOtherForm = true;
        }

        if (content != null) {
            if (!TextUtils.isEmpty(content.getTitle())) {
                shareContent.setTitle(content.getTitle());
            }
            if (!TextUtils.isEmpty(content.getContent())) {
                shareContent.setContent(content.getContent());
            }
            if (!TextUtils.isEmpty(content.getLinkUrl())) {
                shareContent.setLinkUrl(content.getLinkUrl());
            }
            if (content.getImageUri() != null && !TextUtils.isEmpty(content.getImageUri().toString())) {
                shareContent.setImageUri(content.getImageUri());
            }
            if (content.getImageData() != null && !content.getImageData().isRecycled()) {
                shareContent.setImageData(content.getImageData());
            }
//            mForm = content.getShareContentType();
         // 为了分析分享SDK的相关Bug，而增加相应的log

            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getTitle():" + shareContent.getTitle());
            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getContent():" + shareContent.getContent());
            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getImageUri():" + shareContent.getImageUri());
            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getImageData():" + shareContent.getImageData());
            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getLinkUrl():" + shareContent.getLinkUrl());
            LogUtil.d(TAG, "ShareSpecialMedia shareContent.getShareContentType():" + mForm);
        }
        if (!TextUtils.isEmpty(mForm)) {
            if (mForm.equalsIgnoreCase("image")) {
                if (shareContent.getImageData() != null) {
                    shareContent.setImageUri(null);
                }
                shareContent.setWXMediaObjectType(WXMediaMessage.IMediaObject.TYPE_IMAGE);
                shareContent.setQQRequestType(MobileQQ.IQQReqestType.TYPE_IMAGE);
                shareContent.setQZoneRequestType(MobileQQ.IQZoneRequestType.TYPE_IMAGE);
            } else if (mForm.equalsIgnoreCase("url") || mForm.equalsIgnoreCase("default")) {
              // 幸运抽奖默认为大图分享，大图分享设置ImageData，URL分享设置ImageUri
                if (mShareType.equalsIgnoreCase(SHARE_TYPE_LOTTERY)
                        && mForm.equalsIgnoreCase("default")) {
                    if (shareContent.getImageData() != null && !shareContent.getImageData().isRecycled()) {
                        shareContent.setImageUri(null);
                    }
                } else {
                    if (shareContent.getImageUri() != null) {
                        shareContent.setImageData(null);
                    }
                }
                shareContent.setWXMediaObjectType(WXMediaMessage.IMediaObject.TYPE_URL);
                shareContent.setQQRequestType(MobileQQ.IQQReqestType.TYPE_DEFAULT);
                shareContent.setQZoneRequestType(MobileQQ.IQZoneRequestType.TYPE_DEFAULT);
            } else if (mForm.equalsIgnoreCase("text")) {
             // QQ默认分享形式为URL
                if (shareContent.getImageUri() != null) {
                    shareContent.setImageData(null);
                }
                if (mOtherForm) {
                    shareContent.setImageUri(null);
                    shareContent.setImageData(null);
                }
                shareContent.setWXMediaObjectType(WXMediaMessage.IMediaObject.TYPE_TEXT);
                shareContent.setQQRequestType(MobileQQ.IQQReqestType.TYPE_DEFAULT);
                shareContent.setQZoneRequestType(MobileQQ.IQZoneRequestType.TYPE_DEFAULT);
            }
        }
      // QQ空间要求分享图片最小尺寸为200*200，只有URL分享形式
        if (media.equalsIgnoreCase(MediaType.QZONE.toString())) {
            Bitmap bitmap = null;
            if (shareContent.getImageUri() != null
                    && !TextUtils.isEmpty(shareContent.getImageUri().toString())) {
                bitmap = getBitmapFromLocal(mContext, shareContent.getImageUri().toString());
            } else if (shareContent.getImageData() != null) {
                bitmap = shareContent.getImageData();
            }
            if (bitmap != null && (bitmap.getWidth() < MIN_LINE || bitmap.getHeight() < MIN_LINE)) {
                shareContent.setImageData(resizeImage(bitmap, MIN_LINE, MIN_LINE));
                shareContent.setImageUri(null);
            }
            LogUtil.d(TAG, "QZONE shareContent.getContent():" + shareContent.getContent());
            LogUtil.d(TAG, "QZONE shareContent.getImageUri():" + shareContent.getImageUri());
            LogUtil.d(TAG, "QZONE shareContent.getImageData():" + shareContent.getImageData());
        }
        return shareContent;
    }


    /**图片缩放
     * @param bitmap Bitmap
     * @param newWidth 图片宽度
     * @param newHeight 图片高度
     * @return Bitmap
     */
    public static Bitmap resizeImage(Bitmap bitmap, double newWidth, double newHeight) {
        Bitmap bitmapOrg = bitmap;
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }

    /**
     * 获取本地图片
     *
     * @param ctx
     *      Context
     * @param localImgUrl
     *      本地图片地址
     *
     * @return Bitmap
     */
    private Bitmap getBitmapFromLocal(Context ctx, String localImgUrl) {
        Bitmap bitmap = null;
        try {
            if (!TextUtils.isEmpty(localImgUrl)) {
                String name = ImageCacheUtils.hashKeyForDisk(localImgUrl);
                String path = ctx.getFilesDir().getPath() + File.separator + name;
                bitmap = BitmapFactory.decodeFile(path);
            }
        } catch (Exception e) {

            LogUtil.e(TAG, "Exception: " + e.toString());

            return null;
        }
        return bitmap;
    }
    /**
     * 拉取图片
     *
     * @param imgUrl
     *            图片拉取的 url
     */
    public void loadImage(String imgUrl) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = imageLoader.myDisplayImageOptions();
        DisplayImageOptions specialOptions = new DisplayImageOptions.Builder().cloneFrom(options).cacheInMemory(false)
                .cacheOnDisc(false).build();
        imageLoader.loadImage(imgUrl, specialOptions, new ImageLoaderListener(mContext, imgUrl));
    }

    /**
     * 图片拉取的imageLoader listener
     */
    static class ImageLoaderListener extends SimpleImageLoadingListener {
        /** Context */
        Context context = null;
        /** url*/
        String imageUrl;

        /**构造函数
         * @param ctx Context
         * @param url 图片地址
         */
        public ImageLoaderListener(Context ctx, String url) {
            super();
            context = ctx;
            imageUrl = url;
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            onFailed();
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            LogUtil.d(TAG, "onLoadingComplete: fetch image: " + imageUri);
            Utility.saveBitmapToHashKeyName(context, imageUri, loadedImage, null);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            onFailed();
        }

        /**
         * 加载失败的处理
         */
        public void onFailed() {

            LogUtil.e(TAG, "onFailed: fetch image failed.");

            // 拉取失败的情况下，如果之前有前一条规则缓存的图片，将它删掉
            String fileName = ImageCacheUtils.hashKeyForDisk(imageUrl);
            String path = context.getFilesDir().getPath() + File.separator + fileName;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }

    }

    /**
     * 判断图片是否存在
     *
     * @param ctx     Context
     * @param imgUrl  图片拉取的 url
     * @return Boolean  true为存在
     */
    public Boolean isImageLoaded(Context ctx, String imgUrl) {
        if (!TextUtils.isEmpty(imgUrl)) {
            String fileName = ImageCacheUtils.hashKeyForDisk(imgUrl);
            String path = ctx.getFilesDir().getPath() + File.separator + fileName;
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 未收到服务器返回的结果前显示进度条
     * @param ctx Activity
     */
    private void showProgressDialog(Activity ctx) {
        mProgressDialog = BDProgressDialog.show(ctx, null,
                mContext.getApplicationContext().getString(R.string.app_share_request), true);
    }

    /**
     * 取消进度条显示
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            SysMethodUtils.cancelDialog(mProgressDialog);
            mProgressDialog = null;
        }
    }


    /**
     * 实现分享回调的接口
     * 
     * @author
     * @since
     */

    private IBaiduListener mIBaiduListener = new IBaiduListener() {

        @Override
        public void onComplete(JSONObject data) {
            PluginTransferDataUtils.callback(Constant.ShareConstant.SHARE_COMPLETE_WITH_JSONOBJECT);
            shareComplete();

        }

        @Override
        public void onComplete(JSONArray data) {
            PluginTransferDataUtils.callback(Constant.ShareConstant.SHARE_COMPLETE_WITH_JSONARRAY);
            shareComplete();

        }

        @Override
        public void onComplete() {
            PluginTransferDataUtils.callback(Constant.ShareConstant.SHARE_COMPLETE_WITH_NULL);
            shareComplete();

        }

        @Override
        public void onCancel() {

            PluginTransferDataUtils.callback(Constant.ShareConstant.SHARE_CANCEL);
            shareFinish();

        }

        @Override
        public void onError(BaiduException ex) {

            LogUtil.d(TAG, "SocialShare:IBaiduListener:onError: " + ex.toString());
            PluginTransferDataUtils.callback(Constant.ShareConstant.SHARE_ERROR);
            shareFail();

        }

    };

}

