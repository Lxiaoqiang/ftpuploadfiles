package com.ftp.ftpmutipuploadfile.socket.util.entity;

import java.io.Serializable;
public class FileInfo implements Serializable{
	private int uploadSize;//��ǰ�ϴ����ļ��Ĵ�С
	private String url;//��ǰ�ļ���·��
	private int maxSize;//�ļ��ܴ�С
	private int state = 0;//��ʼ������ͣ�ı�ʶ��0��ʾ��ʼ��1��ʾ��ͣ
	private String resourceId;//�������Ա��ֶΣ��ж��Ƿ����ϴ����ļ�¼

	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public int getUploadSize() {
		return uploadSize;
	}
	public void setUploadSize(int uploadSize) {
		this.uploadSize = uploadSize;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}

