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
package edu.mit.csail.tpmj.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.BasicTPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

// FIXME: This is NOT up-to-date, and has not been tested since v0.2
public class TPMTiming
{
    public static TPMDriver tpmDriver;
    private static int REPEATS = 100;
    private static int FEW_REPEATS = 3;
    private static byte[] oneBytes =
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    private static TPM_DIGEST oneOne = new TPM_DIGEST( oneBytes );

    public static void printSpeed( String s, int count, Stopwatch watch )
    {
        long time = watch.getTime();
        double speed = 1000.0 * (double) count / (double) time;
        double opTime = (double) time / (double) count;
        System.out.println( s + ": (" + count + " ops / " + time + " ms) = "
            + opTime + " ms/op = " + speed + " ops/s." );
    }

    /**
     * @param msg
     * @param repeats
     * @param cmd
     * @throws TPMException
     */
    public static void timeCommand( String msg, int repeats, TPMCommand cmd )
        throws TPMException
    {
        Stopwatch wallClock = new Stopwatch();
//        System.out.println( "Running: " + msg + " " + repeats + " times ..." );
        wallClock.reset();
        wallClock.start();
        for ( int i = 0; i < repeats; i++ )
        {
            TPMOutputStruct output = cmd.execute( tpmDriver );
        }
        wallClock.stop();
        printSpeed( "Result for " + msg, repeats, wallClock );
    }

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tests.TPMTiming <ownerPwd> [srkPwd]" );
    }
    
    
    public static void main( String[] args )
    {
        if ( args.length < 1 )
        {
            usage();
            System.exit( -1 );
        }
        
        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "ownerPwd", "srkPwd" );

        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "ownerPwd" );
        TPM_SECRET parentAuth = TPMToolsUtil.createTPM_SECRETFromParams(
            params, "srkPwd" );

        // Initialize the TPM driver
        tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        getManufacturer();
        
        getVersion11Style();
        
        getVersion12Style();
        
        testDummyCommand();

        testPCRFuncs();

        testKeyOps( ownerAuth, parentAuth );

        // TPM DIR functions

        testDIRFuncs( ownerAuth );

        // TPM NV RAM functions

        testNVRAMFuncs( ownerAuth );
        
        TPMToolsUtil.cleanupTPMDriver();

    }

    /**
     * 
     */
    public static void getVersion12Style()
    {
        System.out.println( "\n*****" );
        System.out.println( "Getting version via TPM 1.2 way ... " );

        TPM_CAP_VERSION_INFO versionInfo12 = null;
        try
        {
            Stopwatch wallClock = new Stopwatch();
            wallClock.reset();
            wallClock.start();

            versionInfo12 = TPMGetCapabilityFuncs.getVersion12Style();

            wallClock.stop();
            printSpeed( "getVersion12Style()", 1, wallClock );

            System.out.println( "Returned: " + versionInfo12 );
            if ( versionInfo12 != null )
            {
                System.out.println( "tag: 0x"
                    + Integer.toHexString( versionInfo12.getTag() ) );
                System.out.println( "version: " + versionInfo12.getVersion() );
                System.out.println( "specLevel: 0x"
                    + Integer.toHexString( versionInfo12.getSpecLevel() ) );
                System.out.println( "errataRev: 0x"
                    + Integer.toHexString( versionInfo12.getErrataRev() ) );
                System.out.println( "tpmVendorID: 0x"
                    + " (" + ByteArrayUtil.toASCIIString( ByteArrayUtil.toBytesInt32BE( versionInfo12.getTpmVendorID() ) ) + ")" );
                System.out.println( "vendorSpecificSize: 0x"
                    + Integer.toHexString( versionInfo12.getVendorSpecificSize() ) );
                System.out.println( "vendorSpecific: "
                    + ByteArrayUtil.toPrintableHexString( versionInfo12.getVendorSpecific() ) );
                System.out.println();
            }
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
    }

    /**
     * 
     */
    public static void getManufacturer()
    {
        System.out.println( "\n*****" );
//        System.out.println( "Getting manufacturer ... " );
        try
        {
            
            Stopwatch wallClock = new Stopwatch();
            wallClock.reset();
            wallClock.start();

            int manuf = TPMGetCapabilityFuncs.getManufacturer();

            wallClock.stop();

            System.out.println( "TPM VENDOR ID = 0x" + Integer.toHexString( manuf ) 
                + " (" + ByteArrayUtil.toASCIIString( ByteArrayUtil.toBytesInt32BE( manuf ) ) + ")" );
            
            printSpeed( "TPMGetCapabilityFuncs.getManufacturer()", 1, wallClock );

        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
    }

    /**
     * 
     */
    public static void getVersion11Style()
    {
        System.out.println( "\n*****" );
        System.out.println( "Getting version via TPM 1.1 way ... " );
        TPM_STRUCT_VER structVer = null;
        try
        {
            Stopwatch wallClock = new Stopwatch();
            wallClock.reset();
            wallClock.start();

            structVer = TPMGetCapabilityFuncs.getVersion11Style();

            wallClock.stop();
            printSpeed( "getVersion11Style()", 1, wallClock );
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
        System.out.println( "Returned: " + structVer );
    }

    /**
     * @param ownerAuth
     */
    private static void testNVRAMFuncs( TPM_SECRET ownerAuth )
    {
        System.out.println( "\n*****" );
        System.out.println( "Testing NVRAM functions ... " );

        try
        {

            Stopwatch wallClock = new Stopwatch();

//            System.out.println( "Trying to read TPM_NV_INDEX_DIR with no authorization." );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMNVFuncs.TPM_NV_ReadValue( TPMNVFuncs.TPM_NV_INDEX_DIR, 0,
                    20, null );
            }
            wallClock.stop();
            printSpeed(
                "TPMNVFuncs.TPM_NV_ReadValue( TPM_NV_INDEX_DIR ) no auth",
                FEW_REPEATS, wallClock );

////            System.out.println( "Trying to read TPM_NV_INDEX_DIR with authorization." );
////            FIXME:  This doesn't seem to work.  On Intel Mac (Infineon), it returns TPM_AUTH_CONFLICT
////            on Broadcom (HP DC7600), it throws an AuthOutDataMismatchException           
//            wallClock.reset();
//            wallClock.start();
//            for ( int i = 0; i < FEW_REPEATS; i++ )
//            {
//                TPMNVFuncs.TPM_NV_ReadValue( TPMNVFuncs.TPM_NV_INDEX_DIR, 0,
//                    20, ownerAuth );
//            }
//            wallClock.stop();
//            printSpeed(
//                "TPMNVFuncs.TPM_NV_ReadValue( TPM_NV_INDEX_DIR ) with auth",
//                FEW_REPEATS, wallClock );

        }
        catch ( TPMException e )
        {
            // Uncomment this to see the exception thrown by the read with authorization
//            e.printStackTrace();
//            TPMToolsUtil.handleTPMException( e );
            
            handleTPMException( e );
        }
    }

    /**
     * @param e
     */
    private static void handleTPMException( TPMException e )
    {
        System.out.println( "TPM Exception: return Code = "
            + e.getReturnCode() + " ("
            + TPM_RESULT.getErrorName( e.getReturnCode() ) + ")" );
    }

    /**
     * @param ownerAuth
     */
    private static void testDIRFuncs( TPM_SECRET ownerAuth )
    {
        System.out.println( "\n*****" );
        System.out.println( "Testing DIR functions ... " );

        TPMCommand cmd;
        try
        {
            Stopwatch wallClock = new Stopwatch();

            //            System.out.println( "\nTesting TPM_DirRead command ... " );
            cmd = new TPM_DirRead( 0 );
            timeCommand( "TPM_DirRead(0)", REPEATS, cmd );

            //            System.out.println( "\nTesting TPMNVFuncs.TPM_DirWriteAuth ... " );
            TPM_DIGEST newContents = TPMNVFuncs.TPM_DirRead( 0 );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMNVFuncs.TPM_DirWriteAuth( 0, newContents, ownerAuth );
            }
            wallClock.stop();
            printSpeed( "TPMNVFuncs.TPM_DirWriteAuth( 0, 0..)", FEW_REPEATS,
                wallClock );
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
    }

    private static void testCreateKey( TPM_SECRET keyAuth, TPM_SECRET parentAuth )
    {
        System.out.println( "Creating a new key using the TPM (this could take a while) ... " );
        int parentHandle = TPMConsts.TPM_KH_SRK;

        int keyUsageFlags = 0;
        TPM_SECRET migAuth = keyAuth;

        short keyUsageType = TPMConsts.TPM_KEY_LEGACY;
        try
        {
            Stopwatch wallClock = new Stopwatch();
            wallClock.reset();
            wallClock.start();
            TPM_KEY key = TPMStorageFuncs.TPM_CreateWrapKey( parentHandle,
                parentAuth, keyAuth, migAuth, keyUsageType, keyUsageFlags );
            wallClock.stop();
            printSpeed( "TPMStorageFuncs.TPM_CreateWrapKey", 1, wallClock );
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }
    }

    private static byte[] testWrapKey( String fileName, TPM_SECRET keyAuth,
        TPM_SECRET parentAuth, TPM_SECRET ownerAuth )
    {
        byte[] blob = null;
        int parentHandle = TPMConsts.TPM_KH_SRK;

        int keyUsageFlags = 0;
        TPM_SECRET migAuth = keyAuth;

        short keyUsageType = TPMConsts.TPM_KEY_LEGACY;

        try
        {
            //            System.out.println( "\n*** Getting SRK public key ... " );

            Stopwatch wallClock = new Stopwatch();

            TPM_STORE_PUBKEY parentStorePubKey = null;

            try
            {
                wallClock.reset();
                wallClock.start();
                // For SRK, TPMStorageFuncs.TPM_GetPubKey calls TPM_OwnerReadInternalPub,
                // which uses owner authorization
                parentStorePubKey = TPMStorageFuncs.TPM_GetPubKey(
                    parentHandle, ownerAuth ).getPubKey();
                wallClock.stop();
                printSpeed(
                    "TPMStorageFuncs.TPM_GetPubKey( SRK ) (TPM_GetPubKey or TPM_OwnerReadInternalPub)",
                    1, wallClock );
            }
            catch ( TPMException getPubErr )
            {
                if ( parentHandle == TPMConsts.TPM_KH_SRK )
                {
                    System.out.println( "Reading public key of SRK failed.\n"
                        + "Return Code (if any): " + getPubErr.getReturnCode()
                        + " ("
                        + TPM_RESULT.getErrorName( getPubErr.getReturnCode() )
                        + ")" + "\n\n" );
                    System.out.println( "Trying to use srk.pubkey file." );
                    try
                    {
                        byte[] srkKeyBytes = FileUtil.readIntoByteArray( "srk.pubkey" );
                        parentStorePubKey = new TPM_KEY( srkKeyBytes ).getPubKey();
                    }
                    catch ( IOException ioe )
                    {
                        System.out.println( "Reading srk.pubkey also failed!" );
                        throw ioe;
                    }
                }
                else
                    throw getPubErr;
            }
            //            System.out.println( "Got parent PubKey: " + parentStorePubKey );

            byte[] parentModulus = parentStorePubKey.getKeyBytes();

            KeyPair keyPair = null;
            RSAPrivateCrtKey rsaPrivKey = null;

            wallClock.reset();
            wallClock.start();

            //            System.out.println( "*** Generating RSA Keypair in software ... " );
            Stopwatch sw2 = new Stopwatch();
            sw2.start();
            keyPair = CryptoUtil.generateRSAPrivateCrtKeyPair();
            rsaPrivKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            sw2.stop();

            //                rsaPrivKey = CryptoUtil.generateRSAPrivateCrtKey();
            // System.out.println( "Generated rsa key: " + rsaPrivKey );
            byte[] privPrimeP = CryptoUtil.getBytesFromUnsignedBigInt(
                rsaPrivKey.getPrimeP(), 128 );
            //            System.out.println( "Private Prime P (" + privPrimeP.length
            //                + ") : " + ByteArrayUtil.toPrintableHexString( privPrimeP ) );
            byte[] pubModulus = CryptoUtil.getBytesFromUnsignedBigInt(
                rsaPrivKey.getModulus(), 256 );
            //            System.out.println( "Public Modulus  (" + pubModulus.length
            //                + ") : " + ByteArrayUtil.toPrintableHexString( pubModulus ) );

            Stopwatch sw3 = new Stopwatch();
            sw3.start();
            TPM_KEY wrappedKey = TPMStorageFuncs.TSS_WrapRSAKey( parentModulus,
                privPrimeP, pubModulus, keyAuth, migAuth, keyUsageType );
            sw3.stop();

            wallClock.stop();

            printSpeed( "Generating RSA Keypair in software", 1, sw2 );
            printSpeed(
                "TPMStorageFuncs.TSS_WrapRSAKey (encrypt key in software)", 1,
                sw3 );
            printSpeed( "Creating and wrapping key in software (total time)",
                1, wallClock );

            //            System.out.println( "\n*** TPM returned Wrapped Key: " + wrappedKey );

            // Write Java-serialized form of private-public key (unencrypted)
            String serName = fileName + ".ser";
            //            System.out.println( "\n*** Writing serialized keypair to "
            //                + serName + " ..." );
            File f = new File( serName );
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream( f ) );
            oos.writeObject( keyPair );
            oos.flush();
            oos.close();

            blob = wrappedKey.toBytes();

            //            System.out.println( "\n*** Writing wrapped keyblob to " + fileName
            //                + " ..." );
            FileUtil.writeByteArray( fileName, blob );

            // At this point, blob has the wrapped key bytes, which will be returned
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }

        return blob;
    }

    /**
     * @param parentAuth
     */
    private static void testKeyOps( TPM_SECRET ownerAuth, TPM_SECRET parentAuth )
    {
        Stopwatch wallClock = new Stopwatch();

        try
        {
            System.out.println( "\n*****" );
            System.out.println( "Testing Key functions ... " );

            String fileName = "TPMTimingTest.key";
            // FIXME: Default to Infineon-style for now
            TPM_SECRET keyAuth = TPMToolsUtil.convertAuthString( "test", "keyAuth" );

            testCreateKey( keyAuth, parentAuth );

            byte[] blob = testWrapKey( fileName, keyAuth, parentAuth, ownerAuth );

            //            System.out.print( "\n*** Testing Loading the key into the TPM ... " );
            wallClock.reset();
            wallClock.start();
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
                blob, parentAuth );
            wallClock.stop();
//            System.out.println( "keyHandle = 0x"
//                + Integer.toHexString( keyHandle ) );
            printSpeed( "TPMStorageFuncs.TPM_LoadKey", 1, wallClock );

            //            System.out.println( "\n*** Trying to sign 'Hello World!' ... " );
            String helloWorld = "Hello World!";
            byte[] helloBytes = helloWorld.getBytes();
            TPM_DIGEST digest = CryptoUtil.computeTPM_DIGEST( helloBytes );

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMStorageFuncs.TPM_SignSHA1Digest( keyHandle, digest, keyAuth );
            }
            wallClock.stop();
            printSpeed( "TPMStorageFuncs.TPM_Sign(digest)", FEW_REPEATS,
                wallClock );

            //            // This didn't seem to make a difference compared to calling generateSHA1RSASignature
            //            // which has the getInstance call inside it            
            //            Signature mySig = Signature.getInstance( "SHA1withRSA" );
            //            wallClock.reset();
            //            wallClock.start();
            //            for ( int i = 0; i < REPEATS; i++ )
            //            {
            //                try
            //                {
            //                    mySig.initSign( rsaPrivKey );
            //                    mySig.update( helloBytes );
            //                    mySig.sign();
            //                }
            //                catch ( Exception e )
            //                {
            //                    // TODO: Improve error handling ...
            //
            //                    System.err.println( "Exception in generateSHA1RSASignature: " + e );
            //                }
            //            }
            //            wallClock.stop();
            //            printSpeed( "SHA1RSA Signature in software using single Signature instance", REPEATS, wallClock );

            //            System.out.println( "\nTrying signing in software ... " );
            //
            //            System.out.println( "*** Reading serialized keypair from file "
            //                + serFileName + " ..." );
            String serFileName = fileName + ".ser";
            File f = new File( serFileName );
            ObjectInputStream ois = new ObjectInputStream( new FileInputStream(
                f ) );
            KeyPair keyPair = (KeyPair) ois.readObject();
            ois.close();

            RSAPrivateCrtKey rsaPrivKey = (RSAPrivateCrtKey) keyPair.getPrivate();

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                CryptoUtil.generateSHA1RSASignature( rsaPrivKey, helloBytes );
            }
            wallClock.stop();
            printSpeed( "SHA1RSA Signature in software", REPEATS, wallClock );

            //            System.out.println( "\n*** Testing TPM_GetPubKey ... " );
            TPM_PUBKEY pubKey = null;
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle, keyAuth );
            }
            wallClock.stop();
            printSpeed( "TPMStorageFuncs.TPM_GetPubKey", REPEATS, wallClock );

            //            System.out.println( "\n*** Binding (encrypting) 'Hello World!' using public key (via software) ... " );
            wallClock.reset();
            wallClock.start();
            byte[] encData = null;
            for ( int i = 0; i < REPEATS; i++ )
            {
                encData = TPMStorageFuncs.TSS_Bind( pubKey, helloBytes );
            }
            wallClock.stop();
            printSpeed(
                "Binding (encrypting) 'Hello World!' using public key (via software) ...",
                REPEATS, wallClock );

            //            System.out.println( "\nTesting UnBinding using the TPMStorageFuncs.TPM_UnBind ... " );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMStorageFuncs.TPM_UnBind( keyHandle, encData, keyAuth );
            }
            wallClock.stop();
            printSpeed( "TPMStorageFuncs.TPM_UnBind(...)", FEW_REPEATS,
                wallClock );

            //            System.out.println( "\nUnbinding (decrypting) in software ... " );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                CryptoUtil.decryptTPM_ES_RSAOAEP_SHA1_MGF1( rsaPrivKey, encData );
            }
            wallClock.stop();
            printSpeed( "Unbinding (decrypting) in software", REPEATS, wallClock );

            ///////

            //            System.out.println( "\nTesting TPM_Quote using TPMPcrFuncs functions ... " );

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, oneOne, 15 );
            }
            wallClock.stop();
            printSpeed( "TPMPcrFuncs.TPMQuote[15]", FEW_REPEATS, wallClock );

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, oneOne, 14, 15 );
            }
            wallClock.stop();
            printSpeed( "TPMPcrFuncs.TPMQuote[14,15]", FEW_REPEATS, wallClock );

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, oneOne, 1, 2, 3, 4,
                    5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 );
            }
            wallClock.stop();
            printSpeed( "TPMPcrFuncs.TPMQuote[1..15]", FEW_REPEATS, wallClock );

            //            System.out.println( "Quoting all " + TPMPcrFuncs.getNumPcrs()
            //                + " PCRs ..." );
            TPM_PCR_SELECTION allPcrs = new TPM_PCR_SELECTION(
                TPMPcrFuncs.getNumPcrs() );
            allPcrs.setAllOn();

            //            System.out.println( "\nTesting PCR functions by calling TPM_Quote directly ..." );

            TPM_Quote quoteCmd = new TPM_Quote( keyHandle, oneOne, allPcrs );

            TPM_QuoteOutput quoteOut = null;
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < FEW_REPEATS; i++ )
            {
                quoteOut = (TPM_QuoteOutput) TPMOIAPSession.executeOIAPSession(
                    tpmDriver, quoteCmd, keyAuth );
            }
            wallClock.stop();
            printSpeed( "TPMOIAPSession + TPM_Quote[all PCRs]", FEW_REPEATS,
                wallClock );

            TPM_PCR_COMPOSITE pcrData = quoteOut.getPcrData();
            byte[] quoteBytes = pcrData.toBytes();
            TPM_QUOTE_INFO quoteInfo = new TPM_QUOTE_INFO(
                TPMPcrFuncs.getVersionForQuoteInfo(), quoteOut.getPcrData(),
                oneOne );
            byte[] sig = quoteOut.getSig();

            //            System.out.println( "\nVerifying Signature (in software) ... " );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                boolean quoteOK = TPMPcrFuncs.verifyQuote( pubKey, quoteOut,
                    oneOne );
            }
            wallClock.stop();
            printSpeed( "Verifying signature using software ...", REPEATS,
                wallClock );

            //            System.out.println( "\nProducing Signature in software ... " );
            byte[] quoteInfoBytes = quoteInfo.toBytes();
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {

                byte[] fakeSig2 = CryptoUtil.generateSHA1RSASignature(
                    rsaPrivKey, quoteInfoBytes );
            }
            wallClock.stop();
            printSpeed( "Signing Quote data using software", REPEATS, wallClock );

            //            boolean quoteOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature( pubKey, sig, quoteInfo.toBytes() );
            //            System.out.println( "Sig OK?" + quoteOK );
            //
            //            System.out.println( "Quoting single PCR #8: " );
            //            TPM_QuoteOutput quoteOut1 = TPMPcrFuncs.TPM_QuotePCRs( keyHandle, keyAuth, externalData, 8 );
            //            TPM_PCRVALUE pcrValue = quoteOut1.getPcrData().getPcrValues()[0];
            //            boolean quote1OK = TPMPcrFuncs.verifyQuoteSig( quoteOut1.getSig(), pubKey.getPubKey().getKeyBytes(), externalData, 8, pcrValue );
            //            System.out.println( "PCR: " + pcrValue );
            //            System.out.println( "Sig OK?" + quote1OK );
            //            
            //            if ( tpmDriver.getTPMVersion().minor > 1 )
            //            {
            //                System.out.println( "Quoting PCRs 16 and 23 ..." );
            //                TPM_PCR_SELECTION pcrSelection = new TPM_PCR_SELECTION( 24, 16, 23 );
            //                TPM_Quote quoteCmd2 = new TPM_Quote( keyHandle, externalData, pcrSelection );
            //                quoteCmd2.setNoAuth();
            //                TPM_QuoteOutput quoteOut2 = quoteCmd2.execute( tpmDriver );
            //                System.out.println( "Output=" + quoteOut2 );
            //    
            //                TPM_QUOTE_INFO quoteInfo2 = new TPM_QUOTE_INFO( TPMPcrFuncs.getVersionForQuoteInfo(),
            //                    quoteOut2.getPcrData(), externalData );
            //                System.out.println( "Quote Info: " + quoteInfo2 );
            //                byte[] sig2 = quoteOut2.getSig();
            //                System.out.println( "Verifying Signature ... ");
            //                boolean quoteOK2 = TPMPcrFuncs.verifyQuoteOutput( pubKey.getPubKey().getKeyBytes(), externalData, quoteOut2 );
            //    //            boolean quoteOK2 = TPMStorageFuncs.TSS_VerifySHA1RSASignature( pubKey, sig2, quoteInfo2.toBytes() );
            //                System.out.println( "Sig OK?" + quoteOK2 );
            //    
            //                System.out.println( "Demonstrating Signing 'attack' ... " );
            //                byte[] fakeSig2 = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle, quoteInfo2.toBytes(), keyAuth );
            //                System.out.println( "Produced sig: " + ByteArrayUtil.toHexString( fakeSig2 ) );
            //                System.out.println( "Equal? " + Arrays.equals( sig2, fakeSig2 ) );
            //            }

            //            System.out.println( "\n*** Evicting keyHandle ..." );
            wallClock.reset();
            wallClock.start();
            TPMStorageFuncs.TPM_EvictKey( keyHandle );
            wallClock.stop();
            printSpeed( "Evicting keyHandle", 1, wallClock );
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( ClassNotFoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private static void testPCRFuncs()
    {
        TPMCommand cmd;
        // TPM PCR handling

        Stopwatch wallClock = new Stopwatch();
        System.out.println( "\n*****" );
        System.out.println( "Testing PCR functions ... " );

        try
        {
            // TPM_PCRRead using TPMCommand directly
            //            System.out.println( "\nTesting TPM_PCRRead command ... " );
            cmd = new TPM_PCRRead( 15 );
            timeCommand( "TPM_PCRRead(15)", REPEATS, cmd );

            // Using TPMPcrFuncs convenience methods
            //            System.out.println( "\nTesting TPMPcrFuncs.TPM_PCRRead (using convenience method around direct TPM call) ..." );
            TPM_PCRVALUE pcrValue = null;
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                pcrValue = TPMPcrFuncs.TPM_PCRRead( 15 );
            }
            wallClock.stop();
            printSpeed( "TPMPcrFuncs.TPM_PCRRead(15) (convenience method)",
                REPEATS, wallClock );

            // TPM_Extend (direct command)

            //            System.out.println( "\nTesting TPM_Extend command " );
            cmd = new TPM_Extend( 15, oneOne );
            timeCommand( "TPM_Extend(15,...)", REPEATS, cmd );

            // Computing extend using a hash operation in software

            //            System.out.println( "\nTesting Extend operation in software (Java) ... " );
            byte[] pcrBytes = pcrValue.toBytes();
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                CryptoUtil.computeSHA1Hash( pcrBytes, oneBytes );
            }
            wallClock.stop();
            printSpeed( "TPM_Extend(...) in software", REPEATS, wallClock );
        }
        catch ( TPMException e )
        {
            handleTPMException( e );
        }
    }

    /**
     * 
     */
    private static void testDummyCommand()
    {
        System.out.println( "\n*****" );
        System.out.println( "Testing DummyCommand to measure Java overhead ... " );
        TPMCommand cmd = new DummyCommand();
        try
        {
            timeCommand( "DummyCommand", REPEATS * 1000, cmd );
        }
        catch ( TPMException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}

//class DummyTPMDriver extends BasicTPMDriver
//{
//    public byte[] transmitBytes( byte[] inputBytes ) throws TPMIOException
//    {
//        return inputBytes;
//    }
//}

/**
 * DummyCommand is a TPMCommand class whose execute method
 * does not actually call the TPMDriver.
 * 
 * @author lfgs
 */
class DummyCommand extends ShortTPMCommand
{
    public DummyCommand()
    {
        super( 0 );
    }

    public TPMOutputStruct execute( TPMDriver tpmDriver ) throws TPMException
    {
        TPMOutputStruct ret = null;
        try
        {
            ret = (TPMOutputStruct) this.getReturnType().newInstance();
        }
        catch ( Exception e )
        {
            throw new TPMException( e );
        }

        // Don't actually call the driver, but simulate some overhead
        // by doing some array copyings

        TPMOutputStruct rawOutput = new ByteArrayTPMOutputStruct(
            new byte[4096] );
        rawOutput.setParamSize( 4096 );
        byte[] buf = rawOutput.toBytes();
        buf = rawOutput.toBytes();

        if ( rawOutput instanceof ByteArrayTPMOutputStruct )
        {
            // NOTE: we check rawOutput for ByteArrayTPMOutputStruct here instead of
            // just ByteArrayStruct because otherwise, we
            // don't have access to getInternalByteArray below.

            if ( ret instanceof ByteArrayStruct )
            {
                // if ret type is a ByteArrayStruct anyway, use recast
                // so we don't waste time creating a copy of the bytes
                ((ByteArrayStruct) ret).recast( (ByteArrayStruct) rawOutput );
            }
            else
            {
                // if ret is not a ByteArrayStruct,
                // then use fromBytes.  Note that in this case,
                // The original bytes cannot be changed,
                // but it doesn't matter because the original bytes
                // are not exposed outside of this method anyway.
                ret.fromBytes(
                    ((ByteArrayTPMOutputStruct) rawOutput).getInternalByteArray(),
                    0 );
            }
        }
        else
        {
            ret.fromBytes( rawOutput.toBytes(), 0 );
        }

        return ret;
    }

}
