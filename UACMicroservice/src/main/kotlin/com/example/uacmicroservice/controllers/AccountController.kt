package com.example.uacmicroservice.controllers

import com.example.uacmicroservice.interfaces.AccountInterface
import com.example.uacmicroservice.interfaces.HashingInterface
import com.example.uacmicroservice.models.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@CrossOrigin
@RequestMapping(path = arrayOf("uac/uac/accounts"))
class AccountController {
    @Autowired
    private val accountInterface: AccountInterface? = null

    @Autowired
    private val hashingInterface: HashingInterface? = null

    @GetMapping(path = [""])
    fun getAccounts(): ResponseEntity<List<Account?>?>? {
        val accounts = accountInterface!!.getAccounts()
        return if (accounts!!.isEmpty()) ResponseEntity(null, HttpStatus.NOT_FOUND) else ResponseEntity(
            accounts,
            HttpStatus.OK
        )
    }

    @GetMapping(path = ["/{id}"])
    fun getAccountById(@PathVariable id: Int?): ResponseEntity<Account?>? {
        val account = accountInterface!!.getAccountById(id)
        return if (account != null) ResponseEntity(account, HttpStatus.OK) else ResponseEntity(
            null,
            HttpStatus.NOT_FOUND
        )
    }

    @PostMapping(path = [""])
    fun addAccount(@RequestBody account: Account): ResponseEntity<Account> {
        return if (accountInterface?.getEmail(account.getEmail()) == false) {
            val hashedPassword = hashingInterface!!.encode(account.getPassword())
            account.setPassword(hashedPassword!!)

            val isSuccessful = this.accountInterface.addAccount(account)
            if (isSuccessful) ResponseEntity(account, HttpStatus.CREATED) else ResponseEntity(
                null,
                HttpStatus.BAD_REQUEST
            )
        } else {
            ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteAccount(@PathVariable id: Int?): ResponseEntity<*>? {
        val isSuccessful = accountInterface!!.deleteAccountById(id)
        return if (isSuccessful) ResponseEntity<Account>(HttpStatus.NO_CONTENT) else ResponseEntity<Any>(HttpStatus.NOT_FOUND)
    }

    @PutMapping(path = ["/{id}"])
    fun updateAccount(@PathVariable id: Int, @RequestBody account: Account): ResponseEntity<Account?>? {
        account.setId(id)
        val hashedPassword: String? = hashingInterface?.encode(account.getPassword())
        hashedPassword?.let { account.setPassword(it) }
        val updatedAccount: Account? = this.accountInterface?.updateAccount(id, account)
        return if (updatedAccount != null) ResponseEntity(updatedAccount, HttpStatus.OK) else ResponseEntity(
            null,
            HttpStatus.NOT_FOUND
        )
    }
}