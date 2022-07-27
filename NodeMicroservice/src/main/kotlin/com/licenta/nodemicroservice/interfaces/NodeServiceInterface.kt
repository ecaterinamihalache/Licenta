package com.licenta.nodemicroservice.interfaces

import com.licenta.nodemicroservice.models.DataSend
import com.licenta.nodemicroservice.models.FileTransfer
import com.licenta.nodemicroservice.models.FileWrite
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.zip.Checksum

interface NodeServiceInterface {
    fun createMessageForSend(socketNode: DatagramSocket): String?
    fun createMessageForShutDown(socketNode: DatagramSocket): String?
    fun sendMessage(jsonString: String?, socketNode: DatagramSocket, group: InetAddress, port: Int)
    fun readNameFiles():MutableList<FileTransfer>
    fun readFiles():MutableList<FileWrite>
    fun writeFile(file : FileWrite, idUser:Int): Boolean
    fun receiveMessageFromManagerAndDeleteFile(idUser:Int, file: String)
    fun saveFileOnDisk(dataReceive: DataSend)
    fun saveFileOnDiskAndSendToAnotherNode(dataReceive: DataSend, serverPort:Int, socketS : DatagramSocket, theInetAddress: InetAddress)
    fun executeCommand(message: String, serverPort: Int, socketS : DatagramSocket)
    fun receiveMessageFromManagerAndSendMessageOnNode(dataReceive: DataSend, socketS : DatagramSocket, theInetAddress: InetAddress)
    fun readFileOnDisk(fisier: String): FileWrite
    fun readFileAndCreateMessage(file: FileWrite, idUser: Int, listPorts: MutableList<Int>): String
    fun receiveMessageAndSendFileToWebServer(dataReceive: DataSend, socketS: DatagramSocket, theInetAddress: InetAddress)
    fun readFileFromDisk(fisier: String): FileWrite
    fun convertFileToFileWrite(file: String, fileContent: ByteArray): FileWrite
    fun writeF(file:FileWrite, listNameFile: MutableList<FileTransfer>, newPathFile: File, idUser: Int, fileName: String):Boolean
    fun calculateCRC(contentFile: ByteArray): Long
    fun saveCrcToFile(crc: Long, idUser: Int, fileName: String)
    fun writeCrcFile(listNameFile: MutableList<FileTransfer>, newPathFile: File, idUser: Int, fileName: String, crc: Long)
    fun verifyCrc()
    fun readAllFilesAndCreateAHashWithCrcCode():HashMap<String, Long>
}