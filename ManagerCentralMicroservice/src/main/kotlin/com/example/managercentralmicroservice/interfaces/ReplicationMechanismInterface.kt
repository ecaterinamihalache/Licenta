package com.example.managercentralmicroservice.interfaces

import java.net.MulticastSocket

interface ReplicationMechanismInterface {
    fun replicationMechanism(managerCentralSocket: MulticastSocket)
}