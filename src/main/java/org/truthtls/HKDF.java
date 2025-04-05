package org.truthtls;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of HMAC-based Key Derivation Function (HKDF) as per RFC 5869.
 * This implementation only supports SHA-256.
 */
public class HKDF {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int HASH_LENGTH = 32; // SHA-256 hash length in bytes
    
    /**
     * HKDF Extract function (RFC 5869 Section 2.2)
     * 
     * @param salt Optional salt value (a non-secret random value). If not provided, 
     *             it is set to a string of HashLen zeros.
     * @param ikm  Input keying material.
     * @return A pseudorandom key (PRK) of HashLen bytes.
     */
    public static byte[] extract(byte[] salt, byte[] ikm) {
        try {
            // If salt is not provided, use a string of HashLen zeros
            if (salt == null || salt.length == 0) {
                salt = new byte[HASH_LENGTH];
            }
            
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(salt, HMAC_ALGORITHM);
            hmac.init(keySpec);
            
            return hmac.doFinal(ikm);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HKDF extraction failed", e);
        }
    }
    
    /**
     * HKDF Expand function (RFC 5869 Section 2.3)
     * 
     * @param prk   A pseudorandom key of at least HashLen bytes (usually the output from extract).
     * @param info  Optional context and application specific information (can be zero-length).
     * @param length Length of output keying material in bytes (must be <= 255*HashLen).
     * @return Output keying material of specified length.
     */
    public static byte[] expand(byte[] prk, byte[] info, int length) {
        if (length > 255 * HASH_LENGTH) {
            throw new IllegalArgumentException("Requested length too long, must be <= " + (255 * HASH_LENGTH));
        }
        
        try {
            // If info is not provided, use a zero-length byte array
            if (info == null) {
                info = new byte[0];
            }
            
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(prk, HMAC_ALGORITHM);
            hmac.init(keySpec);
            
            // Number of iterations (ceiling of length / hash length)
            int n = (length + HASH_LENGTH - 1) / HASH_LENGTH;
            
            byte[] t = new byte[0];        // T(0) = empty string
            byte[] okm = new byte[length]; // Output keying material
            int okmOffset = 0;
            
            for (int i = 1; i <= n; i++) {
                // T(i) = HMAC-Hash(PRK, T(i-1) | info | i)
                hmac.reset();
                hmac.update(t);
                hmac.update(info);
                hmac.update((byte) i);
                t = hmac.doFinal();
                
                // Copy as much as needed into the output
                int copyLength = Math.min(length - okmOffset, t.length);
                System.arraycopy(t, 0, okm, okmOffset, copyLength);
                okmOffset += copyLength;
            }
            
            return okm;
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HKDF expansion failed", e);
        }
    }
}