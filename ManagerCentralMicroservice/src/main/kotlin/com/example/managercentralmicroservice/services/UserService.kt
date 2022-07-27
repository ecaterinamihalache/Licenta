package com.example.managercentralmicroservice.services

import com.example.managercentralmicroservice.interfaces.UserServiceInterface
import com.example.managercentralmicroservice.models.User
import com.example.managercentralmicroservice.repositories.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserService (
    var userRepository: UserRepository,
    var simpMessagingTemplate: SimpMessagingTemplate
) : UserServiceInterface {

//    @Scheduled(cron = "*/5 * * * * *")
//    override fun getAllUsers() {
//        try {
//            this.simpMessagingTemplate.convertAndSend("/topic/users", userRepository.findAll())
//        } catch (e: Exception) {
//            this.simpMessagingTemplate.convertAndSend("/topic/users", null)
//        }
//    }

    override fun addUser(user: User): Boolean {
        return try {
            this.userRepository.save(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getUserById(id: Int): Optional<User> {
        return userRepository.findById(id)
    }

    override fun getUsersID():List<Int> {
        val users : List<User> = userRepository.findAll()

        val usersID : MutableList<Int> = mutableListOf()
        //in lista usersID pun toate id-urile din users si le returnez
        for (user in users){
            usersID.add(user.getId())
        }
        return usersID
    }

    override fun deleteUserById(id: Int): Boolean {
        return run {
            userRepository.deleteById(id)
            true
        }
    }

    override fun updateUser(id: Int, user: User): User {
        var updateFiles : Array<String> = arrayOf()
        val userOld = getUserById(id) //caut datele user-ului

        val files = userOld.get().getFiles()//array cu fisierele vechi
        val newFiles = user.getFiles() //array-ul cu fisierele noi

        //daca are deja fisierul respectiv nu se actualizeaza lista de fisiere
        updateFiles = if(files.contains(newFiles[0])){
            files
        }else{
            //array-ul ce actulizeaza fisierele vechi si le adauga pe cele noi
            files + newFiles
        }

        //fac update la datele userului
        val updateUser = User(id, user.getEmail(), updateFiles)
        return run {
            userRepository.save(updateUser)
            updateUser
        }
    }
}