package com.ftp.ftpmutipuploadfile.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import com.ftp.ftpmutipuploadfile.ftp.entity.ImageFloder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/7 14:34
 */
public class FileHelper {
    private ContentResolver cr;

    private static FileHelper fileHelper;

    int totalCount = 0;
    private Context mContext;
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
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();
    /**
     * 临时的辅助类，用于防止同一个文件夹的多次扫描
     */
    private HashSet<String> mDirPaths = new HashSet<String>();

    private FileHelper(Context context){
        mContext = context;
        cr = mContext.getContentResolver();
    }

    /**
     * 获取单例
     * @param context
     * @return
     */
    public static FileHelper getInstance(Context context){
        if (fileHelper == null)
            return new FileHelper(context);
        return fileHelper;
    }

    public void showImageAndMovie(){
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String columns[] = new String[] { MediaStore.Audio.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        Cursor mCursor = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
                null);
        while (mCursor.moveToNext()){
            int photoIDIndex = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int photoPathIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int photoNameIndex = mCursor.getColumnIndexOrThrow(Media.DISPLAY_NAME);
            int photoTitleIndex = mCursor.getColumnIndexOrThrow(Media.TITLE);
            int photoSizeIndex = mCursor.getColumnIndexOrThrow(Media.SIZE);
            int bucketDisplayNameIndex = mCursor
                    .getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int picasaIdIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.PICASA_ID);

            String _id = mCursor.getString(photoIDIndex);
            String name = mCursor.getString(photoNameIndex);
            String path = mCursor.getString(photoPathIndex);
            String title = mCursor.getString(photoTitleIndex);
            String size = mCursor.getString(photoSizeIndex);
            String bucketName = mCursor.getString(bucketDisplayNameIndex);
            String bucketId = mCursor.getString(bucketIdIndex);
            String picasaId = mCursor.getString(picasaIdIndex);
            Logger.i("lhq","_id"+_id+"---name"+name+
                    "---path"+path
                    +"---title"+title
                    +"---size"+size
                    +"---bucketName"+bucketName
                    +"---bucketId"+bucketId
                    +"---picasaId"+picasaId);

        }


    }
}
