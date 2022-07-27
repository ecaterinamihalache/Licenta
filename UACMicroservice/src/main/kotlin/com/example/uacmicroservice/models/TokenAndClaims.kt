package com.example.uacmicroservice.models

data class TokenAndClaims(val token: String,
                          val accountId: Int,
                          val role: String
                          )
