package edu.sokolov.websock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readLines


class HttpServer internal constructor(private val socket: ServerSocket) : Closeable {
    constructor(port: Int = 80) : this(ServerSocket(port))

    private lateinit var job: Job
    private var isClosed = false
    private var activeConnectionsCount = 0

    suspend fun start() = coroutineScope {
        if (isClosed) return@coroutineScope

        job = launch {
            outer@ while (isActive) {
                logger.debug { "Waiting for a connection at port ${socket.localPort}" }
                val deferredClient = async { socket.acceptCancellable() }

                val clientConnection = deferredClient.await() ?: break@outer
                activeConnectionsCount++
                logger.debug { "Established a connection with a client: ${clientConnection.inetAddress}:${clientConnection.port}" }
                logger.debug { "Connected clients: $activeConnectionsCount" }
                handleClient(clientConnection)
            }
            logger.debug { "Exit looper" }
        }
    }

    private fun CoroutineScope.handleClient(client: Socket) = launch {
        BufferedReader(InputStreamReader(client.inputStream)).use { istream ->
            PrintWriter(OutputStreamWriter(client.outputStream)).use { out ->
                val requestPath = readFileRequest(client, istream)
                println(requestPath)
                out.println(buildFileAnswer(requestPath))
//                while (isActive && !client.isClosed) {
//                    val clientMsg = istream.readLine().toString() ?: break
//                    println("client msg: ${clientMsg}")
//                    if (clientMsg == ".") break
//                }
            }
        }
        logger.debug { "Close connection with the client ${client.inetAddress}:${client.port}" }
        client.close()
        activeConnectionsCount--
    }

    private fun CoroutineScope.readFileRequest(client: Socket, istream: BufferedReader): Path {
        var line = 0
        var path: Path? = null
        while (isActive && !client.isClosed && line < 3) {
            val clientMsg = istream.readLine() ?: break

            when (line) {
                0 -> path = clientMsg.parseMethodLine()
            }

            line++
        }

        return path ?: throw RequestException("")
    }

    override fun close() {
        isClosed = true
        runBlocking {
            job.cancel()
            socket.close()
        }
    }

    fun stop() = close()


}

private val logger = KotlinLogging.logger { }

private fun ServerSocket.acceptCancellable() = try {
    accept()
} catch (e: IOException) {
    null
}

private fun String.parseMethodLine(): Path {
    val trimmed = trimIndent()
    if (trimmed.isEmpty()) {
        throw RequestException("Invalid empty request")
    }

    val split = trimmed.split("""\s+""".toRegex())
    if (!split.first().equals("GET", ignoreCase = true)) {
        throw RequestException("Unknown method: ${split.first()}")
    }

    if (!split.last().startsWith("HTTP/", ignoreCase = true)) {
        throw RequestException("Unknown scheme: ${split.last()}")
    }

    if (split.last().substringAfter("HTTP/") != "1.1") {
        throw RequestException("Unknown HTTP version: ${split.last().substringAfter("HTTP/")}")
    }

    return Path(split.subList(1, split.lastIndex).joinToString(" "))
}

private fun buildFileAnswer(file: Path) = """
HTTP/1.1 200 OK
Content-Type: text/html

${file.readLines().joinToString("\n")}
        """.trimIndent()