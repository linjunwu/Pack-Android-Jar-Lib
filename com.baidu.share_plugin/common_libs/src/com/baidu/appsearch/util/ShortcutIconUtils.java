package com.baidu.appsearch.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

/**
 * 生成桌面icon的工具类, 从系统源码中扒的代码 ,
 * com.android.launcher3.Utilities
 * 
 * @author chenzhiqin
 * @since 2015/7/7
 */
public final class ShortcutIconUtils {

    /** 桌面icon的宽 */
    private static int sIconWidth = -1;
    /** 桌面icon的高 */
    private static int sIconHeight = -1;


    /**
     * 构造方法
     */
    private ShortcutIconUtils() {
        
    }
    
    /**
     * 初始化参数
     * @param context Context
     */
    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        sIconWidth = sIconHeight;
    }

    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an
     * @param icon Bitmap
     * @param context Context
     * @return Bitmap
     */
    public static Bitmap createIconBitmap(Bitmap icon, Context context) {
        if (sIconWidth == -1) {
            initStatics(context);
        }
        if (sIconWidth == icon.getWidth() && sIconHeight == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), icon), context);
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     *
     * @param icon    Drawable
     * @param context Context
     * @return Bitmap
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        Rect oldBounds = new Rect();
        Canvas canvas = new Canvas();
        if (sIconWidth == -1) {
            initStatics(context);
        }

        int width = sIconWidth;
        int height = sIconHeight;

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
        }
        int sourceWidth = icon.getIntrinsicWidth();
        int sourceHeight = icon.getIntrinsicHeight();
        if (sourceWidth > 0 && sourceHeight > 0) {
            // Scale the icon proportionally to the icon dimensions
            final float ratio = (float) sourceWidth / sourceHeight;
            if (sourceWidth > sourceHeight) {
                height = (int) (width / ratio);
            } else if (sourceHeight > sourceWidth) {
                width = (int) (height * ratio);
            }
        }

        // no intrinsic size --> use default size
        int textureWidth = sIconWidth;
        int textureHeight = sIconHeight;

        final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(bitmap);

        final int left = (textureWidth - width) / 2;
        final int top = (textureHeight - height) / 2;

        oldBounds.set(icon.getBounds());
        icon.setBounds(left, top, left + width, top + height);
        icon.draw(canvas);
        icon.setBounds(oldBounds);
        // 4.0 以下的手机，setBitmap不能设置null
        try {
            canvas.setBitmap(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }


}
