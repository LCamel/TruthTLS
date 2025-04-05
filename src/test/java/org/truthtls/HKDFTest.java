package org.truthtls;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class HKDFTest {
    
    @Test
    public void testCase1() {
        // Test Case 1 from RFC 5869 Appendix A.1
        byte[] ikm = Utils.hexStringToByteArray("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        byte[] salt = Utils.hexStringToByteArray("000102030405060708090a0b0c");
        byte[] info = Utils.hexStringToByteArray("f0f1f2f3f4f5f6f7f8f9");
        int length = 42;
        
        // Expected results from RFC 5869
        byte[] expectedPrk = Utils.hexStringToByteArray(
            "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5");
        
        byte[] expectedOkm = Utils.hexStringToByteArray(
            "3cb25f25faacd57a90434f64d0362f2a"
            + "2d2d0a90cf1a5a4c5db02d56ecc4c5bf"
            + "34007208d5b887185865");
        
        // Test extract function
        byte[] actualPrk = HKDF.extract(salt, ikm);
        assertArrayEquals(expectedPrk, actualPrk, "HKDF Extract - Test Case 1 failed");
        
        // Test expand function
        byte[] actualOkm = HKDF.expand(actualPrk, info, length);
        assertArrayEquals(expectedOkm, actualOkm, "HKDF Expand - Test Case 1 failed");
    }
    
    @Test
    public void testCase2() {
        // Test Case 2 from RFC 5869 Appendix A.2
        byte[] ikm = Utils.hexStringToByteArray(
            "000102030405060708090a0b0c0d0e0f"
            + "101112131415161718191a1b1c1d1e1f"
            + "202122232425262728292a2b2c2d2e2f"
            + "303132333435363738393a3b3c3d3e3f"
            + "404142434445464748494a4b4c4d4e4f");
        
        byte[] salt = Utils.hexStringToByteArray(
            "606162636465666768696a6b6c6d6e6f"
            + "707172737475767778797a7b7c7d7e7f"
            + "808182838485868788898a8b8c8d8e8f"
            + "909192939495969798999a9b9c9d9e9f"
            + "a0a1a2a3a4a5a6a7a8a9aaabacadaeaf");
        
        byte[] info = Utils.hexStringToByteArray(
            "b0b1b2b3b4b5b6b7b8b9babbbcbdbebf"
            + "c0c1c2c3c4c5c6c7c8c9cacbcccdcecf"
            + "d0d1d2d3d4d5d6d7d8d9dadbdcdddedf"
            + "e0e1e2e3e4e5e6e7e8e9eaebecedeeef"
            + "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff");
        
        int length = 82;
        
        // Expected results from RFC 5869
        byte[] expectedPrk = Utils.hexStringToByteArray(
            "06a6b88c5853361a06104c9ceb35b45cef760014904671014a193f40c15fc244");
        
        byte[] expectedOkm = Utils.hexStringToByteArray(
            "b11e398dc80327a1c8e7f78c596a4934"
            + "4f012eda2d4efad8a050cc4c19afa97c"
            + "59045a99cac7827271cb41c65e590e09"
            + "da3275600c2f09b8367793a9aca3db71"
            + "cc30c58179ec3e87c14c01d5c1f3434f"
            + "1d87");
        
        // Test extract function
        byte[] actualPrk = HKDF.extract(salt, ikm);
        assertArrayEquals(expectedPrk, actualPrk, "HKDF Extract - Test Case 2 failed");
        
        // Test expand function
        byte[] actualOkm = HKDF.expand(actualPrk, info, length);
        assertArrayEquals(expectedOkm, actualOkm, "HKDF Expand - Test Case 2 failed");
    }
    
    @Test
    public void testCase3() {
        // Test Case 3 from RFC 5869 Appendix A.3
        byte[] ikm = Utils.hexStringToByteArray("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        byte[] salt = new byte[0]; // Empty salt value
        byte[] info = new byte[0]; // Empty info value
        int length = 42;
        
        // Expected results from RFC 5869
        byte[] expectedPrk = Utils.hexStringToByteArray(
            "19ef24a32c717b167f33a91d6f648bdf96596776afdb6377ac434c1c293ccb04");
        
        byte[] expectedOkm = Utils.hexStringToByteArray(
            "8da4e775a563c18f715f802a063c5a31"
            + "b8a11f5c5ee1879ec3454e5f3c738d2d"
            + "9d201395faa4b61a96c8");
        
        // Test extract function
        byte[] actualPrk = HKDF.extract(salt, ikm);
        assertArrayEquals(expectedPrk, actualPrk, "HKDF Extract - Test Case 3 failed");
        
        // Test expand function
        byte[] actualOkm = HKDF.expand(actualPrk, info, length);
        assertArrayEquals(expectedOkm, actualOkm, "HKDF Expand - Test Case 3 failed");
    }
    
    @Test
    public void testNullValues() {
        // Test with null values
        byte[] ikm = Utils.hexStringToByteArray("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        
        // Null salt should be treated as an empty array
        byte[] actualPrk1 = HKDF.extract(null, ikm);
        byte[] actualPrk2 = HKDF.extract(new byte[0], ikm);
        assertArrayEquals(actualPrk1, actualPrk2, "HKDF Extract with null salt should be equivalent to empty salt");
        
        // Null info should be treated as an empty array
        byte[] prk = HKDF.extract(null, ikm);
        byte[] actualOkm1 = HKDF.expand(prk, null, 42);
        byte[] actualOkm2 = HKDF.expand(prk, new byte[0], 42);
        assertArrayEquals(actualOkm1, actualOkm2, "HKDF Expand with null info should be equivalent to empty info");
    }
    
    @Test
    public void testInvalidLength() {
        byte[] prk = new byte[32]; // Dummy PRK
        byte[] info = new byte[0]; // Empty info
        
        // Length must be <= 255*Hash_Length (255*32 = 8160 for SHA-256)
        assertThrows(IllegalArgumentException.class, () -> {
            HKDF.expand(prk, info, 8161);
        });
    }
}