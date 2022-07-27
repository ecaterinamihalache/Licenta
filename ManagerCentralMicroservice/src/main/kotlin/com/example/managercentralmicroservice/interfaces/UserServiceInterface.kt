package com.example.managercentralmicroservice.interfaces

import com.example.managercentralmicroservice.models.User
import java.util.*

interface UserServiceInterface {
    //fun getAllUsers()
    fun getUsersID():List<Int>
    fun addUser(user: User): Boolean
    fun getUserById(id: Int): Optional<User>
    fun deleteUserById(id: Int): Boolean
    fun updateUser(id: Int, user: User): User
}