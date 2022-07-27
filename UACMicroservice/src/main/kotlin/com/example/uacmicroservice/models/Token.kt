package com.example.uacmicroservice.models

import lombok.Data

@Data
class Token {
    private val token: String = ""

    fun getToken(): String {
        return token
    }
}