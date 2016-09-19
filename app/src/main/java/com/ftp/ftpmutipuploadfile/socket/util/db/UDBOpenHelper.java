package com.ftp.ftpmutipuploadfile.socket.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UDBOpenHelper extends SQLiteOpenHelper{

	private final static String dbName = "uploadInfo.db";
	/**
	 * 版本号
	 * */
	private final static int VERSION = 1;

	/**
	 *  state--下一步是开始上传还是暂停的标志
	 * 	uploadSize--已经上传的文件的大小
	 * 	maxSize--文件的大小
	 * 	url--文件在本地的路径
	 * 	resourceId--服务器判断是否上传过的标识字段
	 * */
	private final String sql = "create table upload(_id integer primary key autoincrement,state integer,uploadSize integer,maxSize integer,url text,resourceId text)";
	//"create table thread_info(_id integer primary key autoincrement,"
	//+ "thread_id integer,url text,start integer,end integer,finished integer)"
	public UDBOpenHelper(Context context) {
		super(context, dbName, null, VERSION);
	}
	private static UDBOpenHelper sHelper;
	/**
	 * 单例模式
	 * @param context
	 * @return
	 */
	public static UDBOpenHelper getInstanceDBHelper(Context context)
	{
		//提高效率
		if(sHelper == null)
		{
			//同步锁
			synchronized (UDBOpenHelper.class)
			{
				if(sHelper == null)
					sHelper = new UDBOpenHelper(context);
			}
		}
		return sHelper;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}
