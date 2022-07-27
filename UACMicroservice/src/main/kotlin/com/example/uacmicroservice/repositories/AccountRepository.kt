package com.example.uacmicroservice.repositories

import com.example.uacmicroservice.models.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRepository : CrudRepository<Account, Int>{
    fun findByEmailAndPassword(email: String?, password: String?): Optional<Account?>?
    fun save(account: Account?)
    fun findByEmail(email: String?): Optional<Account?>?
}