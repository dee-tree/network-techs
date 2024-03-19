package edu.sokolov.smtpclient

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class SMTPClient: Closeable {

    protected lateinit var socket: Socket
    var isOpened = false
    protected set

    open val port: Int = 25

    open fun openConnection(server: String, timeout: Duration = 10.seconds) {
        socket = Socket(server, port.toInt())
        isOpened = true
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
        socket.expect(220)
    }

    open fun <T> send(buildMessage: SMTPMessage.() -> T) {
        require(isOpened) { "It's required to call openConnection() before" }

        with(socket) {
            val message = SMTPMessage().apply { buildMessage() }
            expect(250, "HELO alice")
            expect(250, "MAIL FROM: <${message.fromAddress}>")
            expect(250, "RCPT TO: <${message.toAddress}>")
            expect(354, "DATA")
            expect(250, message.data)
            expect(221, "QUIT")
        }
//        val socket = Socket(server, 25)
//        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
//        BufferedReader(InputStreamReader(socket.inputStream)).use { istream ->
//            PrintWriter(OutputStreamWriter(socket.outputStream), true).use { out ->
//                val initialResponse = istream.readLine()
//                logger.debug { "Response on establishing connection: $initialResponse" }
//                if (!initialResponse.startsWith("220")) {
//                    throw Exception("Response with code 220 was not received from the server")
//                }
//
//                out.println("HELO alice")
//                val helloResponse = istream.readLine()
//                logger.debug { "Response on hello message: $helloResponse" }
//                if (!helloResponse.startsWith("250")) {
//                    throw Exception("Response with code 250 was not received from the server on hello message")
//                }
//
//
//                out.println("MAIL FROM: <${fromAddress}>")
//                istream.readLine().checkResponse(250)
//                out.println("RCPT TO: <${toAddress}>")
//                istream.readLine().checkResponse(250)
//                out.println("DATA")
//                istream.readLine().checkResponse(354)
//                out.println(data)
//                istream.readLine().checkResponse(250)
//                out.println("QUIT")
//            }
//        }
//        socket.close()
    }

    override fun close() {
        if (isOpened) {
            logger.debug { "SMTP Client socket is closed" }
            socket.close()
        }
    }
}

internal fun Socket.expect(
    message: String? = null,
    expected: BufferedReader.() -> Boolean,
    errorMessage: () -> String
) {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val writer = PrintWriter(OutputStreamWriter(outputStream), true)
    message?.let { msg -> writer.println(msg) }
    if (!expected(reader)) throw IllegalStateException("Unexpected server response! ${errorMessage()}")
}

internal fun Socket.expect(expectedCode: Int, message: String? = null) {
    var errorMessage = "Response message wasn't received"
    expect(message, expected = lam@{
        val line = readLine() ?: return@lam false
        logger.debug { "Response: $line" }
        errorMessage = "Expected $expectedCode code response, but got $line"
        line.startsWith((expectedCode.toString()))
    },
        errorMessage = { errorMessage }
    )
}

internal fun String.checkResponse(expectedCode: Int) {
    if (!startsWith((expectedCode.toString()))) {
        logger.debug { "Response: $this" }
        throw Exception("Response with code $expectedCode was not received from the server")
    }
}

private val logger = KotlinLogging.logger { }