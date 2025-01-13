## What is QUIC?
**QUIC**(Quick UDP Internet Connection) is a transport layer network protocol originally developed by Google.
It is designed to improve the performance of internet communication by addressing limitations in traditional protocols like TCP, especially for modern web applications.

QUIC operates over UDP instead of TCP, combining transport and some application-layer capabilities, such as multiplexing and encryption, into a single protocol.
<br><br>

## Key Features of QUIC

### Connection Migration
QUIC connections are identified by connection IDs instead of IP addresses, allowing them to survive network changes (e.g., switching from Wi-Fi to mobile data).

### Low Latency
QUIC reduces latency by combining connection setup and encryption negotiation into a single handshake, often requiring only one round trip.<br>

### Resilience to Packet Loss
QUIC handles packet loss more gracefully than TCP, improving performance for applications in unreliable or high-latency networks.

### Resolves HOL Blocking in HTTP/2
In HTTP/2, there is a chance of HOL Blocking in TCP layer because it uses one TCP connection.<br>
Data streams in QUIC connection work independently so a problem in a single stream won't affect the other streams.

---

## Usage
Before we get started, you need to make sure that `curl` supports http/3.<br>
```shell
curl --version
```
<img src="https://github.com/user-attachments/assets/e0328685-40f2-483e-b288-ad80bdb45130" alt="curl features"/>

- Create self-signed certificates
```shell
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -sha256 -days 365 -nodes -subj "/CN=localhost"
```

- Move certificates to /resources

Locate the created *.pem files into a `resources` directory.

- Launch a server

The server will be running on port 8443.

- Access the server

Use below commands to access the server on HTTP/3 protocol.
```
# hello
curl --http3 -k -v https://localhost:8443/

# download a image
curl --http3 -k -v 'https://localhost:8443/image?name=sample.png' > out.png
```

---

## References
https://jetty.org/docs/jetty/12/programming-guide/server/http3.html<br>
https://github.com/murphye/spring-boot-http-3-jetty
