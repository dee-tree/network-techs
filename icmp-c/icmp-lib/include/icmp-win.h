
#ifndef ICMP_PINGER_ICMP_WIN_H
#define ICMP_PINGER_ICMP_WIN_H

#include <string>
#include <cstdint>

void ping(const std::string &addr, uint8_t iters = 4);

#endif //ICMP_PINGER_ICMP_WIN_H
