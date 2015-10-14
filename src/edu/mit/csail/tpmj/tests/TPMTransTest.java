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

import java.io.*;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.counters.CountStampFuncs;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.transport.TPMTransportLog;
import edu.mit.csail.tpmj.transport.TPMTransportLogEntry;
import edu.mit.csail.tpmj.util.*;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

public class TPMTransTest
{
    public static void usage()
    {

        System.out.println( "Usage: TPMTransTest <counterID> <counterPwd> <keyFile> [keyPwd]\n"
            + "- counterID should be an integer\n"
            + "- Use \"\" for counterPwd if you want an all-zeroes password\n"
            + "Note: this assumes that the keyFile's parent is the SRK, and the SRK has null authorization." );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        //            Debug.setDebugOn( TPMDriver.class, true );
        //            Debug.setDebugOn( TPMTransportSession.class, true );
        //            Debug.setDebugOn( TPMTransportLog.class, true );
        //            Debug.setDebugOn( TPMTransportLogEntry.class, true );
        //            Debug.setDebugOn( TPM_TRANSPORT_LOG_IN.class, true );
        //            Debug.setDebugOn( TPM_TRANSPORT_LOG_OUT.class, true );
        //            Debug.setDebugOn( TPMAuth1Command.class, true );
        //            Debug.setDebugOn( TPMAuth2Command.class, true );
        //            Debug.setDebugOn( TPMAuthorizationSession.class, true );
        //            Debug.setDebugOn( TPMDriver.class, true );

        if ( args.length < 1 )
        {
            usage();
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "counterID",
            "counterPwd", "keyFile", "keyPwd" );

        TPMToolsUtil.initDriverFromParams( params );

        try
        {
            int countID = params.getInt( "counterID" );

            // <counter password>
            TPM_SECRET counterAuth = TPMToolsUtil.createTPM_SECRETFromParams(
                params, "counterPwd" );

            if ( counterAuth == null )
            {
                counterAuth = TPM_SECRET.NULL;
                System.out.println( "Using NULL (all zeroes) as counterAuth" );
            }

            TPM_SECRET parentAuth = TPM_SECRET.NULL;
            TPM_SECRET keyAuth = null;
            TPM_SECRET migAuth = null;
            String fileName = params.getString( "keyFile", "aik.key" );
            String keyAuthString = params.getString( "keyPwd", "test" );
            //            System.out.println( "keyAuthString: " + keyAuthString );
            if ( keyAuthString.length() > 0 )
            {
                keyAuth = TPMToolsUtil.convertAuthString( keyAuthString,
                    "keyAuthString" );
            }

            // ReadCounter

            System.out.println( "*** Reading saved key blob from file "
                + fileName + " ..." );
            byte[] blob = FileUtil.readIntoByteArray( fileName );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
                blob, parentAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );
            TPM_KEY key = new TPM_KEY( blob );

            // ReadCounter

            System.out.println( "\n\nReading Counter " );

            TPMTransportLog transLog = CountStampFuncs.signedReadCounter( countID, TPM_SECRET.NULL,
                keyHandle,
                keyAuth );

            System.out.println( "DONE." );

            TPM_SIGN_INFO signInfo = transLog.getLogSignInfo( null );
            System.out.println( "got transport session log sign info:\n"
                + signInfo );
            byte[] signInfoBytes = signInfo.toBytes();
            //            System.out.println( "transport session log sign info bytes: "
            //                + ByteArrayUtil.toPrintableHexString( signInfoBytes ) );
            System.out.println( "Signature:\n"
                + ByteArrayUtil.toPrintableHexString( transLog.getSignature() ) );

            byte[] modulusBytes = key.getPubKey().getKeyBytes();
//            System.out.println( "PubKey key byte array (" + modulusBytes.length
//                + " bytes): " + ByteArrayUtil.toHexString( modulusBytes ) );

            // FIXME: Right now, we are not using the keyMap yet.
            System.out.println( "Verifying signature ... " );
            boolean signOK = transLog.verify( modulusBytes, null );
            System.out.println( "Signature OK? " + signOK );

            // IncrementCounter

            System.out.println( "\n\nIncrementing Counter " );

            transLog = CountStampFuncs.signedIncrementCounter( countID, counterAuth, TPM_SECRET.NULL,
                keyHandle,
                keyAuth );

            System.out.println( "DONE." );

            signInfo = transLog.getLogSignInfo( null );
            System.out.println( "got transport session log sign info:\n"
                + signInfo );
            signInfoBytes = signInfo.toBytes();
            //            System.out.println( "transport session log sign info bytes: "
            //                + ByteArrayUtil.toPrintableHexString( signInfoBytes ) );
            System.out.println( "Signature:\n"
                + ByteArrayUtil.toPrintableHexString( transLog.getSignature() ) );

            modulusBytes = key.getPubKey().getKeyBytes();
//            System.out.println( "PubKey key byte array (" + modulusBytes.length
//                + " bytes): " + ByteArrayUtil.toHexString( modulusBytes ) );

            // FIXME: Right now, we are not using the keyMap yet.
            System.out.println( "Verifying signature ... " );
            signOK = transLog.verify( modulusBytes, null );
            System.out.println( "Signature OK? " + signOK );

            // Test toBytes and fromBytes of TransportLog and Entries

            System.out.println( "\n\nTesting that byte array conversion of transport log works ..." );

            byte[] transLogBytes = transLog.toBytes();
            TPMTransportLog transLog2 = new TPMTransportLog( transLogBytes );
            System.out.println( "Transport log length = "
                + transLogBytes.length + " bytes" );
            System.out.println( "transLog.equals( transLog2 )? "
                + transLog.equals( transLog2 ) );


            // FIXME: Right now, we are not using the keyMap yet.
            System.out.println( "Verifying signature ... " );
            signOK = transLog2.verify( modulusBytes, null );
            System.out.println( "Signature OK? " + signOK );


            System.out.println( "*** Evicting keyHandle ..." );
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
