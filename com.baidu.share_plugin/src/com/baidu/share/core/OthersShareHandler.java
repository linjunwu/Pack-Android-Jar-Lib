package com.baidu.share.core;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.android.gporter.hostapi.HostUtil;
import com.baidu.shareplugin.R;
import com.baidu.cloudsdk.BaiduException;
import com.baidu.cloudsdk.IBaiduListener;
import com.baidu.cloudsdk.common.imgloader.AsyncImageLoader;
import com.baidu.cloudsdk.common.imgloader.ImageManager;
import com.baidu.cloudsdk.common.util.GetTimgTask;
import com.baidu.cloudsdk.common.util.Utils;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.cloudsdk.social.share.SocialShareConfig;
import com.baidu.share.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * OthersShareHandler
 *
 * @author linjunwu
 * @since 2016/2/3
 */
public class OthersShareHandler {

    private static final String TAG = "OthersShareHandler";
    private Context mContext;
    private IBaiduListener mIBaiduListener;
    private ShareContent mShareContent;

    public OthersShareHandler(Context context, IBaiduListener iBaiduListener, ShareContent shareContent) {
        this.mContext = context;
        this.mIBaiduListener = iBaiduListener;
        this.mShareContent = shareContent;
    }

    public void share() {
        Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
                mContext.getText(R.string.bdsocialshare_pls_waiting), Toast.LENGTH_SHORT)
                .show();
        Uri imageUri = mShareContent.getImageUri();
        if ((imageUri != null) && (!Utils.isUrl(imageUri))) {
            LogUtil.d(TAG, "(imageUri != null) && (!Utils.isUrl(imageUri)");
            doShare(imageUri);
        } else if (imageUri != null) {
            LogUtil.d(TAG, "imageUri != null");
            Uri imageUriTmp = imageUri;
            if (SocialShareConfig.getInstance(mContext).getInt("timg") == 1) {
                imageUriTmp = Uri.parse(GetTimgTask.getTimgString(imageUri.toString()));
            }
            ImageManager.getInstance().loadImage(mContext,
                    imageUriTmp, new CustomAsyncImageLoaderListener(imageUriTmp));
        } else {
            doShare(null);
        }
    }

    protected void doShare(Uri paramUri) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, 0);
        if (!resolveInfos.isEmpty()) {
            ArrayList<Intent> intents = new ArrayList();
            Object object1 = resolveInfos.iterator();
            Object object2;
            while (((Iterator) object1).hasNext()) {
                object2 = (ResolveInfo) ((Iterator) object1).next();
                ActivityInfo activityInfo = ((ResolveInfo) object2).activityInfo;

                LogUtil.d(TAG, activityInfo.packageName + "   " + activityInfo.name);
                List noSupportedPackages = SocialShareConfig.getInstance(mContext).getNoSupportedPackages();
                if ((noSupportedPackages != null) && (noSupportedPackages.size() != 0)
                        && (noSupportedPackages.contains(activityInfo.packageName))) {
                    continue;
                }
                Intent intentTmp;
                if (activityInfo.packageName.equalsIgnoreCase("com.android.mms")) {
                    intentTmp = getSmsIntent(paramUri);
                } else if (activityInfo.packageName.equalsIgnoreCase("com.android.email")) {
                    intentTmp = getEmailIntent(paramUri);
                } else {
                    intentTmp = getOthersIntent(paramUri, activityInfo.packageName, activityInfo.name);
                }
                intents.add(intentTmp);
            }
            object1 = SocialShareConfig.getInstance(mContext).getString("chooser_title");
            if (!intents.isEmpty()) {
                object2 = Intent.createChooser((Intent) intents.get(0), (CharSequence) object1);
                ((Intent) object2).putExtra("android.intent.extra.INITIAL_INTENTS",
                        (Parcelable[]) intents.toArray(new Parcelable[0]));
                try {
                    ((Activity) mContext).startActivity((Intent) object2);
                } catch (ActivityNotFoundException localActivityNotFoundException) {
                    onStartLocalShareFailed("no_others", mIBaiduListener);
                }
            } else {
                onStartLocalShareFailed("no_others", mIBaiduListener);
            }
        } else {
            onStartLocalShareFailed("no_others", mIBaiduListener);
        }
    }

    /**
     * 获取Sms的Intent
     * @param uri 资源uri
     * @return
     */
    protected Intent getSmsIntent(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder(mShareContent.getContent());
        String linkUrl = mShareContent.getLinkUrl();
        if (!TextUtils.isEmpty(linkUrl)) {
            stringBuilder.append("\r\n").append(linkUrl);
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage("com.android.mms");
        intent.putExtra("sms_body", stringBuilder.toString());
        if (uri != null) {
            intent.setType("image/png");
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.putExtra("android.intent.extra.TEXT", stringBuilder.toString());
        } else {
            intent.setAction("android.intent.action.SENDTO");
            intent.setData(Uri.parse("smsto:"));
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.sonyericsson.conversations",
                    "com.sonyericsson.conversations.ui.ConversationListActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.huawei.message", "com.hotalk.ui.chat.singleChat.SingleChatActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.google.android.talk", "com.google.android.apps.babel.phone.ShareIntentActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.lewa.PIM", "com.lewa.PIM.mms.ui.ComposeMessageActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.android.contacts", "com.android.mms.ui.ComposeMessageActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.lenovo.ideafriend", "com.lenovo.ideafriend.mms.android.ui.ComposeMessageActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setClassName("com.android.contacts", "com.android.mms.ui.ComposeMessageActivity");
        } else {
            return intent;
        }
        if (!Utils.isActivityExist(mContext, intent)) {
            intent.setPackage(null);
            intent.setComponent(null);
        }
        return intent;
    }

    /**
     * 获取Email的Intent
     * @param uri 资源uri
     * @return
     */
    protected Intent getEmailIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage("com.android.email");
        intent.setType("message/rfc822");
        intent.putExtra("android.intent.extra.SUBJECT", mShareContent.getTitle());
        String emailBody = mShareContent.getEmailBody();
        if (Build.VERSION.SDK_INT >= 16) {
            intent.putExtra("android.intent.extra.TEXT", emailBody);
        } else {
            intent.putExtra("android.intent.extra.TEXT", Html.fromHtml(emailBody));
        }
        if (uri != null) {
            intent.putExtra("android.intent.extra.STREAM", uri);
        }
        return intent;
    }

    /**
     * 获取其他具有分享的Intent
     * @param uri 资源uri
     * @param packageName 包名
     * @param componentName 组件名
     * @return
     */
    protected Intent getOthersIntent(Uri uri, String packageName, String componentName) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage(packageName);
        if (!TextUtils.isEmpty(componentName)) {
            intent.setClassName(packageName, componentName);
        }
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.SUBJECT", mShareContent.getTitle());
        if (packageName.equalsIgnoreCase("com.android.bluetooth")) {
            intent.putExtra("android.intent.extra.TEXT", mShareContent.getLinkUrl());
        } else {
            StringBuilder stringBuilder = new StringBuilder(mShareContent.getContent());
            String linkUrl = mShareContent.getLinkUrl();
            if (!TextUtils.isEmpty(linkUrl)) {
                stringBuilder.append("\r\n").append(linkUrl);
            }
            intent.putExtra("android.intent.extra.TEXT", stringBuilder.toString());
        }
        if (uri != null) {
            intent.setType("image/png");
            intent.putExtra("android.intent.extra.STREAM", uri);
        }
        return intent;
    }

    /**
     * 分享失败处理
     * @param errcode
     * @param iBaiduListener
     */
    protected void onStartLocalShareFailed(String errcode, IBaiduListener iBaiduListener) {
        SocialShareConfig socialShareConfig = SocialShareConfig.getInstance(mContext);
        String errcodeText = socialShareConfig.getString(errcode);
        Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
                errcodeText, Toast.LENGTH_SHORT).show();
        if (iBaiduListener != null) {
            iBaiduListener.onError(new BaiduException("start local app for share failed, errcode: " + errcode));
        }
    }

    /**
     * 自定义异步图片加载监听器
     */
    class CustomAsyncImageLoaderListener implements AsyncImageLoader.IAsyncImageLoaderListener {
        private Uri mUri;

        public CustomAsyncImageLoaderListener(Uri uri) {
            this.mUri = uri;
        }

        @Override
        public void onComplete(Bitmap bitmap) {
            if ((bitmap != null) && (!bitmap.isRecycled())) {
                String str = ImageManager.getInstance().getCachedFilePath(this.mUri);
                OthersShareHandler.this.doShare(Uri.fromFile(new File(str)));
            } else {
                OthersShareHandler.this.doShare(null);
            }
        }
    }
}
