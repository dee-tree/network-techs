package edu.sokolov.icmp

import java.net.InetAddress
import kotlin.time.Duration
import kotlin.time.measureTimedValue

fun main(args: Array<String>) {
    val server = args.getOrNull(0) ?: throw IllegalArgumentException("Expected server address/name as the first argument")
    val addr = InetAddress.getByName(server)
    val iterations = 4

    val rtts = mutableListOf<Duration>()
    repeat(iterations) {
        val (success, duration) = measureTimedValue {
            addr.isReachable(1000)
        }
        if (success) {
            rtts += duration
            println("Reply from ${addr.hostAddress}, time = ${duration.inWholeMilliseconds}ms")
        } else {
            println("Request timed out")
        }
    }
    val failures = iterations - rtts.size

    println("\nPing statistics for ${addr.hostAddress}:")
    println("\tPackets: Sent = $iterations, Received = ${iterations - failures}, Lost = $failures (${(failures.toDouble() / iterations) * 100}% loss)")
    println("Approximate round trip times in milli-seconds:")
    val minms = if (rtts.isEmpty()) 0 else rtts.min().inWholeMilliseconds
    val maxms = if (rtts.isEmpty()) 0 else rtts.max().inWholeMilliseconds
    val averagems = if (rtts.isEmpty()) 0 else rtts.sumOf { it.inWholeMilliseconds } / rtts.size
    println("\tMinimum = ${minms}ms, Maximum = ${maxms}ms, Average = ${averagems}ms")
}