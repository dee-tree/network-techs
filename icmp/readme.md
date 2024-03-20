# Task 5: ICMP pinger

Just an app to send ICMP echo messages to a host

# Run

`gradle :icmp:run --args="<server>"`

* `server` - remote server to be pinged

example: `gradle :icmp:run --args="google.com"`

example: `gradle :icmp:run --args="192.168.1.244"` # any random address