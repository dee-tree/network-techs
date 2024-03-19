# Task 4: Proxy server

Implementation (web pages) caching proxy server

# Run

`gradle :proxy:run --args="<server> <server port> <port> <model>"`

* `server` - remote server
* `server port` - port of the remote server
* `port` - port of proxy server
* `model` - should run the proxy server in couple with the client for modelling purposes? Optional argument, default: false

example: `gradle :proxy:run --args="google.com 80 37851 true"`
example: `gradle :proxy:run --args="google.com 80 37851"`