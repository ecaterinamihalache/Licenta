package com.example.serverwebjava.models;

import java.io.Serializable;
import java.util.List;

public class FilesMap implements Serializable {
    private String ipPortNode;
    private List<String> files;

    public FilesMap(String ipPortNode, List<String> files) {
        this.ipPortNode = ipPortNode;
        this.files = files;
    }

    public FilesMap() {
    }

    public String getIpPortNode() {
        return ipPortNode;
    }

    public void setIpPortNode(String port) {
        this.ipPortNode = ipPortNode;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}


