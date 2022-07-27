package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.HashingInterface
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PBKDF2Hashing : HashingInterface {
    private var encoder: Pbkdf2PasswordEncoder? = null

    init {
        encoder = Pbkdf2PasswordEncoder()
    }

    override fun encode(password: String?): String? {
        return encoder?.encode(password)
    }
}