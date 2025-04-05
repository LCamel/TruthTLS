package org.truthtls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private static final String SERVER_HOST = "localhost"; //"google.com";
    private static final int SERVER_PORT = 4433; //443;
    private static final int BUFFER_SIZE = 4096;

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
            Keys keys = new Keys();
            byte[] uncompressedPublicKey = keys.getUncompressedPublicKey();
            
            // Replace the last 65 bytes of the request with the uncompressed public key
            // The uncompressed key format is exactly 65 bytes (1 byte prefix + 32 bytes X + 32 bytes Y)
            int pubKeyOffset = requestData.length - 65;
            System.arraycopy(uncompressedPublicKey, 0, requestData, pubKeyOffset, 65);
            
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
                
                // Get input stream and read response
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                int totalBytesRead = 0;
                
                // Create a dynamic byte array to store all response data
                byte[] responseData = new byte[0];
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    // Extend the response array to accommodate new data
                    byte[] newResponseData = new byte[responseData.length + bytesRead];
                    System.arraycopy(responseData, 0, newResponseData, 0, responseData.length);
                    System.arraycopy(buffer, 0, newResponseData, responseData.length, bytesRead);
                    responseData = newResponseData;
                    
                    totalBytesRead += bytesRead;
                    System.out.println("Received " + bytesRead + " bytes...");
                    
                    // Some servers may not close the connection, so we need to break manually
                    // This is a simple approach - in real applications you would parse the TLS records
                    if (totalBytesRead > 0 && in.available() == 0) {
                        try {
                            // Give the server a short time to send more data
                            Thread.sleep(100);
                            if (in.available() == 0) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                System.out.println("Total bytes received: " + totalBytesRead);
                Utils.hexdump("Response", responseData);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }
}