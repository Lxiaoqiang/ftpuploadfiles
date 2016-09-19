package com.ftp.ftpmutipuploadfile.ftp.entity;

import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 14:24
 */
public class ParentFileInfo {

    private String parentFilePath;
    private List<FTPFileInfo> list;

    public String getParentFilePath() {
        return parentFilePath;
    }

    public void setParentFilePath(String parentFilePath) {
        this.parentFilePath = parentFilePath;
    }

    public List<FTPFileInfo> getList() {
        return list;
    }

    public void setList(List<FTPFileInfo> list) {
        this.list = list;
    }
}
