package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.HashingInterface
import lombok.SneakyThrows
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter


@Service
@Primary
class MD5Hashing : HashingInterface {
    private var encoder: MessageDigest? = null

    @SneakyThrows
    override fun encode(password: String?): String? {
        encoder = MessageDigest.getInstance("MD5")
        val digest = encoder?.digest(password?.toByteArray(StandardCharsets.UTF_8))
        return DatatypeConverter.printHexBinary(digest)
    }
}
