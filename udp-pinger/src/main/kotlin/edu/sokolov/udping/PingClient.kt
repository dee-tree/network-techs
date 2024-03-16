package edu.sokolov.udping

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PingClient internal constructor(private val socket: DatagramSocket) : Closeable {
    constructor() : this(DatagramSocket())

    private var pingIdx = -1
    private val buf = ByteArray(256)

    fun ping(server: String, port: Int, timeout: Duration = 1.seconds): Duration {
        pingIdx++
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
        val timeBefore = LocalDateTime.now()
        val msg = "Pinging $server: try $pingIdx at ${timeBefore.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
        val msgarray = msg.toByteArray()
        val serverAddress = InetAddress.getByName(server)
        val packet = DatagramPacket(msgarray, msgarray.size, serverAddress, port)
        logger.debug { "Send ping to the server ${serverAddress.hostAddress}:$port" }
        socket.send(packet)

        val received = DatagramPacket(buf, buf.size)
        try {
            socket.receive(received)
        } catch (e: SocketTimeoutException) {
            println("$pingIdx Request timeout")
            throw e
        }
        val timeAfter = LocalDateTime.now()
        val receivesMsg = String(received.data, 0, received.length)
        val rtt =
            (timeAfter.getLong(ChronoField.MILLI_OF_SECOND) - timeBefore.getLong(ChronoField.MILLI_OF_SECOND)).milliseconds
        println("$pingIdx|\t Message: $receivesMsg|\t RDD millis: ${rtt.inWholeMilliseconds}")
        return rtt
    }

    override fun close() {
        socket.close()
    }


}

private val logger = KotlinLogging.logger { }