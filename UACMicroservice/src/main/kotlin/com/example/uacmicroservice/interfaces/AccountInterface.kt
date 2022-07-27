package com.example.uacmicroservice.interfaces

import com.example.uacmicroservice.models.Account
import java.util.*

interface AccountInterface {
    fun getAccounts(): List<Account?>?
    fun addAccount(account: Account?): Boolean
    fun deleteAccountById(id: Int?): Boolean
    fun getAccountByCredentials(email: String?, password: String?): Optional<Account?>?
    fun getAccountById(id: Int?): Account?
    fun updateAccount(id: Int?, account: Account?): Account?
    fun getEmail(email: String): Boolean
}