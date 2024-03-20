#include <iostream>
#include <icmp-win.h>
using std::cout;
using std::cerr;
using std::string;

int main(int argc, char **argv) {
    string addr { "108.177.14.100" }; // google.com

    if (argc < 2) {
        cerr << "Specify address to be pinged as command line argument or " << addr << " will be used as default!" << std::endl;
    } else {
        addr = argv[1];
    }

    ping(addr);

    return 0;
}
