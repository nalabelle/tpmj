/*
 * Copyright (c) 2006,2007 Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006-2007
 */
package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateWrapKey;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMStorageFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_KEY;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 * This class can be used to test key generation, loading and usage.  This test will 
 * create a key of the specified type, load the key into the TPM, and then attempt to 
 * use the key to sign and encrypt some data.
 * 
 * As soon as one operation fails, the test will stop.  So, based on the key type, you 
 * should expect different behavior.
 * 
 * Signing key (s) - The signing will work, but encryption will fail.
 * Binding key (b) - The signing will fail.
 * Storage key (e) - The signing will fail.
 * Legacy key (l) - Both the signing and encryption will succeed. 
 */
public class TPMCreateKey
{

    public static void usage()
    {
        System.out.println( 
            "Usage: java edu.mit.csail.tpmj.tools.TPMCreateKey\n"
            + "           <fileName> [keyType] [keyPwd]\n"
            + "           [parentHandle] [parentPwd]\n"
            + "           [/m migPwd]\n\n"
            + "Output: a key blob (TPM_KEY format) under <fileName> containing the new wrapped key.\n"
            + "\n"
            + "- Valid keyTypes are: s (signing), b (bind), e (storage), l (legacy)\n"
            + "- parentHandle should be the handle number of the loaded parent key.\n"
            + "  Use \"SRK\" for the SRK.  Default is SRK with no authorization.\n"
            + "- If parentPwd is not given, null password with no authorization is assumed.\n"
            + "- For migratable keys, use /m followed by space, and the migration password." 
        );
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
            "keyPwd", "parentHandle", "parentPwd" );

        String fileName = params.getString( "fileName" );
        String keyTypeString = params.getString( "keyType", "l" ).toLowerCase();
        char keyType = keyTypeString.charAt( 0 );
        System.out.println( "keyType = " + keyType );

        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "keyPwd" );

        int parentHandle = TPMConsts.TPM_KH_SRK;
        String parentHandleString = params.getString( "parentHandle" );
        if ( (parentHandleString == null) || "srk".equalsIgnoreCase( parentHandleString ) )
        {
            System.out.println( "Using SRK as parent." );
            parentHandle = TPMConsts.TPM_KH_SRK;
        }
        else
        {
            parentHandle = params.getInt( "parentHandle" );
            System.out.println( "parentHandle = 0x"
                + Integer.toHexString( parentHandle ) );
        }

        TPM_SECRET parentAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "parentPwd" );

        int keyUsageFlags = 0;
        TPM_SECRET migAuth = null;

        String migPwdString = params.getString( "m" );
        if ( (migPwdString != null)
            && ("true".equalsIgnoreCase( migPwdString )) )
        {
            // This means that there was no migPwd after -m

            System.out.println( "Key will be migratable." );
            keyUsageFlags = TPMConsts.TPM_KEY_FLAGS_MASK.migratable;
            // migAuth remains null
        }
        else
        {
            migAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "m" );
            if ( migAuth != null )
            {
                System.out.println( "Key will be migratable." );
                keyUsageFlags = TPMConsts.TPM_KEY_FLAGS_MASK.migratable;
            }
        }

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
            //            System.out.println( "Creating key, keyAuth = " + keyAuth
            //                + "\nmigAuth: " + migAuth
            //                + "\nkeyInfo: " + keyInfo );
            //            TPM_CreateWrapKey createCmd = TPMStorageFuncs.TPM_CreateWrapKey(
            //                TPMConsts.TPM_KH_SRK, parentAuth, keyAuth, migAuth, keyUsageType, keyUsageFlags  );
            //            // NOTE: we cannot use setNoAuth here because that will
            //            // bypass the session, and not encode keyAuth and migAuth properly.
            //            if ( parentAuth == null )
            //            {
            //                parentAuth = TPM_SECRET.NULL;
            //            }
            //            TPM_CreateWrapKeyOutput output = 
            //                (TPM_CreateWrapKeyOutput) TPMOSAPSession.executeOSAPSession(
            //                tpmDriver, createCmd, TPMConsts.TPM_ET_SRK,
            //                TPMConsts.TPM_KH_SRK, parentAuth );

            System.out.println( "\nCreating the Key ..." );
            
            TPM_KEY key = TPMStorageFuncs.TPM_CreateWrapKey( parentHandle,
                parentAuth, keyAuth, migAuth, keyUsageType, keyUsageFlags );

            System.out.println( "Returned wrapped key: " + key );

            //            byte[] blob = output.getWrappedKeyBytes();
            //            System.out.println( "Equal to blob? " + Arrays.equals( blob, key.toBytes() ) );

            byte[] blob = key.toBytes();

            System.out.println( "\nWriting " + fileName + " ..." );
            FileUtil.writeByteArray( fileName, blob );

            System.out.println( "\nLoading the key into the TPM ..." );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( parentHandle, blob,
                parentAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );

            System.out.println( "\nReading the public key ... " );
            TPM_PUBKEY pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle,
                keyAuth );
            System.out.println( "PubKey=" + pubKey );

            System.out.println( "\nTrying to sign 'Hello World!' ... " );
            String helloWorld = "Hello World!";
            byte[] helloBytes = helloWorld.getBytes();

            byte[] sig = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle,
                helloBytes, keyAuth );
            System.out.println( "Signature returned (" + sig.length
                + " bytes): " + ByteArrayUtil.toHexString( sig ) );

            System.out.println( "Verifying signature ... " );
            // NOTE: this uses helloWorld not helloWorldHash 
            // since verifySignature already uses SHA1withRSA
            boolean signOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature(
                pubKey, sig, helloBytes );
            System.out.println( "Signature OK? " + signOK );

            System.out.println( "\nBinding 'Hello World!' ... " );
            byte[] encData = TPMStorageFuncs.TSS_Bind( pubKey, helloBytes );
            System.out.println( "Encrypted Data (" + encData.length
                + " bytes): " + ByteArrayUtil.toHexString( encData ) );

            System.out.println( "\nUnBinding 'Hello World!' ... " );
            byte[] unboundDataBytes = TPMStorageFuncs.TPM_UnBind( keyHandle,
                encData, keyAuth );
            System.out.println( "Unbound Data returned ("
                + unboundDataBytes.length + " bytes): "
                + ByteArrayUtil.toPrintableHexString( unboundDataBytes ) );
            String msg = new String( unboundDataBytes );
            System.out.println( "Payload as string: " + msg );

            System.out.println( "\nEvicting keyHandle: 0x"
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
