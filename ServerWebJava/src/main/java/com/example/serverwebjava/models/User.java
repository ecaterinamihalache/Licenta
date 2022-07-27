package com.example.serverwebjava.models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private Integer id;
    private String email;
    private ArrayList<String> files;

    public User(Integer id, String email, ArrayList<String> files) {
        this.id = id;
        this.email = email;
        this.files = files;
    }

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }
}
