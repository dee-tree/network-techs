# Task 1: web server

http server + client implementation.

1. Web-server receives http request from a client
2. Web-server returns http response with a requested file (via request, p. 1) from the server file system

In case, if a requested file not found - return 404 Not Found.

There is an ability to run model in three ways:

# Run server

`gradle :web-sockets:run --args="server <port> <T>"`
* `port` - open the server on the specified port
* `T` - terminate the server after T milliseconds, optional argument

example: `gradle :web-sockets:run --args="server 8080 10000"`

# Run client

`gradle :web-sockets:run --args="client <server addr> <server port> <F>"`
* `server addr` - address/domain of the server
* `server port` - port of the server
* `F` - File/filepath to be requested from the server

example: `gradle :web-sockets:run --args="client 127.0.0.1 8080 ./src/main/resources/hello.txt"`


# Run both | Model

Just model client-server communication with specified server port.

`gradle :web-sockets:run --args="both <port> <T>"`

* `port` - run the server on this port. Client connections will be modeled to this port. Default value: 80.
* `T` - time of modeling (milliseconds).

example: `gradle :web-sockets:run --args="both 8080 10000"`
