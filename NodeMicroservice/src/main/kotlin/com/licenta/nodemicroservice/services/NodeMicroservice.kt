package com.licenta.nodemicroservice.services

import com.licenta.nodemicroservice.interfaces.NodeServiceInterface
import java.io.IOException
import java.net.*

class NodeMicroservice {
    private var buffer: ByteArray? = null
    private var heartBeat: Heartbeat? = Heartbeat()
    private var verifyCRC: VerifyCRC? = VerifyCRC()
    private var theInetAddress : InetAddress? = null
    private lateinit var socketS : DatagramSocket
    private var nodeServiceInterface : NodeServiceInterface = NodeService()

    private var group = InetAddress.getByName("230.0.0.1")
    private val serverPort: Int = this.javaClass.classLoader.getResource("serverPort1.txt").readText().toInt()
    private var socketNode: DatagramSocket = DatagramSocket(serverPort)

    companion object {
        const val GROUP_PORT = 4446
    }

    fun receiveMessage(buffer: ByteArray): String {
        val packet = DatagramPacket(buffer, buffer.size)
        socketNode.receive(packet)
        //se afiseaza continutul pachetului
        val s = String(packet.data, packet.offset, packet.length)
        println("Mesajul primit este: $s")
        return s
    }

    fun startNod() {
        try {
            println("Nodul se executa pe portul: ${socketNode.localPort} si la adresa: ${socketNode.localSocketAddress}")
            println("Se asteapta conexiuni si mesaje...")

            val heartbeatThread = Thread {
                try {
                    heartBeat = Heartbeat()
                    heartBeat!!.heartbeatSendMessage(socketNode, group, GROUP_PORT)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val crcThread = Thread {
                try {
                    verifyCRC = VerifyCRC()
                    verifyCRC!!.verifyCRCForEachFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            heartbeatThread.start()
            crcThread.start()

            while (true) {
                theInetAddress = InetAddress.getByName("localhost")
                socketS = DatagramSocket()
                buffer = ByteArray(2048)
                val message = receiveMessage(buffer!!)

                nodeServiceInterface.executeCommand(message, serverPort, socketS)
            }
        } finally {
            socketNode.close()
        }
    }

    fun stopNod(){
        println("se trimite mesajul de inchis!")
        val jsonString = nodeServiceInterface.createMessageForShutDown(socketNode)
        nodeServiceInterface.sendMessage(jsonString, socketNode, group, GROUP_PORT)
    }
}