package org.truthtls;

import java.security.SecureRandom;

public class Utils {
    // 使用 SecureRandom 來生成隨機字節
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 生成指定長度的隨機字節陣列
     * 
     * @param length 需要生成的字節數
     * @return 隨機字節陣列
     */
    public static byte[] getRandomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be a non-negative integer");
        }
        
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

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

    /**
     * Prints a formatted hexdump of the byte array to stdout with a title.
     * The output format includes both hexadecimal and ASCII representation.
     * 
     * @param title A title to print before the hexdump
     * @param bytes The byte array to dump
     */
    public static void hexdump(String title, byte[] bytes) {
        if (title != null && !title.isEmpty()) {
            System.out.println(title + ":");
        }
        
        if (bytes == null || bytes.length == 0) {
            System.out.println("[Empty]");
            return;
        }
        
        int bytesPerLine = 16;
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            // Print offset at the beginning of each line
            if (i % bytesPerLine == 0) {
                if (i > 0) {
                    // Print the ASCII representation at the end of the previous line
                    System.out.println(String.format("%-49s %s", hex.toString(), ascii.toString()));
                    hex.setLength(0);
                    ascii.setLength(0);
                }
                System.out.print(String.format("%04X: ", i));
            }
            
            // Add hex representation
            hex.append(String.format("%02X ", bytes[i]));
            
            // Add a separator after 8 bytes for better readability
            if ((i % bytesPerLine) == 7) {
                hex.append(" ");
            }
            
            // Add ASCII representation (printable characters only)
            if (bytes[i] >= 32 && bytes[i] <= 126) {
                ascii.append((char) bytes[i]);
            } else {
                ascii.append('.');
            }
        }
        
        // Print the last line, padding if necessary
        int remaining = bytes.length % bytesPerLine;
        if (remaining > 0) {
            // Calculate how many spaces we need to add for padding
            int spacesToAdd = (bytesPerLine - remaining) * 3;
            // Add extra space if we're padding past the 8-byte separator position
            if (remaining <= 8) {
                spacesToAdd += 1;
            }
            
            for (int i = 0; i < spacesToAdd; i++) {
                hex.append(" ");
            }
        }
        System.out.println(String.format("%-49s %s", hex.toString(), ascii.toString()));
    }
}
