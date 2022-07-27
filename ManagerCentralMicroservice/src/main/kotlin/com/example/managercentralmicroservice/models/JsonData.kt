package com.example.managercentralmicroservice.models

data class JsonData(var ipPortNode: String, var listFilesToSend: MutableList<FileTransfer>, var message: String)