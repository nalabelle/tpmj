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
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.tools.special.TPMSelfTest;
import edu.mit.csail.tpmj.util.*;

public class TPMTakeOwnership
{

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMTakeOwnership <ownerPwd> [srkPwd]\n"
            + "\n"
            + "If srkPassword is not given, all-zeros will be used, and authorization will not be required." );
    }

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
            return;
        }
     
        String fileName = "srk.pubkey";

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "ownerPwd", "srkPwd"  );
        // <owner password>
        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "ownerPwd" );
        // <srk password>
        TPM_SECRET srkAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "srkPwd" );

        int keyUsageFlags = 0;
        short keyUsageType = TPMConsts.TPM_KEY_STORAGE;

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {

            TPMSelfTest.doSelfTest();
            
            // create SRK key info

            // if srkAuth is all-zeros or null, then set no authorization needed
            byte authDataUsage = ((srkAuth == null) || srkAuth.equals( TPM_SECRET.NULL ))
                ? (byte) 0
                : (byte) 1;
            TPM_KEY keyInfo = TPMStorageFuncs.createKeyInfo( keyUsageType,
                keyUsageFlags, authDataUsage );

            System.out.println( "keyInfo: " + keyInfo );

            // encrypt auth data

            System.out.println( "Reading pub EK ... " );
            TPM_ReadPubek readPubekCmd = new TPM_ReadPubek( TPM_SECRET.NULL );
            TPM_ReadPubekOutput pubekOut = readPubekCmd.execute( tpmDriver );
            TPM_PUBKEY pubKey = pubekOut.getPubKey();
            TPM_DIGEST checksum = pubekOut.getChecksum();
            System.out.println( "Public Endorsement Key: " + pubKey );

            if ( ownerAuth == null )
            {
                ownerAuth = TPM_SECRET.NULL;
            }
            byte[] encOwnerAuth = CryptoUtil.encryptTPM_ES_RSAOAEP_SHA1_MGF1(
                pubKey.getPubKey().getKeyBytes(), ownerAuth.toBytes() );

            if ( srkAuth == null )
            {
                srkAuth = TPM_SECRET.NULL;
            }
            byte[] encSrkAuth = CryptoUtil.encryptTPM_ES_RSAOAEP_SHA1_MGF1(
                pubKey.getPubKey().getKeyBytes(), srkAuth.toBytes() );

            System.out.println( "encOwnerAuth = "
                + ByteArrayUtil.toPrintableHexString( encOwnerAuth ) );
            System.out.println( "encSrkAuth = "
                + ByteArrayUtil.toPrintableHexString( encSrkAuth ) );

            // Take ownership
            TPM_TakeOwnership cmd = new TPM_TakeOwnership( encOwnerAuth,
                encSrkAuth, keyInfo );

            System.out.println( "Executing take ownership command ... " );
            TPM_TakeOwnershipOutput output = (TPM_TakeOwnershipOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, ownerAuth );
            TPM_KEY srkPub = output.getSrkPub();

            System.out.println( "Returned srk pub: " + srkPub );

            byte[] blob = srkPub.toBytes();

            byte[] srkPubBytes = output.getSrkPubBytes();
            System.out.println( "Equal to blob? "
                + Arrays.equals( blob, srkPubBytes ) );

            System.out.println( "Writing " + fileName + " ..." );
            FileUtil.writeByteArray( fileName, blob );
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
