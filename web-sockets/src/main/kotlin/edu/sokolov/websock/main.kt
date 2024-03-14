package edu.sokolov.websock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.io.path.Path


fun main() {
    val serv = HttpServer()
    val client = HttpClient()


    runBlocking {
        launch {
            withContext(Dispatchers.IO) {
                serv.start()
                println("after serv start")
            }
        }

        launch {
            delay(100)
            client.requestFile(Path("W:\\edu\\term8\\network-techs\\web-sockets\\src\\main\\resources\\logback.xml"))
//            client.close()
        }


//        delay(5000)
//        println("Go to close")
//        client.close()
//        serv.stop()
    }
}