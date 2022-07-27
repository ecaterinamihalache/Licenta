package com.example.managercentralmicroservice.services

import com.example.managercentralmicroservice.models.FileTransfer
import com.example.managercentralmicroservice.interfaces.ManagerCentralInterfaceService
import com.example.managercentralmicroservice.interfaces.ReplicationMechanismInterface
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.ConcurrentHashMap

class ManagerCentralMicroservice {
    private var managerCentralService: ManagerCentralInterfaceService = ManagerCentralService()
    private var buffer: ByteArray? = null
    private var replicationMechanism: ReplicationMechanismInterface? = ReplicationMechanism()

    companion object {
        const val GROUP_PORT = 4446
        @JvmStatic var filesMap: ConcurrentHashMap<String, MutableList<FileTransfer>> = ConcurrentHashMap()
        var managerCentralSocket: MulticastSocket = MulticastSocket()
    }

    private fun receiveMessage(buffer: ByteArray): String {
        val packetReceive = DatagramPacket(buffer, buffer.size)
        managerCentralSocket.receive(packetReceive)
        //se afiseaza continutul pachetului
        return String(packetReceive.data, packetReceive.offset, packetReceive.length)
    }

    fun startServer() {
        //adresa IP si portul care reprezinta grupul de clienti
        val group = InetAddress.getByName("230.0.0.1")

        try {
            println("Nodul se executa pe portul: ${managerCentralSocket.localPort} si la adresa: ${managerCentralSocket.localSocketAddress}")
            println("Se asteapta conexiuni si mesaje...")

            //Se alatura grupului aflat la adresa si portul specificate
            managerCentralSocket = MulticastSocket(GROUP_PORT)
            managerCentralSocket.joinGroup(group)

            val replicationMechanism = Thread{
                Thread.sleep(40000)
                try{
                    replicationMechanism = ReplicationMechanism()
                    replicationMechanism!!.replicationMechanism(managerCentralSocket)
                }catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            replicationMechanism.start()

            //debug
            while (true) {
                val receiveMessage = Thread {
                    try {
                        buffer = ByteArray(2048)
                        val message = receiveMessage(buffer!!)
                        val flag = managerCentralService.verifyMessage(message)

                        when (flag) {
                            "EXIT" -> { //daca se deconecteaza nodul
                                filesMap = managerCentralService.updateMapFiles(message, filesMap) //actualizez hashMap-ul -> sterg nodul
                                println("hashMap-ul actualizat este: $filesMap")
                            }
                            else -> {
                                filesMap = managerCentralService.createMapFiles(message)
                            }
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                receiveMessage.start()
            }
        } finally {
            managerCentralSocket.leaveGroup(group)
            managerCentralSocket.close()
        }
    }
}
