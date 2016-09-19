package com.ftp.ftpmutipuploadfile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.ftp.entity.ImageFloder;

import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 16:14
 */
public class FileDirAdapter extends BaseAdapter {

    private Context mContext;
    private List<ImageFloder> mImageFloders;

    public FileDirAdapter(Context context, List<ImageFloder> imageFloders) {
        mContext = context;
        mImageFloders = imageFloders;
    }

    @Override
    public int getCount() {
        return mImageFloders != null ? mImageFloders.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mImageFloders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.list_dir_item, null);
        ImageView icon = (ImageView) view.findViewById(R.id.id_dir_item_image);
        ImageView select = (ImageView) view.findViewById(R.id.iv_item_choose);
        TextView path = (TextView) view.findViewById(R.id.id_dir_item_name);
        TextView count = (TextView) view.findViewById(R.id.id_dir_item_count);
        ImageFloder entity = mImageFloders.get(i);
        if (entity.isImage()){
            Glide.with(mContext).load(entity.getFirstImagePath()).into(icon);
        }else{
            icon.setImageResource(R.mipmap.ic_movice);
        }
        path.setText(entity.getName());
        count.setText(entity.getCount() + "");

        return view;
    }
}
