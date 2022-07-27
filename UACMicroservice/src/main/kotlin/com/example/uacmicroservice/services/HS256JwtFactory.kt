package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.JwtFactoryInterface
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class HS256JwtFactory : JwtFactoryInterface<String> {

    private var expirationTimeOffset: Long = 0

    @PostConstruct
    fun initFactoryParameters() {
        // 15 minutes in miliseconds
        expirationTimeOffset = 900000
    }

    private fun createJTI(): String {
        return String(Base64.getEncoder().encode(UUID.randomUUID().toString().toByteArray()))
    }

    override fun generateToken(claims: Map<String, String>, templateKey: String?): String? {
        val issuedAtDate = Date(System.currentTimeMillis())
        return Jwts.builder()
            .setClaims(claims)
            .setId(createJTI())
            .setIssuedAt(issuedAtDate)
            .setExpiration(Date(issuedAtDate.time + expirationTimeOffset))
            .signWith(SignatureAlgorithm.HS256, templateKey)
            .compact()
    }
}

