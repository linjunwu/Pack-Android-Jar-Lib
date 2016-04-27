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
import com.baidu.share.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * GridContentAdapter
 *
 * @author linjunwu
 * @since 2016/2/1
 */
class GridContentAdapter extends ArrayAdapter implements MediaTypeAdapter {
    private static final String TAG = GridContentAdapter.class.getSimpleName();
    private List arrayList = new ArrayList();

    public GridContentAdapter(Context context, List list) {
        super(context, 0, list);

        this.arrayList = list;

    }

    public int getCount() {
        return this.arrayList.size();
    }

    @Override
    public MediaType getMediaType(int pos) {
        return (MediaType) this.arrayList.get(pos);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int id;
        if (convertView != null && convertView.getTag() != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(this.getContext().getApplicationContext());
            id = LayoutUtils.getLayoutResId(this.getContext(), "bdsocialshare_sharemenugriditem");
            convertView = layoutInflater.inflate(id, (ViewGroup) null);
            int resourceId = LayoutUtils.getResourceId(this.getContext(), "sharemenugrid_iconview");
            viewHolder.mImageView = (ImageView) convertView.findViewById(resourceId);
            int resourceId1 = LayoutUtils.getResourceId(this.getContext(), "sharemenugrid_icontext");
            viewHolder.mTextView = (TextView) convertView.findViewById(resourceId1);
            viewHolder.mTextView.setTextColor(Color.parseColor(LayoutUtils.getMediaTextColor(this.getContext())));
        }

        MediaType mediaType = this.getMediaType(position);
        id = LayoutUtils.getResourceDrawable(this.getContext(), "bdsocialshare_" + mediaType.toString());
        viewHolder.mImageView.setImageResource(id);
        LogUtil.d(TAG, mediaType.toString());
        viewHolder.mTextView.setText(LayoutUtils.getResourceString(this.getContext(),
                "bdsocialshare_" + mediaType.toString()));
        return convertView;
    }

    class ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        private ViewHolder() {
        }
    }
}
