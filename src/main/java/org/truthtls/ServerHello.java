package org.truthtls;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for parsing TLS 1.3 ServerHello messages
 */
public class ServerHello {
    // Public fields as requested
    public int cipher_suite;
    public byte[] extension_data;
    public List<Extension> extensions;
    
    // Additional fields for completeness
    private byte[] random;
    private byte[] legacy_session_id_echo;
    
    /**
     * Reads a ServerHello message from the given input stream
     * 
     * @param in The DataInput to read from
     * @throws RuntimeException If an I/O error occurs or the message format is invalid
     */
    public static ServerHello read(DataInput in) {
        ServerHello serverHello = new ServerHello();
        
        try {
            // Read legacy_version (2 bytes), must be 0x0303
            int legacyVersion = in.readUnsignedShort();
            Utils.assertEquals("Invalid ServerHello legacy_version", 0x0303, legacyVersion);
            
            // Read random (32 bytes)
            serverHello.random = new byte[32];
            in.readFully(serverHello.random);
            
            // Read legacy_session_id_echo (variable length, 0-32 bytes)
            int sessionIdLength = in.readUnsignedByte();
            Utils.assertAtMost("Invalid session ID length", 32, sessionIdLength);
            serverHello.legacy_session_id_echo = new byte[sessionIdLength];
            in.readFully(serverHello.legacy_session_id_echo);
            
            // Read cipher_suite (2 bytes)
            serverHello.cipher_suite = in.readUnsignedShort();
            
            // Read legacy_compression_method (1 byte), must be 0
            int compressionMethod = in.readUnsignedByte();
            Utils.assertEquals("Invalid compression method", 0, compressionMethod);
            
            // Read extensions length (2 bytes)
            int extensionsLength = in.readUnsignedShort();
            
            // Extensions must be at least 6 bytes and at most 2^16-1 bytes
            if (extensionsLength < 6 || extensionsLength > 65535) {
                throw new RuntimeException("Invalid extensions length: " + extensionsLength);
            }
            
            // Read extensions data
            serverHello.extension_data = new byte[extensionsLength];
            in.readFully(serverHello.extension_data);
            
            // Parse extensions
            serverHello.extensions = new ArrayList<>();
            ByteArrayInputStream bis = new ByteArrayInputStream(serverHello.extension_data);
            DataInputStream dis = new DataInputStream(bis);
            while (dis.available() > 0) {
                Extension extension = Extension.read(dis);
                serverHello.extensions.add(extension);
            }
            
            return serverHello;
        } catch (IOException e) {
            throw new RuntimeException("I/O error while reading ServerHello message", e);
        }
    }
    
    /**
     * Reads a ServerHello message from a byte array
     * 
     * @param data The byte array containing the ServerHello message
     * @return The parsed ServerHello object
     */
    public static ServerHello read(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bis);
            return read(dis);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing ServerHello from byte array", e);
        }
    }
    
    /**
     * Display the ServerHello content
     */
    public void dump() {
        System.out.println("ServerHello:");
        System.out.printf("  Cipher Suite: 0x%04X\n", cipher_suite);
        System.out.println("  Random:");
        Utils.hexdump("    ", random);
        System.out.println("  Session ID Echo [" + legacy_session_id_echo.length + "]:");
        Utils.hexdump("    ", legacy_session_id_echo);
        System.out.println("  Extensions [" + extension_data.length + "]:");
        Utils.hexdump("    ", extension_data);
        System.out.println("  Parsed Extensions:");
        for (Extension extension : extensions) {
            extension.dump();
        }
    }
}