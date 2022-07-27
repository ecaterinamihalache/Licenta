package com.licenta.nodemicroservice.models

data class JsonData(var ipPortNode: String, var listFilesToSend: MutableList<FileTransfer>, var message: String)