package org.truthtls;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import javax.crypto.KeyAgreement;

/**
 * A class that generates and stores a secp256r1 (P-256) key pair upon instantiation.
 * Provides methods to access the keys, including the uncompressed public key format.
 */
public class Keys {
    // Public data members to store the key pair
    public KeyPair keyPair;
    
    // Store the shared secret after performing ECDH key agreement
    public byte[] sharedSecret;
    
    // Store transcript data
    private ByteArrayOutputStream transcript = new ByteArrayOutputStream();
    
    // Pre-Shared Key, initialized to all zeros
    private byte[] PSK = new byte[32]; // 32 bytes = SHA-256 hash length
    
    // TLS 1.3 Traffic Secrets
    private byte[] earlySecret;
    private byte[] handshakeSecret;
    private byte[] clientHandshakeTrafficSecret;
    private byte[] serverHandshakeTrafficSecret;
    
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
     * Adds data to the transcript
     * @param data The byte array to append to the transcript
     */
    public void addTranscript(byte[] data) {
        try {
            transcript.write(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add data to transcript: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the current full transcript as a byte array
     * @return The complete transcript as a byte array
     */
    public byte[] getTranscript() {
        return transcript.toByteArray();
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
     * Computes the shared secret using ECDH with our private key and the other party's public key
     * 
     * @param peerPublicKeyBytes The other party's uncompressed public key (65 bytes, starting with 0x04)
     * @return The computed shared secret
     */
    public byte[] computeSharedSecret(byte[] peerPublicKeyBytes) {
        try {
            // Verify that the key starts with 0x04 (uncompressed format)
            if (peerPublicKeyBytes.length != 65 || peerPublicKeyBytes[0] != 0x04) {
                throw new IllegalArgumentException("Invalid uncompressed public key format");
            }
            
            // Extract x and y coordinates from the uncompressed format
            byte[] xBytes = new byte[32];
            byte[] yBytes = new byte[32];
            System.arraycopy(peerPublicKeyBytes, 1, xBytes, 0, 32);
            System.arraycopy(peerPublicKeyBytes, 33, yBytes, 0, 32);
            
            // Create BigInteger values for x and y
            java.math.BigInteger x = new java.math.BigInteger(1, xBytes);
            java.math.BigInteger y = new java.math.BigInteger(1, yBytes);
            
            // Create EC point from x and y
            ECPoint point = new ECPoint(x, y);
            
            // Get the EC parameters spec from our key pair
            ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
            ECParameterSpec ecParameterSpec = ecPublicKey.getParams();
            
            // Create a public key specification
            ECPublicKeySpec peerKeySpec = new ECPublicKeySpec(point, ecParameterSpec);
            
            // Generate the public key
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey peerPublicKey = keyFactory.generatePublic(peerKeySpec);
            
            // Create and initialize the key agreement
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(keyPair.getPrivate());
            
            // Perform the key agreement protocol
            keyAgreement.doPhase(peerPublicKey, true);
            
            // Generate the shared secret
            this.sharedSecret = keyAgreement.generateSecret();
            
            // Output information about the shared secret
            System.out.println("ECDH shared secret computed successfully");
            Utils.hexdump("Shared Secret", this.sharedSecret);
            
            return this.sharedSecret;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute shared secret: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculates the handshake secrets according to TLS 1.3 Key Schedule
     * This should be called after the shared secret is computed via ECDH
     * 
     * @return The client_handshake_traffic_secret
     */
    public byte[] calculateHandshakeSecrets() {
        if (sharedSecret == null) {
            throw new IllegalStateException("Shared secret must be computed first");
        }
        
        try {
            // 1. Calculate early_secret = HKDF-Extract(salt=0, key=PSK)
            byte[] zeroSalt = new byte[32]; // 32 zeros for SHA-256
            earlySecret = HKDF.extract(zeroSalt, PSK);
            Utils.hexdump("early_secret", earlySecret);
            
            // 2. For Derive-Secret, we need an empty string, not its hash
            byte[] emptyString = new byte[0];
            
            // 3. Calculate derived_secret = HKDF.deriveSecret(earlySecret, "derived", emptyString)
            byte[] derivedSecret = HKDF.deriveSecret(earlySecret, "derived", emptyString);
            Utils.hexdump("derived_secret", derivedSecret);
            
            // 4. Calculate handshake_secret = HKDF-Extract(derived_secret, shared_secret)
            handshakeSecret = HKDF.extract(derivedSecret, sharedSecret);
            Utils.hexdump("handshake_secret", handshakeSecret);
            
            // 5. Calculate client/server_handshake_traffic_secret
            byte[] transcriptBytes = getTranscript();
            clientHandshakeTrafficSecret = 
                HKDF.deriveSecret(handshakeSecret, "c hs traffic", transcriptBytes);
            Utils.hexdump("client_handshake_traffic_secret", clientHandshakeTrafficSecret);
            
            serverHandshakeTrafficSecret = 
                HKDF.deriveSecret(handshakeSecret, "s hs traffic", transcriptBytes);
            Utils.hexdump("server_handshake_traffic_secret", serverHandshakeTrafficSecret);
            
            return clientHandshakeTrafficSecret;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate handshake secrets", e);
        }
    }
    
    /**
     * Gets the client_handshake_traffic_secret
     * 
     * @return The client handshake traffic secret or null if not yet calculated
     */
    public byte[] getClientHandshakeTrafficSecret() {
        return clientHandshakeTrafficSecret;
    }
    
    /**
     * Gets the server_handshake_traffic_secret
     * 
     * @return The server handshake traffic secret or null if not yet calculated
     */
    public byte[] getServerHandshakeTrafficSecret() {
        return serverHandshakeTrafficSecret;
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