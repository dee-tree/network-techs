package edu.sokolov.websock

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


fun main(args: Array<String>) {
    val modelType = ModelType.entries.firstOrNull { it.name.equals(args[0], ignoreCase = true) }
        ?: throw Exception("Expected server/client/both connection type as a first command line argument!")

    runBlocking {
        when (modelType) {
            ModelType.SERVER -> {
                val port = args.getOrElse(1) { "80" }.toInt()
                if (port < 0 || port > 65535) throw IllegalStateException("Illegal port ${port}. Expected 0..65535")
                val terminationDelay = args.getOrNull(2)?.toLong()?.milliseconds

                launch {
                    runServer(port, terminationDelay)
                }
            }

            ModelType.CLIENT -> {
                val serverAddress = args.getOrNull(1)
                    ?: throw IllegalStateException("Server address is expected as the second argument")
                val serverPort = args.getOrElse(2) { "80" }.toInt()
                if (serverPort < 0 || serverPort > 65535) throw IllegalStateException("Illegal port ${serverPort}. Expected 0..65535")
                val requestedFilePath = args.getOrNull(3)
                    ?: throw IllegalStateException("Expected filepath to be requested from the server as the forth argument!")

                launch {
                    val client = HttpClient(serverAddress, serverPort)
                    client.requestFile(Path(requestedFilePath))
                }
            }

            ModelType.BOTH -> {
                val serverPort = args.getOrElse(1) { "80" }.toInt()
                if (serverPort < 0 || serverPort > 65535) throw IllegalStateException("Illegal port ${serverPort}. Expected 0..65535")
                val modelTime = args.getOrNull(2)?.toLong()?.milliseconds

                launch {
                    modelBoth(serverPort, modelTime)
                }
            }

        }

    }
}

fun CoroutineScope.runServer(port: Int, terminationDelay: Duration? = null): HttpServer {
    val server = HttpServer(port = port)
    launch {
        withContext(Dispatchers.IO) {
            server.start()
        }
    }
    launch {
        terminationDelay?.let { disableAfter ->
            delay(disableAfter)
            server.stop()
        }
    }
    return server
}

private val randomPaths = arrayOf(
    Path("/bin/shellll"),
    Path("/etc/resolv.conf"),
    Path("C:\\Windows\\explorer.notexe"),
    Path("./src/main/resources/hello.txt"),
    Path("./src/main/resources/goodluck.html"),
    Path("./src/main/resources/logback.xml"),
)

fun CoroutineScope.modelBoth(port: Int = 80, modelTime: Duration? = 10.seconds) {
    var isServerRunning = { false }
    launch {
        val server = runServer(port, terminationDelay = modelTime)
        isServerRunning = { !server.isClosed }
    }

    launch {
        while (!isServerRunning()) {
            delay(50)
        }

        while (isServerRunning()) {
            launch {
                try {
                    val client = HttpClient("127.0.0.1", port)
                    delay((500L..2000L).random())
                    client.requestFile(randomPaths.random())
                } catch (_: Exception) {
                }
            }
            delay((100L..2000L).random())
        }
    }
}