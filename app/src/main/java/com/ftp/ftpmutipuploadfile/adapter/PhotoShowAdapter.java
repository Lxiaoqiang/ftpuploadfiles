package com.ftp.ftpmutipuploadfile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.utils.CustomToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 14:04
 */
public class PhotoShowAdapter extends BaseAdapter{

    private Context mContext;
    private List<String> mList;
    private String mParentFilePath;
    /**
     * 全局存储用来上传的图片的地址
     */
    public static final List<String> globalImagePath = new ArrayList<>();

    public PhotoShowAdapter(Context context,String parentFile,List<String> list){
        mContext = context;
        mList = list;
        mParentFilePath = parentFile;
    }
    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0 ;
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        final String fileName = mList.get(i);
        Glide.with(mContext).load(mParentFilePath+"/"+fileName).into(holder.image);

        if (globalImagePath.contains(mParentFilePath + "/"+fileName))
        {
            holder.select.setImageResource(R.mipmap.pictures_selected);
            holder.image.setColorFilter(Color.parseColor("#77000000"));
        }
        else
        {
            holder.select.setImageResource(R.mipmap.picture_unselected);
            holder.image.setColorFilter(null);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (globalImagePath.contains(mParentFilePath + "/"+fileName))
                {
                    if (globalImagePath.size()< 9) {
                        globalImagePath.remove(mParentFilePath + "/" + fileName);
                        holder.select.setImageResource(R.mipmap.picture_unselected);
                        holder.image.setColorFilter(null);
                    }else{
                        CustomToast.showToast(mContext,"最多只能选择9张上传",2000);
                    }
                }
                else
                {
                    globalImagePath.add(mParentFilePath + "/"+fileName);
                    holder.select.setImageResource(R.mipmap.pictures_selected);
                    holder.image.setColorFilter(Color.parseColor("#77000000"));
                }
            }
        });

        return view;
    }

    class ViewHolder{
        public ViewHolder(View view){
            select = (ImageButton) view.findViewById(R.id.id_item_select);
            image = (ImageView) view.findViewById(R.id.id_item_image);
        }
        ImageButton select;
        ImageView image;
    }
}
