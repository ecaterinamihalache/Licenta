package com.example.managercentralmicroservice.services

import com.example.managercentralmicroservice.services.ManagerCentralMicroservice
import com.example.managercentralmicroservice.interfaces.ManagerCentralInterfaceService
import com.example.managercentralmicroservice.interfaces.ReplicationMechanismInterface
import com.example.managercentralmicroservice.services.ManagerCentralService
import java.net.MulticastSocket
import java.util.*

class ReplicationMechanism : ReplicationMechanismInterface {
    private var managerCentralService: ManagerCentralInterfaceService?= ManagerCentralService()

    override fun replicationMechanism(managerCentralSocket: MulticastSocket) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                managerCentralService?.filesReplication(ManagerCentralMicroservice.filesMap, managerCentralSocket)
            }
        }, 0, 40000) //1 minut -> 60 secunde -> 60 000 milisecunde
    }
}