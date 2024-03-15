package edu.sokolov.websock

class RequestException(message: String, val errorCode: Int? = null) : Exception(message) {
    override fun toString(): String = "RequestException(code=${errorCode}, message: ${message})"
}