package edu.sokolov.udping

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

class PingServer internal constructor(private val socket: DatagramSocket): Closeable {
    constructor(port: Int) : this(DatagramSocket(port))

    private lateinit var job: Job
    var isClosed = false
    private set

    private val buf = ByteArray(256)

    suspend fun start() = coroutineScope {
        if (isClosed) return@coroutineScope
        logger.debug { "Server is started at port ${socket.localPort}" }

        job = launch {
            outer@ while (isActive && !isClosed) {
                val packet = DatagramPacket(buf, buf.size)
                socket.receiveCancellable(packet) ?: break
                val receivedMsg = String(packet.data, 0, packet.length)
                logger.debug { "Received message from client ${packet.address.hostAddress}:${packet.port}: $receivedMsg" }
                if ((1..10).random() < 4) {
                    logger.debug { "Drop client packet" }
                    continue@outer
                }

                logger.debug { "Response to the client ${packet.address.hostAddress}:${packet.port}" }
                socket.send(DatagramPacket(receivedMsg.uppercase().toByteArray(), receivedMsg.length, packet.address, packet.port))
            }
        }
    }

    override fun close() {
        isClosed = true
        runBlocking {
            job.cancel()
            socket.close()
        }
    }

}

private val logger = KotlinLogging.logger {  }

private fun DatagramSocket.receiveCancellable(packet: DatagramPacket) = try {
    receive(packet)
} catch (e: IOException) {
    null
}