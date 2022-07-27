package com.licenta.nodemicroservice

import com.licenta.nodemicroservice.services.NodeMicroservice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NodeMicroserviceApplication

fun main(args: Array<String>) {
    runApplication<NodeMicroserviceApplication>(*args)
    val nodeMicroservice = NodeMicroservice()

    Runtime.getRuntime().addShutdownHook(Thread {
        println("se inchide!")
        nodeMicroservice.stopNod()
    })
    nodeMicroservice.startNod()
}
