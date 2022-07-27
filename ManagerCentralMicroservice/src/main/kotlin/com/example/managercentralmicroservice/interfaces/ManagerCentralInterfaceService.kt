package com.example.managercentralmicroservice.interfaces

import com.example.managercentralmicroservice.models.FileTransfer
import com.example.managercentralmicroservice.models.FilesMap
import com.example.managercentralmicroservice.models.FilesMapNew
import com.example.managercentralmicroservice.models.UserData
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

interface ManagerCentralInterfaceService {
    fun createMapFiles(messageReceive: String): ConcurrentHashMap<String, MutableList<FileTransfer>>
    fun updateMapFiles(message: String, filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): ConcurrentHashMap<String, MutableList<FileTransfer>>
    fun verifyMessage(message: String): String
    fun countFrequency(listFile: MutableList<String>): MutableMap<String, Int>
    fun filesReplication(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, managerCentralSocket: MulticastSocket)
    fun getFreeNodes(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<Int>
    fun verificationContainsValue(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, fileName: String): MutableList<Int>
    fun sendMessageToNodeForDeleteFile(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, key: String, managerCentralSocket: MulticastSocket, group:InetAddress)
    fun createMessage(key: String, cod: Int, portGood: MutableList<Int>): String
    fun sendMessageToNodeForCopyFileAndSendItToAnotherNode(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, key: String, managerCentralSocket: MulticastSocket, group:InetAddress)
    fun verificationNotContainsSamePort(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, randomPort: Int): MutableList<Int>
    fun getPortForWriteFile(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>, listPortsWrite: MutableList<Int>): Int
    fun getPortNode(idUser: Int, fileName: String, filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>):Int
    fun changeHashMap(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): HashMap<String, List<String>>
    fun getFilesMap(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<FilesMap>
    fun splitKeyOfHashMap(ipPortNode: String): List<String>
    fun getNameNode(port: String): String
    fun getUsersFromFilesMap(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>):MutableList<UserData>
    fun getFilesMapNew(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<FilesMapNew>
    fun getNrOfNodes(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): Int
}