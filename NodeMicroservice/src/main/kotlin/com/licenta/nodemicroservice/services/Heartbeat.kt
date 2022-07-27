package com.licenta.nodemicroservice.services

import com.licenta.nodemicroservice.interfaces.HeartbeatInterface
import com.licenta.nodemicroservice.interfaces.NodeServiceInterface
import com.licenta.nodemicroservice.services.NodeService
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class Heartbeat : HeartbeatInterface {
    private var nodeServiceInterface : NodeServiceInterface = NodeService()

    override fun heartbeatSendMessage(socketNode: DatagramSocket, group: InetAddress, port: Int) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val jsonString = nodeServiceInterface.createMessageForSend(socketNode)
                nodeServiceInterface.sendMessage(jsonString, socketNode, group, port)
            }
        }, 0, 5000) //5sec
    }
}

