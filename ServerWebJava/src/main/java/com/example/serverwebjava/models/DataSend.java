package com.example.serverwebjava.models;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class DataSend implements Serializable {
    public String fileName;
    public byte[] fileContent;
    public Integer cod;
    public Integer idUser;
    public List<Integer> listPorts;

    public DataSend(String fileName, byte[] fileContent, Integer cod, Integer idUser, List<Integer> listPorts) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.cod = cod;
        this.idUser = idUser;
        this.listPorts = listPorts;
    }

    public DataSend() {

    }
}
