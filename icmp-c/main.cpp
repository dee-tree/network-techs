#include <iostream>
#include <icmp-win.h>
//#define WIN32_LEAN_AND_MEAN
//#include <winsock2.h>
//#include <windows.h>

//#pragma comment(lib, "iphlpapi.lib")
//#pragma comment(lib, "ws2_32.lib")

using std::cout;
using std::cerr;
using std::string;

int main(int argc, char **argv) {
    cout << "Hello!" << std::endl;

    ping(string{"192.168.1.1"});
//    unsigned long addr = inet_addr("192.168.1.1");
//    if (addr == INADDR_NONE) {
//        cerr << "Please, specify IP address to be pinged" << std::endl;
//        return 1;
//    }

    return 0;
}
