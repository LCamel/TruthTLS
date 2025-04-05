package org.truthtls;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class Handshake {
    // Constants for HandshakeType as defined in the TLS specification
    public static final int CLIENT_HELLO = 1;
    public static final int SERVER_HELLO = 2;
    public static final int NEW_SESSION_TICKET = 4;
    public static final int END_OF_EARLY_DATA = 5;
    public static final int ENCRYPTED_EXTENSIONS = 8;
    public static final int CERTIFICATE = 11;
    public static final int CERTIFICATE_REQUEST = 13;
    public static final int CERTIFICATE_VERIFY = 15;
    public static final int FINISHED = 20;
    public static final int KEY_UPDATE = 24;
    public static final int MESSAGE_HASH = 254;

    // Public fields as requested
    public int msg_type;
    public int length;
    public byte[] data;
    public Object object = null;  // New field for parsed handshake objects
    
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
        
        // Parse the data into appropriate object based on message type
        if (handshake.msg_type == SERVER_HELLO) {
            handshake.object = ServerHello.read(handshake.data);
        }
        // Additional message types can be handled here as they are implemented
        
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
        
        // If object is parsed, also dump its contents
        if (object != null) {
            System.out.println("Parsed Object:");
            if (object instanceof ServerHello) {
                ((ServerHello) object).dump();
            } else {
                System.out.println("  " + object.toString());
            }
        }
    }
    
}