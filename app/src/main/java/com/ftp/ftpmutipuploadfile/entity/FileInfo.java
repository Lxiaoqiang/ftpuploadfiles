package com.ftp.ftpmutipuploadfile.entity;

/**
 * Created by lihuiqiang
 *
 * @date 2016/9/8 14:24
 */
public class FileInfo {
    private String fileName;
    private boolean selected;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
