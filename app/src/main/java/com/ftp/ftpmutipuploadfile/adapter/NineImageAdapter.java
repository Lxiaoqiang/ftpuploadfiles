package com.ftp.ftpmutipuploadfile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ftp.ftpmutipuploadfile.R;

import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 15:20
 */
public class NineImageAdapter extends BaseAdapter {

    private List<String> mList;
    private Context mContext;
    public NineImageAdapter(Context context,List<String> list){

        mList = list;
        mContext = context;
    }
    @Override
    public int getCount() {
        return mList != null ? mList.size():0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.nineiamge_item_layout,null);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_item_bg);
        Glide.with(mContext).load(mList.get(i)).into(iv);
        return view;
    }
}
