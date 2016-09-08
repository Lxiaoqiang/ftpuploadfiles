package com.ftp.ftpmutipuploadfile.ftp;


/**
 * Created by lihuiqiang
 *
 * @date 2016/9/7 9:07
 */
public class UploadTask{



    public void upload(final String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FTP().uploadFiles(path,"lhq");

            }
        }).start();
    }
}
