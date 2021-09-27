#pragma once

#include <iostream>
#include <set>
#include <map>
#include <ctime>
#include <arpa/inet.h>
#include <sstream>

#define LENGTH_IPV6 46
#define LENGTH_IPV4 16

class PrintAddress {

private:
    bool ipv4;

public:
    explicit PrintAddress(bool ipv4) {
        this -> ipv4 = ipv4;
    }

    [[nodiscard]] std::string outputSenderAddress(const sockaddr &address) const {
        char ip[ipv4 ? LENGTH_IPV4 : LENGTH_IPV6];
        std::stringstream str;
        str << "IP: " << inet_ntop(ipv4 ? AF_INET : AF_INET6, ipv4 ? (void *) &((sockaddr_in *) &address) -> sin_addr :
                                   (void *) &((sockaddr_in6 *) &address) -> sin6_addr, ip, ipv4 ? LENGTH_IPV4 : LENGTH_IPV6);

        return str.str();
    }
};

class CheckAddress {
public:
    bool isUpdate = false;
    std::map<std::string, timespec> address;

    void check() {
        timespec currentTime{};
        clock_gettime(CLOCK_REALTIME, &currentTime);

        bool change = false;
        for (auto i = address.begin(); i != address.end(); ++i) {
            if (currentTime.tv_sec - i -> second.tv_sec > 1) {
                change = true;
                i = address.erase(i);
            }
        }

        if (change) {
            isUpdate = false;
            for (const auto &address : address) {
                std::cout << address.first << '\n';
            }
            std::cout << "Update:" << std::endl;
        }
    }
};
