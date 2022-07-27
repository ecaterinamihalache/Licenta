package com.example.managercentralmicroservice.controllers

import com.example.managercentralmicroservice.services.ManagerCentralMicroservice
import com.example.managercentralmicroservice.interfaces.ManagerCentralInterfaceService
import com.example.managercentralmicroservice.interfaces.UserServiceInterface
import com.example.managercentralmicroservice.models.FilesMap
import com.example.managercentralmicroservice.models.User
import com.example.managercentralmicroservice.services.ManagerCentralService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(path = ["manager"])
class ManagerCentralController(
    private val userServiceInterface: UserServiceInterface
) {
    private val managerCentralInterfaceService: ManagerCentralInterfaceService = ManagerCentralService()

    @GetMapping(path = ["/files"])
    fun getPorts(): ResponseEntity<MutableList<Int>> {
        val listPortsNodes: MutableList<Int> =
            managerCentralInterfaceService.getFreeNodes(ManagerCentralMicroservice.filesMap)
        println("aici in manager controller: $listPortsNodes")
        return if (listPortsNodes.isEmpty()) ResponseEntity(null, HttpStatus.NOT_FOUND) else ResponseEntity(
            listPortsNodes,
            HttpStatus.OK
        )
    }

    @GetMapping(path = ["/userFiles/{id}"])//pentru id-ul asta da-mi fisierele user-ului asta
    fun getUserFiles(@PathVariable id: Int): ResponseEntity<List<String>> {
        println("id-ul este: $id")
        var listFiles = arrayListOf<String>()
        val listUsersFiles = managerCentralInterfaceService.getUsersFromFilesMap(ManagerCentralMicroservice.filesMap)

        for (user in listUsersFiles){
            if(user.id.toInt() == id){
                listFiles = user.files as ArrayList<String>
            }
        }
        if(listFiles.isEmpty()){
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(listFiles, HttpStatus.OK)
//        val user = userServiceInterface.getUserById(id)
//        if(user.get().getFiles().isEmpty()){
//            return ResponseEntity(null, HttpStatus.NOT_FOUND)
//        }
//        return ResponseEntity(user.get().getFiles(), HttpStatus.OK)
    }

    @PostMapping(path = ["/users"])
    fun addAccount(@RequestBody user: User): ResponseEntity<User> {
        println("user email: ${user.getEmail()}")
        val usersID = this.userServiceInterface.getUsersID()
        //daca exista deja user-ul cu id-ul dat fac update
        if(usersID.contains(user.getId())){
            val updatedUser: User = this.userServiceInterface.updateUser(user.getId(), user)
            return ResponseEntity(updatedUser, HttpStatus.OK)
        }else{
            //daca nu il adaug
            val isSuccessful = this.userServiceInterface.addUser(user)
            if (!isSuccessful) {
                return ResponseEntity(null,HttpStatus.BAD_REQUEST )
            }
        }
        return ResponseEntity(user, HttpStatus.CREATED)
    }

    @GetMapping(path = ["/hashMapFiles"])
    fun getHashMapFiles(): ResponseEntity<List<FilesMap>> {
        val hashMapFiles = ManagerCentralMicroservice.filesMap
        val filesMap = managerCentralInterfaceService.getFilesMap(hashMapFiles)
        return ResponseEntity(filesMap, HttpStatus.OK)
    }

    @GetMapping(path = ["/downloadFile/{idUser}/{fileName}"])
    fun getPortNode(@PathVariable idUser: Int, @PathVariable fileName: String): ResponseEntity<Int> {
        println("id-ul primit: $idUser")
        println("fisierul primit: $fileName")
        val port = managerCentralInterfaceService.getPortNode(idUser, fileName, ManagerCentralMicroservice.filesMap)
        return ResponseEntity(port, HttpStatus.OK)
    }
}