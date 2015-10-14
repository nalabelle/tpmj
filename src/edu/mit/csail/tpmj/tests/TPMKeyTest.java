/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2007
 */
package edu.mit.csail.tpmj.tests;

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

public class TPMKeyTest
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tests.TPMKeyTest\n"
            + "           <fileName> [keyPwd]\n"
            + "           [parentHandle] [parentPwd]\n"
            + "           [-ownerPwd ownerPwd]\n"
            + "           [-serFile fileName]\n\n"
            + "\n"
            + "- Valid keyTypes are: s (signing), b (bind), e (storage), l (legacy)\n"
            + "- Wrapped software keys MUST be migratable.\n"
            + "- parentHandle should be the handle number of a loaded parent storage key.\n"
            + "  Use \"SRK\" for the SRK.  Default is SRK with no authorization.\n"
            + "- If parentPwd is not given, null password with no authorization is assumed." );
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
        SwitchParams params = new SwitchParams( args, "fileName", "keyPwd",
            "parentHandle", "parentPwd" );

        // filename
        String fileName = params.getString( "fileName" );

        // key authorization
        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "keyPwd" );

        // parent key info
        
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

        // Owner authorization
        
        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "ownerPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {

            // get parent
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

            
            
            // Read Java-serialized form of private-public key (unencrypted)
            String serName = params.getString( "serFile" );
            boolean useSerKey = false;

            KeyPair keyPair = null;
            RSAPrivateCrtKey rsaPrivKey = null;

            if ( serName != null )
            {
                try
                {
                    System.out.println( "\n*** Reading serialized keypair from file "
                        + serName + " ..." );
                    File f = new File( serName );
                    ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream( f ) );
                    keyPair = (KeyPair) ois.readObject();
                    rsaPrivKey = (RSAPrivateCrtKey) keyPair.getPrivate();
                    ois.close();
                    useSerKey = true;
                }
                catch ( IOException e )
                {
                    System.out.println( "Error reading file " + serName + ":\n"
                        + e );
                    useSerKey = false;
                    keyPair = null;
                    rsaPrivKey = null;
                }
            }

            System.out.println( "\n*** Reading saved key blob from file "
                + fileName + " ..." );
            byte[] buf = FileUtil.readIntoByteArray( fileName );

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
