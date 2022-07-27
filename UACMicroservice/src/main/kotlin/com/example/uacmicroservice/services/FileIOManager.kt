package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.FileIOManagerInterface
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class FileIOManager : FileIOManagerInterface{
    override fun readFile(filename: String): ByteArray {
        return Files.readAllBytes(File(filename).toPath())
    }

    override fun writeFile(filename: String, bytes: ByteArray) {
        val file = File(filename)
        val fileParent = file.parentFile
        if (!file.exists()) {
            if (!fileParent.exists()) {
                fileParent.mkdirs()
            }
            file.createNewFile()
        }
        Files.write(file.toPath(), bytes)
    }

    override fun deleteFile(filename: String): Boolean {
        return Files.deleteIfExists(File(filename).toPath())
    }
}