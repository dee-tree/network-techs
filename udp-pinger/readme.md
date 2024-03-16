# Task 2: udp pinger

udp echo server + udp echo client

1. Echo-server receives echo message via UDP from a client
2. Echo-server returns response with a capitalized message from a request
3. Echo-client sends N (10) echo requests and computes round-trip time


There is an ability to run modeling of this situation:

# Run modeling

Just model client-server communication with specified server port.

`gradle :udp-pinger:run --args="<port> <K>"`

* `port` - run the server on this port. Client connections will be modeled to this port. Default value: random (non-well-known port).
* `K` - count of ping request. Default value: 10.

example: `gradle :udp-pinger:run`
example: `gradle :udp-pinger:run --args="1234 10"`
