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

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_ExecuteTransportOutput;
import edu.mit.csail.tpmj.commands.TPM_IncrementCounter;
import edu.mit.csail.tpmj.commands.TPM_IncrementCounterOutput;
import edu.mit.csail.tpmj.commands.TPM_ReadCounter;
import edu.mit.csail.tpmj.commands.TPM_ReleaseTransportSignedOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMStorageFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.funcs.TPMOIAPSession;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_RESULT;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.transport.TPMTransportSession;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

public class TPMCounterTimingTest
{    
    public static void usage()
    {
        System.out.println( "Usage: TPMCounterTimingTest <counterID>\n"
            + "- counterID should be an integer" );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        TPMUtilityFuncs.initTPMDriver();

        try
        {
            TPMDriver tpmDriver = TPMUtilityFuncs.getTPMDriver();

            if ( args.length != 1 )
            {
                usage();
            }

            String countIDString = args[0];
            int countID = 0;
            
            if ( args[0].toLowerCase().startsWith( "0x" ) )
            {
                countID = Integer.parseInt( args[0].substring( 2 ), 16 );
            }
            else
            {
                countID = Integer.parseInt( countIDString );
            }

            TPM_SECRET parentAuth = TPM_SECRET.NULL;
            TPM_SECRET keyAuth = null;
            TPM_SECRET migAuth = null;
            String fileName = "testaik.key";
            String keyAuthString = "test";
            //            System.out.println( "keyAuthString: " + keyAuthString );
            if ( keyAuthString.length() > 0 )
            {
                keyAuth = TPMToolsUtil.convertAuthString( keyAuthString, "keyAuthString" );
            }

            // ReadCounter

            System.out.println( "*** Reading saved key blob from file "
                + fileName + " ..." );
            byte[] blob = FileUtil.readIntoByteArray( fileName );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
                blob, parentAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );

            // Timing tests

            TPMTransportSession transSession;
            int transHandle;
            TPM_ReleaseTransportSignedOutput signOut;

            int REPEATS = 5;
            Stopwatch wallClock = new Stopwatch();

            System.out.println( "Timing just EstablishTransport and ReleastTransportSigned ... " );

            wallClock.reset();
            wallClock.start();

            for ( int i = 0; i < REPEATS; i++ )
            {
                transSession = new TPMTransportSession( tpmDriver,
                    TPMConsts.TPM_TRANSPORT_LOG );

                transHandle = transSession.startSession(
                    TPMConsts.TPM_KH_TRANSPORT, null,
                    TPM_SECRET.NULL.toBytes(), TPM_SECRET.NULL );

                signOut = transSession.releaseTransportSigned( keyHandle,
                    TPM_SECRET.NULL, keyAuth );
            }
            wallClock.stop();
            TPMTiming.printSpeed(
                "Transport session with Establish and ReleaseTranportSigned only",
                REPEATS, wallClock );

            System.out.println( "Timing transport session with TPM_ReadCounter ... " );

            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                transSession = new TPMTransportSession( tpmDriver,
                    TPMConsts.TPM_TRANSPORT_LOG );

                transHandle = transSession.startSession(
                    TPMConsts.TPM_KH_TRANSPORT, null,
                    TPM_SECRET.NULL.toBytes(), TPM_SECRET.NULL );

                TPM_ReadCounter readCounterCmd = new TPM_ReadCounter( countID );

                TPM_ExecuteTransportOutput output = transSession.executeTransport(
                    readCounterCmd, true );

                // Note: This has been proven to work ...
                //                TPM_ReadCounterOutput readOut = (TPM_ReadCounterOutput) transSession.decryptOutput( output.getWrappedCmd() );
                //
                //                // FIXME: It's possible for the output to be an error.  Develop a general way of handling this.
                //                if ( readOut.getReturnCode() != 0 )
                //                {
                //                    System.out.println( "Wrapped command returned error code: "
                //                        + readOut.getReturnCode() + " "
                //                        + TPM_RESULT.getErrorName( readOut.getReturnCode() ) );
                //
                //                    // throw new TPMErrorReturnCodeException( incCmd, incOut, "Wrapped Command resulted in error return code." );
                //                }
                //                else
                //                {
                //                    TPM_COUNTER_VALUE counterValue = readOut.getCount();
                //                    System.out.println( "Counter Value: " + counterValue );
                //                }

                signOut = transSession.releaseTransportSigned( keyHandle,
                    TPM_SECRET.NULL, keyAuth );
            }
            wallClock.stop();
            TPMTiming.printSpeed( "Transport session for TPM_ReadCounter",
                REPEATS, wallClock );

            // FIXME: For some reason, sometimes I run out of resources here ...

            System.out.println( "Waiting 2s ... " );
            Thread.sleep( 2000 );

            System.out.println( "Timing transport session with TPM_IncrementCounter ... " );

            Stopwatch masterClock = new Stopwatch();
            masterClock.reset();
            masterClock.start();

            int OUTER_REPEATS = 100;
            int success = 0;

            for ( int j = 0; j < OUTER_REPEATS; j++ )
            {
                boolean failedInc = false;

                wallClock.reset();
                wallClock.start();
                for ( int i = 0; i < 1; i++ )
                {
                    TPM_SECRET counterAuth = TPM_SECRET.NULL;
                    TPMOIAPSession oiapSession = new TPMOIAPSession(
                        tpmDriver );
                    oiapSession.startSession();
                    oiapSession.setSharedSecret( counterAuth );

                    transSession = new TPMTransportSession( tpmDriver,
                        TPMConsts.TPM_TRANSPORT_LOG );

                    transHandle = transSession.startSession(
                        TPMConsts.TPM_KH_TRANSPORT, null,
                        TPM_SECRET.NULL.toBytes(), TPM_SECRET.NULL );

                    TPM_IncrementCounter incCmd = new TPM_IncrementCounter(
                        countID );

                    TPM_NONCE newNonceOdd = CryptoUtil.generateRandomNonce();

                    incCmd.computeAuthInData1( oiapSession, newNonceOdd, false );

                    // Old code
                    //                    TPMAuthInData authInData = oiapSession.generateAuthInData( incCmd, false );
                    //                    incCmd.setAuthInData1( authInData );

                    //                    TPM_ExecuteTransportOutput output = transSession.executeTransport(
                    //                        incCmd, true );
                    //
                    //                    TPM_IncrementCounterOutput incOut = (TPM_IncrementCounterOutput) transSession.decryptOutput( output.getWrappedCmd(), incCmd );

                    TPM_IncrementCounterOutput incOut = (TPM_IncrementCounterOutput) transSession.wrapAndExecuteCmd( incCmd );

                    // FIXME: It's possible for the output to be an error.  Develop a general way of handling this.
                    if ( incOut.getReturnCode() != 0 )
                    {
                        System.out.println( "Wrapped command returned error code: "
                            + incOut.getReturnCode()
                            + " "
                            + TPM_RESULT.getErrorName( incOut.getReturnCode() ) );
                        failedInc = true;
                        // throw new TPMErrorReturnCodeException( incCmd, incOut, "Wrapped Command resulted in error return code." );
                    }
                    else
                    {
                        TPM_COUNTER_VALUE counterValue = incOut.getCount();
                        System.out.println( "Counter Value: " + counterValue );
                        success++;
                    }

                    signOut = transSession.releaseTransportSigned( keyHandle,
                        TPM_SECRET.NULL, keyAuth );
                }

                if ( failedInc )
                {
                    // NOTE: This is needed to clear the authorization session of the increment
                    System.out.println( "Calling TPM_Reset() ... " );
                    TPMUtilityFuncs.TPM_Reset();
                }

                wallClock.stop();
                TPMTiming.printSpeed(
                    "Transport session for TPM_IncrementCounter", 1, wallClock );

                long waitTime = 2100 - wallClock.getTime();
                System.out.println( "Waiting " + waitTime
                    + " ms to make wait total 2.1s ... " );
                Thread.sleep( waitTime );

            }

            masterClock.stop();
            TPMTiming.printSpeed(
                "Transport session for TPM_IncrementCounter, including wait time",
                OUTER_REPEATS, masterClock );
            System.out.println( "Successes: " + success + " = "
                + ((double) masterClock.getTime() / success) + " ms/success." );

            // FIXME: The following has not been fully tested.  Last time I tried, I ran out of
            // auth session handles.  Need to first have a way of flushing auth sessions.
            // Then need to verify that this actually works.

            //            System.out.println( "Timing transport session with TPM_OIAP and TPM_IncrementCounter ... " );
            //            wallClock.reset();
            //            wallClock.start();
            //            for ( int i = 0; i < REPEATS; i++ )
            //            {
            //                transSession = new TPMTransportSession( tpmDriver, TPMConsts.TPM_TRANSPORT_LOG );
            //                
            //                transHandle = transSession.startSession( TPMConsts.TPM_KH_TRANSPORT, null, 
            //                    TPM_SECRET.NULL.toBytes(), TPM_SECRET.NULL );
            //
            //                counterAuth = TPM_SECRET.NULL;
            //                oiapSession = new TPMOIAPSession( tpmDriver );
            //
            //                // the following code is from TPMOIAPSession.startSession() ...
            //                TPM_OIAP oiapCmd = new TPM_OIAP();
            //                TPM_ExecuteTransportOutput excOut = transSession.executeTransport( oiapCmd, true );
            //                               
            //                TPM_OIAPOutput oiapOut = (TPM_OIAPOutput) transSession.decryptOutput( excOut.getWrappedCmd() );
            //                
            //                // Save authHandle, authLastNonceEven
            //                oiapSession.initialize( oiapOut.getAuthHandle(), oiapOut.getNonceEven() );
            //                oiapSession.setSharedSecret( counterAuth );
            //                
            //                incCmd = new TPM_IncrementCounter( countID );
            //                authInData = oiapSession.generateAuthInData( incCmd, false );
            //                incCmd.setAuthInData1( authInData );
            //                
            //                TPM_ExecuteTransportOutput output = transSession.executeTransport( incCmd, true );
            //                
            //                signOut = transSession.releaseTransportSigned( keyHandle, 
            //                    TPM_SECRET.NULL, keyAuth );
            //            }
            //            wallClock.stop();
            //            TPMTiming.printSpeed( "Transport session for TPM_IncrementCounter with TPM_OIAP inside the transport session", REPEATS, wallClock );

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
        
        TPMUtilityFuncs.cleanupTPMDriver();

    }

}
