# TruthTLS

I want to learn about TLSNotary through implementation.

1. In some applications, there are no secrets in the response that needs to be verified. I plan to directly reveal the decryption key to the Verifier. This can significantly reduce the computation in MPC and overcome current response size limitations.
2. Implement TLS 1.3, which is not yet supported by TLSNotary.
3. Provide a small, easy-to-modify codebase for experimentation and learning.

According to the FAQ, TLSNotary does not yet support TLS 1.3.

Additionally, TLSNotary's encryption/decryption is currently done within MPC. I want to try changing this approach to reduce computational overhead.
(Previous discussion in [TLSNotary discord](https://discord.com/channels/943612659163602974/968798323341418547/1351479225454035005))

This is a project for [ETHGlobal Taipei 2025](https://ethglobal.com/events/taipei).

I'll develop a minimal TLS 1.3 client from scratch. As minimal as possible.

I don't have time to develop MPC. I'll define the interface and use local computation for now.

I'll implement as much as I can within the time constraints.

I'll write in Chinese first, and then ask AI to translate it into English at the end.

----

It's 5:33 now.

There are two approaches for the Client: write code to generate it, or modify existing code.

I'll start with modifying existing code.

Called on Github Copilot - Agent - Claude 3.7 Sonnet for help.

```prompt
Help me write a script.
Create a local port forwarder on port 4433.
Use openssl s_client through it to connect to google.com port 443. Just send a HEAD request. HTTP/1.1. Use TLS 1.3.
Capture the content with tcpdump. (without sudo)
Open wireshark to view the contents.
```

![wireshark](image.png)

But the content is clearly too much. I only need minimal content:
cipher_suite: TLS_AES_128_GCM_SHA256
supported_versions: TLS 1.3
signature_algorithms: rsa_pkcs1_sha256 / rsa_pss_rsae_sha256 / ecdsa_secp256r1_sha256
supported_groups: secp256r1

The request being sent now looks like this (copied hexdump from wireshark):
```
0000   16 03 01 00 c3 01 00 00 bf 03 03 67 81 67 36 7e
0010   a8 8d c6 18 97 c3 a6 fe d9 f6 55 e5 e8 f2 64 27
0020   7e 12 8d f7 63 5d 4f 52 d8 fa 83 20 bf 12 12 ea
0030   9b 2d 53 6d d6 59 3d 39 c2 0a 39 0b f4 e2 e1 6b
0040   ae 3e e9 e0 39 db 67 34 c1 e2 c8 fb 00 02 13 01
0050   01 00 00 74 00 0b 00 04 03 00 01 02 00 0a 00 04
0060   00 02 00 17 00 0d 00 08 00 06 04 01 08 04 04 03
0070   00 2b 00 03 02 03 04 00 2d 00 02 01 01 00 33 00
0080   47 00 45 00 17 00 41 04 e3 2e 31 55 94 19 7a 08
0090   d8 fe ea 3e af 27 92 d5 10 1d 87 a3 04 f1 2a 87
00a0   aa 4a 52 db 3b 82 76 57 71 6c 35 96 77 f1 00 ce
00b0   61 6e f9 96 a2 62 da 9a f5 63 d4 b6 5f 91 bb 12
00c0   26 17 fd d3 87 ba 60 46
```

16030100c3010000bf0303678167367ea88dc61897c3a6fed9f655e5e8f264277e128df7635d4f52d8fa8320bf1212ea9b2d536dd6593d39c20a390bf4e2e16bae3ee9e039db6734c1e2c8fb0002130101000074000b000403000102000a000400020017000d00080006040108040403002b0003020304002d000201010033004700450017004104e32e315594197a08d8feea3eaf2792d5101d87a304f12a87aa4a52db3b827657716c359677f100ce616ef996a262da9af563d4b65f91bb122617fdd387ba6046

```prompt
I want a Java method that can accept 16030100c3010000bf0303678167367ea88dc61897c3a6fed9f655e5e8f26427
or accept
16 03 01 00 c3 01 00 00 bf 03 03 67 81 67 36 7e
a8 8d c6 18 97 c3 a6 fe d9 f6 55 e5 e8 f2 64 27
as input strings
and return byte[]
```

```prompt
Help me add a hexdump util method
Given a title and a byte[]
Print to stdout
```

```
Help me add a class Client
Add a connect() method that doesn't require parameters.
Send this request to google.com port 443
16030100c3010000bf0303678167367ea88dc61897c3a6fed9f655e5e8f264277e128df7635d4f52d8fa8320bf1212ea9b2d536dd6593d39c20a390bf4e2e16bae3ee9e039db6734c1e2c8fb0002130101000074000b000403000102000a000400020017000d00080006040108040403002b0003020304002d000201010033004700450017004104e32e315594197a08d8feea3eaf2792d5101d87a304f12a87aa4a52db3b827657716c359677f100ce616ef996a262da9af563d4b65f91bb122617fdd387ba6046
Use basic Socket / OutputStream / InputStream / byte[]
Read all responses to a byte[]
Then hexdump it
Catch Exceptions at the outermost level and convert to RuntimeException before throwing
Use class Utils during the process
```

Now it looks like the Client can connect to Google and get a response.
However, some things should be replaced.
Random: offset 0x43, length 32
Session ID: offset 0x64, length 32
Key share: offset 0xBF, length 65

```prompt
Help me add a method in class Utils to get random byte[]
```

```
In Client's connect(), 
replace two sections of the request with random bytes before sending
"Random": offset (0x43 - 0x38), length 32
"Session ID": offset (0x64 - 0x38), length 32
```

```
Write a class Keys
When instantiated, it should generate a secp256r1 key pair, stored in public data members
Also provide a method that returns 65 bytes of uncompressed key
```

```
Write a Java class Keys
When instantiated, it should generate a secp256r1 key pair, stored in public data members
Also provide a method that returns 65 bytes of uncompressed key

I want bigIntegerToBytes to reject negative values
If the integer converted to bytes is longer than length, throw a runtime exception

I want all method exceptions to be wrapped as runtime exceptions
```

```
Replace the final bytes of the Client's request with the content from Keys.getUncompressedPublicKey() before sending
```

12:29
Time to actually process the response.
First, manually extract the original dump method elsewhere.

```
Write two integer comparison methods:
assertEquals(msg, expected, actual)
assertAtMost(msg, expected, actual)
Throw RuntimeException on failure
```

```
Write a class TLSRecord
Read data from java.io.DataInput

Data layout as follows. Integers are unsigned, big-endian.

struct {
ContentType type; // 1 bytes integer
ProtocolVersion legacy_record_version; // 2 bytes integer, should be 0x0303
uint16 length; // 2 bytes integer, should be at most 2^14
opaque fragment[TLSPlaintext.length]; // "length" bytes 
} TLSPlaintext;

TLSRecord has two public fields, no getter/setter:
int type; // from "type"
byte[] data; // from "fragment"
```

```
Modify class TLSRecord
1. Use the assert methods from class Utils for checks
2. Add a hexdump(title) method, using Utils' hexdump. Also display type in the title
3. Don't throw checked exceptions, wrap in RuntimeException
```

```
In class Client's connect(), use a loop to read multiple TLSRecord entries from DataInputStream
Hexdump each record as it's read
If there are no new available bytes within 1 second after reading a record, end the program
```

Now I need to start handling the record content.
I'll take a shortcut here!
Skipping the problem of handshakes being fragmented!!!!
Also not considering coalescing!

```
Write a class Handshake
Read data from java.io.DataInput

Data layout as follows. Integers are unsigned, big-endian.

      struct {
          HandshakeType msg_type;    /* handshake type */ // 1 byte
          uint24 length;             /* remaining bytes in message */ // 3 bytes
          // "length" bytes
      } Handshake;

Read into:
class Handshake with the following public fields, no getter/setter:
int msg_type
int length
byte[] data
```

```
Add a Handshake read(byte[]) method, input a byte[], wrap a DataInputStream internally, and pass to the original read(DataInput)
```

```
Add a dump() to Handshake, showing each field
```

```
In readTLSRecords(), for each record read, check if type = 22 (don't extract constants yet)
If so, create a Handshake from the data and dump it
```

Finally reaching the ServerHello

```
Write a class TLSRecord
Read data from java.io.DataInput

Data layout as follows. Integers are unsigned, big-endian.

      struct {
          ProtocolVersion legacy_version = 0x0303;    // 2 bytes. must be 0x0303.
          Random random;                              // 32 bytes[]
          opaque legacy_session_id_echo<0..32>;       // 1 byte length, then byte[length]
          CipherSuite cipher_suite;                   // 2 byte integer
          uint8 legacy_compression_method = 0;        // 1 byte. must be 0
          Extension extensions<6..2^16-1>;            // 2 byte length, then byte[length]
      } ServerHello;
      
Read into:
class ServerHello with the following public fields, no getter/setter:
int cipher_suite
byte[] extension_data
```

```
Create Java class Handshake, all as int constants, keeping original naming
enum {
client_hello(1),
server_hello(2),
new_session_ticket(4),
end_of_early_data(5),
encrypted_extensions(8),
certificate(11),
certificate_request(13),
certificate_verify(15),
finished(20),
key_update(24),
message_hash(254),
(255)
} HandshakeType;
```
The prompt above was incorrect. I originally wanted to create a separate class, but now they've become constants in Handshake. That's fine too.

```
In class Handshake's read(), add a public field:
Object object = null;
If msg_type is handshake, build the Handshake object using data
```

```
Write a class Extension
Read data from java.io.DataInput

Data layout as follows. Integers are unsigned, big-endian.
struct {
  ExtensionType extension_type;   // 1 byte int
  opaque extension_data<0..2^16-1>; // 2 byte length followed by byte[length]
} Extension;

class Extension has the following public fields, no getter/setter:
int type // from extension_type
byte[] data // from extension_data
```

```
In ServerHello, read extension_data in a loop to extract individual Extension objects, store in List<Extension> extensions
```

```
Make git ignore the "captures" directory
```

```
Add these to class Extension as int constants:
enum {
server_name(0), /* RFC 6066 */
max_fragment_length(1), /* RFC 6066 */
status_request(5), /* RFC 6066 */
supported_groups(10), /* RFC 8422, 7919 */
signature_algorithms(13), /* RFC 8446 */
use_srtp(14), /* RFC 5764 */
heartbeat(15), /* RFC 6520 */
application_layer_protocol_negotiation(16), /* RFC 7301 */
signed_certificate_timestamp(18), /* RFC 6962 */
client_certificate_type(19), /* RFC 7250 */
server_certificate_type(20), /* RFC 7250 */
padding(21), /* RFC 7685 */
pre_shared_key(41), /* RFC 8446 */
early_data(42), /* RFC 8446 */
supported_versions(43), /* RFC 8446 */
cookie(44), /* RFC 8446 */
psk_key_exchange_modes(45), /* RFC 8446 */
certificate_authorities(47), /* RFC 8446 */
oid_filters(48), /* RFC 8446 */
post_handshake_auth(49), /* RFC 8446 */
signature_algorithms_cert(50), /* RFC 8446 */
key_share(51), /* RFC 8446 */
(65535)
} ExtensionType;
```

```
Write a class KeyShareEntry
Read data from java.io.DataInput

Data layout as follows. Integers are unsigned, big-endian.
struct {
NamedGroup group; // 2 byte integer
opaque key_exchange<1..2^16-1>; // 2 byte length, then byte[length]
} KeyShareEntry;

class KeyShareEntry has these public fields, no getter/setter:
int group
byte[] key_exchange
```

```
In class Extension, add a public field Object object.
Check type, if it's KeyShareEntry, build object from data.
```

Now I've finally parsed the server key share layer by layer
Need ECDHE

```
I have my secp256r1 PrivateKey
I have the other party's uncompressed public key bytes
Add code in Keys class to calculate the shared secret, store in Keys field
```

```
In Client.java
1. Move Keys to a Client instance field
2. In readTLSRecords(), check if handshake contains server hello, then read the key share extension to call Keys' computeSharedSecret
```

Now the difficult part begins.

Earlier we had Wireshark to check answers. This next part requires going a long way before we can verify answers.

The first checkpoint should be using server_handshake_traffic_secret to decode encrypted extensions.

To shorten the process, we might connect to a local openssl server's key log file.

The whole process has several inputs:
- client DH private key
- server DH public key
- ClientHello + ServerHello transcription
- Correctly implemented algorithm

```
Add a method in Utils.java to write byte[] to a file given String path
Throw runtime exception
Also add a method to write content from an offset to a file
```

```
Write a script to start a local openssl server
```

```
Add options to capture_openssl.sh:
Current behavior if no arguments
If "local" argument is provided, start openssl server on port 4433, no socat needed
```

```
Add a private ByteArrayOutputStream transcript to class Keys
Add a public function addTranscript(byte[]) to append
Add a public function getTranscript(byte[]) to get all appended bytes from current stream
```

```
After sending the client hello, write all request data excluding the first 5 bytes to transcript
```

```
Write all handshakes from the server to transcript
```

```
Implement HKDF-Extract and HKDF-Expand according to RFC 5869 in class HKDF
I only need SHA256
```
```
Write some test cases using the SHA256 test vectors from Appendix A of RFC 5869
```

```
I want to use SHA256 hash
Write a test program for HKDF using Python's cryptography package
Use the test vectors from the HKDF RFC Appendix
```
```
source myenv/bin/activate && python test_hkdf.py
```
```
It looks like the test data in test_hkdf.py is correct.
Implement HKDF's extract and expand in Java, and compare with test data from test_hkdf.py, write Java unit tests.
```

```
I want to use HKDF.java to implement TLS 1.3 Key Schedule
Implement according to the spec below. No need to consider optimization.

HKDF-Expand-Label(Secret, Label, Context, Length) =
HKDF-Expand(Secret, HkdfLabel, Length)
Where HkdfLabel is specified as:
struct {
uint16 length = Length;
opaque label<7..255> = "tls13 " + Label;
opaque context<0..255> = Context;
} HkdfLabel;
Derive-Secret(Secret, Label, Messages) =
HKDF-Expand-Label(Secret, Label,
Transcript-Hash(Messages), Hash.length)
```

```
Add a private field byte[] PSK to Keys.java, no getter/setter.
Initial value should be hash-length of zero bytes
```

```
According to TLS 1.3 Key Schedule
Add a function in Keys.java to calculate client_handshake_traffic_secret
After receiving server hello,
Call this function in Client.java to print client_handshake_traffic_secret
```

After comparing with ssl_keylog.txt generated by the local openssl server
Now b5f93efb454433cf3e9ea99f133cfe1144a202dbccc205d782965ae2fad13b18 is correct!
This means the key-related functions are all working!

----
Now to move to the next step. Since we've simplified fragments, the first record contains the entire ServerHello.

Then there will be an optional record: change_cipher_spec (20) (0x14)

After that, everything will have application_data (23) (0x17) as the outer type.