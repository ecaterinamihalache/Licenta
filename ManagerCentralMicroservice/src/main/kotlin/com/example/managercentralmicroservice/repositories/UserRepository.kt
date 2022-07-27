package com.example.managercentralmicroservice.repositories

import com.example.managercentralmicroservice.models.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository: MongoRepository<User, Int>{
    fun save(user: User)
}