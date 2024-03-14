package edu.sokolov.websock

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.nio.file.Path

class HttpClient internal constructor(private val socket: Socket) : Closeable {
    constructor(server: String = "127.0.0.1", port: Int = 80) : this(Socket(server, port))

    fun send() {
        if (socket.isClosed) return
        val out = PrintWriter(OutputStreamWriter(socket.outputStream), true)

        GlobalScope.launch {
            repeat(10) {
                out.println("text")
                println("message sent!")
                delay(1000)
            }
            out.println(".")


        }
//        val input = BufferedReader(InputStreamReader(socket.inputStream))
//        println("client received: ${input.readLine()}")
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

                while (!socket.isClosed) {
                    val answer = istream.readLine() ?: break
                    println("got answer: ${answer}")
                }
            }
        }


    }

    private fun buildRequest(host: String, filepath: Path) = """
GET ${filepath} HTTP/1.1
Host: ${host}
Connection: Keep-Alive
        """.trimIndent()

}
