package com.samanta.myfilebrowser;

import java.io.Serializable;

/**
 * Created by Наталия on 12.09.2015.
 */
public class DownloadedFile implements Serializable{
    private String name;
    private String location;
    private int size;
    private String extension;
    private String date;
    private boolean isDeleted;

    public DownloadedFile(){
    }

    public DownloadedFile(String name, String location, int size, String extension, String date) {
        this.name = name;
        this.location = location;
        this.size = size;
        this.extension = extension;
        this.date = date;
        this.isDeleted = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}

