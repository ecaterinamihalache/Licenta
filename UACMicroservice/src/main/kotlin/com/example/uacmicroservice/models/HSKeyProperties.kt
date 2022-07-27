package com.example.uacmicroservice.models

import com.example.uacmicroservice.interfaces.FileIOManagerInterface
import com.example.uacmicroservice.services.FileIOManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.annotation.PostConstruct

@Component
@ConfigurationProperties(prefix = "key.set")
class HSKeyProperties {
    @Value("\${secret.key.file}")
    private val SECRET_KEY_FILE: String? = null

    private var secretKey: String? = null

    fun getSecretKey(): String? {
        return secretKey
    }

    @PostConstruct
    @Throws(Exception::class)
    private fun createHSKey() {
        val fileIOManagerService: FileIOManagerInterface = FileIOManager()

        // Reading secret key from file
        val keyBytes: ByteArray? = SECRET_KEY_FILE?.let { fileIOManagerService.readFile(it) }
        secretKey = keyBytes?.let { String(it, StandardCharsets.UTF_8) }
    }
}