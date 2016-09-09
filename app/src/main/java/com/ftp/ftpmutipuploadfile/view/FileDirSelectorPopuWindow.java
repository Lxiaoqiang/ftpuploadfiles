package com.ftp.ftpmutipuploadfile.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.adapter.FileDirAdapter;
import com.ftp.ftpmutipuploadfile.entity.ImageFloder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 16:21
 */
public class FileDirSelectorPopuWindow extends PopupWindow {


    private ListView lv;
    private FileDirAdapter adapter;
    private List<ImageFloder> mList = new ArrayList<>();
    public FileDirSelectorPopuWindow(Context context,View view,int width,int height){
        super(view,width,height,true);

//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7));

        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
                {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        lv = (ListView) view.findViewById(R.id.id_list_dir);
        adapter = new FileDirAdapter(context,mList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (onDirSelectedListener != null)
                    onDirSelectedListener.selected(((ImageFloder)adapterView.getAdapter().getItem(i)));
            }
        });
    }

    public void addList(List<ImageFloder> list){
        mList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private OnDirSelectedListener onDirSelectedListener;

    public void setOnDirSelectedListener(OnDirSelectedListener onDirSelectedListener) {
        this.onDirSelectedListener = onDirSelectedListener;
    }

    public interface OnDirSelectedListener {
        void selected(ImageFloder floder);
    }
}
