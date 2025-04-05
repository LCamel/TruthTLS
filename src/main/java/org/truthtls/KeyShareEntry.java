package org.truthtls;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class KeyShareEntry {
    public int group;
    public byte[] key_exchange;
    
    public KeyShareEntry(DataInput in) {
        try {
            read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read KeyShareEntry", e);
        }
    }
    
    public void read(DataInput in) throws IOException {
        // Read the named group (2 bytes)
        group = in.readUnsignedShort();
        
        // Read the key_exchange data
        int keyExchangeLength = in.readUnsignedShort();
        key_exchange = new byte[keyExchangeLength];
        in.readFully(key_exchange);
    }
    
    // Constructor for creating from a byte array
    public static KeyShareEntry read(byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            return new KeyShareEntry(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create KeyShareEntry from bytes", e);
        }
    }
    
    // Utility method to dump the entry
    public void dump() {
        System.out.println("KeyShareEntry:");
        System.out.println("  group: " + group);
        Utils.hexdump("  key_exchange", key_exchange);
    }
}