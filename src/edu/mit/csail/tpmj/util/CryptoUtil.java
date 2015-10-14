/*
 * Copyright (c) 2006, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of MIT nor the names of its contributors may be used 
 *    to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * Original author:  Luis F. G. Sarmenta, MIT, 2006
 */
package edu.mit.csail.tpmj.util;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.structs.TPM_STORE_PUBKEY;

public class CryptoUtil
{
    public static final String TPM_RSAESOAEP_SHA1_MGF1_ALGNAME = "RSA/NONE/OAEPWithSHA1AndMGF1Padding";

    public static final OAEPParameterSpec TCPA_OAEPSHA1MGF1_SPEC = new OAEPParameterSpec(
        "SHA1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(
            "TCPA".getBytes() ) );
    private static Random rng;

    // static initializer
    static
    {
        Security.addProvider( new sun.security.provider.Sun() );
        Security.addProvider( new BouncyCastleProvider() );
        try
        {
            CryptoUtil.rng = SecureRandom.getInstance( "SHA1PRNG", "SUN" );
        }
        catch ( Exception e )
        {
            System.err.println( "Error getting SecureRandom instance: " + e );
            System.err.println( "Using Java default Random class instead." );
            CryptoUtil.rng = new Random();
        }
    }

    // General Crypto operations (using byte[])

    public static byte[] generateRandomByteArray( int length )
    {
        byte[] ret = new byte[length];
        CryptoUtil.rng.nextBytes( ret );
        return ret;
    }

    /**
     * Returns a new array with the xor of source with key.
     * If key is shorter than source, then it is reused.
     * 
     * @param source
     * @param key
     * @return
     */
    public static byte[] xor( byte[] source, byte[] key )
    {
        int srcLen = source.length;
        int keyLen = key.length;
        byte[] ret = new byte[srcLen];

        for ( int i = 0; i < srcLen; i++ )
        {
            int keyIndex = i % keyLen;
            ret[i] = (byte) (source[i] ^ key[keyIndex]);
        }
        return ret;
    }

    public static byte[] computeSHA1Hash( byte[] input )
    {
        MessageDigest messageDigest;
        try
        {
            messageDigest = MessageDigest.getInstance( "SHA", "SUN" );
            return messageDigest.digest( input );
        }
        catch ( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
            return null;
        }
        catch ( NoSuchProviderException e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] computeSHA1Hash( ByteArrayable ba )
    {
        return CryptoUtil.computeSHA1Hash( ba.toBytes() );
    }

    public static byte[] computeSHA1Hash( byte[]... arrays )
    {
        // TODO: This can probably be more efficiently computed
        // by using the MessageDigest's update function
        // to read the byte arrays one by one instead of
        // concatenating them all first and producing a new
        // byte array.
        byte[] total = ByteArrayUtil.concat( arrays );
        return CryptoUtil.computeSHA1Hash( total );
    }

    public static byte[] computeSHA1Hash( ByteArrayable... bas )
    {
        // TODO: This can probably be more efficiently computed
        // by using the MessageDigest's update function
        // to read the byte arrays one by one instead of
        // concatenating them all first and producing a new
        // byte array.
        byte[] total = ByteArrayUtil.concat( bas );
        return CryptoUtil.computeSHA1Hash( total );
    }

    // TPM-related operations (using TPM structs)

    public static TPM_NONCE generateRandomNonce()
    {
        return new TPM_NONCE( CryptoUtil.generateRandomByteArray( 20 ) );
    }

    public static TPM_DIGEST computeTPM_DIGEST( byte[] data )
    {
        byte[] digest = CryptoUtil.computeSHA1Hash( data );
        return new TPM_DIGEST( digest );
    }

    public static TPM_DIGEST computeTPM_DIGEST( ByteArrayable ba )
    {
        return CryptoUtil.computeTPM_DIGEST( ba.toBytes() );
    }

    public static TPM_DIGEST computeTPM_DIGEST( byte[][] arrays )
    {
        byte[] digest = CryptoUtil.computeSHA1Hash( arrays );
        return new TPM_DIGEST( digest );
    }

    public static TPM_DIGEST computeTPM_DIGEST( ByteArrayable... bas )
    {
        byte[] digest = CryptoUtil.computeSHA1Hash( bas );
        return new TPM_DIGEST( digest );
    }

    public static byte[] computeHMAC( byte[] keyBytes, byte[] text )
    {
        try
        {
            Debug.println( "Computing HMAC. key = ", keyBytes, "\ntext = ",
                text );

            SecretKey key = new SecretKeySpec( keyBytes, "HMACSHA1" );
            Mac mac = Mac.getInstance( key.getAlgorithm() );
            mac.init( key );
            byte[] digest = mac.doFinal( text );

            Debug.println( "Raw HMAC: ", digest );

            return digest;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }

    }

    public static TPM_DIGEST computeHMAC_TPM_DIGEST( TPM_NONCE secret,
        byte[] data )
    {
        try
        {
            byte[] digest = CryptoUtil.computeHMAC( secret.toBytes(), data );

            // Note that digest might not be the same size as TPM_DIGEST
            // so just get the first so many bytes
            TPM_DIGEST ret = new TPM_DIGEST();
            ret.fromBytes( digest, 0 );
            return ret;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public static TPM_DIGEST computeHMAC_TPM_DIGEST( TPM_NONCE secret,
        byte[]... arrays )
    {
        return CryptoUtil.computeHMAC_TPM_DIGEST( secret,
            ByteArrayUtil.concat( arrays ) );
    }

    public static TPM_DIGEST computeHMAC_TPM_DIGEST( TPM_NONCE secret,
        ByteArrayable... structs )
    {
        return CryptoUtil.computeHMAC_TPM_DIGEST( secret,
            ByteArrayUtil.concat( structs ) );
    }

    // BigInteger methods

    /**
     * Return a BigInteger which has the value
     * that the internal bytes of bi would have if they
     * were treated as an unsigned integer.
     * <p>
     * Note that if the original BigInteger was negative,
     * then the byte representation (returned by toByteArray)
     * of the new integer would have a zero as the first byte.
     *  
     * @param bi
     * @return bi, if it is non-negative, or a new BigInteger with the corresponding unsigned value.
     */
    public static BigInteger treatAsUnsigned( BigInteger bi )
    {
        if ( bi.signum() < 0 )
        {
            // if modulus is negative, it means that the byte array was misinterpreted.
            return createUnsignedBigInt( bi.toByteArray() );
        }
        else
        {
            return bi;
        }
    }

    /**
     * Creates a BigInteger whose value is the
     * integer value of reading the byte array as
     * an unsigned big-endian integer.
     * <p>
     * This is useful for creating BigInteger objects that 
     * Java's crypto algorithms can use from
     * byte arrays which represent the numbers as 
     * unsigned values (e.g., the TPM's keys).
     * <p>
     * Note that if the original BigInteger was negative,
     * then the byte representation (returned by toByteArray)
     * of the new integer would have a zero as the first byte.
     * 
     * @param bytes
     * @return
     */
    public static BigInteger createUnsignedBigInt( byte[] bytes )
    {
        if ( (bytes[0] & 0x80) != 0 )
        {
            byte[] singleZero =
                { 0 };
            bytes = ByteArrayUtil.concat( singleZero, bytes );
        }
        BigInteger bi = new BigInteger( bytes );
        return bi;
    }

    /**
     * Returns an unsigned big-endian representation of the BigInteger's
     * value, padding or truncating accordingly according to fixedLength.
     * This is useful when producing a byte array representation
     * of fixed-length keys.  Specifically, if the value of the BigInteger
     * can be represented in less than fixedLength bytes, then
     * its toByteArray() method will return a shorter array, and additional
     * zero padding needs to be added.  Also, if the BigInteger
     * was originally created from a fixed-length byte array
     * representation of a key (e.g, using createUnsignedBigInt()), then
     * the correct unsigned value would produce a fixedLength+1 length
     * array where the first byte would be 0 and must be truncated.
     * <p> 
     * (Note that in general, if fixedLength is less than 
     * the length of <code>bi.toByteArray()</code>, then the 
     * most significant bytes of the array are dropped regardless
     * of their value.)  
     * 
     * @param bi BigInteger with value to be encoded
     * @param fixedLength  length of the byte array to be produced in bytes
     * @return
     */
    public static byte[] getBytesFromUnsignedBigInt( BigInteger bi,
        int fixedLength )
    {
        byte[] bytes = bi.toByteArray();
        if ( bytes.length == fixedLength )
        {
            return bytes;
        }
        else if ( bytes.length < fixedLength )
        {
            byte[] padding = new byte[fixedLength - bytes.length];
            return ByteArrayUtil.concat( padding, bytes );
        }
        else
        // if ( bytes.length > fixedLength )
        {
            return ByteArrayUtil.readBytes( bytes, bytes.length - fixedLength,
                fixedLength );
        }
    }

    /**
     * Returns an unsigned big-endian representation of a BigInteger.
     * This is different from <code>bi.toByteArray</code> in that
     * it uses one byte less than <code>bi.toByteArray</code> in
     * cases where <code>bi.toByteArray</code> has a most significant byte of 0
     * This happens in cases where the value would have otherwise been representable in
     * fewer bytes as an unsigned number but had to be zero padded
     * because Java's BigIntegers are signed (e.g., consider
     * the value 128, which would be represented as 0x00, 0x80
     * by toByteArray, but can be represented as simply 0x80
     * if we are using unsigned representation).
     *  
     * @param bi
     * @return
     */
    public static byte[] getBytesFromUnsignedBigInt( BigInteger bi )
    {
        // if this BigInteger was originally converted using createUnsignedBigInt
        // or treatAsUnsigned then the first byte would be 0 and should be removed.

        byte[] bytes = bi.toByteArray();
        if ( (bytes.length > 1) && (bytes[0] == 0) )
        {
            bytes = ByteArrayUtil.readBytes( bytes, 1, bytes.length - 1 );
        }
        return bytes;
    }

    // RSA Signature methods

    public static RSAPublicKey generateRSAPublicKey( byte[] modulusBytes )
    {
        // TODO: Make version which takes TPM_PUB_KEY and uses exponent from struct
        try
        {
            BigInteger pubKeyModulus = CryptoUtil.createUnsignedBigInt( modulusBytes );
            BigInteger exponent = TPMConsts.TPMDefaultPublicExponentBigInt; // e

            KeyFactory myFac = KeyFactory.getInstance( "RSA" );
            KeySpec rsaSpec = new RSAPublicKeySpec( pubKeyModulus, exponent );
            return (RSAPublicKey) myFac.generatePublic( rsaSpec );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public static RSAPublicKey generateRSAPublicKey( TPM_STORE_PUBKEY key )
    {
        return generateRSAPublicKey( key.getKeyBytes() );
    }

    public static RSAPublicKey generateRSAPublicKey( TPM_PUBKEY key )
    {
        return generateRSAPublicKey( key.getPubKey() );
    }

    /**
     * Given the publicKey (and assuming the TPM default public exponent),
     * verify an RSA signature for the SHA1 hash of a given text.
     * 
     * @param modulusBytes -- the public key (modulus = P*Q) as an unsigned big-endian byte array
     * @param signature -- the signature
     * @param text -- the text whose SHA-1 hash was signed
     * @return
     */
    public static boolean verifySHA1RSASignature( byte[] modulusBytes,
        byte[] signature, byte[] text )
    {
        try
        {
            RSAPublicKey pubKey = CryptoUtil.generateRSAPublicKey( modulusBytes );
            Signature mySig = Signature.getInstance( "SHA1withRSA" );
            mySig.initVerify( pubKey );
            mySig.update( text );
            boolean success = mySig.verify( signature );
            return success;
        }
        catch ( Exception e )
        {
            // TODO: Improve error handling ...

            System.err.println( "Exception in verifySignature: " + e );
            return false;
        }
    }

    //    /**
    //     * Given the publicKey (and assuming the TPM default public exponent),
    //     * verify an RSA signature for the given text.
    //     * 
    //     * @param modulusBytes -- the public key (modulus = P*Q) as an unsigned big-endian byte array
    //     * @param signature -- the signature
    //     * @param text -- the text that was signed
    //     * @return
    //     */
    //    public static boolean verifyRSASignature( byte[] modulusBytes,
    //        byte[] signature, byte[] text )
    //    {
    //        // FIXME: This does not work.  There is NoSuchAlgorithm as RSA for signatures.
    //        try
    //        {
    //            RSAPublicKey pubKey = CryptoUtil.generateRSAPublicKey( modulusBytes );
    //            Signature mySig = Signature.getInstance( "RSA" );
    //            mySig.initVerify( pubKey );
    //            mySig.update( text );
    //            boolean success = mySig.verify( signature );
    //            return success;
    //        }
    //        catch ( Exception e )
    //        {
    //            // TODO: Improve error handling ...
    //
    //            System.err.println( "Exception in verifySignature: " + e );
    //            return false;
    //        }
    //    }

    /**
     * Given the privateKey produce an RSA signature for the SHA1 hash of a given text.
     * 
     * @return
     */
    public static byte[] generateSHA1RSASignature( RSAPrivateKey privKey,
        byte[] text )
    {
        try
        {
            Signature mySig = Signature.getInstance( "SHA1withRSA" );
            mySig.initSign( privKey );
            mySig.update( text );
            return mySig.sign();
        }
        catch ( Exception e )
        {
            // TODO: Improve error handling ...

            System.err.println( "Exception in generateSHA1RSASignature: " + e );
            return null;
        }
    }

    /**
     * Given the publicKey modulus (and assuming the TPM default public exponent),
     * encrypt a text.
     * 
     * @param modulusBytes -- the public key (modulus = P*Q) as an unsigned big-endian byte array
     * @param text -- the text to be encrypted
     * @return
     */
    public static byte[] encryptTPM_ES_RSAOAEP_SHA1_MGF1( byte[] modulusBytes,
        byte[] text )
    {
        try
        {
            RSAPublicKey pubKey = CryptoUtil.generateRSAPublicKey( modulusBytes );

            // Encrypt this block
            Cipher myCipher = Cipher.getInstance( "RSA/NONE/OAEPWithSHA1AndMGF1Padding" );
            myCipher.init( Cipher.ENCRYPT_MODE, pubKey, TCPA_OAEPSHA1MGF1_SPEC );
            return myCipher.doFinal( text );
        }
        catch ( Exception e )
        {
            // TODO: Improve error handling ...

            System.err.println( "Exception in encryptWithRSAModulus: " + e );
            return null;
        }
    }

    /**
     * Given P and P*Q (and assuming the TPM default public exponent),
     * decrypt a text.
     * 
     * @param text -- the encrypted text to be decrypted
     * @return
     */
    public static byte[] decryptTPM_ES_RSAOAEP_SHA1_MGF1(
        RSAPrivateKey privKey, byte[] text )
    {
        try
        {
            // Decrypt this block
            Cipher myCipher = Cipher.getInstance( "RSA/NONE/OAEPWithSHA1AndMGF1Padding" );
            myCipher.init( Cipher.DECRYPT_MODE, privKey, TCPA_OAEPSHA1MGF1_SPEC );
            return myCipher.doFinal( text );
        }
        catch ( Exception e )
        {
            // TODO: Improve error handling ...

            System.err.println( "Exception in decryptWithRSAPrivateKey: " + e );
            return null;
        }
    }

    public static KeyPair generateRSAPrivateCrtKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
            keyGen.initialize( TPMConsts.TPM_RSA_KEY_SIZE_BITS );
            KeyPair keyPair = keyGen.genKeyPair();
            return keyPair;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public static RSAPrivateCrtKey generateRSAPrivateCrtKey()
    {
        try
        {
            //            KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
            //            keyGen.initialize( TPMConsts.TPM_RSA_KEY_SIZE_BITS );
            //            KeyPair keyPair = keyGen.genKeyPair();
            KeyPair keyPair = generateRSAPrivateCrtKeyPair();
            return (RSAPrivateCrtKey) keyPair.getPrivate();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Demos and tests.
     * 
     * @param args
     */
    public static void main( String[] args )
    {
        System.out.println( "Testing XOR ..." );
        byte[] source =
            { 0, 0, 1, 1 };
        byte[] key =
            { (byte) 0xff, 0 };
        byte[] xor = CryptoUtil.xor( source, key );
        System.out.println( "XOR of 0,0,1,1 with 0xff,0: "
            + ByteArrayUtil.toPrintableHexString( xor ) );

        System.out.println( "Testing computeHMAC_TPM_DIGEST ... " );
        byte[] nullData = new byte[20];

        System.out.println( CryptoUtil.computeHMAC_TPM_DIGEST( TPM_SECRET.NULL,
            nullData ) );

        System.out.println( "Test BigInteger handling ..." );

        BigInteger bi128 = new BigInteger( "128" );
        byte[] bi128Bytes = bi128.toByteArray();
        System.out.println( "toByteArray returns: "
            + ByteArrayUtil.toPrintableHexString( bi128Bytes ) );
        byte[] uBytes = CryptoUtil.getBytesFromUnsignedBigInt( bi128 );
        System.out.println( "getBytesFromUnsignedBigInt returns: "
            + ByteArrayUtil.toPrintableHexString( uBytes ) );
        byte[] u20Bytes = CryptoUtil.getBytesFromUnsignedBigInt( bi128, 20 );
        System.out.println( "getBytesFromUnsignedBigInt(x,20) returns: "
            + ByteArrayUtil.toPrintableHexString( u20Bytes ) );
        byte[] u1Bytes = CryptoUtil.getBytesFromUnsignedBigInt( bi128, 1 );
        System.out.println( "getBytesFromUnsignedBigInt(x,1) returns: "
            + ByteArrayUtil.toPrintableHexString( u1Bytes ) );
        BigInteger bi2 = new BigInteger( uBytes );
        BigInteger bi3 = CryptoUtil.createUnsignedBigInt( uBytes );
        System.out.println( "BigInteger created from single byte: via constructor = "
            + bi2 + ", via createUnsignedBigInt()= " + bi3 );

        System.out.println( "Testing encryption ..." );
        System.out.println( "Trying to sign 'Hello World!' (without authorization) ... " );
        String helloWorld = "Hello World!";
        byte[] helloBytes = helloWorld.getBytes();
        System.out.println( "Hello World! bytes: "
            + ByteArrayUtil.toPrintableHexString( helloBytes ) );

        System.out.println( "Generating key ..." );
        RSAPrivateCrtKey privKey = CryptoUtil.generateRSAPrivateCrtKey();
        System.out.println( "privKey generated: " + privKey );
        BigInteger p = privKey.getPrimeP();
        BigInteger modulus = privKey.getModulus();
        System.out.println( "P = 0x" + p.toString( 16 ) + "\nP*Q = "
            + modulus.toString( 16 ) );
        byte[] modulusBytes = CryptoUtil.getBytesFromUnsignedBigInt( modulus );
        System.out.println( "Modulus byte array (" + modulusBytes.length
            + " bytes): " + ByteArrayUtil.toHexString( modulusBytes ) );

        byte[] encData = CryptoUtil.encryptTPM_ES_RSAOAEP_SHA1_MGF1(
            modulusBytes, helloBytes );
        System.out.println( "Encrypted Data (" + encData.length + " bytes): "
            + ByteArrayUtil.toHexString( encData ) );

        byte[] decData = CryptoUtil.decryptTPM_ES_RSAOAEP_SHA1_MGF1( privKey,
            encData );
        System.out.println( "Decrypted Data (" + decData.length + " bytes): "
            + ByteArrayUtil.toHexString( decData ) );
        System.out.println( "Equals? " + Arrays.equals( helloBytes, decData ) );

    }

}
