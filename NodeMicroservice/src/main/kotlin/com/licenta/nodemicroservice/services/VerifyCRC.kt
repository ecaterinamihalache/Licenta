package com.licenta.nodemicroservice.services

import com.licenta.nodemicroservice.interfaces.CrcInterface
import com.licenta.nodemicroservice.interfaces.NodeServiceInterface
import com.licenta.nodemicroservice.services.NodeService
import java.util.*

class VerifyCRC : CrcInterface {
    private var nodeServiceInterface : NodeServiceInterface = NodeService()

    override fun verifyCRCForEachFile() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                nodeServiceInterface.verifyCrc()
            }
        }, 0, 60000) //5s = 5000 10s = 10 000 60 s = 60 000 = 1 min
    }
}