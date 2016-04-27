package com.baidu.share.core;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.cloudsdk.social.core.MediaType;
import com.baidu.cloudsdk.social.core.util.LayoutUtils;
import com.baidu.cloudsdk.social.share.SocialShareConfig;

import java.util.List;

/**
 * ListContentAdapter
 *
 * @author linjunwu
 * @since 2016/2/1
 */
class ListContentAdapter extends ArrayAdapter implements  MediaTypeAdapter {
    private SocialShareConfig mSocialShareConfig;

    public ListContentAdapter(Context context, List list) {
        super(context, 0, list);
        this.mSocialShareConfig = SocialShareConfig.getInstance(context);
    }

    public View getView(int pos, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        int layoutResId;
        if (view != null && view.getTag() != null) {
            viewHolder = (ViewHolder) view.getTag();
        } else {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(this.getContext().getApplicationContext());
            layoutResId = LayoutUtils.getLayoutResId(this.getContext(), "bdsocialshare_sharemenulistitem");
            view = layoutInflater.inflate(layoutResId, (ViewGroup) null);
            int bgResId = LayoutUtils.getBgResId(this.getContext(), "bdsocialshare_sharemenu_item_click");
            view.setBackgroundResource(bgResId);
            int resourceId = LayoutUtils.getResourceId(this.getContext(), "sharemenulist_iconview");
            viewHolder.mImageView = (ImageView) view.findViewById(resourceId);
            int resourceId1 = LayoutUtils.getResourceId(this.getContext(), "sharemenulist_icontext");
            viewHolder.mTextView = (TextView) view.findViewById(resourceId1);
            viewHolder.mTextView.setTextColor(Color.parseColor(LayoutUtils.getMediaTextColor(this.getContext())));
        }

        MediaType mediaType = (MediaType) this.getItem(pos);
        layoutResId = LayoutUtils.getResourceDrawable(this.getContext(), "bdsocialshare_" + mediaType.toString());
        viewHolder.mImageView.setImageResource(layoutResId);
        viewHolder.mTextView.setText(this.mSocialShareConfig.getString(mediaType.toString()));
        return view;
    }

    @Override
    public MediaType getMediaType(int pos) {
        return (MediaType) this.getItem(pos);
    }

    class ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        private ViewHolder() {
        }
    }
}
