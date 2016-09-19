package com.ftp.ftpmutipuploadfile.ftp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

import com.ftp.ftpmutipuploadfile.activity.MainActivity;
import com.ftp.ftpmutipuploadfile.ftp.constant.FTPConfig;
import com.ftp.ftpmutipuploadfile.utils.Logger;


/**
 * Created by lihuiqiang
 *
 * @date 2016/9/5 11:01
 */
public class FTP {
    /**
     * 服务器名.
     */
    private String hostName;

    /**
     * 端口号
     */
    private int serverPort;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    public FTP() {
        this.hostName = "192.168.2.161";
        this.serverPort = 21;
        this.userName = "lhq";
        this.password = "123456";
        this.ftpClient = new FTPClient();
    }

    /**
     * @param localPath  本地文件路径
     * @param remotePath 服务器文件存储路径
     */
    public void uploadFiles(String localPath, String remotePath) {
        RandomAccessFile raf = null;
        OutputStream output = null;
        try {
            openConnect();
            // FTP下创建文件夹
            ftpClient.makeDirectory(remotePath);
            // 改变FTP目录
            ftpClient.changeWorkingDirectory(remotePath);

            File localFile = new File(localPath);
            long localFileSize = localFile.length();
            if (!localFile.exists()) {
                Log.i("lhq", "文件不存在");
                return;
            }
            String localFileName = localFile.getName();
            //获取服务器文件
            FTPFile[] files = ftpClient.listFiles(localFileName);
            if (files == null) {
                return;
            }
            long serFileSize = 0;
            if (files.length == 0) {
                Log.i("lhq", "服务器不存在此文件");
            } else {
                serFileSize = files[0].getSize();
                Log.i("lhq", "服务器已经存在此文件");
                System.out.println("服务器文件存在 文件大小为" + serFileSize);
                System.out.println("本地文件存在 文件大小为" + localFileSize);
            }

            raf = new RandomAccessFile(localFile, "r");
            // 进度
            long step = localFileSize / 100;
            long process = 0;
            long currentSize = 0;
            // 好了，正式开始上传文件
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            ftpClient.setRestartOffset(serFileSize);
            raf.seek(serFileSize);
            output = ftpClient.appendFileStream(localFileName);
            byte[] b = new byte[1024];
            int length = 0;
            while ((length = raf.read(b)) != -1) {
                output.write(b, 0, length);
                currentSize = currentSize + length + serFileSize;
                if (currentSize / step != process) {
                    process = currentSize / step;
                    if (process % 10 == 0) {
                        System.out.println("上传进度：" + process);
                    }
                }
            }

            closeConnect();

            if (ftpClient.completePendingCommand()) {
                System.out.println("文件上传成功");
            } else {
                System.out.println("文件上传失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("lhq", "文件上传异常");
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
                if (raf != null)
                    raf.close();

                closeConnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // -------------------------------------------------------文件上传方法------------------------------------------------

    /**
     * 上传单个文件.
     * <p/>
     * 本地文件
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadSingleFile(File singleFile, String remotePath,
                                 UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;
        flag = uploadingSingle(singleFile, listener);
        if (flag) {
            listener.onUploadProgress(MainActivity.FTP_UPLOAD_SUCCESS, 0,
                    singleFile);
        } else {
            listener.onUploadProgress(MainActivity.FTP_UPLOAD_FAIL, 0,
                    singleFile);
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传多个文件.
     * <p/>
     * 本地文件
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadMultiFile(LinkedList<File> fileList, String remotePath,
                                UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        for (File singleFile : fileList) {
            flag = uploadingSingle(singleFile, listener);
            if (flag) {
                listener.onUploadProgress(MainActivity.FTP_UPLOAD_SUCCESS, 0,
                        singleFile);
            } else {
                listener.onUploadProgress(MainActivity.FTP_UPLOAD_FAIL, 0,
                        singleFile);
            }
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传单个文件.
     *
     * @param localFile 本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile,
                                    UploadProgressListener listener) throws IOException {
        boolean flag = true;
        // 不带进度的方式
        // // 创建输入流
        // InputStream inputStream = new FileInputStream(localFile);
        // // 上传单个文件
        // flag = ftpClient.storeFile(localFile.getName(), inputStream);
        // // 关闭文件流
        // inputStream.close();


        // 带有进度的方式
        BufferedInputStream buffIn = new BufferedInputStream(
                new FileInputStream(localFile));
        ProgressInputStream progressInput = new ProgressInputStream(buffIn,
                listener, localFile);
        flag = ftpClient.storeFile(localFile.getName(), progressInput);
        buffIn.close();

        return flag;
    }

    public void uploadContinueMultiFile(LinkedList<File> fileList, String remotePath,
                                        UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        for (File singleFile : fileList) {
            flag = uploadContinueFile(singleFile, singleFile.getName(), listener);
//            if (flag) {
//                listener.onUploadProgress(MainActivity.FTP_UPLOAD_SUCCESS, 0,
//                        singleFile);
//            } else {
//                listener.onUploadProgress(MainActivity.FTP_UPLOAD_FAIL, 0,
//                        singleFile);
//            }
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    private boolean uploadContinueFile(File localFile, String serverFileName,
                                       UploadProgressListener listener) throws IOException {
        boolean flag;

        FTPFile[] files = ftpClient.listFiles(serverFileName);

        // 带有进度的方式
        BufferedInputStream buffIn = new BufferedInputStream(
                new FileInputStream(localFile));
        ProgressInputStream progressInput = new ProgressInputStream(buffIn,
                listener, localFile);
        Logger.i("lhq", "files" + files.length);

        if (files != null && files.length > 0) {
            long remoteFileSize = 0L;

            //如果服务器存在文件，获取到服务器上文件的大小
            remoteFileSize = files[0].getSize();

            if (localFile != null && localFile.length() <= remoteFileSize) {
                listener.onUploadProgress(FTPConfig.FPT_FILE_UPLOADED,0,localFile);
                Logger.i("lhq", "服务器已经上传此文件");
                return false;
            }
            long a = progressInput.skip(remoteFileSize);
            Logger.i("lhq","server file aready exit,skip "+ a);
            ftpClient.setRestartOffset(remoteFileSize);
        } else {
            Logger.i("lhq", "未上传此文件");
        }

        flag = ftpClient.storeFile(localFile.getName(), progressInput);
        buffIn.close();

        return flag;
    }

    /**
     * 上传文件之前初始化相关参数
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    private void uploadBeforeOperate(String remotePath,
                                     UploadProgressListener listener) throws IOException {

        // 打开FTP服务
        try {
            this.openConnect();
            listener.onUploadProgress(MainActivity.FTP_CONNECT_SUCCESSS, 0,
                    null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onUploadProgress(MainActivity.FTP_CONNECT_FAIL, 0, null);
            return;
        }

        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // FTP下创建文件夹
        ftpClient.makeDirectory(remotePath);
        // 改变FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        // 上传单个文件

    }

    /**
     * 上传完成之后关闭连接
     *
     * @param listener
     * @throws IOException
     */
    private void uploadAfterOperate(UploadProgressListener listener)
            throws IOException {
        this.closeConnect();
        listener.onUploadProgress(MainActivity.FTP_DISCONNECT_SUCCESS, 0, null);
    }

    // -------------------------------------------------------文件下载方法------------------------------------------------

    /**
     * 下载单个文件，可实现断点下载.
     *
     * @param serverPath Ftp目录及文件路径
     * @param localPath  本地目录
     * @param fileName   下载之后的文件名称
     * @param listener   监听器
     * @throws IOException
     */
    public void downloadSingleFile(String serverPath, String localPath, String fileName, DownLoadProgressListener listener)
            throws Exception {

        // 打开FTP服务
        try {
            this.openConnect();
            listener.onDownLoadProgress(MainActivity.FTP_CONNECT_SUCCESSS, 0, null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDownLoadProgress(MainActivity.FTP_CONNECT_FAIL, 0, null);
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onDownLoadProgress(MainActivity.FTP_FILE_NOTEXISTS, 0, null);
            return;
        }

        //创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }

        localPath = localPath + fileName;
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            if (localSize >= serverSize) {
                File file = new File(localPath);
                file.delete();
            }
        }

        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        // 开始准备下载文件
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        InputStream input = ftpClient.retrieveFileStream(serverPath);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 5 == 0) {  //每隔%5的进度返回一次
                    listener.onDownLoadProgress(MainActivity.FTP_DOWN_LOADING, process, null);
                }
            }
        }
        out.flush();
        out.close();
        input.close();

        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand()) {
            listener.onDownLoadProgress(MainActivity.FTP_DOWN_SUCCESS, 0, new File(localPath));
        } else {
            listener.onDownLoadProgress(MainActivity.FTP_DOWN_FAIL, 0, null);
        }

        // 下载完成之后关闭连接
        this.closeConnect();
        listener.onDownLoadProgress(MainActivity.FTP_DISCONNECT_SUCCESS, 0, null);

        return;
    }

    // -------------------------------------------------------文件删除方法------------------------------------------------

    /**
     * 删除Ftp下的文件.
     *
     * @param serverPath Ftp目录及文件路径
     * @param listener   监听器
     * @throws IOException
     */
    public void deleteSingleFile(String serverPath, DeleteFileProgressListener listener)
            throws Exception {

        // 打开FTP服务
        try {
            this.openConnect();
            listener.onDeleteProgress(MainActivity.FTP_CONNECT_SUCCESSS);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDeleteProgress(MainActivity.FTP_CONNECT_FAIL);
            return;
        }

        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onDeleteProgress(MainActivity.FTP_FILE_NOTEXISTS);
            return;
        }

        //进行删除操作
        boolean flag = true;
        flag = ftpClient.deleteFile(serverPath);
        if (flag) {
            listener.onDeleteProgress(MainActivity.FTP_DELETEFILE_SUCCESS);
        } else {
            listener.onDeleteProgress(MainActivity.FTP_DELETEFILE_FAIL);
        }

        // 删除完成之后关闭连接
        this.closeConnect();
        listener.onDeleteProgress(MainActivity.FTP_DISCONNECT_SUCCESS);

        return;
    }

    // -------------------------------------------------------打开关闭连接------------------------------------------------

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(hostName, serverPort);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient
                    .setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 退出FTP
            ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
        }
    }

    // ---------------------------------------------------上传、下载、删除监听---------------------------------------------

    /*
     * 上传进度监听
     */
    public interface UploadProgressListener {
        public void onUploadProgress(String currentStep, long uploadSize, File file);
    }

    /*
     * 下载进度监听
     */
    public interface DownLoadProgressListener {
        public void onDownLoadProgress(String currentStep, long downProcess, File file);
    }

    /*
     * 文件删除监听
     */
    public interface DeleteFileProgressListener {
        public void onDeleteProgress(String currentStep);
    }
}
