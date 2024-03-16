package edu.sokolov.smtpclient

fun main(args: Array<String>) {
    with(SMTPClient()) {
        val smtpServer = args.getOrNull(0) ?: throw IllegalArgumentException("Expected SMTP server as the first argument")
        val fromAddress = args.getOrNull(1) ?: throw IllegalArgumentException("Expected email FROM as the second argument")
        val toAddress = args.getOrNull(2) ?: throw IllegalArgumentException("Expected email TO as the third argument")
        val body = args.asList().subList(3, args.size).joinToString(" ")
        SMTPMessage().apply {
            this.fromAddress = fromAddress
            this.toAddress = toAddress
            this.data = body
        }.send(smtpServer)
    }
}