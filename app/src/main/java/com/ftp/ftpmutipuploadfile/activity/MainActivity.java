package com.ftp.ftpmutipuploadfile.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baozi.Zxing.CaptureActivity;
import com.bumptech.glide.Glide;
import com.ftp.ftpmutipuploadfile.R;
import com.ftp.ftpmutipuploadfile.adapter.NineImageAdapter;
import com.ftp.ftpmutipuploadfile.adapter.PhotoShowAdapter;
import com.ftp.ftpmutipuploadfile.ftp.FTP;
import com.ftp.ftpmutipuploadfile.ftp.UploadTask;
import com.ftp.ftpmutipuploadfile.socket.util.StreamTool;
import com.ftp.ftpmutipuploadfile.socket.util.db.UDBDao;
import com.ftp.ftpmutipuploadfile.socket.util.entity.FileInfo;
import com.ftp.ftpmutipuploadfile.utils.CustomToast;
import com.ftp.ftpmutipuploadfile.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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

    @BindView(R.id.gv_show)
    GridView gv;

    @BindView(R.id.tv_result)
    TextView result;

    NineImageAdapter adapter;
    private UDBDao dao;

    String dstAddress;
    int dstPort;
    LinkedList<File> linkedList = new LinkedList<>();

//    private ExecutorService mExecutorService;
//    private static final int CORE_POOL_SIZE = 5;
//    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
//        private final AtomicInteger mCount = new AtomicInteger(1);
//
//        public Thread newThread(Runnable r) {
//            return new Thread(r, "MobileShow thread #"
//                    + mCount.getAndIncrement());
//        }
//    };
//    /**
//     * Return an ExecutorService (global to the entire application) that may be
//     * used by clients when running long tasks in the background.
//     *
//     * @return An ExecutorService to used when processing long running tasks
//     */
//    public ExecutorService getExecutor() {
//        if (mExecutorService == null) {
//            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,
//                    sThreadFactory);
//        }
//        return mExecutorService;
//    }

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(9);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        dao = new UDBDao(this);
        adapter = new NineImageAdapter(this, PhotoShowAdapter.globalImagePath);
        gv.setAdapter(adapter);
//        mExecutorService = getExecutor();
    }

    @OnClick(R.id.btn_scan)
    void doScan() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 2);
    }

    @OnClick(R.id.btn_upload)
    void upload() {
        if (!TextUtils.isEmpty(dstAddress) && dstPort != 0 && PhotoShowAdapter.globalImagePath != null && PhotoShowAdapter.globalImagePath.size()>0) {
            for (int i = 0; i < PhotoShowAdapter.globalImagePath.size(); i++) {
//            linkedList.add(new File(PhotoShowAdapter.globalImagePath.get(i)));
//                MyClientTask task = new MyClientTask(dstAddress, dstPort, PhotoShowAdapter.globalImagePath.get(i));
//                task.execute();
                fixedThreadPool.execute(new UploadThread(dstAddress,dstPort,PhotoShowAdapter.globalImagePath.get(i)));
            }
        } else {
            CustomToast.showToast(this, "先扫描二维码获取IP地址和端口号和选择图片", 2000);
        }

    }
    class UploadThread implements Runnable{
        String mPath;
        public UploadThread(String dstAddress, int dstPort, String mPath){
            this.mPath = mPath;
        }
        @Override
        public void run() {
            doUpload(dstAddress,dstPort,mPath);
        }
    }

    @OnClick(R.id.btn_choice)
    void selectImage() {
        Intent intent = new Intent(this, FileListActivity.class);
        startActivityForResult(intent, 1);
    }

    private void doUpload(String addr, int port, String filePath){
        FileInfo mFileInfo = new FileInfo();
        String souceid = dao.selectOne(filePath)
                .getResourceId();
        String head = "Content-Length="
                + filePath.length() + ";filename="
                + filePath.substring(filePath.lastIndexOf("/"), filePath.length()) + ";sourceid="
                + (souceid == null ? "" : souceid) + "\r\n";

        Socket socket = null;
        PushbackInputStream inStream = null;
        RandomAccessFile fileOutStream = null;
        OutputStream outStream = null;
        try {
            //连接socket
            socket = new Socket(dstAddress, dstPort);
            outStream = socket.getOutputStream();
            //向服务器写数据，供服务器查询是否上传过此文件
            outStream.write(head.getBytes());
            //接收服务器返回数据
            inStream = new PushbackInputStream(
                    socket.getInputStream());
            String response = StreamTool.readLine(inStream);
            String[] items = response.split(";");
            //sourceId,文件是否上传过的标识
            String responseid = items[0].substring(items[0]
                    .indexOf("=") + 1);

            Log.i("lhq", "response==========" + response);
            //文件上传了多少
            String position = items[1].substring(items[1].indexOf("=") + 1);
            if (souceid == null) // 代表原来没有上传过此文件，往数据库添加一条绑定记录
            {
                mFileInfo.setResourceId(responseid);
                dao.insert(mFileInfo);
            } else {
                if (Long.valueOf(position) == new File(filePath).length()) {
                    Log.i("lhq", "已经上传过" + filePath);
                    return;
                }
            }

            mFileInfo.setMaxSize((int) new File(filePath)
                    .length());
            fileOutStream = new RandomAccessFile(
                    new File(filePath), "r");
            fileOutStream.seek(Integer.valueOf(position));

            byte[] buffer = new byte[1024];
            int len = -1;
            int length = Integer.valueOf(position);
            while ((len = fileOutStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
                length += len;
                mFileInfo.setUploadSize(length);
                // 放入全局的map，用于更新下载界面的UI
//                    MyApplication.getMap().put(fileInfo.getUrl(), fileInfo);
                dao.update(mFileInfo);
            }
            System.out.println("总文件大小:================"
                    + new File(filePath).length());
            System.out.println("已经上传的文件大小:======================"
                    + mFileInfo.getUploadSize());


            if (length == mFileInfo.getMaxSize()) {//上传完成
                dao.delete(mFileInfo.getUrl());
//                    MyApplication.getMap().remove(fileInfo.getUrl());
//                    service.stopSelf();

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutStream != null)
                    fileOutStream.close();
                if (outStream != null)
                    outStream.close();
                if (inStream != null)
                    inStream.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        FileInfo mFileInfo;
        String mPath;

        MyClientTask(String addr, int port, String filePath) {
            dstAddress = addr;
            dstPort = port;
            mFileInfo = new FileInfo();
            mPath = filePath;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            doUpload(dstAddress,dstPort,mPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    // 显示扫描到的内容
                    String str = data.getStringExtra("result");
                    String[] ipAndPort = str.split(",");
                    if (ipAndPort.length > 1) {
                        dstAddress = ipAndPort[0];
                        dstPort = Integer.valueOf(ipAndPort[1]);
                    }
                    result.setText(str);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
