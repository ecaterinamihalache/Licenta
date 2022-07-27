package com.example.uacmicroservice.interfaces

interface JwtFactoryInterface<T> {
    fun generateToken(claims: Map<String, String>, templateKey: T?): String?
}