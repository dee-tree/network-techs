cmake_minimum_required(VERSION 3.10)
project(icmp-lib)
set (CMAKE_CXX_STANDARD 11)

include_directories(include)

add_library(icmp-lib STATIC icmp-win.cpp icmp-linux.cpp)
target_link_libraries(icmp-lib -lws2_32 -lIphlpapi)
target_include_directories(icmp-lib PUBLIC include)

