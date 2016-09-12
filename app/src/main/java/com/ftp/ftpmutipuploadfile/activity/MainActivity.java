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
import com.ftp.ftpmutipuploadfile.ftp.FTP;
import com.ftp.ftpmutipuploadfile.ftp.UploadTask;
import com.ftp.ftpmutipuploadfile.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
    public static final String FTP_CONNECT_FAIL = "ftp连接失败";
    public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
    public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";

    public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
    public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
    public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";

    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";

    public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
    public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";

    @BindView(R.id.btn_choice)
    Button choice;

    @BindView(R.id.btn_upload)
    Button upload;

    @BindView(R.id.iv_test)
    ImageView iv;

    @BindView(R.id.gv_show)
    GridView gv;


    NineImageAdapter adapter;

    LinkedList<File> linkedList = new LinkedList<>();
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
            linkedList.add(new File(PhotoShowAdapter.globalImagePath.get(i)));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    new FTP().uploadMultiFile(linkedList, "lhq", new FTP.UploadProgressListener() {
//                        @Override
//                        public void onUploadProgress(String currentStep, long uploadSize, File file) {
//
//                        }
//                    });

                    new FTP().uploadContinueMultiFile(linkedList, "lhq", new FTP.UploadProgressListener() {
                        @Override
                        public void onUploadProgress(String currentStep, long uploadSize, File file) {
                            Logger.i("lhq","currentStep="+currentStep+"----uploadSize"+uploadSize);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
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
