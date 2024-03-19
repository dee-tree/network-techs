package edu.sokolov.proxy

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

fun main(args: Array<String>) {
    val remoteServer =
        args.getOrNull(0) ?: throw IllegalArgumentException("Expected remote server as the first argument")

    val remotePort = args.getOrNull(1)?.toInt()
        ?: throw IllegalArgumentException("Expected remote server port as the second argument")
    if (remotePort < 0 || remotePort > 65535) throw IllegalStateException("Illegal port ${remotePort}. Expected 0..65535")

    val port = args.getOrNull(2)?.toInt()
        ?: throw IllegalArgumentException("Expected port for proxy server as the third argument")
    if (port < 0 || port > 65535) throw IllegalStateException("Illegal port ${port}. Expected 0..65535")

    val modeling = args.getOrNull(3)?.toBoolean() ?: false

    val proxy = ProxyServer(remoteServer, remotePort, port)

    runBlocking {
        launch {
            withContext(Dispatchers.IO) {
                proxy.start()
            }
        }
        launch {
            if (!modeling) return@launch
            delay(100)

            logger.debug { "Modelling GET request of /index.html page" }
            val socket = Socket("127.0.0.1", port)
            val writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            writer.println("GET /index.html")

            logger.debug { "Got answer from the server:" }
            reader.readLines().joinToString("\n").also { println(it) }

//            delay(10000)
        }
    }
}

private val logger = KotlinLogging.logger { }