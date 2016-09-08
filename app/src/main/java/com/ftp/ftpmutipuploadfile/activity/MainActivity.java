package com.ftp.ftpmutipuploadfile.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;

import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.adapter.MyAdapter;
import com.ftp.ftpmutipuploadfile.utils.FileHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.btn_choice)
    Button choice;
    @BindView(R.id.btn_upload)
    Button upload;


    @BindView(R.id.gv_show)
    GridView gv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btn_upload)
    void upload(){

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
                    if (data!= null) {
                        String[] strs = data.getStringArrayExtra("mList");

                        List<String> list = Arrays.asList(strs);
                                String parentDir = data.getStringExtra("parentDir");
                        MyAdapter adapter = new MyAdapter(MainActivity.this,list,R.layout.grid_item,parentDir);
                        gv.setAdapter(adapter);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
