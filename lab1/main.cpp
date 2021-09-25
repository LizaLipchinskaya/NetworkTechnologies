#include <sys/socket.h>
#include <cstring>
#include <unistd.h>
#include <fcntl.h>
#include <memory>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <ctime>
#include "CheckAddress.h"

int main(int argc, char **argv) {
    if (argc != 3) {
        std::cerr << "Wrong count argument" << std::endl;
        exit(1);
    }

    char *multicastAddress = argv[1];
    int multicastPort = strtol(argv[2], nullptr, 0);
    bool ipv4 = strstr(multicastAddress, ":") == nullptr;
    std::shared_ptr<sockaddr> multicastEndpoint;

    pid_t pid = getpid();
    pid_t pidSend;
    CheckAddress address;
    PrintAddress printAddress(ipv4);

    timespec currentTime{};
    timespec lastCheckTime{};

    int multicastUdpSocket = socket(ipv4 ? AF_INET : AF_INET6, SOCK_DGRAM, IPPROTO_UDP);
    fcntl(multicastUdpSocket, F_SETFL, O_NONBLOCK);

    const int optval = 1;
    setsockopt(multicastUdpSocket, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval));

    if (ipv4) {
        auto *ipv4Endpoint = (sockaddr_in *) calloc(1, sizeof(sockaddr_in));
        ipv4Endpoint -> sin_family = AF_INET;
        ipv4Endpoint -> sin_port = htons(multicastPort);
        inet_pton(AF_INET, multicastAddress, &ipv4Endpoint->sin_addr);
        multicastEndpoint = {(sockaddr *) ipv4Endpoint, free};

        struct sockaddr_in address = {50, AF_INET, htons(multicastPort)};

        bind(multicastUdpSocket, (struct sockaddr *) &address, sizeof address);

        ip_mreq IPv4Group = {};
        IPv4Group.imr_interface.s_addr = htonl(INADDR_ANY);
        inet_pton(AF_INET, multicastAddress, &(IPv4Group.imr_multiaddr));

        setsockopt(multicastUdpSocket, IPPROTO_IP, IP_ADD_MEMBERSHIP, &IPv4Group, sizeof(IPv4Group));
    } else {
        auto *ipv6Endpoint = (sockaddr_in6 *) calloc(1, sizeof(sockaddr_in6));
        ipv6Endpoint -> sin6_family = AF_INET6;
        ipv6Endpoint -> sin6_port = htons(multicastPort);
        inet_pton(AF_INET6, multicastAddress, &ipv6Endpoint->sin6_addr);

        multicastEndpoint = {(sockaddr *) ipv6Endpoint, free};

        struct sockaddr_in6 address = {50, AF_INET6, htons(multicastPort)};

        bind(multicastUdpSocket, (struct sockaddr *) &address, sizeof address);

        ipv6_mreq IPv6Group = {};
        IPv6Group.ipv6mr_interface = 0;
        inet_pton(AF_INET6, multicastAddress, &(IPv6Group.ipv6mr_multiaddr));

        setsockopt(multicastUdpSocket, IPPROTO_IPV6, IPV6_JOIN_GROUP, &IPv6Group, sizeof(IPv6Group));
    }

    while (true) {
        sendto(multicastUdpSocket, &pid, sizeof(pid_t), 0, multicastEndpoint.get(), ipv4 ? sizeof(sockaddr_in) :
            sizeof(sockaddr_in6));

        auto *addressSend = (sockaddr *) calloc(1, ipv4 ? sizeof(sockaddr_in) : sizeof(sockaddr_in6));
        socklen_t length = ipv4 ? sizeof(sockaddr_in) : sizeof(sockaddr_in6);

        int cnt = recvfrom(multicastUdpSocket, &pidSend, sizeof(pidSend), 0, addressSend, &length);

        clock_gettime(CLOCK_REALTIME, &currentTime);

        if (cnt > 0) {
            if (address.address.count(printAddress.outputSenderAddress(*addressSend)) <= 0) {
                address.isUpdate = true;
            }

            address.address[printAddress.outputSenderAddress(*addressSend)] = currentTime;
        }

        if (address.isUpdate || currentTime.tv_sec - lastCheckTime.tv_sec > 2) {
            address.check();

            if (address.isUpdate) {
                address.isUpdate = false;

                for (const auto &address : address.address) {
                    std::cout << address.first << '\n';
                }

                std::cout << "Update:" << std::endl;
            }

            lastCheckTime = currentTime;
        }

        free(addressSend);
        sleep(1);
    }
}
