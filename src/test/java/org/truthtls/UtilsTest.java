package org.truthtls;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    public void testHexStringToByteArray_ContinuousString() {
        // Test with a continuous hex string
        String hexString = "16030100c3010000bf0303678167367ea88dc61897c3a6fed9f655e5e8f26427";
        byte[] result = Utils.hexStringToByteArray(hexString);
        
        // Expected results - manually verified
        byte[] expected = new byte[] {
            0x16, 0x03, 0x01, 0x00, (byte)0xc3, 0x01, 0x00, 0x00, 
            (byte)0xbf, 0x03, 0x03, 0x67, (byte)0x81, 0x67, 0x36, 0x7e,
            (byte)0xa8, (byte)0x8d, (byte)0xc6, 0x18, (byte)0x97, (byte)0xc3, (byte)0xa6, (byte)0xfe, 
            (byte)0xd9, (byte)0xf6, 0x55, (byte)0xe5, (byte)0xe8, (byte)0xf2, 0x64, 0x27
        };
        
        assertArrayEquals(expected, result, "Continuous hex string conversion failed");
    }
    
    @Test
    public void testHexStringToByteArray_SpaceSeparatedString() {
        // Test with a space-separated hex string
        String hexString = "16 03 01 00 c3 01 00 00 bf 03 03 67 81 67 36 7e "
                + "a8 8d c6 18 97 c3 a6 fe d9 f6 55 e5 e8 f2 64 27";
        byte[] result = Utils.hexStringToByteArray(hexString);
        
        // Expected results - should be the same as in the previous test
        byte[] expected = new byte[] {
            0x16, 0x03, 0x01, 0x00, (byte)0xc3, 0x01, 0x00, 0x00, 
            (byte)0xbf, 0x03, 0x03, 0x67, (byte)0x81, 0x67, 0x36, 0x7e,
            (byte)0xa8, (byte)0x8d, (byte)0xc6, 0x18, (byte)0x97, (byte)0xc3, (byte)0xa6, (byte)0xfe, 
            (byte)0xd9, (byte)0xf6, 0x55, (byte)0xe5, (byte)0xe8, (byte)0xf2, 0x64, 0x27
        };
        
        assertArrayEquals(expected, result, "Space-separated hex string conversion failed");
    }
    
    @Test
    public void testHexStringToByteArray_MixedFormatString() {
        // Test with a mixed format (some spaces, some without)
        String hexString = "16 0301 00c3 010000bf0303 678167367e a88dc61897c3a6fed9f655e5e8f26427";
        byte[] result = Utils.hexStringToByteArray(hexString);
        
        // Expected results - should be the same as in the previous tests
        byte[] expected = new byte[] {
            0x16, 0x03, 0x01, 0x00, (byte)0xc3, 0x01, 0x00, 0x00, 
            (byte)0xbf, 0x03, 0x03, 0x67, (byte)0x81, 0x67, 0x36, 0x7e,
            (byte)0xa8, (byte)0x8d, (byte)0xc6, 0x18, (byte)0x97, (byte)0xc3, (byte)0xa6, (byte)0xfe, 
            (byte)0xd9, (byte)0xf6, 0x55, (byte)0xe5, (byte)0xe8, (byte)0xf2, 0x64, 0x27
        };
        
        assertArrayEquals(expected, result, "Mixed format hex string conversion failed");
    }
    
    @Test
    public void testHexStringToByteArray_ShortString() {
        // Test with a short hex string
        String hexString = "0A0B0C";
        byte[] result = Utils.hexStringToByteArray(hexString);
        
        byte[] expected = new byte[] {0x0A, 0x0B, 0x0C};
        
        assertArrayEquals(expected, result, "Short hex string conversion failed");
    }
    
    @Test
    public void testHexStringToByteArray_OddLengthString() {
        // Test with a hex string of odd length - should throw exception
        String hexString = "0A0B0";
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.hexStringToByteArray(hexString);
        });
    }
    
    @Test
    public void testHexStringToByteArray_InvalidHexChar() {
        // Test with invalid hex characters - should throw exception
        String hexString = "0A0G0C";
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.hexStringToByteArray(hexString);
        });
    }
}