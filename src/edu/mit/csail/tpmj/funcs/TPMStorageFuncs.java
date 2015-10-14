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
package edu.mit.csail.tpmj.funcs;

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.interfaces.RSAPrivateCrtKey;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMStorageFuncs extends TPMUtilityFuncs
{

    /**
     * Wraps payloadData in a TPM_BOUND_DATA structure and
     * encrypts it using the byte[] representation of the
     * modulus from the public key.
     * <p>
     * Note: This is NOT meant to comply with the official
     * TSS spec.  It is just called TSS_* here to point out
     * that this is implemented purely in software as opposed 
     * to TPM_* functions which are implemented using calls to the TPM.
     * 
     * @param modulus
     * @param payloadData
     * @return
     */
    public static byte[] TSS_Bind( byte[] modulus, byte[] payloadData )
    {
        TPM_BOUND_DATA boundData = new TPM_BOUND_DATA( payloadData );
        byte[] encData = CryptoUtil.encryptTPM_ES_RSAOAEP_SHA1_MGF1( modulus,
            boundData.toBytes() );
        return encData;
    }

    public static byte[] TSS_Bind( TPM_STORE_PUBKEY key, byte[] payloadData )
    {
        return TSS_Bind( key.getKeyBytes(), payloadData );
    }

    public static byte[] TSS_Bind( TPM_PUBKEY key, byte[] payloadData )
    {
        return TSS_Bind( key.getPubKey(), payloadData );
    }

    public static byte[] TPM_UnBind( int keyHandle, byte[] encData,
        TPM_SECRET keyAuth ) throws TPMException
    {
        TPM_UnBind cmd = new TPM_UnBind( keyHandle, encData );
        TPM_UnBindOutput output = null;
        if ( keyAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_UnBindOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, keyAuth );
        }
        byte[] unboundData = output.getOutData();
        return unboundData;
    }

    /**
     * Verifies a Signature signed with SHA1WithRSA.
     * Note that the original plaintext is used,
     * not the digest, since the signature verifier
     * already performs the SHA1 hash.
     * 
     * @param key
     * @param sig -- signature bytes
     * @param text -- plaintext of original message
     * @return
     */
    public static boolean TSS_VerifySHA1RSASignature( byte[] modulus,
        byte[] sig, byte[] text )
    {
        return CryptoUtil.verifySHA1RSASignature( modulus, sig, text );
    }

    /**
     * Verifies a Signature signed with SHA1WithRSA.
     * Note that the original plaintext is used,
     * not the digest, since the signature verifier
     * already performs the SHA1 hash.
     * 
     * @param key
     * @param sig -- signature bytes
     * @param text -- plaintext of original message
     * @return
     */
    public static boolean TSS_VerifySHA1RSASignature( TPM_STORE_PUBKEY key,
        byte[] sig, byte[] text )
    {
        return TSS_VerifySHA1RSASignature( key.getKeyBytes(), sig, text );
    }

    /**
     * Verifies a Signature signed with SHA1WithRSA.
     * Note that the original plaintext is used,
     * not the digest, since the signature verifier
     * already performs the SHA1 hash.
     * 
     * @param key
     * @param sig -- signature bytes
     * @param text -- plaintext of original message
     * @return
     */
    public static boolean TSS_VerifySHA1RSASignature( TPM_PUBKEY key,
        byte[] sig, byte[] text )
    {
        return TSS_VerifySHA1RSASignature( key.getPubKey(), sig, text );
    }

    /**
     * Creates a TPM Key "blob" data structure from an RSA key pair
     * (expressed as primes P and P*Q).  This version of the method
     * allows for finer control of the keyInfo structure.
     * (Note that in any case, the keyUsageFlags field of keyInfo MUST be migratable.) 
     * 
     * @param parentModulus
     * @param privKeyPrimeP
     * @param pubKeyModulus
     * @param plainDataUsageAuth
     * @param plainDataMigrationAuth
     * @param keyInfo
     * @return
     */
    public static TPM_KEY TSS_WrapRSAKey( byte[] parentModulus,
        byte[] privKeyPrimeP, byte[] pubKeyModulus,
        TPM_SECRET plainDataUsageAuth, TPM_SECRET plainDataMigrationAuth,
        TPM_KEY keyInfo )
    {
        Debug.println( "inside TSS_WrapRSAKey ..." );

        int keyUsageFlags = keyInfo.getKeyFlags();
        if ( (keyUsageFlags & TPMConsts.TPM_KEY_FLAGS_MASK.migratable) == 0 )
        {
            throw new IllegalArgumentException(
                "TSS_WrapRSAKey: keyUsageFlags MUST be migratable" );
        }

        TPM_STORE_PUBKEY storePubKey = new TPM_STORE_PUBKEY( pubKeyModulus );
        keyInfo.setPubKey( storePubKey );
        byte[] pubDataBytes = keyInfo.getPubDataBytes();
        Debug.println( "pubKey pubDataBytes: ", pubDataBytes );
        TPM_DIGEST pubDataDigest = keyInfo.getPubDataDigest();

        TPM_STORE_PRIVKEY storePrivKey = new TPM_STORE_PRIVKEY( privKeyPrimeP );

        TPM_STORE_ASYMKEY asymkey = new TPM_STORE_ASYMKEY(
            TPMConsts.TPM_PT_ASYM, plainDataUsageAuth, plainDataMigrationAuth,
            pubDataDigest, storePrivKey );
        Debug.println( "Created asymkey: ", asymkey );
        Debug.println( "asymkey bytes: ", asymkey.toBytes() );

        byte[] encAsymKey = CryptoUtil.encryptTPM_ES_RSAOAEP_SHA1_MGF1(
            parentModulus, asymkey.toBytes() );

        keyInfo.setEncDataBytes( encAsymKey );
        return keyInfo;
    }

    protected static TPM_KEY TSS_WrapRSAKey( byte[] parentModulus,
        byte[] privKeyPrimeP, byte[] pubKeyModulus,
        TPM_SECRET plainDataUsageAuth, TPM_SECRET plainDataMigrationAuth,
        short keyUsageType, int keyUsageFlags )
    {
        // NOTE: I've tested it, and it really doesn't seem to work if the keyFlags is not migratable.
        // This restriction seems to be specified by the spec (see LoadKey2, item 7d)

        if ( (keyUsageFlags & TPMConsts.TPM_KEY_FLAGS_MASK.migratable) == 0 )
        {
            throw new IllegalArgumentException(
                "TSS_WrapRSAKey: keyUsageFlags MUST be migratable" );
        }

        byte authDataUsage = (plainDataUsageAuth == null) ? (byte) 0 : (byte) 1;
        TPM_KEY keyInfo = createKeyInfo( keyUsageType, keyUsageFlags,
            authDataUsage );
        return TSS_WrapRSAKey( parentModulus, privKeyPrimeP, pubKeyModulus,
            plainDataUsageAuth, plainDataMigrationAuth, keyInfo );
    }

    /**
     * Creates a TPM Key "blob" data structure from an RSA key pair
     * (expressed as primes P and P*Q). 
     * 
     * @param parentModulus
     * @param privKeyPrimeP
     * @param pubKeyModulus
     * @param plainDataUsageAuth
     * @param plainDataMigrationAuth
     * @param keyUsageType
     * @return
     */
    public static TPM_KEY TSS_WrapRSAKey( byte[] parentModulus,
        byte[] privKeyPrimeP, byte[] pubKeyModulus,
        TPM_SECRET plainDataUsageAuth, TPM_SECRET plainDataMigrationAuth,
        short keyUsageType )
    {
        return TSS_WrapRSAKey( parentModulus, privKeyPrimeP, pubKeyModulus,
            plainDataUsageAuth, plainDataMigrationAuth, keyUsageType,
            TPMConsts.TPM_KEY_FLAGS_MASK.migratable );
    }

    public static TPM_KEY TPM_CreateWrapKey( int parentHandle,
        TPM_SECRET parentAuth, TPM_SECRET plainDataUsageAuth,
        TPM_SECRET plainDataMigrationAuth, TPM_KEY keyInfo )
        throws TPMException
    {
        TPM_CreateWrapKey createCmd = new TPM_CreateWrapKey(
            parentHandle, plainDataUsageAuth, plainDataMigrationAuth,
            keyInfo );
        // NOTE: we cannot use setNoAuth here because that will
        // bypass the session, and not encode keyAuth and migAuth properly.
        if ( parentAuth == null )
        {
            parentAuth = TPM_SECRET.NULL;
        }

        TPM_CreateWrapKeyOutput output = (TPM_CreateWrapKeyOutput) TPMOSAPSession.executeKeyOSAPSession(
            tpmDriver, createCmd, parentAuth );
        return output.getWrappedKey();
    }

    public static TPM_KEY createKeyInfo( short keyUsageType,
        int keyUsageFlags, byte authDataUsage )
    {
        TPM_KEY keyInfo = new TPM_KEY();

        keyInfo.setKeyFlags( keyUsageFlags );
        keyInfo.setAuthDataUsage( authDataUsage );

        TPM_KEY_PARMS keyParms = new TPM_KEY_PARMS();
        keyParms.setAlgorithmID( TPMConsts.TPM_ALG_RSA );
        keyInfo.setKeyUsage( keyUsageType );
        switch ( keyUsageType )
        {
            // Note: these defaults were taken from tpm-3.0.3
            case TPMConsts.TPM_KEY_SIGNING:
                keyParms.setEncScheme( TPMConsts.TPM_ES_NONE );
                keyParms.setSigScheme( TPMConsts.TPM_SS_RSASSAPKCS1v15_SHA1 );
                break;
            case TPMConsts.TPM_KEY_STORAGE:
                keyParms.setEncScheme( TPMConsts.TPM_ES_RSAESOAEP_SHA1_MGF1 );
                keyParms.setSigScheme( TPMConsts.TPM_SS_NONE );
                break;
            case TPMConsts.TPM_KEY_BIND:
                keyParms.setEncScheme( TPMConsts.TPM_ES_RSAESOAEP_SHA1_MGF1 );
                keyParms.setSigScheme( TPMConsts.TPM_SS_NONE );
                break;
            case TPMConsts.TPM_KEY_LEGACY:
                keyParms.setEncScheme( TPMConsts.TPM_ES_RSAESOAEP_SHA1_MGF1 );
                keyParms.setSigScheme( TPMConsts.TPM_SS_RSASSAPKCS1v15_SHA1 );
                break;
            default:
                throw new IllegalArgumentException(
                    "TPM_createKeyInfo: key Usage must be signing, storage, bind, or legacy only" );
        }
        TPM_RSA_KEY_PARMS rsaKeyParms = new TPM_RSA_KEY_PARMS( 2048, 2,
            new byte[0] );
        keyParms.setParmData( rsaKeyParms );
        keyInfo.setAlgorithmParms( keyParms );

        return keyInfo;
    }

    public static TPM_KEY TPM_CreateWrapKey( int parentHandle,
        TPM_SECRET parentAuth, TPM_SECRET plainDataUsageAuth,
        TPM_SECRET plainDataMigrationAuth, short keyUsageType, int keyUsageFlags )
        throws TPMException
    {
        byte authDataUsage = (plainDataUsageAuth == null) ? (byte) 0 : (byte) 1;
        TPM_KEY keyInfo = createKeyInfo( keyUsageType, keyUsageFlags,
            authDataUsage );
        return TPM_CreateWrapKey( parentHandle, parentAuth, plainDataUsageAuth,
            plainDataMigrationAuth, keyInfo );
    }

    /**
     * Loads a key.  Uses TPM_LoadKey if using a 1.1 chip (based on tpmDriver.isTPM11()), 
     * and uses TPM_LoadKey2 if using a 1.2 chip. 
     * 
     * @param parentHandle
     * @param key
     * @param parentAuth
     * @return
     * @throws TPMException
     */
    public static int TPM_LoadKey( int parentHandle, TPM_KEY key,
        TPM_SECRET parentAuth ) throws TPMException
    {
        TPM_LoadKey lkCmd = null;
        TPM_LoadKeyOutput lkOut = null;
        
        if ( tpmDriver.isTPM11() )
        {
            lkCmd = new TPM_LoadKey( parentHandle, key );
        }
        else
        {
            lkCmd = new TPM_LoadKey2( parentHandle, key );
        }

        if ( parentAuth == null )
        {
            lkCmd.setNoAuth();
            lkOut = lkCmd.execute( tpmDriver );
        }
        else
        {
            lkOut = (TPM_LoadKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, lkCmd, parentAuth );
        }
        return lkOut.getInKeyHandle();
    }

    public static int TPM_LoadKey( int parentHandle, byte[] keyBlob,
        TPM_SECRET parentAuth ) throws TPMException
    {
        TPM_KEY key = new TPM_KEY( keyBlob );
        return TPM_LoadKey( parentHandle, key, parentAuth );
    }

    public static int TPM_LoadKey( int parentHandle, String fileName, TPM_SECRET parentAuth )
        throws TPMException, IOException
    {
        byte[] keyBlob = FileUtil.readIntoByteArray( fileName );
        return TPM_LoadKey( parentHandle, keyBlob, parentAuth );
    }
    
    /**
     * Returns the public key for a key handle, using TPM_GetPubKey.
     * When not using a TPM 1.1 chip (i.e., TPM 1.2 and above),
     * and the keyHandle is either the EK or SRK, then
     * this actually uses TPM_OwnerReadInternalPub, and
     * keyAuth should be the ownerAuth.
     * 
     * @param keyHandle
     * @param keyAuth
     * @return
     * @throws TPMException
     */
    public static TPM_PUBKEY TPM_GetPubKey( int keyHandle, TPM_SECRET keyAuth )
        throws TPMException
    {
        TPM_GetPubKey cmd;

        if ( !tpmDriver.isTPM11() &&
            ( (keyHandle == TPMConsts.TPM_KH_EK) || (keyHandle == TPMConsts.TPM_KH_SRK) ) )
        {
//            System.out.println( "Creating TPM_OwnerReadInternalPub command ... " );
//            System.out.println( "keyHandle = 0x" + Integer.toHexString( keyHandle ) );
//            System.out.println( "keyAuth = " + keyAuth );
            cmd = new TPM_OwnerReadInternalPub( keyHandle );
        }
        else
        {
            cmd = new TPM_GetPubKey( keyHandle );
        }
        
        TPM_GetPubKeyOutput output = null;
        if ( keyAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, keyAuth );
        }
        TPM_PUBKEY pubKey = output.getPubKey();
        return pubKey;
    }

    public static byte[] TPM_SignSHA1Digest( int keyHandle, TPM_DIGEST digest,
        TPM_SECRET keyAuth ) throws TPMException
    {
        TPM_Sign cmd = new TPM_Sign( keyHandle, digest );
        TPM_SignOutput output = null;
        if ( keyAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_SignOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, keyAuth );
        }
        byte[] sigBytes = output.getSig();
        return sigBytes;
    }

    public static byte[] TPM_SignSHA1OfData( int keyHandle, byte[] text,
        TPM_SECRET keyAuth ) throws TPMException
    {
        TPM_DIGEST digest = CryptoUtil.computeTPM_DIGEST( text );
        TPM_Sign cmd = new TPM_Sign( keyHandle, digest );
        TPM_SignOutput output = null;
        if ( keyAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_SignOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, keyAuth );
        }
        byte[] sigBytes = output.getSig();
        return sigBytes;
    }

    public static TPM_STORED_DATA TPM_Seal( int keyHandle, 
        TPM_SECRET keyAuth, TPM_SECRET dataAuth, byte[] text, int... pcrNums ) throws TPMException
    {
        TPM_PCR_INFO pcrInfo = null;
        if ( ( pcrNums != null ) && ( pcrNums.length > 0 ) )
        {
            pcrInfo = TPMPcrFuncs.readPCRsIntoPCRInfo( pcrNums );
        }
        return TPM_Seal( keyHandle, keyAuth, dataAuth, text, pcrInfo );
    }

    public static TPM_STORED_DATA TPM_Seal( int keyHandle, 
        TPM_SECRET keyAuth, TPM_SECRET dataAuth, byte[] text, TPM_PCR_COMPOSITE pcrComposite ) throws TPMException
    {
        TPM_PCR_INFO pcrInfo = null;
        if ( pcrComposite != null )
        {
            pcrInfo = new TPM_PCR_INFO( pcrComposite );
        }
        return TPM_Seal( keyHandle, keyAuth, dataAuth, text, pcrInfo );
    }
    
    
    /**
     * @param keyHandle
     * @param keyAuth
     * @param dataAuth
     * @param text
     * @param pcrInfo
     * @return
     * @throws TPMException
     */
    private static TPM_STORED_DATA TPM_Seal( int keyHandle, TPM_SECRET keyAuth, TPM_SECRET dataAuth, byte[] text, TPM_PCR_INFO pcrInfo ) throws TPMException
    {
        TPM_Seal cmd = new TPM_Seal( keyHandle, dataAuth, pcrInfo, text );

        // NOTE: we cannot use setNoAuth here because that will
        // bypass the session, and not encode dataAuth properly
        if ( keyAuth == null )
        {
            keyAuth = TPM_SECRET.NULL;
        }
        if ( dataAuth == null )
        {
            dataAuth = TPM_SECRET.NULL;
        }

        TPM_SealOutput output = (TPM_SealOutput) TPMOSAPSession.executeKeyOSAPSession(
            tpmDriver, cmd, keyAuth );
        
        TPM_STORED_DATA outData = output.getSealedData();
        return outData;
    }

    public static byte[] TPM_Unseal( int keyHandle, TPM_SECRET keyAuth, 
        TPM_STORED_DATA inData, TPM_SECRET dataAuth ) throws TPMException
    {
        // NOTE: we cannot use setNoAuth here because it's the middle authorization session
        if ( keyAuth == null )
        {
            keyAuth = TPM_SECRET.NULL;
        }
        if ( dataAuth == null )
        {
            dataAuth = TPM_SECRET.NULL;
        }

        TPM_Unseal cmd = new TPM_Unseal( keyHandle, inData );


        TPMOIAPSession keySession = new TPMOIAPSession( tpmDriver );
        keySession.startSession();
        keySession.setSharedSecret( keyAuth );

        TPMOIAPSession dataSession = new TPMOIAPSession( tpmDriver );
        dataSession.startSession();
        dataSession.setSharedSecret( dataAuth );

        TPM_UnsealOutput output = (TPM_UnsealOutput) cmd.execute(
            keySession, false, dataSession, false );
        
        return output.getSecret();
    }
    
    
    /**
     * Evicts the key with keyHandle.  If tpmDriver.isTPM11(), uses
     * old TPM_EvictKey command, otherwise, uses TPM_FlushSpecific.
     * @param keyHandle
     * @throws TPMException
     */
    public static void TPM_EvictKey( int keyHandle ) throws TPMException
    {
        if ( tpmDriver.isTPM11() )
        {
            TPM_EvictKey evictCmd = new TPM_EvictKey( keyHandle );
            evictCmd.execute( tpmDriver );
        }
        else
        {
            TPM_FlushSpecific flushCmd = new TPM_FlushSpecific( keyHandle, TPMConsts.TPM_RT_KEY );
            flushCmd.execute( tpmDriver );
        }
    }
}
