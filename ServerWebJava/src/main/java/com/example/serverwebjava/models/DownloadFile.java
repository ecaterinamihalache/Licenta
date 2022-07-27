package com.example.serverwebjava.models;

import java.io.Serializable;

public class DownloadFile implements Serializable {
    private Integer id;
    private String fileName;

    public DownloadFile(Integer id, String token, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }

    public DownloadFile() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
