package com.example.uacmicroservice.interfaces

interface HashingInterface {
    fun encode(password: String?): String?
}