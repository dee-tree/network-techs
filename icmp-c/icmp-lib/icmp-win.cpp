#ifdef _WIN32

#include <icmp-win.h>
#include <iostream>

#define WIN32_LEAN_AND_MEAN
#include <WS2tcpip.h>
#include <windows.h>
#include <iphlpapi.h>
#include <icmpapi.h>

#pragma comment(lib, "iphlpapi.lib")
#pragma comment(lib, "ws2_32.lib")

using std::cout;
using std::cerr;

void ping(const std::string& addr, uint8_t iters) {

    IN_ADDR server{};
    if (1 != InetPtonA(AF_INET, addr.data(), &server)) {
        cerr << "Specified host is not valid!" << std::endl;
        throw;
    }

    HANDLE icmpHandle = IcmpCreateFile();
    if (icmpHandle == INVALID_HANDLE_VALUE) {
        cerr << "Unable to open handle" << std::endl;
        return;
    }

    constexpr WORD dataSize = 1;
    char data[dataSize] { 42 };
    constexpr DWORD replySize = sizeof(ICMP_ECHO_REPLY) + dataSize + 8;
    char* replyBuffer = new char[replySize];

    cout << "Pinging " << addr << " with " << dataSize << " byte(s) of data:" << std::endl;

    for (int i = 0; i < iters; ++i) {
        DWORD ret = IcmpSendEcho(icmpHandle, server.S_un.S_addr, data, dataSize, nullptr, replyBuffer, replySize, 1000);

        if (ret == 0) {
            auto lasterr = GetLastError();

            if (lasterr == IP_REQ_TIMED_OUT) {
                cout << "Request timed out" << std::endl;
            } else {
                DWORD buf_size = 1000;
                WCHAR buf[1000];
                GetIpErrorString(lasterr, buf, &buf_size);
                cout << "IcmpSendEcho returned error " << lasterr << " (" << buf << ")" << std::endl;
                throw;
            }
        } else {
            const auto *reply = (const ICMP_ECHO_REPLY *) replyBuffer;
            struct in_addr replyAddr;
            replyAddr.s_addr = reply->Address;
            char *s_ip = inet_ntoa (replyAddr);

            switch (reply->Status) {
                case IP_DEST_HOST_UNREACHABLE: std::cout << "Reply from: " << s_ip << ": Destination host unreachable" << std::endl; break;
                case IP_DEST_NET_UNREACHABLE: std::cout << "Reply from: " << s_ip << ": Destination net unreachable" << std::endl; break;
                case IP_DEST_PORT_UNREACHABLE: std::cout << "Reply from: " << s_ip << ": Destination port unreachable" << std::endl; break;
                case IP_DEST_UNREACHABLE: std::cout << "Reply from: " << s_ip << ": Destination unreachable" << std::endl; break;
                case IP_SUCCESS: std::cout << "Reply from: " << s_ip << ": bytes=" << reply->DataSize << " time=" << reply->RoundTripTime << "ms TTL=" << (int) reply->Options.Ttl << std::endl; break;
                default: std::cout << "Reply from: " << s_ip << ": Status: " << reply->Status << std::endl; break;
            }

        }
    }

    delete[] replyBuffer;
    IcmpCloseHandle(icmpHandle);


}

#endif
