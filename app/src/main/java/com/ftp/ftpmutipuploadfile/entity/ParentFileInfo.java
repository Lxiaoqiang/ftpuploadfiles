package com.ftp.ftpmutipuploadfile.entity;

import java.util.List;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 14:24
 */
public class ParentFileInfo {

    private String parentFilePath;
    private List<FileInfo> list;

    public String getParentFilePath() {
        return parentFilePath;
    }

    public void setParentFilePath(String parentFilePath) {
        this.parentFilePath = parentFilePath;
    }

    public List<FileInfo> getList() {
        return list;
    }

    public void setList(List<FileInfo> list) {
        this.list = list;
    }
}
