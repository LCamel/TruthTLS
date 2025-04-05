package org.truthtls;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class Handshake {
    // Public fields as requested
    public int msg_type;
    public int length;
    public byte[] data;
    
    /**
     * Reads a Handshake message from the given input stream
     * 
     * @param in The DataInput to read from
     * @throws IOException If an I/O error occurs
     */
    public static Handshake read(DataInput in) throws IOException {
        Handshake handshake = new Handshake();
        
        // Read HandshakeType (1 byte)
        handshake.msg_type = in.readUnsignedByte();
        
        // Read uint24 length (3 bytes)
        // Since Java doesn't have a direct method to read 3 bytes as an integer,
        // we need to read each byte and combine them
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        
        // Combine bytes to form uint24 (big-endian)
        handshake.length = (b1 << 16) | (b2 << 8) | b3;
        
        // Read data
        handshake.data = new byte[handshake.length];
        in.readFully(handshake.data);
        
        return handshake;
    }
    
    /**
     * Reads a Handshake message from the given byte array
     * 
     * @param bytes The byte array containing the handshake message
     * @return The parsed Handshake object
     * @throws RuntimeException If an I/O error occurs
     */
    public static Handshake read(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bais)) {
            return read(dis);
        } catch (IOException e) {
            throw new RuntimeException("Error reading Handshake from byte array", e);
        }
    }
    
    /**
     * Creates an empty Handshake
     */
    public Handshake() {
        this.msg_type = 0;
        this.length = 0;
        this.data = new byte[0];
    }
    
    /**
     * Creates a Handshake with the specified type, length, and data
     * 
     * @param msg_type The handshake message type
     * @param data The handshake data
     */
    public Handshake(int msg_type, byte[] data) {
        this.msg_type = msg_type;
        this.length = data.length;
        this.data = data;
    }
    
    /**
     * Displays the Handshake fields and data in a formatted way
     */
    public void dump() {
        System.out.println("Handshake Type: " + msg_type);
        System.out.println("Length: " + length);
        Utils.hexdump("Handshake Data", data);
    }
    
}