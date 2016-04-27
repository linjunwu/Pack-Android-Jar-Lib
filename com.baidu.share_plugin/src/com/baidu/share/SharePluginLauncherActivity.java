package com.baidu.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Toast;

import com.baidu.android.gporter.hostapi.HostUtil;
import com.baidu.cloudsdk.social.core.util.MobileQQ;
import com.baidu.cloudsdk.social.share.ShareContent;
import com.baidu.cloudsdk.social.share.handler.WXMediaMessage;
import com.baidu.share.core.Constant;
import com.baidu.share.core.ShareManager;
import com.baidu.share.utils.LogUtil;
import com.baidu.shareplugin.R;

import java.io.File;


public class SharePluginLauncherActivity extends Activity {

    private static final String TAG = "SharePluginLauncherActivity";

    private boolean mShowed = false;
    private boolean mCanShareAgain = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onDestroy() {
        mCanShareAgain = true;
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Toast.makeText(HostUtil.getHostApplicationContext(this).getApplicationContext(),
                getText(R.string.bdsocialshare_cancel_share),
                Toast.LENGTH_SHORT).show();
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        // 因为分享界面是继承popwindow实现的，所以必须在activity已经attach到window之后才能调用分享的接口，
        // 而onWindowFocusChanged方法通过是否有焦点可以判断activity已经attach到window，所以将分享接口调用放于
        // 此回调接口中

        if (hasFocus) {
            LogUtil.d(TAG, "hasFocus");
            if (!mShowed) {

                if (mCanShareAgain) {
                    mCanShareAgain = false;
                } else {
                    return;
                }
//                ShareContent shareContent = new ShareContent(
//                        "百度开发中心",
//                        "欢迎使用百度社会化分享组件，相关问题请邮件dev_support@baidu.com",
//                        "http://developer.baidu.com/");

                // 获取从host应用传递过来的分享参数
                String title = getIntent().getStringExtra(Constant.ShareContextKey.KEY_TITLE);
                String content = getIntent().getStringExtra(Constant.ShareContextKey.KEY_CONTENT);
                String imageUrl = getIntent().getStringExtra(Constant.ShareContextKey.KEY_IMAGE_URL);
                String linkUrl = getIntent().getStringExtra(Constant.ShareContextKey.KEY_LINK_URL);
                String shareType = getIntent().getStringExtra(Constant.ShareContextKey.KEY_SHARE_TYPE);
                String form = getIntent().getStringExtra(Constant.ShareContextKey.KEY_FORM);
                int from = getIntent().getIntExtra(Constant.ShareContextKey.KEY_FROM, 0);
                String bitmap = getIntent().getStringExtra(Constant.ShareContextKey.KEY_BITMAP);
                boolean updateDatabase = getIntent().getBooleanExtra(
                        Constant.ShareContextKey.KEY_UPDATEDATABASE, false);


                LogUtil.d(TAG, Constant.ShareContextKey.KEY_TITLE + ":" + title
                        + "," + Constant.ShareContextKey.KEY_CONTENT + ":" + content
                        + "," + Constant.ShareContextKey.KEY_IMAGE_URL + ":" + imageUrl
                        + "," + Constant.ShareContextKey.KEY_LINK_URL + ":" + linkUrl
                        + "," + Constant.ShareContextKey.KEY_SHARE_TYPE + ":" + shareType
                        + "," + Constant.ShareContextKey.KEY_FORM + ":" + form
                        + "," + Constant.ShareContextKey.KEY_FROM + ":" + from
                        + "," + Constant.ShareContextKey.KEY_BITMAP + ":" + bitmap
                        + "," + Constant.ShareContextKey.KEY_UPDATEDATABASE + ":" + updateDatabase);

                LogUtil.d(TAG, "!mShowed");
                mShowed = true;
                ShareContent shareContent = new ShareContent();
                shareContent.setTitle(title != null ? title : "");
                shareContent.setContent(content != null ? content : "");

                if (bitmap != null && !"".equals(bitmap) && (new File(bitmap).exists())) {
//                    BitmapFactory.Options opts = new BitmapFactory.Options();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        opts.inMutable = true;
//                    } else {
//                        opts.inPurgeable = true;
//                    }
//                    opts.inSampleSize = 2;
                    shareContent.setImageData(BitmapFactory.decodeFile(bitmap));
                    File file = new File(bitmap);
                    file.delete();
                    /**插件模式下微信分享时会遇到一个强转的问题，就是当微信平台分享的类型为图片，而这时候
                     * shareContent的ImageData为null，ImageUri不为null的是否，SDK底下会去做一个解码图片
                     * 的动作，这时候传入的Context是host的Context，无法强转为Activity*/
//                    shareContent.setImageUri(Uri.parse(file.getAbsolutePath()));
                    shareContent.setWXMediaObjectType(WXMediaMessage.IMediaObject.TYPE_IMAGE);
                    shareContent.setQZoneRequestType(MobileQQ.IQZoneRequestType.TYPE_DEFAULT);
                    shareContent.setQQRequestType(MobileQQ.IQQReqestType.TYPE_IMAGE);
                } else {
                    shareContent.setImageData(null);
                }
                if (!TextUtils.isEmpty(imageUrl)) {
                    shareContent.setImageUri(Uri.parse(imageUrl));
                }
                shareContent.setLinkUrl(linkUrl != null ? linkUrl : "");
                ShareManager.getInstance(SharePluginLauncherActivity.this)
                        .showShareContent(SharePluginLauncherActivity.this, shareContent,
                                shareType, form, from, updateDatabase);
            } else {
                LogUtil.d(TAG, "mShowed");
//                finish();
            }

        }
    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify getMediaType parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


}
