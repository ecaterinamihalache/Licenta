package com.example.managercentralmicroservice

import com.example.managercentralmicroservice.services.ManagerCentralMicroservice
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ManagerCentralMicroserviceApplication

fun main(args: Array<String>) {
    runApplication<ManagerCentralMicroserviceApplication>(*args)
    val managerCentralMicroservice = ManagerCentralMicroservice()
    managerCentralMicroservice.startServer()
}
