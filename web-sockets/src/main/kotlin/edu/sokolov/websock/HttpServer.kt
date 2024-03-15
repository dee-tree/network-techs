package edu.sokolov.websock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit


class HttpServer internal constructor(private val socket: ServerSocket) : Closeable {
    constructor(port: Int = 80, opTimeout: Duration = 5000.milliseconds) : this(ServerSocket(port)) {
        socket.soTimeout = opTimeout.toInt(DurationUnit.MILLISECONDS)
    }

    private lateinit var job: Job
    var isClosed = false
    private set

    private var activeConnectionsCount = AtomicInteger(0)

    suspend fun start() = coroutineScope {
        if (isClosed) return@coroutineScope

        job = launch {
            outer@ while (isActive) {
                logger.debug { "Waiting for a connection at port ${socket.localPort}" }
                val deferredClient = async { socket.acceptCancellable() }

                val clientConnection = deferredClient.await() ?: break@outer
                activeConnectionsCount.incrementAndGet()
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
                try {
                    val path = readFileRequest(client, istream)
                    logger.debug { "Request on file $path received from ${client.inetAddress.hostAddress}:${client.port}" }
                    if (path.isRegularFile()) {
                        logger.debug { "Sending response message to ${client.inetAddress.hostAddress}:${client.port}" }
                        sendResponse(out, path.readText())
                    } else {
                        logger.debug { "File $path, requested from ${client.inetAddress.hostAddress}:${client.port}, not found" }
                        RequestException("Not Found", 404).sendError(out)
                    }
                } catch (e: RequestException) {
                    logger.error { "Exception during reading request from ${client.inetAddress.hostAddress}:${client.port}: $e" }
                    e.sendError(out)
                }
            }
        }

        delay(100)
        logger.debug { "Close connection with the client ${client.inetAddress}:${client.port}" }
        client.close()
        activeConnectionsCount.decrementAndGet()
    }

    private fun CoroutineScope.readFileRequest(client: Socket, istream: BufferedReader): Path {
        if (!isActive || client.isClosed) throw RequestException("Connection closed")
        val clientMethodLine = istream.readLine() ?: throw RequestException("No messages were received")

        return clientMethodLine.parseMethodLine()
    }

    private fun RequestException.sendError(out: PrintWriter) {
        val code = errorCode ?: INTERNAL_SERVER_ERROR_CODE
        out.println(
            """
HTTP/1.1 $code $message
            """.trimIndent()
        )
    }

    private fun sendResponse(out: PrintWriter, content: String, contentType: String = "text/plain") {
        out.println(
            """
HTTP/1.1 200 OK
Content-Type: $contentType

$content
            """.trimIndent()
        )
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

private const val INTERNAL_SERVER_ERROR_CODE = 500

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
