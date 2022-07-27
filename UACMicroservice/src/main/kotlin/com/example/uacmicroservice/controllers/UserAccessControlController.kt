package com.example.uacmicroservice.controllers

import com.example.uacmicroservice.interfaces.AccountInterface
import com.example.uacmicroservice.interfaces.HashingInterface
import com.example.uacmicroservice.interfaces.JwtFactoryInterface
import com.example.uacmicroservice.interfaces.JwtParserInterface
import com.example.uacmicroservice.models.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

//http://127.0.0.1:8080/uac/
@CrossOrigin
@RestController
@RequestMapping("uac")
class UserAccessControlController {

    @Autowired
    var hs256KeyProperties: HSKeyProperties? = null

    @Autowired
    var jwtFactory: JwtFactoryInterface<String>? = null

    @Autowired
    var jwtParser: JwtParserInterface<String>? = null

    @Autowired
    var accountService: AccountInterface? = null

    @Autowired
    var hashingService: HashingInterface? = null

    @PostMapping("/authenticate")
    fun authenticateUser(@RequestBody credentials: Credentials): ResponseEntity<TokenAndClaims?> {
        val optionalAccount: Optional<Account?>? = accountService!!.getAccountByCredentials(
            credentials.getEmail(),
            hashingService!!.encode(credentials.getPassword())
        )
        val validCredentials = optionalAccount!!.isPresent
        return if (validCredentials) {
            val token = jwtFactory?.generateToken(object : HashMap<String, String>() {
                init {
                    put("role", optionalAccount.get().getRole())
                }
            }, hs256KeyProperties?.getSecretKey())
            val tokenAndClaims = TokenAndClaims(token!!, optionalAccount.get().getId(), optionalAccount.get().getRole())
            ResponseEntity(tokenAndClaims, HttpStatus.CREATED)
        } else {
            ResponseEntity(null, HttpStatus.UNAUTHORIZED)
        }
    }

    @PostMapping("/validate-token")
    fun validateToken(@RequestBody token: Token): ResponseEntity<*>? {
        return if (hs256KeyProperties?.getSecretKey()?.let {
                jwtParser?.isTokenValid(
                    token.getToken(), it
                )
            } == true
        ) ResponseEntity.status(
            HttpStatus.OK
        ).build<Any>() else ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()
    }
}