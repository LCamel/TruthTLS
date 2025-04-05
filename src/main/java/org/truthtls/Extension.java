package org.truthtls;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;

/**
 * Class for parsing TLS extension structures
 */
public class Extension {
    // Extension type constants from RFC standards
    public static final int SERVER_NAME = 0;                           /* RFC 6066 */
    public static final int MAX_FRAGMENT_LENGTH = 1;                   /* RFC 6066 */
    public static final int STATUS_REQUEST = 5;                        /* RFC 6066 */
    public static final int SUPPORTED_GROUPS = 10;                     /* RFC 8422, 7919 */
    public static final int SIGNATURE_ALGORITHMS = 13;                 /* RFC 8446 */
    public static final int USE_SRTP = 14;                            /* RFC 5764 */
    public static final int HEARTBEAT = 15;                           /* RFC 6520 */
    public static final int APPLICATION_LAYER_PROTOCOL_NEGOTIATION = 16; /* RFC 7301 */
    public static final int SIGNED_CERTIFICATE_TIMESTAMP = 18;         /* RFC 6962 */
    public static final int CLIENT_CERTIFICATE_TYPE = 19;              /* RFC 7250 */
    public static final int SERVER_CERTIFICATE_TYPE = 20;              /* RFC 7250 */
    public static final int PADDING = 21;                             /* RFC 7685 */
    public static final int PRE_SHARED_KEY = 41;                      /* RFC 8446 */
    public static final int EARLY_DATA = 42;                          /* RFC 8446 */
    public static final int SUPPORTED_VERSIONS = 43;                   /* RFC 8446 */
    public static final int COOKIE = 44;                              /* RFC 8446 */
    public static final int PSK_KEY_EXCHANGE_MODES = 45;               /* RFC 8446 */
    public static final int CERTIFICATE_AUTHORITIES = 47;              /* RFC 8446 */
    public static final int OID_FILTERS = 48;                         /* RFC 8446 */
    public static final int POST_HANDSHAKE_AUTH = 49;                 /* RFC 8446 */
    public static final int SIGNATURE_ALGORITHMS_CERT = 50;            /* RFC 8446 */
    public static final int KEY_SHARE = 51;                           /* RFC 8446 */
    
    // Public fields as requested
    public int type;  // extension_type
    public byte[] data; // extension_data
    public Object object = null; // Parsed object based on extension type
    
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
            
            // Parse specific extension types
            if (extension.type == KEY_SHARE) {
                extension.object = KeyShareEntry.read(extension.data);
            }
            
            return extension;
        } catch (Exception e) {
            throw new RuntimeException("Error while reading Extension", e);
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
        
        // If parsed object exists, also dump its information
        if (object != null) {
            System.out.println("Parsed Extension Content:");
            if (object instanceof KeyShareEntry) {
                ((KeyShareEntry) object).dump();
            } else {
                System.out.println("  " + object.toString());
            }
        }
    }
}