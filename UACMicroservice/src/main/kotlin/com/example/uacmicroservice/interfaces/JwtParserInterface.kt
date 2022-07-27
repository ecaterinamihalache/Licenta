package com.example.uacmicroservice.interfaces

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws


interface JwtParserInterface<T> {
    fun validateSignatureAndGetTokenParser(token: String?, templateKey: T): Jws<Claims?>?
    fun isTokenValid(token: String?, templateKey: T): Boolean
}
