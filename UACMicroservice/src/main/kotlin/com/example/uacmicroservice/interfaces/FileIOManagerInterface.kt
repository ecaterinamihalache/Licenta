package com.example.uacmicroservice.interfaces

import java.io.IOException

interface FileIOManagerInterface {
    @Throws(IOException::class)
    fun readFile(filename: String): ByteArray

    @Throws(IOException::class)
    fun writeFile(filename: String, bytes: ByteArray)

    @Throws(IOException::class)
    fun deleteFile(filename: String): Boolean
}