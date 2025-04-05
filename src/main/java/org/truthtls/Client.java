package org.truthtls;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private static final String SERVER_HOST = "localhost"; //"google.com";
    private static final int SERVER_PORT = 4433; //443;
    private static final int BUFFER_SIZE = 4096;
    
    // 将 Keys 移到 Client 的实例字段
    private Keys keys;
    
    // 用于存储 TLS handshake 的 transcript
    private List<byte[]> transcript = new ArrayList<>();

    /**
     * Connects to google.com on port 443, sends a predefined TLS request,
     * and reads all response data.
     */
    public void connect() {
        // TLS request as specified in hex format
        String tlsRequestHex = "16030100c3010000bf0303678167367ea88dc61897c3a6fed9f655e5e8f264277e128df7635d4f52d8fa8320bf1212ea9b2d536dd6593d39c20a390bf4e2e16bae3ee9e039db6734c1e2c8fb0002130101000074000b000403000102000a000400020017000d00080006040108040403002b0003020304002d000201010033004700450017004104e32e315594197a08d8feea3eaf2792d5101d87a304f12a87aa4a52db3b827657716c359677f100ce616ef996a262da9af563d4b65f91bb122617fdd387ba6046";
        
        try {
            // Convert the hex request to byte array
            byte[] requestData = Utils.hexStringToByteArray(tlsRequestHex);
            
            byte[] randomBytes = Utils.getRandomBytes(32);
            System.arraycopy(randomBytes, 0, requestData, 0x43 - 0x38, 32);
            
            byte[] sessionIdBytes = Utils.getRandomBytes(32);
            System.arraycopy(sessionIdBytes, 0, requestData, 0x64 - 0x38, 32);
            
            // Generate EC key pair and get uncompressed public key
            keys = new Keys();
            byte[] uncompressedPublicKey = keys.getUncompressedPublicKey();
            
            // Replace the last 65 bytes of the request with the uncompressed public key
            // The uncompressed key format is exactly 65 bytes (1 byte prefix + 32 bytes X + 32 bytes Y)
            int pubKeyOffset = requestData.length - 65;
            System.arraycopy(uncompressedPublicKey, 0, requestData, pubKeyOffset, 65);
            
            // 跳过前5个字节（TLS record header）并将剩余内容保存到 transcript 中
            // TLS record header: 1 byte (content type) + 2 bytes (protocol version) + 2 bytes (length)
            byte[] clientHelloTranscript = new byte[requestData.length - 5];
            System.arraycopy(requestData, 5, clientHelloTranscript, 0, clientHelloTranscript.length);
            transcript.add(clientHelloTranscript);
            System.out.println("Saved Client Hello to transcript (length: " + clientHelloTranscript.length + " bytes)");
            
            // Display the modified request with randomized sections and new public key
            Utils.hexdump("Modified TLS request", requestData);
            
            System.out.println("Connecting to " + SERVER_HOST + ":" + SERVER_PORT);
            
            // Open socket connection
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
                // Get output stream and send request
                OutputStream out = socket.getOutputStream();
                out.write(requestData);
                out.flush();
                
                System.out.println("Request sent, waiting for response...");
                
                // Get input stream and create DataInputStream for reading TLSRecords
                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);
                
                // Read TLS records from the stream
                readTLSRecords(dis);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }

    private void readTLSRecords(DataInputStream dis) {
        int recordCount = 0;
        boolean hasMoreData = true;
        
        try {
            while (hasMoreData) {
                // Check if there is data available to read
                if (dis.available() > 0) {
                    // Read a TLS record from the stream
                    TLSRecord record = TLSRecord.read(dis);
                    recordCount++;
                    
                    // Perform hexdump on the record
                    record.hexdump("TLS Record #" + recordCount);
                    
                    // Check if this is a handshake record (type 22)
                    if (record.type == 22) {
                        System.out.println("Handshake record detected, creating Handshake object:");
                        Handshake handshake = Handshake.read(record.data);
                        handshake.dump();
                        
                        // 將整個 handshake 記錄到 transcript 中
                        transcript.add(record.data);
                        System.out.println("Saved server handshake to transcript (length: " + record.data.length + " bytes)");
                        
                        // 检查是否为 ServerHello 消息
                        if (handshake.msg_type == Handshake.SERVER_HELLO && handshake.object instanceof ServerHello) {
                            ServerHello serverHello = (ServerHello) handshake.object;
                            
                            // 查找 key_share extension
                            for (Extension extension : serverHello.extensions) {
                                if (extension.type == Extension.KEY_SHARE && extension.object instanceof KeyShareEntry) {
                                    KeyShareEntry keyShareEntry = (KeyShareEntry) extension.object;
                                    
                                    System.out.println("Found server KeyShareEntry, computing shared secret...");
                                    
                                    // 计算共享密钥
                                    keys.computeSharedSecret(keyShareEntry.key_exchange);
                                    
                                    break; // 找到 key_share 后跳出循环
                                }
                            }
                        }
                    }
                } else {
                    // No data available, wait for 1 second
                    System.out.println("Waiting for more data...");
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    
                    // After waiting, check if there's new data
                    if (dis.available() == 0) {
                        hasMoreData = false;
                        System.out.println("No more data received after waiting 1 second. Ending connection.");
                    }
                }
            }
            
            System.out.println("Total TLS records received: " + recordCount);
            
        } catch (IOException e) {
            throw new RuntimeException("Error reading TLS records: " + e.getMessage(), e);
        }
    }

    private void getResponseData(InputStream in) throws IOException {
        // This method is no longer used and can be removed or refactored.
    }
}