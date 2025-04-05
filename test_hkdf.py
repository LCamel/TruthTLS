#!/usr/bin/env python3
"""
HKDF Test Program

This script tests the HMAC-based Key Derivation Function (HKDF) implementation
using test vectors from RFC 5869.

Reference: https://datatracker.ietf.org/doc/html/rfc5869
"""

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
import binascii

def perform_hkdf(ikm, salt, info, length, hash_algo):
    """
    Perform HKDF key derivation.
    
    Args:
        ikm (bytes): Initial keying material
        salt (bytes): Salt value
        info (bytes): Context and application specific information
        length (int): Length of output keying material in bytes
        hash_algo (algorithm): Hash algorithm to use
        
    Returns:
        bytes: Output keying material
    """
    hkdf = HKDF(
        algorithm=hash_algo,
        length=length,
        salt=salt,
        info=info,
    )
    okm = hkdf.derive(ikm)
    return okm

def test_case_1():
    """Test Case 1 from RFC 5869 Appendix A.1"""
    print("Test Case 1:")
    ikm = binascii.unhexlify("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b")
    salt = binascii.unhexlify("000102030405060708090a0b0c")
    info = binascii.unhexlify("f0f1f2f3f4f5f6f7f8f9")
    length = 42
    
    okm = perform_hkdf(ikm, salt, info, length, hashes.SHA256())
    
    expected = binascii.unhexlify(
        "3cb25f25faacd57a90434f64d0362f2a"
        "2d2d0a90cf1a5a4c5db02d56ecc4c5bf"
        "34007208d5b887185865"
    )
    
    print(f"OKM:      {binascii.hexlify(okm).decode()}")
    print(f"Expected: {binascii.hexlify(expected).decode()}")
    print(f"Match:    {okm == expected}")
    return okm == expected

def test_case_2():
    """Test Case 2 from RFC 5869 Appendix A.2"""
    print("\nTest Case 2:")
    ikm = binascii.unhexlify(
        "000102030405060708090a0b0c0d0e0f"
        "101112131415161718191a1b1c1d1e1f"
        "202122232425262728292a2b2c2d2e2f"
        "303132333435363738393a3b3c3d3e3f"
        "404142434445464748494a4b4c4d4e4f"
    )
    salt = binascii.unhexlify(
        "606162636465666768696a6b6c6d6e6f"
        "707172737475767778797a7b7c7d7e7f"
        "808182838485868788898a8b8c8d8e8f"
        "909192939495969798999a9b9c9d9e9f"
        "a0a1a2a3a4a5a6a7a8a9aaabacadaeaf"
    )
    info = binascii.unhexlify(
        "b0b1b2b3b4b5b6b7b8b9babbbcbdbebf"
        "c0c1c2c3c4c5c6c7c8c9cacbcccdcecf"
        "d0d1d2d3d4d5d6d7d8d9dadbdcdddedf"
        "e0e1e2e3e4e5e6e7e8e9eaebecedeeef"
        "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"
    )
    length = 82
    
    okm = perform_hkdf(ikm, salt, info, length, hashes.SHA256())
    
    expected = binascii.unhexlify(
        "b11e398dc80327a1c8e7f78c596a4934"
        "4f012eda2d4efad8a050cc4c19afa97c"
        "59045a99cac7827271cb41c65e590e09"
        "da3275600c2f09b8367793a9aca3db71"
        "cc30c58179ec3e87c14c01d5c1f3434f"
        "1d87"
    )
    
    print(f"OKM:      {binascii.hexlify(okm).decode()}")
    print(f"Expected: {binascii.hexlify(expected).decode()}")
    print(f"Match:    {okm == expected}")
    return okm == expected

def test_case_3():
    """Test Case 3 from RFC 5869 Appendix A.3"""
    print("\nTest Case 3:")
    ikm = binascii.unhexlify("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b")
    # Note: Empty salt value
    salt = b""
    # Note: Empty info value
    info = b""
    length = 42
    
    okm = perform_hkdf(ikm, salt, info, length, hashes.SHA256())
    
    expected = binascii.unhexlify(
        "8da4e775a563c18f715f802a063c5a31"
        "b8a11f5c5ee1879ec3454e5f3c738d2d"
        "9d201395faa4b61a96c8"
    )
    
    print(f"OKM:      {binascii.hexlify(okm).decode()}")
    print(f"Expected: {binascii.hexlify(expected).decode()}")
    print(f"Match:    {okm == expected}")
    return okm == expected

def main():
    """Run all test cases."""
    print("HKDF Test Vectors from RFC 5869\n")
    
    results = [
        test_case_1(),
        test_case_2(),
        test_case_3()
    ]
    
    print("\nSummary:")
    print(f"Total tests: {len(results)}")
    print(f"Passed: {sum(results)}")
    print(f"Failed: {len(results) - sum(results)}")
    
    if all(results):
        print("\nAll tests PASSED!")
        return 0
    else:
        print("\nSome tests FAILED!")
        return 1

if __name__ == "__main__":
    exit(main())