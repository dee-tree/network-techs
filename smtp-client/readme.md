# Task 3: SMTP client

Implementation of SMTP client over TCP sockets

# Run

Just model client-server communication with specified server port.

`gradle :smtp-client:run --args="<smtp server> <from> <to> <body>"`

* `smtp server` - address of smtp server
* `from` - email address of sender
* `to` - email address of receiver
* `body` - content to send

example: `gradle :smtp-client:run --args="smtp.gmail.com abc@gmail.com test@gmail.com Hello guy!"`

# startTLS

To connect with SMTP server with startTLS, use **ssl** as a first argument as there:
example: `gradle :smtp-client:run --args="ssl smtp.gmail.com abc@gmail.com test@gmail.com Hello guy!"`
Note: do remember to specify java keystore/truststore of certificates