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

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMCounterFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_RESULT;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.TPMToolsUtil;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

public class TPMCounterTest
{
    public static void usage()
    {
        System.out.println( "Usage: TPMCounterTest <counterID> [counterAuth]\n\n"
            + "- counterID should be an integer (in decimal or hex 0x...)" );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        if ( args.length < 1 )
        {
            usage();
        }

        SwitchParams params = new SwitchParams( args, "counterID", "counterAuth" );

        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        
        try
        {
            int REPEATS = 3;


            int countID = params.getInt( "counterID" );

            TPM_SECRET counterAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "counterAuth" );

            System.out.println( "Reading counter 0x"
                + Integer.toHexString( countID ) );
            TPM_COUNTER_VALUE counterValue = TPMCounterFuncs.TPM_ReadCounter( countID );
            System.out.println( "Counter Value: " + counterValue );

            System.out.println( "Incrementing counter 0x"
                + Integer.toHexString( countID ) );
            counterValue = TPMCounterFuncs.TPM_IncrementCounter( countID,
                counterAuth );
            System.out.println( "Counter Value: " + counterValue );

            System.out.println( "Reading counter 0x"
                + Integer.toHexString( countID ) );
            counterValue = TPMCounterFuncs.TPM_ReadCounter( countID );
            System.out.println( "Counter Value: " + counterValue );

            Stopwatch wallClock = new Stopwatch();

            System.out.println( "\n\n" );

            System.out.println( "Timing TPMCounterFuncs.TPM_ReadCounter ... " );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                counterValue = TPMCounterFuncs.TPM_ReadCounter( countID );
            }
            wallClock.stop();
            TPMTiming.printSpeed( "TPMCounterFuncs.TPM_ReadCounter", REPEATS,
                wallClock );

            // Note: according to TPM specifications, the TPM only has to allow 
            // increments every 5 seconds.
            System.out.println( "Waiting 5s until Incrementing Counter again ... " );
            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println( "Timing TPMCounterFuncs.TPM_IncrementCounter ... " );
            wallClock.reset();
            wallClock.start();
            for ( int i = 0; i < REPEATS; i++ )
            {
                boolean ok = false;
                do
                {
                    try
                    {
                        Stopwatch incClock = new Stopwatch();
                        incClock.reset();
                        incClock.start();
                        counterValue = TPMCounterFuncs.TPM_IncrementCounter(
                            countID, counterAuth );
                        incClock.stop();
                        TPMTiming.printSpeed(
                            "single TPMCounterFuncs.TPM_IncrementCounter", 1,
                            incClock );

                        ok = true;
                    }
                    catch ( TPMException e )
                    {
                        if ( e.getReturnCode() == TPM_RESULT.TPM_BAD_COUNTER )
                        {
                            System.out.println( "got TPM_BAD_COUNTER, waiting 400 ms ... " );
                            try
                            {
                                Thread.sleep( 400 );
                            }
                            catch ( InterruptedException ie )
                            {
                                // TODO Auto-generated catch block
                                ie.printStackTrace();
                            }
                        }

                    }
                }
                while ( !ok );
                System.out.println( "Waiting 2s until Incrementing Counter again ... " );
                try
                {
                    Thread.sleep( 2000 );
                }
                catch ( InterruptedException ie )
                {
                    // TODO Auto-generated catch block
                    ie.printStackTrace();
                }
            }
            wallClock.stop();
            TPMTiming.printSpeed( "TPMCounterFuncs.TPM_IncrementCounter",
                REPEATS, wallClock );

            System.out.println( "Reading counter 0x"
                + Integer.toHexString( countID ) );
            counterValue = TPMCounterFuncs.TPM_ReadCounter( countID );
            System.out.println( "Counter Value: " + counterValue );

        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

        TPMToolsUtil.cleanupTPMDriver();
    }
}
