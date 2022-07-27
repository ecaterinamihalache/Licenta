package com.example.managercentralmicroservice.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
class User(id: Int, email: String, files: Array<String>){

    @Id
    private var id: Int
    private var email: String = ""
    private var files: Array<String> = arrayOf()

    init {
        this.id=id
        this.email=email
        this.files=files
    }

    fun getId(): Int{
        return id
    }

    fun setId(id:Int){
        this.id = id
    }

    fun getEmail(): String{
        return email
    }

    fun setEmail(email:String){
        this.email = email
    }

    fun getFiles(): Array<String>{
        return files
    }

    fun setFiles(files:Array<String>){
        this.files = files
    }
}