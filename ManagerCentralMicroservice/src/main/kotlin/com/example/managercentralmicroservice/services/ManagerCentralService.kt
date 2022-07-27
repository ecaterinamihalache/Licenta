package com.example.managercentralmicroservice.services

import com.example.managercentralmicroservice.interfaces.ManagerCentralInterfaceService
import com.example.managercentralmicroservice.models.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.ConcurrentHashMap


class ManagerCentralService : ManagerCentralInterfaceService {
    private var replicationFactory = 2
    private var filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>> = ConcurrentHashMap()

    override fun createMapFiles(messageReceive: String): ConcurrentHashMap<String, MutableList<FileTransfer>> {
        val mapper = jacksonObjectMapper()
        val jsonData: JsonData = mapper.readValue(messageReceive)
        val cheie = jsonData.ipPortNode
        val fisiere = jsonData.listFilesToSend

        if (!filesMap.containsKey(cheie)) {
            filesMap[cheie] = fisiere
        } else {
            filesMap[cheie] = mutableListOf()
            filesMap[cheie] = fisiere
        }
        println("HashMap-ul este: $filesMap")
        return filesMap
    }

    override fun updateMapFiles(message: String, filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): ConcurrentHashMap<String, MutableList<FileTransfer>> {
        println("actualizez hashMap-ul!!!")
        val mapper = jacksonObjectMapper()
        val jsonData: JsonData = mapper.readValue(message)
        val portNode = jsonData.ipPortNode

        for (key in filesMap.keys) {
            //caut fisierele de la portul dat primit si sterg din hashMap acele fisiere (pentru ca nodul s-a inchis)
            if(key == portNode){
                filesMap.remove(key)
            }
        }
        return filesMap
    }

    override fun splitKeyOfHashMap(ipPortNode: String): List<String>{
        return ipPortNode.split(";")
    }

    override fun countFrequency(listFile: MutableList<String>): MutableMap<String, Int> {//imi face un map cu frecventa aparitiei fiecarui fisier in harta de fisiere
        val frequencyMap: MutableMap<String, Int> = HashMap()
        for (s in listFile) {
            var count = frequencyMap[s]
            if (count == null) count = 0
            frequencyMap[s] = count + 1
        }
        return frequencyMap
    }

    override fun verifyMessage(message: String): String {
        val mapper = jacksonObjectMapper()
        val jsonData: JsonData = mapper.readValue(message)
        return if (jsonData.message == "DISCONNECTED") {
            "EXIT"
        } else{
            "NONE"
        }
    }

    override fun verificationContainsValue(//verific ce noduri contin fisierul meu
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        fileName: String
    ): MutableList<Int> {
        val listPorts: MutableList<Int> = mutableListOf()
        for ((key, value) in filesMap) { //port si lista fisiere
            for (v in value) { //parcurg lista de fisiere a fiecarui port din hash si fac o lista de porturi care contin fisierul primit
                if (v.fileName == fileName) {
                    val list = splitKeyOfHashMap(key) //splituiesc cheia si ii pun doar portul (care este int) in list[0] am ip-ul si in list[1] am portul
                    listPorts.add(list[1].toInt())
                }
            }
        }
        //println("in verificationContainsValue lista de porturi returnate este: $listPorts")
        return listPorts
    }

    override fun verificationNotContainsSamePort(
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        randomPort: Int
    ): MutableList<Int> { //verific ce noduri nu contin fisierul meu
        val listPorts: MutableList<Int> = mutableListOf()

        println("hash-ul este: $filesMap")
        for (key in filesMap.keys()) {
            val list = splitKeyOfHashMap(key)
            if (list[1].toInt() != randomPort){
                listPorts.add(list[1].toInt())
            }
        }
        //println("in verificationNotContainsValue lista de porturi returnate este: $listPorts")
        return listPorts
    }

    override fun getPortForWriteFile(
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        listPortsWrite: MutableList<Int>
    ): Int {
        val countFiles = hashMapOf<Int, Int>()
        for ((key, value) in filesMap) {
            val list = splitKeyOfHashMap(key)
            countFiles[list[1].toInt()] = value.size
        }
        //{port=nr fisiere}
        val result = countFiles.toList().sortedBy { (_, value) -> value }.toMap()
        var port = 0

        //verific ce port din lista mea se afla in lista de noduri mai putin incarcate si sa il aleg pe primul care indeplineste conditia
        for (r in result) {
            for (i in listPortsWrite) {
                if (r.key == i) {
                    port = r.key
                }
            }
        }
        return port
    }

    override fun createMessage(key: String, cod: Int, portGood: MutableList<Int>): String {
        val list = key.split("/")
        val file = File(list[1])
        val objectMapper = ObjectMapper()
        val dataSend = DataSend(
            list[1],
            byteArrayOf(),
            cod,
            list[0].toInt(),
            portGood
        )
        //trimit numele fisierului si codul ca sa stie ca e de la manager si ca trebuie sa stearga fisierul atasat
        return objectMapper.writeValueAsString(dataSend)
    }

    override fun sendMessageToNodeForCopyFileAndSendItToAnotherNode(
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        key: String,
        managerCentralSocket: MulticastSocket,
        group: InetAddress
    ) {
        val listPorts : MutableList<Int> = mutableListOf()
        //verific ce nod contine deja fisierul
        //key e numele fisierului iar functia imi intoarce o lista de noduri ce contin acel fisier
        val listPortsToSend = verificationContainsValue(filesMap, key)
        println("lista de porturi ce contin fisierul $key este: $listPortsToSend")

        //println(listPortsToSend)
        println("Se trimite comanda de copiere fisier si trimitere catre alt nod!")
        //aleg un nod random de pe care vreau sa citesc fisierul si sa il trimit dupa la un alt nod
        val randomPort = listPortsToSend.random()
        println("portul la care sa fie trimis fisierul $key ales random este: $randomPort")

        //verific ce noduri nu contin fiserul
        val listPortsWrite : MutableList<Int> = verificationNotContainsSamePort(filesMap, randomPort)
        //println("lista de porturi ce nu contin fisierul $key este: $listPortsWrite")

        //aleg un nod din cele mai putin incarcate care sa nu contina fisierul ce trebuie copiat
        val portGood = getPortForWriteFile(filesMap, listPortsWrite)
        listPorts.add(portGood)

        //ma asigur ca nodul la care trimit nu este in lista de fisiere la care trebuie el sa trimita in continuare mesajul
        listPorts.remove(randomPort)
        //println("lista de porturi ce nu contin fisierul $key este: $listPorts")

        //creez mesajul cu codul de 21
        //mesajul meu este trimis catre randomPort cu mesajul: ia fisierul asta citeste-l de pe hard disk-ul tau si trimite-l la portul portGood
        //imi creez mesajul sub forma de json
        val gsonString = createMessage(key, 21, listPorts)

        //trimit mesajul catre nodul care are fisierul
        val packetSend = DatagramPacket(gsonString.toByteArray(), gsonString.length, group, randomPort)
        //trimit mesajul catre nodul ales random
        managerCentralSocket.send(packetSend)
        println("am trimis mesajul catre nod")
    }

    //imi verifica pe pe noduri am respectivul fisier si imi face o lista cu porturile -> din lista aia aleg un port random si ii trimit comanda sa stearga fisierul respectiv
    override fun sendMessageToNodeForDeleteFile(
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        key: String,
        managerCentralSocket: MulticastSocket,
        group: InetAddress
    ) {
        val ports : MutableList<Int> = mutableListOf()
        //key e numele fisierului iar functia imi intoarce o lista de noduri care contin acel fisier
        val listPorts = verificationContainsValue(filesMap, key)
        println("se trimite comanda de stergere fisier")

        //aleg un nod random de pe care vreau sa sterg fisierul
        val randomPort = listPorts.random()

        //imi creez mesajul sub forma de json
        val gsonString = createMessage(key, 20, ports)

        val packetSend = DatagramPacket(gsonString.toByteArray(), gsonString.length, group, randomPort)
        managerCentralSocket.send(packetSend) //trimit mesajul catre nodul ales random
    }

    override fun filesReplication(
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>,
        managerCentralSocket: MulticastSocket
    ) {
        val theInetAddress: InetAddress = InetAddress.getByName("localhost")
        val listFile: MutableList<String> = mutableListOf()
        for (value in filesMap.values) {
            for (v in value) {
                listFile.add(v.fileName)
            }
        }
        //aici am un hashMap cu fiecare fisier de pe toate nodurile si numarul replicilor (de cate ori apare in retea)
        val freqFiles = countFrequency(listFile)
        //println("lista de frecvente: $freqFiles")

        val nrNodes = getNrOfNodes(filesMap)
        //daca numarul nodurilor pornite este mai mic decat un factor de replicare dat
        if(nrNodes < replicationFactory){
            //se actualizeaza factorul de replicare cu numarul respectiv
            for ((key, value) in freqFiles) {
                //println("$key = $value")
                //de acum de verificat daca e mai mic sau mare decat factorul de replicare
                if (value < nrNodes) {
                    //de verificat ce nod contine fisierul a carei nr de apartitie este mai mic decat factorul de replicare si de trimis mesaj catre portul ala ca sa trimita fisierul la un alt port dat de mine
                    sendMessageToNodeForCopyFileAndSendItToAnotherNode(
                        filesMap,
                        key,
                        managerCentralSocket,
                        theInetAddress
                    )
                } else if (value > nrNodes) {
                    //de verificat ce nod contine fisierul a carei nr de apartitie este mai mare decat factorul de replicare si de trimis mesaj catre portul ala ca sa steraga fisierul
                    sendMessageToNodeForDeleteFile(filesMap, key, managerCentralSocket, theInetAddress)
                } else {
                    println("Este totul in regula!")
                }
            }
        }else{
            //altfel se mentine factorul de replicare initial
            for ((key, value) in freqFiles) {
                //println("$key = $value")
                //de acum de verificat daca e mai mic sau mare decat factorul de replicare
                if (value < replicationFactory) {
                    //de verificat ce nod contine fisierul a carei nr de apartitie este mai mic decat factorul de replicare si de trimis mesaj catre portul ala ca sa trimita fisierul la un alt port dat de mine
                    sendMessageToNodeForCopyFileAndSendItToAnotherNode(filesMap, key, managerCentralSocket, theInetAddress)
                } else if (value > replicationFactory) {
                    //de verificat ce nod contine fisierul a carei nr de apartitie este mai mare decat factorul de replicare si de trimis mesaj catre portul ala ca sa steraga fisierul
                    sendMessageToNodeForDeleteFile(filesMap, key, managerCentralSocket, theInetAddress)
                }else{
                    println("Este totul in regula!")
                }
            }
        }
    }

    //verific harta sa vad care sunt cele mai putin incarcate noduri
    //fac o lista cu cele mai putin incarcate (adica cu numarul portului)
    //o trimit catre ServerulWeb care o va trimite catre noduri
    //numarul nodurilor trimis catre server trebuie sa fie egal cu factorul de replicare  => aici sa ma mai gandesc putin!!!!
    override fun getFreeNodes(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<Int> {
        val listNodes: MutableList<Int> = mutableListOf()
        val countFiles = hashMapOf<Int, Int>()
        for ((key, value) in filesMap) {
            val list = splitKeyOfHashMap(key)
            countFiles[list[1].toInt()] = value.size
        }
        //{port=nr fisiere}
        val nrNodes = getNrOfNodes(filesMap)
        val result = countFiles.toList().sortedBy { (_, value) -> value }.toMap()

        if(nrNodes < replicationFactory){
            if (result.size >= nrNodes) {
                for (i in 0 until nrNodes) {
                    listNodes.add(result.keys.elementAt(i))
                }
            } else {
                println("Nodurile deschise sunt insuficiente!")
            }
        }else{
            if (result.size >= replicationFactory) {
                for (i in 0 until replicationFactory) {
                    listNodes.add(result.keys.elementAt(i))
                }
            }else{
                println("Nodurile deschise sunt insuficiente!")
            }
        }
        //println("result is: $result")
        println("nodesFree este: $listNodes")
        return listNodes
    }

    //imi da portul de pe care sa se descarce fisierul cu numele dat ca parametru (nod ce este trimis catre ServerulWeb)
    override fun getPortNode(
        idUser: Int,
        fileName: String,
        filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>
    ): Int {
        //verific ce port are fisierul si il trimit la server
        val fileN = idUser.toString() + "/" + fileName
        println("fileN este: $fileN")
        val ports: MutableList<Int> = mutableListOf()
        for ((key, value) in filesMap) {
            //imi separa IP-ul de PORT
            val list = splitKeyOfHashMap(key)
            for (v in value) {
                if (v.fileName == fileN) {
                    ports.add(list[1].toInt())
                }
            }
        }
        return ports.random()
    }

    override fun changeHashMap(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): HashMap<String, List<String>> {
        val fileMap: HashMap<String, List<String>> = hashMapOf()

        for ((key, value) in hashMapFiles) {
            val listFiles: MutableList<String> = mutableListOf()
            for (v in value) {
                listFiles.add(v.fileName)
            }
            fileMap[key] = listFiles
        }
        println(fileMap)
        return fileMap
    }

    override fun getFilesMap(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<FilesMap>{
        val filesMap = changeHashMap(hashMapFiles)
        //println("harta este: $filesMap")
        val listFilesMap : MutableList<FilesMap> = mutableListOf()

        for ((key, value) in filesMap) {
            //println("key:  $key si value: $value")
            val list = key.split(";")

            val nodeName = getNameNode(list[1])
            listFilesMap.add(FilesMap(nodeName, value))
        }
        println("lista: $listFilesMap")
        return listFilesMap
    }

    override fun getFilesMapNew(hashMapFiles: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<FilesMapNew>{
        val filesMap = changeHashMap(hashMapFiles)
        //println("harta este: $filesMap")
        val listFilesMap : MutableList<FilesMapNew> = mutableListOf()
        val hashMapUsers : HashMap<String, MutableList<String>> = hashMapOf()

        for ((key, value) in filesMap) {
            //println("key:  $key si value: $value")
            val list = key.split(";")
            val nodeName = getNameNode(list[1])

            val listFilesUsers = mutableListOf<String>()
            //parcurg lista de fisiere din hash-ul mare si o despart intr-un hash cu cheia=id-ul userului si
            //valoarea fiind lista de fisiere a utilizatorului respectiv
            for(v in value){
                //separa numele fiecarui fisier
                val listUser = v.split("/")

                if(!hashMapUsers.containsKey(listUser[0])){
                    //daca nu contine cheia introduc datele in hash
                    listFilesUsers.add(listUser[1])
                    hashMapUsers[listUser[0]] = listFilesUsers
                }else {
                    //daca cheia este deja in hash, actualizez valoarea listei
                    //verific daca lista mea din hash contine deja numele fisierului
                    if(hashMapUsers[listUser[0]]!!.contains(listUser[1])) {
                        break //daca da, nu il mai adauga inca o data
                    }else{
                        hashMapUsers[listUser[0]]?.add(listUser[1]) //daca nu actualizeaza hashul
                    }
                }
            }
            listFilesMap.add(FilesMapNew(nodeName, hashMapUsers))
        }
        println("lista: $listFilesMap")
        return listFilesMap
    }

    override fun getNameNode(port: String): String{
        return when(port.toInt()) {
            49788 -> "Node1"
            49789 -> "Node2"
            49790 -> "Node3"
            49791 -> "Node4"
            49792 -> "Node5"
            else -> {
                "Nu exista portul!"
            }
        }
    }

    override fun getUsersFromFilesMap(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): MutableList<UserData> {
        println("*************************")
        val map = changeHashMap(filesMap)
        println("harta este: $filesMap")

        val listUserFilesMap : MutableList<UserData> = mutableListOf()
        val hashMapUsers : HashMap<String, MutableList<String>> = hashMapOf()

        for ((key, value) in map) {
            //println("key:  $key si value: $value")
            val listFilesUsers = mutableListOf<String>()
            //parcurg lista de fisiere din hash-ul mare si o despart intr-un hash cu cheia=id-ul userului si
            //valoarea fiind lista de fisiere a utilizatorului respectiv
            for(v in value){
                //separa numele fiecarui fisier
                val listUser = v.split("/")
                if(!hashMapUsers.containsKey(listUser[0])){
                    //daca nu contine cheia introduc datele in hash
                    listFilesUsers.add(listUser[1])
                    hashMapUsers[listUser[0]] = listFilesUsers
                }else {
                    //daca cheia este deja in hash, actualizez valoarea listei
                    //verific daca lista mea din hash contine deja numele fisierului
                    if(hashMapUsers[listUser[0]]!!.contains(listUser[1])) {
                        break //daca da, nu il mai adauga inca o data
                    }else{
                        hashMapUsers[listUser[0]]?.add(listUser[1]) //daca nu actualizeaza hashul
                    }
                }
            }
        }
        for ((key, value) in hashMapUsers){
            val user = UserData(key, value)
            listUserFilesMap.add(user)
        }
        println("lista de utilizatori este: $listUserFilesMap")
        return listUserFilesMap
    }

    override fun getNrOfNodes(filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>>): Int {
        println("---------------------------------------------")
        println("numarul de noduri pornite: ${filesMap.size}")
        return filesMap.size
    }
}
