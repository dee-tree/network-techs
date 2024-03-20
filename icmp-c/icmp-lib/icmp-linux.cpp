#ifndef _WIN32

#include <icmp-win.h>
#include <iostream>

void ping(const std::string& addr, uint8_t iters) {
    std::cerr << "Sorry, ping is not implemented for linux :(" << std::endl;
}


#endif