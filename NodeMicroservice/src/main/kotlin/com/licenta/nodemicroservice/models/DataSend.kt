package com.licenta.nodemicroservice.models

import java.io.File

data class DataSend(var fileName: String, var fileContent: ByteArray, var cod: Int, var idUser: Int, var listPorts: MutableList<Int>)

