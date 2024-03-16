package edu.sokolov.smtpclient

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SMTPClient {
    fun SMTPMessage.send(server: String, timeout: Duration = 10.seconds) {
        val socket = Socket(server, 25)
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
        BufferedReader(InputStreamReader(socket.inputStream)).use { istream ->
            PrintWriter(OutputStreamWriter(socket.outputStream), true).use { out ->
                val initialResponse = istream.readLine()
                logger.debug { "Response on establishing connection: $initialResponse" }
                if (!initialResponse.startsWith("220")) {
                    throw Exception("Response with code 220 was not received from the server")
                }

                out.println("HELO alice")
                val helloResponse = istream.readLine()
                logger.debug { "Response on hello message: $helloResponse" }
                if (!helloResponse.startsWith("250")) {
                    throw Exception("Response with code 250 was not received from the server on hello message")
                }


                out.println("MAIL FROM: <${fromAddress}>")
                istream.readLine().checkResponse(250)
                out.println("RCPT TO: <${toAddress}>")
                istream.readLine().checkResponse(250)
                out.println("DATA")
                istream.readLine().checkResponse(354)
                out.println(data)
                istream.readLine().checkResponse(250)
                out.println("QUIT")
            }
        }
        socket.close()
    }
}

private fun String.checkResponse(expectedCode: Int) {
    if (!startsWith((expectedCode.toString()))) {
        logger.debug { "Response: $this" }
        throw Exception("Response with code $expectedCode was not received from the server")
    }
}

private val logger = KotlinLogging.logger { }