package com.licenta.nodemicroservice.interfaces

import java.net.DatagramSocket
import java.net.InetAddress

interface HeartbeatInterface {
    fun heartbeatSendMessage(socketNode: DatagramSocket, group: InetAddress, port: Int)
}