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
package edu.mit.csail.tpmj.tools;

import java.io.*;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

public class TPMWrapKey
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMWrapKey\n"
            + "           <fileName> [keyType] [keyPwd]\n"
            + "           [migPwd]\n"
            + "           [parentHandle|\"SRK\"|parentFileName] [parentPwd] [/ownerPwd ownerPwd]\n\n"
            + "Output: \n"
            + "    a key blob (TPM_KEY format) under <fileName> containing the new wrapped key.\n"
            + "\n"
            + "- Valid keyTypes are: s (signing), b (bind), e (storage), l (legacy)\n"
            + "- Wrapped software keys MUST be migratable.\n"
            + "- parentHandle should be the handle number of a loaded parent storage key.\n"
            + "  Use \"SRK\" for the SRK.  Default is SRK with no authorization.\n"
            + "- Alternatively, instead of parentHandle, put a file name here\n"
            + "  (ending with .key or .pubkey) to use a saved key or public key blob.\n"
            + "  (In this case, no parentPwd is required to just create the key, but it is"
            + "   required to load the key.)"
            + "- If parentPwd is not given, null password with no authorization is assumed.\n"
            + "- ownerPwd is only necessary when parent is the SRK." );
    }

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
            return;
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "fileName", "keyType",
            "keyPwd", "migPwd", "parentHandle", "parentPwd" );

        String fileName = params.getString( "fileName" );
        String keyTypeString = params.getString( "keyType", "l" ).toLowerCase();
        char keyType = keyTypeString.charAt( 0 );

        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "keyPwd" );

        TPM_SECRET migAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "migPwd" );

        boolean useParentFile = false;
        String parentFileName = null;
        int parentHandle = TPMConsts.TPM_KH_SRK;
        String parentHandleString = params.getString( "parentHandle" );
        if ( (parentHandleString == null)
            || "srk".equalsIgnoreCase( parentHandleString ) )
        {
            System.out.println( "Using SRK as parent." );
            parentHandle = TPMConsts.TPM_KH_SRK;
        }
        else if ( parentHandleString.toLowerCase().endsWith( ".key" )
            || parentHandleString.toLowerCase().endsWith( ".pubkey" ) )
        {
            useParentFile = true;
            parentFileName = parentHandleString;
            System.out.println( "Using parent file: " + parentHandleString );
        }
        else
        {
            parentHandle = params.getInt( "parentHandle" );
            System.out.println( "parentHandle = 0x"
                + Integer.toHexString( parentHandle ) );
        }

        TPM_SECRET parentAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "parentPwd" );

        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "ownerPwd" );

        short keyUsageType = TPMConsts.TPM_KEY_LEGACY;
        switch ( keyType )
        {
            case 's':
                keyUsageType = TPMConsts.TPM_KEY_SIGNING;
                break;
            case 'e':
                keyUsageType = TPMConsts.TPM_KEY_STORAGE;
                break;
            case 'b':
                keyUsageType = TPMConsts.TPM_KEY_BIND;
                break;
            case 'l':
                keyUsageType = TPMConsts.TPM_KEY_LEGACY;
                break;
            default:
                throw new IllegalArgumentException(
                    "TPM_createKeyInfo: key Usage must be signing (s), storage (e), bind (b), or legacy (l) only" );
        }

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            byte[] blob = null;
            KeyPair keyPair = null;
            RSAPrivateCrtKey rsaPrivKey = null;

            System.out.println( "*** Generating RSA Keypair in software ... " );
            keyPair = CryptoUtil.generateRSAPrivateCrtKeyPair();
            rsaPrivKey = (RSAPrivateCrtKey) keyPair.getPrivate();

            //                rsaPrivKey = CryptoUtil.generateRSAPrivateCrtKey();
            // System.out.println( "Generated rsa key: " + rsaPrivKey );
            byte[] privPrimeP = CryptoUtil.getBytesFromUnsignedBigInt(
                rsaPrivKey.getPrimeP(), 128 );
            System.out.println( "Private Prime P (" + privPrimeP.length
                + ") : " + ByteArrayUtil.toPrintableHexString( privPrimeP ) );
            byte[] pubModulus = CryptoUtil.getBytesFromUnsignedBigInt(
                rsaPrivKey.getModulus(), 256 );
            System.out.println( "Public Modulus  (" + pubModulus.length
                + ") : " + ByteArrayUtil.toPrintableHexString( pubModulus ) );

            System.out.println( "\n*** Getting parent public key ... " );

            TPM_STORE_PUBKEY parentStorePubKey = null;

            if ( useParentFile )
            {
                System.out.println( "Reading parent key from " + parentFileName );
                byte[] parentBlob = FileUtil.readIntoByteArray( parentFileName );

                short tag = ByteArrayUtil.readShortBE( parentBlob, 0 );

                if ( (tag == TPMConsts.TPM_TAG_KEY12) || (tag == 0x0101) )
                {
                    // data is a TPM_KEY12 or TPM_KEY structure
                    TPM_KEY parentKey = new TPM_KEY( parentBlob );
                    parentStorePubKey = parentKey.getPubKey();
                }
                else
                {
                    parentStorePubKey = new TPM_STORE_PUBKEY();
                    parentStorePubKey.fromBytes( parentBlob, 0 );
                }
            }
            else
            {

                try
                {
                    if ( parentHandle == TPMConsts.TPM_KH_SRK )
                    {
                        // For SRK, TPMStorageFuncs.TPM_GetPubKey calls TPM_OwnerReadInternalPub,
                        // which uses owner authorization
                        parentStorePubKey = TPMStorageFuncs.TPM_GetPubKey(
                            parentHandle, ownerAuth ).getPubKey();
                    }
                    else
                    {
                        parentStorePubKey = TPMStorageFuncs.TPM_GetPubKey(
                            parentHandle, parentAuth ).getPubKey();
                    }
                }
                catch ( TPMException getPubErr )
                {
                    if ( parentHandle == TPMConsts.TPM_KH_SRK )
                    {
                        System.out.println( "Reading public key of SRK failed.\n"
                            + "Return Code (if any): "
                            + getPubErr.getReturnCode()
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
            }
            
            System.out.println( "Got parent PubKey: " + parentStorePubKey );
            byte[] parentModulus = parentStorePubKey.getKeyBytes();

            TPM_KEY wrappedKey = TPMStorageFuncs.TSS_WrapRSAKey(
                parentModulus, privPrimeP, pubModulus, keyAuth, migAuth,
                keyUsageType );
            System.out.println( "\n*** TPM returned Wrapped Key: " + wrappedKey );

            // Write Java-serialized form of private-public key (unencrypted)
            String serName = fileName + ".ser";
            System.out.println( "\n*** Writing serialized keypair to "
                + serName + " ..." );
            File f = new File( serName );
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream( f ) );
            oos.writeObject( keyPair );
            oos.flush();
            oos.close();

            blob = wrappedKey.toBytes();

            System.out.println( "\n*** Writing wrapped keyblob to " + fileName
                + " ..." );
            FileUtil.writeByteArray( fileName, blob );

            System.out.println( "\n*** Reading saved key blob from file "
                + fileName + " ..." );
            byte[] buf = FileUtil.readIntoByteArray( fileName );

            //            System.out.println( "buf == blob?" + Arrays.equals( buf, blob ) );

            TPM_KEY readKey = new TPM_KEY( buf );
            System.out.println( "Key structure read from file: " + readKey );

            System.out.println( "\n*** Loading the key into the TPM ..." );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( parentHandle, buf,
                parentAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );

            System.out.println( "\n*** Reading the public key ... " );
            TPM_PUBKEY pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle,
                keyAuth );
            System.out.println( "PubKey=" + pubKey );

            System.out.println( "\n*** Trying to sign 'Hello World!' ... " );
            String helloWorld = "Hello World!";
            byte[] helloBytes = helloWorld.getBytes();

            byte[] sig = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle,
                helloBytes, keyAuth );
            System.out.println( "Signature returned (" + sig.length
                + " bytes): " + ByteArrayUtil.toHexString( sig ) );

            System.out.println( "\n*** Verifying signature by software using public key ... " );
            // NOTE: this uses helloWorld not helloWorldHash 
            // since verifySignature already uses SHA1withRSA
            boolean signOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature(
                pubKey, sig, helloBytes );
            System.out.println( "Signature OK? " + signOK );

            if ( rsaPrivKey != null )
            {
                System.out.println( "\n*** Comparing with software-generated signature using Private key ... " );
                byte[] softSig = CryptoUtil.generateSHA1RSASignature(
                    rsaPrivKey, helloBytes );
                System.out.println( "Generated signature: "
                    + ByteArrayUtil.toHexString( softSig ) );
                System.out.println( "same as TPM sig? "
                    + Arrays.equals( sig, softSig ) );
            }

            System.out.println( "\n*** Binding (encrypting) 'Hello World!' using public key ... " );
            byte[] encData = TPMStorageFuncs.TSS_Bind( pubKey, helloBytes );
            System.out.println( "Encrypted Data (" + encData.length
                + " bytes): " + ByteArrayUtil.toHexString( encData ) );

            System.out.println( "\n*** UnBinding 'Hello World!' using TPM ... " );
            byte[] unboundDataBytes = TPMStorageFuncs.TPM_UnBind( keyHandle,
                encData, keyAuth );
            System.out.println( "Unbound Data returned ("
                + unboundDataBytes.length + " bytes): "
                + ByteArrayUtil.toPrintableHexString( unboundDataBytes ) );
            String msg = new String( unboundDataBytes );
            System.out.println( "Payload as string: " + msg );

            System.out.println( "\n*** Evicting keyHandle: 0x"
                + Integer.toHexString( keyHandle ) );
            TPMStorageFuncs.TPM_EvictKey( keyHandle );
            System.out.println( "OK" );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }

        TPMToolsUtil.cleanupTPMDriver();
    }

}
