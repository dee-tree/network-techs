package edu.sokolov.udping

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.time.Duration

fun main(args: Array<String>) {
    val port = args.getOrElse(0) { "${(1024..65535).random()}" }.toInt()
    if (port < 0 || port > 65535) throw IllegalStateException("Illegal port ${port}. Expected 0..65535")
    val pingIterations = args.getOrElse(1) { "10" }.toInt()
    if (pingIterations < 0) throw IllegalStateException("Expected positive ping operations, but got $pingIterations")
    val server = PingServer(port)
    val client = PingClient()
    runBlocking {
        launch {
            launch {
                withContext(Dispatchers.IO) {
                    server.start()
                }
            }

            launch {
                delay(100)
                var failures = 0
                val rtts = ArrayList<Duration>(pingIterations)
                repeat(pingIterations) {
                    try {
                        rtts += client.ping("127.0.0.1", port)
                    } catch (_: SocketTimeoutException) {
                        failures++
                    }
                }
                client.close()
                server.close()

                println("\nPing statistics:")
                println("\tPackets: Sent = $pingIterations, Received = ${pingIterations - failures}, Lost = $failures (${(failures.toDouble() / pingIterations) * 100}% loss)")
                println("Approximate round trip times in milli-seconds:")
                println("\tMinimum = ${rtts.min().inWholeMilliseconds}ms, Maximum = ${rtts.max().inWholeMilliseconds}ms, Average = ${rtts.sumOf { it.inWholeMilliseconds } / rtts.size}ms")
            }

        }
    }
}