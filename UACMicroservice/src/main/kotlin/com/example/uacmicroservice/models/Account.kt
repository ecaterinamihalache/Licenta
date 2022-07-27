package com.example.uacmicroservice.models

import javax.persistence.*

@Entity
@Table(name = "Accounts")
class Account(id: Int, email: String, password: String, role: String, firstname: String, lastname: String) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: Int = 0
    private var email: String = ""
    private var password: String = ""
    private var role: String = ""
    private var firstname: String = ""
    private var lastname: String = ""

    init {
        this.id = id
        this.email = email
        this.password = password
        this.role = role
        this.firstname = firstname
        this.lastname = lastname
    }

    fun getId(): Int{
        return id
    }

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }

    fun getRole(): String {
        return role
    }

    fun getFirstName(): String {
        return firstname
    }

    fun getLastName(): String {
        return lastname
    }

    fun setId(id: Int){
       this.id = id
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun setPassword(password : String){
        this.password = password
    }

    fun setRole(role: String){
        this.role = role
    }

    fun setFirstName(firstname: String){
        this.firstname = firstname
    }

    fun setLastName(lastname : String){
        this.lastname = lastname
    }
}