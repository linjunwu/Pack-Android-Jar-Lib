/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.share.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.shareplugin.R;

// CHECKSTYLE:OFF
// 从系统ProgressDialog源码转过来的，不做CheckStyle了

/**
 * <p>A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.</p>
 * <p>The dialog can be made cancelable on back key press.</p>
 * <p>The progress range is 0..10000.</p>
 */
public class BDProgressDialog extends Dialog {
    
    private ProgressBar mProgress;
    private TextView mMessageView;
    
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    
    private int mMax;
    private int mProgressVal;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    
    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    
    public BDProgressDialog(Context context) {
        this(context, R.style.libui_BDTheme_Dialog_Noframe);
    }

    public BDProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static BDProgressDialog show(Context context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, false, null, false);
    }

    public static BDProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean cancelable) {
        return show(context, title, message, cancelable, null, false);
    }

    public static BDProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean cancelable, OnCancelListener cancelListener, boolean showProgressStep) {
        BDProgressDialog dialog = new BDProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.setShowProgressStep(showProgressStep);
        dialog.show();
        return dialog;
    }

    public static BDProgressDialog show(Context context, CharSequence message) {
        BDProgressDialog dialog = new BDProgressDialog(context);
        dialog.setTitle(null);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        dialog.setShowProgressStep(false);
        dialog.setProgressBarVisibility(View.GONE);
        dialog.show();
        return dialog;
    }

    @Override
    @SuppressLint("WrongConstant")
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (isShowProgressStep) {
            
            /* Use a separate handler to update the text views as they
             * must be updated on the same thread that created them.
             */
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    
                    /* Update the number and percent */
                    int progress = getProgress();
                    int max = getMax();
                    String format = mProgressNumberFormat;
                    mProgressNumber.setText(String.format(format, progress, max));
                    mProgressNumber.setVisibility(View.VISIBLE);
                }
            };
        }
        
        View view = inflater.inflate(R.layout.bd_progress_dialog, null);
        mProgress = (ProgressBar) view.findViewById(R.id.ProgressT);
        mProgress.setVisibility(mProgressBarVisibility);
        mProgressNumber = (TextView) view.findViewById(R.id.txt_progress_message);
        mProgressNumberFormat = "( %d/%d )";
        mMessageView = (TextView) view.findViewById(R.id.txt_message);
        setContentView(view);
        
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    /** 是否显示进度 */
    private int mProgressBarVisibility = View.VISIBLE;

    public void setProgressBarVisibility(int v) {
        mProgressBarVisibility = v;
    }

    public void setProgress(int value) {
        mProgressVal = value;
        if (mHasStarted) {
            onProgressChanged();
        }
    }

    public int getProgress() {
        return mProgressVal;
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        mMax = max;
        if (mProgress != null) {
            onProgressChanged();
        }
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
            if (message == null) { 
                mMessageView.setVisibility(View.GONE);
            } else {
                mMessageView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Change the format of Progress Number. The default is "current/max".
     * Should not be called during the number is progressing.
     * @param format Should contain two "%d". The first is used for current number
     * and the second is used for the maximum.
     * @hide
     */
    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
    }
    
    private void onProgressChanged() {
        if (isShowProgressStep) {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
    
    /** 是否显示进度 */
    private boolean isShowProgressStep;

    /**
     * @param isShowProgressStep the isShowProgressStep to set
     */
    public void setShowProgressStep(boolean isShowProgressStep) {
        this.isShowProgressStep = isShowProgressStep;
    }
    
}
// CHECKSTYLE:ON
