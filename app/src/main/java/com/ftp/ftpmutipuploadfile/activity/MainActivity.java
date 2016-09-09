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
