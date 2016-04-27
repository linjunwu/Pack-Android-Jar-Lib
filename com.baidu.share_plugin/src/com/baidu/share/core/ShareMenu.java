package com.baidu.share.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.android.gporter.hostapi.HostUtil;
import com.baidu.appsearch.config.StatisticConstants;
import com.baidu.appsearch.util.Utility;
import com.baidu.cloudsdk.IBaiduListener;
import com.baidu.cloudsdk.common.util.Utils;
import com.baidu.cloudsdk.social.core.MediaType;
import com.baidu.cloudsdk.social.core.SocialConfig;
import com.baidu.cloudsdk.social.core.util.LayoutUtils;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.cloudsdk.social.share.SocialShare;
import com.baidu.cloudsdk.social.share.SocialShareConfig;
import com.baidu.share.utils.AppUtils;
import com.baidu.share.utils.LogUtil;
import com.baidu.shareplugin.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * ShareMenu
 *
 * @author linjunwu
 * @since 2016/2/1
 */
class ShareMenu extends PopupWindow implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "ShareMenu";
    /**QQ包名*/
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";
    /**微信包名*/
    private static final String WEIXIN_PACKAGE = "com.tencent.mm";
    /** 分享来源，通过titlebar上的按钮分享 */
    public static final int SHARE_FROM_TITLEBAR = 1;

    private IBaiduListener mIBaiduListener;
    private String mShareType;
    private int mFrom;
    private ShareContent mShareContent;
    private Context mContext;
    private List<MediaType> mListContent;
    private int mId;

    public ShareMenu(final Activity activity, ShareContent shareContent,
                     IBaiduListener iBaiduListener,
                     String shareType, int from) throws IllegalArgumentException {
        super(activity);

        this.mContext = activity;
        this.mShareContent = shareContent;
        this.mIBaiduListener = iBaiduListener;
        this.mShareType = shareType;
        mFrom = from;
        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                activity.finish();
            }
        });

        this.setBackgroundDrawable((Drawable) null);

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        int animationStyleResId = LayoutUtils.getAnimationStyleResId(activity, "bdsocialsahre_sharemenu_animation");
        if (animationStyleResId != 0) {
            this.setAnimationStyle(animationStyleResId);
        }

        LayoutInflater layoutInflater = LayoutInflater.from(activity.getApplicationContext());
        this.mListContent = SocialShareConfig.getInstance(activity).getSupportedMediaTypes();
        List<MediaType> excludedMediaTypes = SocialShareConfig.getInstance(activity).getExcludedMediaTypesInShareMenu();
        Iterator iterator = excludedMediaTypes.iterator();
        while (iterator.hasNext()) {
            MediaType mediaType = (MediaType) iterator.next();
            this.mListContent.remove(mediaType);
        }

        if (Utils.isEmpty(this.mListContent)) {
            throw new IllegalArgumentException("config item for [supported_medias] shouldn\'t be empty");
        } else {
            int tmpValue;
            View view;
            int layoutResId;
            int layoutResId2;
            if (this.mListContent.size() < 5) {
                layoutResId = LayoutUtils.getLayoutResId(activity, "bdsocialshare_sharemenulistlayout");
                view = layoutInflater.inflate(layoutResId, (ViewGroup) null);
                this.setContentView(view);
                int resourceId = LayoutUtils.getResourceId(activity, "sharemenulistview");
                ListView listView = (ListView) view.findViewById(resourceId);
                listView.setAdapter(new ListContentAdapter(activity, mListContent));
                listView.setCacheColorHint(0);
                tmpValue = LayoutUtils.getBgResId(activity, "bdsocialshare_list_divider");
                listView.setDivider(activity.getApplicationContext().getResources().getDrawable(tmpValue));
                listView.setOnItemClickListener(this);
            } else {
                layoutResId2 = LayoutUtils.getLayoutResId(activity, "bdsocialshare_sharemenugridlayout");
                view = layoutInflater.inflate(layoutResId2, null);
                this.setContentView(view);


                layoutResId2 = LayoutUtils.getResourceId(activity, "sharemenugridview");
                GridView gridView = (GridView) view.findViewById(layoutResId2);
                gridView.setAdapter(new GridContentAdapter(activity, this.mListContent));
                gridView.setCacheColorHint(0);
                gridView.setOnItemClickListener(this);

            }

            layoutResId = LayoutUtils.getResourceId(activity, "sharemenulistrootlayout");
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(layoutResId);
            this.setFocusable(true);
            relativeLayout.setOnClickListener(this);
            relativeLayout.setFocusableInTouchMode(true);
            relativeLayout.setOnKeyListener(new MenuKeyListener(this));

            layoutResId2 = LayoutUtils.getResourceId(activity, "sharemenulistlinearlayout");
            LinearLayout linearLayout = (LinearLayout) view.findViewById(layoutResId2);
            linearLayout.setBackgroundColor(Color.parseColor(LayoutUtils.getMenuBackgroundString(activity)));

            this.mId = LayoutUtils.getResourceId(activity, "sharemenulistcancelbutton");
            Button button = (Button) view.findViewById(this.mId);
            button.setText(LayoutUtils.getResourceString(activity, "bdsocialshare_cancel"));
            button.setTextColor(Color.parseColor(LayoutUtils.getMenuCancelTextColor(activity)));
            int bgResId2 = LayoutUtils.getBgResId(activity, "bdsocialshare_sharemenu_cancelbutton");
            button.setBackgroundResource(bgResId2);
            button.setOnClickListener(this);
        }


    }

    public void show(View view, ShareContent shareContent) {

        this.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == this.mId) {
            this.dismiss();
        } else {
            this.dismiss();
        }
        Toast.makeText(HostUtil.getHostApplicationContext(mContext).getApplicationContext(),
                mContext.getText(R.string.bdsocialshare_cancel_share),
                Toast.LENGTH_SHORT).show();
        mIBaiduListener.onCancel();
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int pos, long id) {
        MediaType mediaType = ((MediaTypeAdapter) adapterView.getAdapter()).getMediaType(pos);
        LogUtil.d(TAG, "onItemClick:" + mediaType.toString());

        Context hostContext = HostUtil.getHostApplicationContext(mContext).getApplicationContext();

        if ((mediaType.equals(MediaType.QQFRIEND) || mediaType.equals(MediaType.QZONE))
                && !AppUtils.isAppPackageInstalled(mContext.getApplicationContext(),
                    QQ_PACKAGE)) {
//            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo ra : run) {
//                LogUtil.d(TAG, "name:" + ra.processName + ",pid:" + ra.pid + ",importance:"
//                + ra.importance);
//            }
            Toast.makeText(mContext,
                    mContext.getApplicationContext().getString(R.string.share_no_qq_toast),
                    Toast.LENGTH_SHORT).show();

            LogUtil.d(TAG, "qq not install");
            return ;
        }

        if ((mediaType.equals(MediaType.WEIXIN_FRIEND) || mediaType.equals(MediaType.WEIXIN_TIMELINE))
                && !AppUtils.isAppPackageInstalled(mContext.getApplicationContext(),
                    WEIXIN_PACKAGE)) {
            Toast.makeText(hostContext,
                    mContext.getApplicationContext().getString(R.string.share_no_weixin_toast),
                    Toast.LENGTH_SHORT).show();

            LogUtil.d(TAG, "weixin not install");
            return ;
        }

        /*mShareContent = */ShareManager.getInstance(mContext).shareSpecialMedia(mShareContent, mediaType.toString());
        SocialShare social = SocialShare.getInstance(hostContext);

        if (mediaType.equals(MediaType.WEIXIN_TIMELINE) && mShareContent.getImageData() != null) {
                 // 微信图片分享大小限制为1MB
            if (mShareContent.getImageData() != null) {
                mShareContent.setImageData(imageZoom(mShareContent.getImageData()));
            }
        }

        if (hostContext == mContext) {
            LogUtil.d(TAG, "hostContext == mContext");
        } else {
            LogUtil.d(TAG, "hostContext != mContext");
        }

        SocialConfig.clean();
        if (mediaType.equals(MediaType.WEIXIN_FRIEND) || mediaType.equals(MediaType.WEIXIN_TIMELINE)) {
            SocialConfig.getInstance(hostContext);
            LogUtil.d(TAG, SocialConfig.getInstance(hostContext).getClientId(mediaType));
            social.setContext(hostContext);
            social.share(mShareContent, mediaType.toString().toString(), mIBaiduListener);
        } else if (mediaType.equals(MediaType.OTHERS)) {
            LogUtil.d(TAG, "OTHERS SHARE.");
            SocialConfig.getInstance(hostContext);
            LogUtil.d(TAG, SocialConfig.getInstance(mContext).getClientId(mediaType));
            OthersShareHandler othersShareHandler = new OthersShareHandler(mContext, mIBaiduListener, mShareContent);
            othersShareHandler.share();
        } else {
            SocialConfig.getInstance(hostContext);
            LogUtil.d(TAG, SocialConfig.getInstance(mContext).getClientId(mediaType));
            social.setContext(mContext.getApplicationContext());
            social.share(mShareContent, mediaType.toString().toString(), mIBaiduListener);
        }

        if (mShareType.equalsIgnoreCase(ShareManager.SHARE_TYPE_APP)) {
            Utility.addOnlyValueUEStatisticCache(mContext, StatisticConstants.UEID_0111556,
                    mediaType.toString());
        }

        if (mFrom == SHARE_FROM_TITLEBAR) {
            Utility.addOnlyValueUEStatisticCache(mContext,
                    StatisticConstants.UEID_017707, mediaType.toString());
        }


        this.dismiss();
    }

    /***
     * 压缩图片到指定大小
     *
     * @param bitMap 源图片
     * @return  Bitmap 压缩图片
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Bitmap imageZoom(Bitmap bitMap) {
        // 图片允许最大空间1MB,单位：KB
        double maxSize = 900.00; // SUPPRESS CHECKSTYLE
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // SUPPRESS CHECKSTYLE
        byte[] bytes = baos.toByteArray();
        double mid = bytes.length / 1024; // SUPPRESS CHECKSTYLE
        int options = 100;  // SUPPRESS CHECKSTYLE
        int j = 0;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        while (mid > maxSize) {

            Log.e(TAG, "imageZoom size:(kb) " + mid);
            if (j > 10) { // SUPPRESS CHECKSTYLE
                break;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                opts.inBitmap = bitMap;
                opts.inMutable = true;
            } else {
                opts.inPurgeable = true;
            }
            opts.inSampleSize = 2;
            bitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            baos.reset();
            bitMap.compress(Bitmap.CompressFormat.JPEG, options, baos); // SUPPRESS CHECKSTYLE
            bytes = baos.toByteArray();
            mid = bytes.length / 1024; // SUPPRESS CHECKSTYLE
            j++;
        }
        try {
            baos.close();
        } catch (IOException e) {
            LogUtil.printException("e:", e);
        }
        return bitMap;
    }
}
