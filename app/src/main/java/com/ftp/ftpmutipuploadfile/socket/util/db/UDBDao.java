package com.ftp.ftpmutipuploadfile.socket.util.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ftp.ftpmutipuploadfile.socket.util.entity.FileInfo;


public class UDBDao {
	private UDBOpenHelper mHelper;

	public UDBDao(Context context) {
		//mHelper = UDBOpenHelper.getInstanceDBHelper(context);
		mHelper = new UDBOpenHelper(context);
	}

	public synchronized void insert(FileInfo info) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("state", info.getState());
		values.put("uploadSize", info.getUploadSize());
		values.put("url", info.getUrl());
		values.put("maxSize", info.getMaxSize());
		values.put("resourceId", info.getResourceId());
		db.insert("upload", null, values);

		db.close();
	}
	/**
	 * 将文件的URL作为唯一的标识
	 * */
	public synchronized void delete(String url){
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("delete from upload where url = ?",new String[]{url});
		db.close();
	}
	/**
	 * 循环遍历的时候数据库会lock上，可能原因：访问次数过于频繁（有待确认）
	 * */
	public synchronized void update(FileInfo info){
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("update upload set state = ?, uploadSize = ?,maxSize = ? ,resourceId = ? where url = ?",new Object[]{info.getState(),info.getUploadSize(),info.getMaxSize(),info.getResourceId(),info.getUrl()});
		db.close();
	}
	public synchronized FileInfo selectOne(String url){
		FileInfo info = new FileInfo();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from upload where url = ?",new String[]{url});
		while(c.moveToNext()){
			info.setMaxSize(c.getInt(c.getColumnIndex("maxSize")));
			info.setState(c.getInt(c.getColumnIndex("state")));
			info.setUploadSize(c.getInt(c.getColumnIndex("uploadSize")));
			info.setUrl(c.getString(c.getColumnIndex("url")));
			info.setResourceId(c.getString(c.getColumnIndex("resourceId")));
		}
		return info;
	}
	public synchronized  List<FileInfo> selectAll() {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from upload",null);
		List<FileInfo> list = new ArrayList<FileInfo>();
		while (c.moveToNext()) {
			FileInfo info = new FileInfo();
			info.setUploadSize(c.getInt(c.getColumnIndex("uploadSize")));
			info.setUrl(c.getString(c.getColumnIndex("url")));
			info.setMaxSize(c.getInt(c.getColumnIndex("maxSize")));
			info.setState(c.getInt(c.getColumnIndex("state")));
			info.setResourceId(c.getString(c.getColumnIndex("resourceId")));
			list.add(info);
		}
		c.close();
		db.close();
		return list;
	}
}

