package edu.sokolov.smtpclient

fun main(args: Array<String>) {
    val isStartTls = args.getOrNull(0)?.equals("ssl", ignoreCase = true) == true
    val args = if (isStartTls) args.copyOfRange(1, args.lastIndex) else args
    val smtpServer = args.getOrNull(0) ?: throw IllegalArgumentException("Expected SMTP server as the first argument")
    val fromAddress = args.getOrNull(1) ?: throw IllegalArgumentException("Expected email FROM as the second argument")
    val toAddress = args.getOrNull(2) ?: throw IllegalArgumentException("Expected email TO as the third argument")
    val body = args.asList().subList(3, args.size).joinToString(" ")

    val client = if(isStartTls) SMTPSslClient() else SMTPClient()
    client.use { client ->
        client.openConnection(smtpServer)
        client.send {
            this.fromAddress = fromAddress
            this.toAddress = toAddress
            this.data = body
        }
    }
}