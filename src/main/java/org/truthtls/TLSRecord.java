package org.truthtls;

import java.io.DataInput;
import java.io.IOException;

public class TLSRecord {
    // Public fields as requested
    public int type;
    public byte[] data;
    
    // Max fragment length as specified (2^14)
    private static final int MAX_FRAGMENT_LENGTH = 16384;
    
    /**
     * Reads a TLS record from the given input stream
     * 
     * @param in The DataInput to read from
     * @throws RuntimeException If an I/O error occurs or the record format is invalid
     */
    public static TLSRecord read(DataInput in) {
        TLSRecord record = new TLSRecord();
        
        try {
            // Read ContentType (1 byte)
            record.type = in.readUnsignedByte();
            
            // Read legacy_record_version (2 bytes), should be 0x0303
            int version = in.readUnsignedShort();
            Utils.assertEquals("Invalid TLS record version", 0x0303, version);
            
            // Read length (2 bytes)
            int length = in.readUnsignedShort();
            Utils.assertAtMost("Invalid TLS record length", MAX_FRAGMENT_LENGTH, length);
            
            // Read fragment data
            record.data = new byte[length];
            in.readFully(record.data);
            
            return record;
        } catch (IOException e) {
            throw new RuntimeException("I/O error while reading TLS record", e);
        }
    }
    
    /**
     * Creates an empty TLSRecord
     */
    public TLSRecord() {
        this.type = 0;
        this.data = new byte[0];
    }
    
    /**
     * Creates a TLSRecord with the specified type and data
     * 
     * @param type The content type
     * @param data The fragment data
     */
    public TLSRecord(int type, byte[] data) {
        this.type = type;
        this.data = data;
    }
    
    /**
     * Displays a hexdump of the record data with the given title and includes the record type
     * 
     * @param title The title to display before the hexdump
     */
    public void hexdump(String title) {
        String fullTitle = String.format("%s (Type: 0x%02X)", title, this.type);
        Utils.hexdump(fullTitle, this.data);
    }
}