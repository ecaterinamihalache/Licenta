package com.licenta.nodemicroservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.licenta.nodemicroservice.interfaces.NodeServiceInterface
import com.licenta.nodemicroservice.models.*
import org.springframework.stereotype.Service
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.CRC32
import java.util.zip.Checksum

@Service
class NodeService : NodeServiceInterface {
    private lateinit var packetSend: DatagramPacket
    private var file: String = "filePath1.txt"
    private val  crcPath = "D:/An_4/Licenta_Github/Licenta/Licenta/SpringRest/NodeMicroservice/src/main/resources/static/node1/crc/"
    private var fileContent: String = this.javaClass.classLoader.getResource(file).readText()
    private var filePath: File = File(fileContent)
    private var crcPathFile: File = File(crcPath)
    private val listNameFiles: MutableList<FileTransfer> = mutableListOf()
    private val listFiles: MutableList<FileWrite> = mutableListOf()

    companion object {
        @JvmStatic
        var listFilesToDelete: MutableList<String> = mutableListOf()
    }

    override fun createMessageForSend(socketNode: DatagramSocket): String? {
        val listFilesToSend = readNameFiles()
        val objectMapper = ObjectMapper()
        //println("lista de fisire sterse: $listFilesToDelete")
        //trebuie sa vad daca am fisierele pe care le-am sters in lista mea si daca da sa le sterg
        for (file in listFilesToSend) {
            for (fileD in listFilesToDelete) {
                if (file.fileName == fileD) {
                    listFilesToSend.remove(file)
                }
            }
        }
        val filesNameToSend = listFilesToSend
        //println("lista de fisierele de trimis la manager actualizata este: $filesNameToSend")
        val nodePort = socketNode.localPort
        val nodeIp = socketNode.localAddress
        val nodeName = "$nodeIp;$nodePort"
        val jsonData = JsonData(nodeName, filesNameToSend, "")
        val jsonString = objectMapper.writeValueAsString(jsonData)

        filesNameToSend.clear()
        return jsonString
    }

    override fun createMessageForShutDown(socketNode: DatagramSocket): String? {
        val nodePort = socketNode.localPort
        val nodeIp = socketNode.localAddress
        val nodeName = "$nodeIp;$nodePort"
        val objectMapper = ObjectMapper()
        val listFiles = mutableListOf<FileTransfer>()
        val messageForSendSutdown = JsonData(nodeName, listFiles, "DISCONNECTED")
        //println(messageForSendSutdown)
        return objectMapper.writeValueAsString(messageForSendSutdown)//Gson().toJson(messageForSendSutdown)
    }

    override fun sendMessage(jsonString: String?, socketNode: DatagramSocket, group: InetAddress, port: Int) {
        //println("string: $jsonString")
        if (jsonString != null) {
            packetSend = DatagramPacket(jsonString.toByteArray(), jsonString.length, group, port)
        }
        println("Trimit mesajul: $packetSend")
        socketNode.send(packetSend)
    }

    override fun readNameFiles(): MutableList<FileTransfer> {
        //val filePath = File(newPath)
        //imi da lista de fisiere de la calea data
        val listOfFiles: Array<File>? = filePath.listFiles()
        if (listOfFiles != null) {
            for (file: File in listOfFiles) {
                //verific daca este director sau nu
                //daca este atunci pun in lista mea /director/nume_fisier
                if (file.isDirectory) {
                    //verific daca este directorul cu fisierele crc si il evit
                    if(file.name.equals("crc")){
                        //println("Se evita citirea din acest folder deoarece contine metadatele pentru crc!")
                        break
                    }else{
                        val idUser = file.name
                        //println("id-ul userului este: $idUser")
                        val newPath: String = fileContent + idUser
                        //println("newPath este: $newPath")
                        val fileNewPath = File(newPath)
                        //lista cu fisierele din director
                        val listOfFileFromDir: Array<File>? = fileNewPath.listFiles()

                        if (listOfFileFromDir != null) {
                            for (f: File in listOfFileFromDir) {
                                val fileName = idUser + "/" + f.name
                                //println("daca e dintr-un dir atunci: $fileName")
                                val fileTransfer = FileTransfer(fileName)
                                listNameFiles.add(fileTransfer)
                            }
                        }
                    }
                } else {
                    //daca nu se pune normal numele
                    val fileTransfer = FileTransfer(file.name)
                    listNameFiles.add(fileTransfer)
                }
            }
        }
        return listNameFiles
    }

    override fun readFiles(): MutableList<FileWrite> {
        val listOfFiles: Array<File>? = filePath.listFiles()
        if (listOfFiles != null) {
            for (file: File in listOfFiles) {
                if (file.isDirectory) {
                    if(file.name.equals("crc")){
                        //println("Se evita citirea din acest folder deoarece contine metadatele pentru crc!")
                        break
                    }else{
                        val idUser = file.name
                        //println("id-ul userului este: $idUser")
                        val newPath: String = fileContent + idUser
                        //println("newPath este: $newPath")
                        val fileNewPath = File(newPath)
                        //lista cu fisierele din director
                        val listOfFileFromDir: Array<File>? = fileNewPath.listFiles()
                        if (listOfFileFromDir != null) {
                            for (f: File in listOfFileFromDir) {
                                val fileName = idUser + "/" + f.name
                                //println("daca e dintr-un dir atunci: $fileName")
                                val contentFile = f.readText(Charsets.UTF_8)
                                val fileTransfer = FileWrite(fileName, contentFile.toByteArray())
                                listFiles.add(fileTransfer)
                            }
                        }
                    }
                } else {
                    //daca nu se pune normal numele
                    val contentFile = file.readText(Charsets.UTF_8)
                    val fileTransfer = FileWrite(file.name, contentFile.toByteArray())
                    listFiles.add(fileTransfer)
                }
            }
        }
        return listFiles
    }

    fun readNameF(newPath: String): MutableList<FileTransfer> {
        val listOfFiles: Array<File>? = File(newPath).listFiles()
        if (listOfFiles != null) {
            for (file: File in listOfFiles) {
                val fileTransfer = FileTransfer(file.name)
                listNameFiles.add(fileTransfer)
            }
        }
        return listNameFiles
    }

    override fun calculateCRC(contentFile: ByteArray): Long {
        val crc32 = CRC32()
        crc32.update(contentFile)
        println("CRC32 Checksum: " + crc32.value)
        return crc32.value
    }

    override fun writeCrcFile(
        listNameFile: MutableList<FileTransfer>,
        newPathFile: File,
        idUser: Int,
        fileName: String,
        crc: Long
    ) {
        if (listNameFile.isEmpty()) {
            newPathFile.createNewFile()
            //scriu continutul fisierului
            newPathFile.writeText("$idUser/$fileName:$crc")
        } else {
            for (f: FileTransfer in listNameFile) {
                //daca nu exista -> creez fisierul si scriu in el continutul primit
                if (fileName != f.fileName) {
                    newPathFile.createNewFile()
                    //scriu continutul fisierului
                    newPathFile.writeText("$idUser/$fileName:$crc")
                } else {
                    return
                }
            }
        }
    }

    override fun saveCrcToFile(crc: Long, idUser: Int, fileName: String) {
        val newDir = "$crcPath$idUser/"
        println("newDir: $newDir")
        val pathDir: Path = Paths.get(newDir)
        val newPath: String = newDir + fileName
        println("newPath: $newPath")
        val newPathFile = File(newPath)
        println("hei!: $newPathFile")

        val exist: Boolean
        //citesc lista de fisiere din folderul nodului respectiv
        val listNameFile = readNameF(newPath) //ar trebui sa ii dau ca parametru calea

        //daca exista directorul asta scrie direct fisierul in el
        if (Files.exists(pathDir)) {
            writeCrcFile(listNameFile, newPathFile, idUser, fileName, crc)
        } else {
            //daca nu il creaza si dupa scrie in el fisierul
            Files.createDirectory(Paths.get(newDir))
            writeCrcFile(listNameFile, newPathFile, idUser, fileName, crc)
        }
    }

    override fun writeF(
        file: FileWrite,
        listNameFile: MutableList<FileTransfer>,
        newPathFile: File,
        idUser: Int,
        fileName: String
    ): Boolean {
        var exist = false
        var crcValue: Long
        if (listNameFile.isEmpty()) {
            //calculez crc-ul
            crcValue = calculateCRC(file.contentFile)
            //salvez valoarea calculata intr-un fisier
            saveCrcToFile(crcValue, idUser, fileName)

            newPathFile.createNewFile()
            //scriu continutul fisierului
            newPathFile.writeBytes(file.contentFile)
        } else {
            for (f: FileTransfer in listNameFile) {
                //daca nu exista -> creez fisierul si scriu in el continutul primit
                if (file.fileName != f.fileName) {
                    //calculez crc-ul
                    crcValue = calculateCRC(file.contentFile)
                    //salvez valoarea calculata intr-un fisier
                    saveCrcToFile(crcValue, idUser, fileName)

                    newPathFile.createNewFile()
                    //scriu continutul fisierului
                    newPathFile.writeBytes(file.contentFile)
                } else {
//                //daca exista sa ii pun o versiune
//                //creez alt fisier cu alt nume pe baza celui dat ca parametru si scriu acolo continutul
//                val nameFile = newPathFile.name
//                val replicFile = File(nameFile + "")
//                replicFile.createNewFile()
//                replicFile.writeBytes(file.contentFile)
                    exist = true
                }
            }
        }
        return exist
    }

    override fun writeFile(file: FileWrite, idUser: Int): Boolean {
        //trebuie sa creez prima data folderul cu id-ul daca nu exista
        //daca exista creez fisierul si scriu in el
        val newDir = "$fileContent$idUser/"
        println("newDir: $newDir")

        val pathDir: Path = Paths.get(newDir)
        val newPath: String = newDir + file.fileName
        //println("newPath: $newPath")
        val newPathFile = File(newPath)
        println("hei! Aici in writeFile: $newPathFile")
        val exist: Boolean
        //citesc lista de fisiere din folderul nodului respectiv
        val listNameFile = readNameF(newPath) //ar trebui sa ii dau ca parametru calea

        //daca exista directorul asta scrie direct fisierul in el cu toate verificarile de rigoare
        if (Files.exists(pathDir)) {
            exist = writeF(file, listNameFile, newPathFile, idUser, file.fileName)
            return if (exist) {
                exist
            } else {
                exist
            }
        } else {
            //daca nu il creaza si dupa scrie in el fisierul
            Files.createDirectory(Paths.get(newDir))
            exist = writeF(file, listNameFile, newPathFile, idUser, file.fileName)
            return if (exist) {
                exist
            } else {
                exist
            }
        }
    }

    override fun receiveMessageFromManagerAndDeleteFile(idUser: Int, file: String) {
        //primesc file-ul sub forma de idUser/nume_fisier
        //imi creez path-ul unde este fisierul
        val fileName = "$idUser/$file"
        val path = fileContent + fileName
        //println("file path for detele a file: $path")
        val fileForDelete = File(path)
        listFilesToDelete.add(fileName) //lista cu fisierele care se sterg
        //println("file for delete: $fileForDelete")
        fileForDelete.delete()
        println("Fisier sters!")

        //ii sterge si fisierul de crc
        val pathCrc = crcPath + fileName
        val fileCRCForDelete = File(pathCrc)
        fileCRCForDelete.delete()
        println("Fisierul CRC sters!")
    }

    override fun convertFileToFileWrite(file: String, fileContent: ByteArray): FileWrite {
        //println("numele fisierului este: ${file.name}")
        return FileWrite(file, fileContent)
    }

    override fun saveFileOnDisk(dataReceive: DataSend) {
        //println("numele fisierului de salvat este: ${dataReceive.file.name}")
        val idUser = dataReceive.idUser
        val fileWrite = convertFileToFileWrite(dataReceive.fileName, dataReceive.fileContent)
        val ok = writeFile(fileWrite, idUser)
        println("sa vezi daca e ok: $ok") //trebuie sa dea false daca nu exista si true daca exista
    }

    override fun readFileOnDisk(fisier: String): FileWrite {
        //imi da o lista de WriteFile
        val listFiles = readFiles()
        //println("lista de fisiere este: $listFiles")
        var fileSend = FileWrite("", byteArrayOf())
        for (file in listFiles) {
            //cand gasesc fisierul il returnez
            if (file.fileName == fisier) {
                fileSend = file
            }
        }
        return fileSend
    }

    override fun readFileFromDisk(fisier: String): FileWrite {
        val path = fileContent + fisier
        println("citesc fisierul cu locatia: $path")
        val f = File(path)
        val list = fisier.split("/")
        println("nume fisier: ${list[1]}")
        println("continutul este: ${f.readBytes()}")
        return FileWrite(list[1], f.readBytes())
    }

//    fun readFile(file: String): File {
//        val path = fileContent + file
//        println("citesc fisierul cu locatia: $path")
//        return File(path)
//    }

    override fun readFileAndCreateMessage(file: FileWrite, idUser: Int, listPorts: MutableList<Int>): String {
        println("am intrat in readFileAndCreateMessage")
        val objectMapper = ObjectMapper()
        val list = file.fileName.split("/")

//        println("fisierul trimis catre salvare: ${list[1]}")
//        println("id-ul usersului este: ${list[0]}")
        //val fileGood = File(list[1])
        //fileGood.writeBytes(file.contentFile)//scrie in fisier continutul din FileWrite
        println("heiii: ${list[1]}")
        println("id-user: $idUser")
        println("lista de porturi la care sa se trimita: $listPorts")

        val dataSend =
            DataSend(list[1], file.contentFile, 3, idUser, listPorts) //trimit fisierul catre nod ca sa il salveze
        println("inainte de iesire din functia readFileAndCreateMessage")
        return objectMapper.writeValueAsString(dataSend)
    }

    override fun receiveMessageFromManagerAndSendMessageOnNode(
        dataReceive: DataSend,
        socketS: DatagramSocket,
        theInetAddress: InetAddress
    ) {
        //primirea mesajului de la manager -> verifica numele fisierului il trimite catre nodul dat din lista de porturi primite
        //citesc nr portului catre care trebuie sa trimit fisierul
        if (dataReceive.listPorts.isEmpty()) {
            println("Portul este null! Nu se poate trimite mesajul!")
        } else {
            //numele fisierului
            val fisier = dataReceive.idUser.toString() + "/" + dataReceive.fileName
            println("Am primit fisierul cu numele: $fisier")

            //citesc fisierul de pe hard disk (un FileWrite)
            val fileRead = readFileOnDisk(fisier)//readFileOnDisk(fisier) //readFileFromDisk(fisier)
            //imi creez mesajul sub forma de json
            val gsonString = readFileAndCreateMessage(fileRead, dataReceive.idUser, dataReceive.listPorts)

            val packetSend =
                DatagramPacket(gsonString.toByteArray(), gsonString.length, theInetAddress, dataReceive.listPorts[0])
            //trimit mesajul catre nod
            println("trimit mesajul catre : ${dataReceive.listPorts[0]}")
            socketS.send(packetSend)
            println("S-a trimis!!")
        }
    }

    //primesc un obiecte cu o lista ori de la Server ori de la Nod
    override fun saveFileOnDiskAndSendToAnotherNode(
        dataReceive: DataSend,
        serverPort: Int,
        socketS: DatagramSocket,
        theInetAddress: InetAddress
    ) {
        //print("am primit mesaj de la un alt nod!")
        val obj = ObjectMapper()
        //actualizeaz lista de porturi => se sterge pe el din lista
        println("portul pe care ruleaza nodul: $serverPort")
        dataReceive.listPorts.remove(serverPort)

        if (dataReceive.listPorts.isNotEmpty()) { //daca nu este goala
            println("lista de porturi la care trebuie trimis fisierul este: ${dataReceive.listPorts}")
            //salvez fisierul pe disk
            saveFileOnDisk(dataReceive)

            //creez mesajul de transmis
            val dataSend =
                DataSend(dataReceive.fileName, dataReceive.fileContent, 3, dataReceive.idUser, dataReceive.listPorts)
            val gsonString = obj.writeValueAsString(dataSend)
            println("portul la care se va trimite este: ${dataReceive.listPorts[0]}")
            val packetSend =
                DatagramPacket(gsonString.toByteArray(), gsonString.length, theInetAddress, dataReceive.listPorts[0])

            print("trimit un mesaj catre nodul : ${dataReceive.listPorts[0]}")
            //trimit mesajul la urmatorul port din lista
            socketS.send(packetSend)
        } else {
            //daca el e ultimul nod la care trebuie sa ajunga fisierul il doar salveaza, nu mai trebuie sa trimita la alt nod!
            saveFileOnDisk(dataReceive) //salvez fisierul pe disk
        }
    }

    //imi trimite fisierul cu tot cu continut inapoi la Serverul Web
    override fun receiveMessageAndSendFileToWebServer(
        dataReceive: DataSend,
        socketS: DatagramSocket,
        theInetAddress: InetAddress
    ) {
        println("Am primit mesaj de la serverul web sa ii trimit fisierul cu numele ${dataReceive.fileName}")
        val obj = ObjectMapper()
        val listFiles: MutableList<Int> = mutableListOf()
        //iau numele fisierului si caut pe disk sa il citesc
        val fileName = dataReceive.idUser.toString() + "/" + dataReceive.fileName
        println("calea catre fisier: $fileName")

        //citesc de pe disk fisierul
        val fileWrite = readFileFromDisk(fileName)//readFile(fileName)//readFileFromDisk(fileName)//readFile(fileName)

        //transform in FileWrite-ul -> il pun intr-un File
        val fileForSend = File(fileWrite.fileName)
        fileForSend.writeBytes(fileWrite.contentFile)

        //formez fisierul de trimis (un DataSend)
        //val dataSend = DataSend(fileWrite.fileName, fileWrite.contentFile, 4, dataReceive.idUser, listFiles)
        val dataForServer = DataForServer(fileWrite.fileName, fileWrite.contentFile)
        //trimit direct fisierul!!!
        val gsonString = obj.writeValueAsString(dataForServer)
        val packetSend = DatagramPacket(gsonString.toByteArray(), gsonString.length, theInetAddress, 777)

        println("send message to Web Server!")
        //trimite mesajul cu fisierul catre ServerulWeb
        socketS.send(packetSend)
    }

    override fun executeCommand(message: String, serverPort: Int, socketS: DatagramSocket) {
        val theInetAddress = InetAddress.getByName("localhost")
        val mapper = jacksonObjectMapper()
        val dataReceive: DataSend = mapper.readValue(message)
        //println("am primit: $dataReceive")

        when (dataReceive.cod) {
            //daca e 1 ar trebui sa primesc de la server fisierul cu lista de porturi -> o functie in care sa salvez pe disk ce am primit
            // adica fisierul si in care sa trimit la urmatorul nod fisierul si lista de noduri actualizata (se sterge pe el din lista)
            1 -> saveFileOnDiskAndSendToAnotherNode(dataReceive, serverPort, socketS, theInetAddress)
            //primirea mesajului de la manager -> verifica numele fisierului si il sterge de pe disk
            20 -> receiveMessageFromManagerAndDeleteFile(dataReceive.idUser, dataReceive.fileName)
            //primirea mesajului de la manager -> verifica numele fisierului, citeste continutul lui si il trimite catre nodul dat (portul primit de la manager)
            21 -> receiveMessageFromManagerAndSendMessageOnNode(dataReceive, socketS, theInetAddress)
            //primirea mesajului de la alt node -> scrie fisierul primit de la alt nod pe disk
            3 -> saveFileOnDiskAndSendToAnotherNode(dataReceive, serverPort, socketS, theInetAddress)
            //primirea mesajului de la Serverul Web ca sa ii trimit fisierul dat ca parametru
            4 -> receiveMessageAndSendFileToWebServer(dataReceive, socketS, theInetAddress)
            else -> {
                print("Invalid code!")
            }
        }
    }

    override fun readAllFilesAndCreateAHashWithCrcCode(): HashMap<String, Long> {
        val hashMapCRCFiles: HashMap<String, Long> = hashMapOf()

        val listOfFiles: Array<File>? = filePath.listFiles()
        if (listOfFiles != null) {
            for (file: File in listOfFiles) {
                //verific daca este director sau nu
                //daca este atunci pun in lista mea /director/nume_fisier
                if (file.isDirectory) {
                    //daca gaseste directorul crc sare peste el!!
                    if(file.name.equals("crc")){
                        //println("Se evita citirea din acest folder deoarece contine metadatele pentru crc!")
                        break
                    }else {
                        val idUser = file.name
                        val newPath: String = fileContent + idUser
                        val fileNewPath = File(newPath)

                        //lista cu fisierele din director
                        val listOfFileFromDir: Array<File>? = fileNewPath.listFiles()
                        if (listOfFileFromDir != null) {
                            for (f: File in listOfFileFromDir) {
                                val fileName = idUser + "/" + f.name
                                val pathFile = newPath + "/" + f.name
                                val readFileContent = File(pathFile)

                                println("Fisierul pentru care se calculeaza crc-ul este: ${fileName}")
                                val crcVerifyFile = calculateCRC(readFileContent.readBytes())
                                hashMapCRCFiles[fileName] = crcVerifyFile
                            }
                        }
                    }
                } else {
                    val otherPath = fileContent + file.name
                    val otherReadFileContent = File(otherPath)
                    val crcVerifyFile = calculateCRC(otherReadFileContent.readBytes())
                    hashMapCRCFiles[file.name] = crcVerifyFile
                }
            }
        }
        return hashMapCRCFiles
    }

    fun readAllCRCFileAnCreateHashMap():HashMap<String, Long> {
        val checkMapCRCFile: HashMap<String, Long> = hashMapOf()

        val listOfFiles: Array<File>? = crcPathFile.listFiles()
        if (listOfFiles != null) {
            for (file: File in listOfFiles) {
                //verific daca este director
                if (file.isDirectory) {
                    val idUser = file.name
                    val newPath: String = crcPath + idUser
                    val fileNewPath = File(newPath)

                    val listOfFileFromDir: Array<File>? = fileNewPath.listFiles()
                    if (listOfFileFromDir != null) {
                        for (f: File in listOfFileFromDir) {
                            val fileName = idUser + "/" + f.name
                            val pathFile = newPath + "/" + f.name
                            val readFileContent = File(pathFile)

                            //citeste din fisier
                            val text = readFileContent.readText(Charsets.UTF_8)
                            val list = text.split(":")
                            checkMapCRCFile[fileName] = list[1].toLong()
                        }
                    }
                } else {
                    val otherPath = crcPath + file.name
                    val otherReadFileContent = File(otherPath)
                    val text = otherReadFileContent.readText(Charsets.UTF_8)
                    val list = text.split(":")
                    checkMapCRCFile[file.name] = list[1].toLong()
                }
            }
        }
        return checkMapCRCFile
    }

    override fun verifyCrc(){
        //citesc toate fisierele de pe disk care nu sunt directoare
        //calculez pentru fiecare continut CRC-ul
        //fac un hashMap cu nume_fisier:valoare_crc
        //println("intru in verificare CRC!!!!")
        val hashMapCRCFiles = readAllFilesAndCreateAHashWithCrcCode()
        //println("primul hashMap: $hashMapCRCFiles")

        //citesc din fisierele cu valoarile crc ce au fost scrise la scrierea fisierlor pe disk
        //fac un hashMap cu nume_fisier:valoare crc
        val checkMapCRCFile = readAllCRCFileAnCreateHashMap()
        //println("al doilea hashMap: $checkMapCRCFile")

        //parcurg cele doua hashMap-uri si verific daca pentru aceeasi cheie am aceeasi valoare
        //daca da atunci e totul ok
        //daca nu apelez functia de stergere pentru respectivul fisier
        for ((key, value) in checkMapCRCFile) {
            for ((key1, value1) in hashMapCRCFiles) {
                if (key == key1) {
                    if (value == value1) {
                        println("Totul in regula cu valoarea CRC-ului pentru fisierul $key!")
                    } else {
                        //sterge fisierul cu numele key
                        println("***********************************")
                        println("Este o eroare de scriere la fisierul $key!")
                        val path = fileContent + key
                        println("file path for detele a file: $path")

                        val pathCRC = crcPath + key
                        println("file path for detele a file with CRC value: $pathCRC")

                        val fileForDelete = File(path)
                        val fileCRCForDelete = File(pathCRC)
                        listFilesToDelete.add(key) //lista cu fisierele care se sterg
                        println("file for delete: $fileForDelete")
                        println("CRC file for delete: $fileCRCForDelete")

                        fileForDelete.delete()
                        //sterg metadatele fisierului respectiv -> in folderul crc se sterge fisierul care are probleme
                        fileCRCForDelete.delete()
                        println("Fisier sters în verificarea CRC-ului!")
                        println("***********************************")
                    }
                }
            }
        }
    }
}

