package com.example.uacmicroservice.services

import com.example.uacmicroservice.interfaces.AccountInterface
import com.example.uacmicroservice.models.Account
import com.example.uacmicroservice.repositories.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer


@Service
class AccountService : AccountInterface {

    @Autowired
    private val accountRepository: AccountRepository? = null

    override fun getAccounts(): List<Account?>? {
        val iterable = accountRepository!!.findAll()
        val accounts = ArrayList<Account>()
        iterable.forEach(Consumer { e: Account -> accounts.add(e) })

        return accounts
    }

    override fun addAccount(account: Account?): Boolean {
        return try {
            this.accountRepository!!.save(account)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteAccountById(id: Int?): Boolean {
        return if (id?.let { this.accountRepository!!.findById(it) } != null) {
            accountRepository?.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun getAccountByCredentials(email: String?, password: String?): Optional<Account?>? {
        return this.accountRepository?.findByEmailAndPassword(email, password)
    }

    override fun getAccountById(id: Int?): Account? {
        val account: Optional<Account?> = accountRepository!!.findById(id!!)
        return account.orElse(null)
    }

    override fun updateAccount(id: Int?, account: Account?): Account? {
        return if (id?.let { this.accountRepository!!.findById(it) } != null) {
            try {
                accountRepository!!.save(account)
                account
            } catch (e: java.lang.Exception) {
                null
            }
        } else {
            null
        }
    }

    override fun getEmail(email: String): Boolean {
        return this.accountRepository?.findByEmail(email)?.isPresent == true
    }
}

