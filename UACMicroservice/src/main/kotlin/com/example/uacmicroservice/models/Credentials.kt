package com.example.uacmicroservice.models

class Credentials(email: String, password: String) {
    private var email: String = ""
    private var password: String = ""

    init {
        this.email = email
        this.password = password
    }

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }

}