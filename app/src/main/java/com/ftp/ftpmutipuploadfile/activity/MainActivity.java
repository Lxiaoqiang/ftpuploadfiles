package com.ftp.ftpmutipuploadfile.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.adapter.NineImageAdapter;
import com.ftp.ftpmutipuploadfile.adapter.PhotoShowAdapter;
import com.ftp.ftpmutipuploadfile.ftp.UploadTask;
import com.ftp.ftpmutipuploadfile.utils.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.btn_choice)
    Button choice;

    @BindView(R.id.btn_upload)
    Button upload;

    @BindView(R.id.iv_test)
    ImageView iv;

    @BindView(R.id.gv_show)
    GridView gv;


    NineImageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        queryAllVideo();
        adapter = new NineImageAdapter(this, PhotoShowAdapter.globalImagePath);
        gv.setAdapter(adapter);
    }

    @OnClick(R.id.btn_upload)
    void upload(){
        for (int i = 0;i<PhotoShowAdapter.globalImagePath.size();i++){
            UploadTask task = new UploadTask();
            task.upload(PhotoShowAdapter.globalImagePath.get(i));
        }
    }
    @OnClick(R.id.btn_choice)
    void selectImage(){
        Intent intent = new Intent(this,FileListActivity.class);
        startActivityForResult(intent,1);
    }
    Bitmap thumbnail;
    public void queryAllVideo() {
        ContentResolver mContentResolver = MainActivity.this
                .getContentResolver();
        Cursor cursor = null;
        try {
            //查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
            cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null ,null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)); //获取唯一id
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)); //文件路径
                    String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)); //文件名
                    //...   还有很多属性可以设置
                    //可以通过下一行查看属性名，然后在Video.Media.里寻找对应常量名
                    Log.i("lhq", "queryAllImage --- all column name --- " + cursor.getColumnName(cursor.getPosition()));
                    Logger.i("lhq","path="+path+",id="+id+",filename="+fileName);
                    //获取缩略图（如果数据量大的话，会很耗时——需要考虑如何开辟子线程加载）
                /*
                 * 可以访问android.provider.MediaStore.Video.Thumbnails查询图片缩略图
                 * Thumbnails下的getThumbnail方法可以获得图片缩略图，其中第三个参数类型还可以选择MINI_KIND
                 */
                    thumbnail = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
                    if (thumbnail == null) {
                        Logger.i("lhq", "bitmap 是空的");
                    }else{
                        Logger.i("lhq", "bitmap 不 是空的");

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        iv.setImageBitmap(thumbnail);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case 1:
                    adapter.notifyDataSetChanged();
//                    if (data!= null) {
//                        String[] strs = data.getStringArrayExtra("mList");
//
//                        List<String> list = Arrays.asList(strs);
//                                String parentDir = data.getStringExtra("parentDir");
//                        MyAdapter adapter = new MyAdapter(MainActivity.this,list,R.layout.grid_item,parentDir);
//                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
