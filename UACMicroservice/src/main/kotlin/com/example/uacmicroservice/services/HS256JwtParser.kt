package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.JwtParserInterface
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.util.*

@Service
class HS256JwtParser : JwtParserInterface<String> {
    override fun validateSignatureAndGetTokenParser(token: String?, templateKey: String): Jws<Claims?>? {
        return Jwts.parser().setSigningKey(templateKey).parseClaimsJws(token)
    }

    override fun isTokenValid(token: String?, templateKey: String): Boolean {
        return try {
            Jwts.parser().setSigningKey(templateKey)
                .parseClaimsJws(token).body.expiration.after(Date(System.currentTimeMillis()))
        } catch (exception: Exception) {
            false
        }
    }
}