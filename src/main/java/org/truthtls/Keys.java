package org.truthtls;

import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

/**
 * A class that generates and stores a secp256r1 (P-256) key pair upon instantiation.
 * Provides methods to access the keys, including the uncompressed public key format.
 */
public class Keys {
    // Public data members to store the key pair
    public KeyPair keyPair;
    
    /**
     * Constructor - generates a new secp256r1 key pair upon instantiation
     * @throws RuntimeException if key generation fails
     */
    public Keys() {
        try {
            // Create a KeyPairGenerator for EC (Elliptic Curve)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            
            // Initialize with secp256r1 (P-256) curve parameters
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec);
            
            // Generate the key pair
            this.keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to generate key pair: " + e.getMessage(), e);
        }
    }
    
    /**
     * Returns the public key in uncompressed format (65 bytes)
     * Format: 0x04 + x-coordinate (32 bytes) + y-coordinate (32 bytes)
     * @return byte array of 65 bytes representing the uncompressed public key
     * @throws RuntimeException if the public key cannot be converted
     */
    public byte[] getUncompressedPublicKey() {
        try {
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            
            // Get the public point coordinates
            ECPoint point = publicKey.getW();
            
            // Convert to byte arrays with padding to ensure they are 32 bytes each
            byte[] xBytes = bigIntegerToBytes(point.getAffineX(), 32);
            byte[] yBytes = bigIntegerToBytes(point.getAffineY(), 32);
            
            // Create the result array: 0x04 + x + y = 65 bytes
            byte[] result = new byte[65];
            result[0] = 0x04; // Uncompressed point indicator
            
            // Copy x and y coordinates
            System.arraycopy(xBytes, 0, result, 1, 32);
            System.arraycopy(yBytes, 0, result, 33, 32);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get uncompressed public key: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to convert BigInteger to byte array with fixed length
     * @param value the BigInteger to convert
     * @param length the desired length of the resulting byte array
     * @return byte array of specified length
     * @throws RuntimeException if value is negative or if byte representation exceeds specified length
     */
    private byte[] bigIntegerToBytes(java.math.BigInteger value, int length) {
        // Check for negative values
        if (value.signum() < 0) {
            throw new RuntimeException("Negative values are not allowed");
        }
        
        byte[] bytes = value.toByteArray();
        byte[] result = new byte[length];
        
        if (bytes.length == length + 1 && bytes[0] == 0) {
            // Remove leading zero if present (due to two's complement representation)
            System.arraycopy(bytes, 1, result, 0, length);
        } else if (bytes.length <= length) {
            // Pad with zeros
            System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
        } else {
            // If bytes are longer than the specified length, throw an exception
            throw new RuntimeException("Integer value too large for the specified byte length");
        }
        
        return result;
    }
}