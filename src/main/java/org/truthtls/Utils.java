package org.truthtls;

public class Utils {
    /**
     * Converts a hexadecimal string to a byte array.
     * Can handle both continuous hex strings and space-separated hex strings.
     * 
     * @param hexString The hexadecimal string to convert
     * @return The resulting byte array
     * @throws IllegalArgumentException if the string length is odd or contains invalid hex characters
     */
    public static byte[] hexStringToByteArray(String hexString) {
        // Remove all spaces from the string
        String cleanHexString = hexString.replaceAll("\\s+", "");
        
        // Check if the string length is even
        if (cleanHexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }
        
        int len = cleanHexString.length();
        byte[] data = new byte[len / 2];
        
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(cleanHexString.charAt(i), 16);
            int low = Character.digit(cleanHexString.charAt(i + 1), 16);
            
            // Check for invalid hex characters
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character in string");
            }
            
            data[i / 2] = (byte) ((high << 4) + low);
        }
        
        return data;
    }
}
