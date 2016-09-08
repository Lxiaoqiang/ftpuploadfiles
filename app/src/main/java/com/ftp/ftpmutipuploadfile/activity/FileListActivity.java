package com.ftp.ftpmutipuploadfile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.adapter.PhotoShowAdapter;
import com.ftp.ftpmutipuploadfile.entity.ImageFloder;
import com.ftp.ftpmutipuploadfile.utils.Logger;
import com.ftp.ftpmutipuploadfile.view.FileDirSelectorPopuWindow;

import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileListActivity extends Activity{

    private ProgressDialog mProgressDialog;

    /**
     * 存储文件夹中的图片数量
     */
    private int mPicsSize;
    /**
     * 图片数量最多的文件夹
     */
    private File mImgDir;
    /**
     * 所有的图片
     */
    private List<String> mImgs;

    private GridView mGirdView;
    /**
     * 临时的辅助类，用于防止同一个文件夹的多次扫描
     */
    private HashSet<String> mDirPaths = new HashSet<String>();

    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();


    private RelativeLayout mBottomLy;

    private TextView mChooseDir;
    private TextView mImageCount;
    int totalCount = 0;

    private int mScreenHeight;


    PhotoShowAdapter adapter;

    private FileDirSelectorPopuWindow popuWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        ButterKnife.bind(this);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;

        initView();


        getImages();
        initEvent();

    }


    @OnClick(R.id.tv_complete)
    void complete() {
        onBack();
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mProgressDialog.dismiss();
            // 为View绑定数据
            data2View();
            // 初始化展示文件夹的popupWindw
            initListDirPopupWindw();
        }
    };

    /**
     * 为View绑定数据
     */
    private void data2View() {
        if (mImgDir == null) {
            Toast.makeText(getApplicationContext(), "擦，一张图片没扫描到",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mImgs = Arrays.asList(mImgDir.list());
        /**
         * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
         */
        adapter = new PhotoShowAdapter(getApplicationContext(), mImgDir.getAbsolutePath(), mImgs);
        mGirdView.setAdapter(adapter);
//        mAdapter = new MyAdapter(getApplicationContext(), mImgs,
//                R.layout.grid_item, mImgDir.getAbsolutePath());
        mImageCount.setText(totalCount + "张");
    }

    ;

    /**
     * 初始化展示文件夹的popupWindw
     */
    private void initListDirPopupWindw() {
        popuWindow = new FileDirSelectorPopuWindow(this,LayoutInflater.from(this).inflate(R.layout.list_dir,null),LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),mImageFloders);
        popuWindow.setOnDirSelectedListener(new FileDirSelectorPopuWindow.OnDirSelectedListener() {
            @Override
            public void selected(ImageFloder floder) {
                mImgDir = new File(floder.getDir());

                mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.endsWith(".jpg") || filename.endsWith(".png")
                                || filename.endsWith(".jpeg")) {
                            return true;
                        }
                        return false;
                    }
                }));

                adapter = new PhotoShowAdapter(getApplicationContext(),mImgDir.getAbsolutePath(),mImgs);
                mGirdView.setAdapter(adapter);
                /**
                 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
                 */
//        mAdapter = new MyAdapter(getApplicationContext(), mImgs,
//                R.layout.grid_item, mImgDir.getAbsolutePath());
//        mGirdView.setAdapter(mAdapter);
                // mAdapter.notifyDataSetChanged();
                mImageCount.setText(floder.getCount() + "张");
                mChooseDir.setText(floder.getName());
                popuWindow.dismiss();
            }
        });

        popuWindow .setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });

    }



    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
     */
    private void getImages() {

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }
        // 显示进度条
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new Thread(new Runnable() {
            @Override
            public void run() {

                String firstImage = null;

                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = FileListActivity.this
                        .getContentResolver();

                // 只查询jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);

                Log.e("TAG", mCursor.getCount() + "");
                while (mCursor.moveToNext()) {

                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    Log.e("TAG", path);
                    // 拿到第一张图片的路径
                    if (firstImage == null)
                        firstImage = path;
                    // 获取该图片的父路径名
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    String dirPath = parentFile.getAbsolutePath();
                    ImageFloder imageFloder = null;
                    // 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        // 初始化imageFloder
                        imageFloder = new ImageFloder();
                        imageFloder.setDir(dirPath);
                        imageFloder.setFirstImagePath(path);

                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if (filename.endsWith(".jpg")
                                    || filename.endsWith(".png")
                                    || filename.endsWith(".jpeg"))
                                return true;
                            return false;
                        }
                    }).length;
                    totalCount += picSize;

                    imageFloder.setCount(picSize);
                    mImageFloders.add(imageFloder);

                    if (picSize > mPicsSize) {
                        mPicsSize = picSize;
                        mImgDir = parentFile;
                    }
                }
                mCursor.close();

                // 扫描完成，辅助的HashSet也就可以释放内存了
                mDirPaths = null;

                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(0x110);

            }
        }).start();

    }

    /**
     * 初始化View
     */
    private void initView() {
        mGirdView = (GridView) findViewById(R.id.id_gridView);
        mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
        mImageCount = (TextView) findViewById(R.id.id_total_count);

        mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);

    }

    private void initEvent() {
        /**
         * 为底部的布局设置点击事件，弹出popupWindow
         */
        mBottomLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popuWindow .setAnimationStyle(R.style.anim_popup_dir);
                popuWindow.showAsDropDown(mBottomLy, 0, 0);

                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = .3f;
                getWindow().setAttributes(lp);
//                mListImageDirPopupWindow
//                        .setAnimationStyle(R.style.anim_popup_dir);
//                mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);
//
//                // 设置背景颜色变暗
//                WindowManager.LayoutParams lp = getWindow().getAttributes();
//                lp.alpha = .3f;
//                getWindow().setAttributes(lp);
            }
        });
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void onBack() {
        Intent intent = new Intent();
        if (mImgDir != null && mImgs != null && mImgs.size() > 0) {
            intent.putExtra("parentDir", mImgDir.getAbsolutePath());
            String[] strs = (String[]) mImgs.toArray();
            intent.putExtra("mList", strs);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
    }
}
