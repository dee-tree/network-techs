package edu.sokolov.smtpclient

import io.github.oshai.kotlinlogging.KotlinLogging
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.time.Duration

class SMTPSslClient : SMTPClient() {

    private lateinit var sslSocket: SSLSocket
    override val port: Int = 587

    override fun openConnection(server: String, timeout: Duration) {
        super.openConnection(server, timeout)
        socket.expect(250, "EHLO alice")
        socket.expect(220, "STARTTLS")
        System.setProperty("javax.net.ssl.trustStore", "C:\\env\\JDKs\\openjdk-20.0.1\\lib\\security\\cacerts")
        val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        sslSocket = sslFactory.createSocket(socket, socket.inetAddress.hostAddress, socket.port, true) as SSLSocket
        sslSocket.useClientMode = true
        sslSocket.enableSessionCreation = true
        logger.debug { "Start SSL handshake with the server" }
        sslSocket.startHandshake()
        logger.debug { "Secured connection with the server is established" }
    }

    override fun <T> send(buildMessage: SMTPMessage.() -> T) {
        require(isOpened) { "It's required to call openConnection() before" }
        with(sslSocket) {
            val message = SMTPMessage().apply { buildMessage() }

            expect(334, "AUTH LOGIN")
            println("Please, specify the username for the SMTP server:")
            expect(334, readlnOrNull() ?: "anonymous")
            println("Please, specify the password for the SMTP server:")
            expect(235, readlnOrNull() ?: "anonymous")

            expect(250, "MAIL FROM: <${message.fromAddress}>")
            expect(250, "RCPT TO: <${message.toAddress}>")

            expect(354, "DATA")
            expect(250, message.data)
            expect(221, "QUIT")
        }
    }

    override fun close() {
        sslSocket.close()
        super.close()
    }
}

private val logger = KotlinLogging.logger { }