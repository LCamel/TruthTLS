package org.truthtls;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Class for parsing TLS extension structures
 */
public class Extension {
    // Public fields as requested
    public int type;  // extension_type
    public byte[] data; // extension_data
    
    /**
     * Reads an Extension from the given DataInput
     * 
     * @param in The DataInput to read from
     * @return The parsed Extension object
     * @throws RuntimeException if there's an error reading the data
     */
    public static Extension read(DataInput in) {
        Extension extension = new Extension();
        
        try {
            // Read extension_type (2 bytes)
            extension.type = in.readUnsignedShort();
            
            // Read extension_data length (2 bytes)
            int dataLength = in.readUnsignedShort();
            
            // Read extension_data
            extension.data = new byte[dataLength];
            in.readFully(extension.data);
            
            return extension;
        } catch (IOException e) {
            throw new RuntimeException("I/O error while reading Extension", e);
        }
    }
    
    /**
     * Reads an Extension from a byte array
     * 
     * @param data The byte array containing the extension
     * @return The parsed Extension object
     */
    public static Extension read(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bis);
            return read(dis);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Extension from byte array", e);
        }
    }
    
    /**
     * Creates an empty Extension
     */
    public Extension() {
        this.type = 0;
        this.data = new byte[0];
    }
    
    /**
     * Creates an Extension with the specified type and data
     * 
     * @param type The extension type
     * @param data The extension data
     */
    public Extension(int type, byte[] data) {
        this.type = type;
        this.data = data;
    }
    
    /**
     * Display the Extension content
     */
    public void dump() {
        System.out.println("Extension Type: 0x" + Integer.toHexString(type));
        System.out.println("Extension Data Length: " + data.length);
        Utils.hexdump("Extension Data", data);
    }
}