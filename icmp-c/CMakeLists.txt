cmake_minimum_required(VERSION 3.10)
project(icmp-pinger)
set (CMAKE_CXX_STANDARD 11)

add_subdirectory(icmp-lib)

add_executable(icmp-pinger main.cpp)
#target_include_directories(icmp-pinger PUBLIC icmp-lib)
#target_link_directories(icmp-pinger PRIVATE icmp-lib)
target_link_libraries(icmp-pinger icmp-lib)
#target_link_libraries(icmp-pinger -lws2_32)
