package com.example.managercentralmicroservice.controllers

import com.example.managercentralmicroservice.services.ManagerCentralMicroservice
import com.example.managercentralmicroservice.interfaces.ManagerCentralInterfaceService
import com.example.managercentralmicroservice.services.ManagerCentralService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController


@CrossOrigin
@RestController
class FilesMapController(
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val managerCentralInterfaceService: ManagerCentralInterfaceService = ManagerCentralService()

    @Scheduled(cron = "*/5 * * * * *")
    @MessageMapping("/hashMapFiles")
    @SendTo("/topic/hashMapFiles")
    fun getHashMapFiles() {
        val hashMapFiles = ManagerCentralMicroservice.filesMap
        val filesMap = managerCentralInterfaceService.getFilesMap(hashMapFiles)

        this.simpMessagingTemplate.convertAndSend("/topic/hashMapFiles", filesMap)
    }

    @Scheduled(cron = "*/5 * * * * *")
    @MessageMapping("/users")
    @SendTo("/topic/users")
    fun getAllUsers() {
        val hashMapFiles = ManagerCentralMicroservice.filesMap
        val userFiles = managerCentralInterfaceService.getUsersFromFilesMap(hashMapFiles)

        this.simpMessagingTemplate.convertAndSend("/topic/users", userFiles)
    }
}