package edu.sokolov.websock

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class HttpClient internal constructor(private val socket: Socket) : Closeable {
    constructor(
        server: String = "127.0.0.1",
        port: Int = 80,
        opTimeout: Duration = 1000.milliseconds
    ) : this(Socket(server, port)) {
        socket.soTimeout = opTimeout.toInt(DurationUnit.MILLISECONDS)
    }

    override fun close() {
        socket.close()
    }

    fun stop() = close()

    suspend fun requestFile(filepath: Path) = coroutineScope {
        if (socket.isClosed) return@coroutineScope
        BufferedReader(InputStreamReader(socket.inputStream)).use { istream ->
            PrintWriter(OutputStreamWriter(socket.outputStream), true).use { out ->
                out.println(buildRequest(socket.inetAddress.hostAddress, filepath))

                val (code, msg) = istream.readLine().getResponseCode()
                if (code != 200) {
                    logger.error { "Server returned $code error with message: $msg" }
                } else {
                    logger.debug { "Received successful response from the server" }

                    do { val line = istream.readLine() ?: break } while (line.isNotBlank())

                    logger.debug { "Server sent the file $filepath content below:" }
                    while (isActive && !socket.isClosed) {
                        val fileLine = istream.readLine() ?: break
                        println(fileLine)
                    }
                }

                if (!socket.isClosed) socket.close()
                logger.debug { "Connection with the server is closed by the client" }
            }
        }
    }
}

private fun String.getResponseCode(): Pair<Int, String> {
    val splited = split("\\s+".toRegex())
    return Pair(splited[1].toInt(), splited.subList(2, splited.size).joinToString(" "))
}

private fun buildRequest(host: String, filepath: Path) = """
GET $filepath HTTP/1.1
Host: $host
Connection: Keep-Alive
        """.trimIndent()

private val logger = KotlinLogging.logger { }
