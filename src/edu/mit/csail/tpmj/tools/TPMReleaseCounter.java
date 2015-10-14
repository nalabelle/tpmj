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

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMCounterFuncs;
import edu.mit.csail.tpmj.funcs.TPMGetCapabilityFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_KEY_HANDLE_LIST;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMReleaseCounter
{
    public static void usage()
    {
        System.out.println( "Usage: TPMReleaseCounter <counterID | \"all\"> [counterPwd] [/ownerPwd password] \n\n"
            + "- Use /ownerPwd followed by a space and the owner password\n" 
            + "  when releasing all counters, or when releasing a counter\n" 
            + "  without knowing the counter password."
            + "\n\n"
            + "- WARNING: This command will irreversibly destroy the specified counter(s) currently loaded "
            + "    in the TPM so that they can no longer be read or incremented." );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
        }

        SwitchParams params = new SwitchParams( args, "counterID", "counterPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            TPM_SECRET auth = null;
            boolean useOwner = false;
            String ownerPwd = params.getString( "ownerPwd" );
            if ( "true".equalsIgnoreCase( ownerPwd ) )
            {
                System.out.println( "Please specify the owner password after \"-ownerPwd\" " );
                System.exit( -1 );
            }
            else if ( ownerPwd != null )
            {
                useOwner = true;
                auth = TPMToolsUtil.createTPM_SECRETFromParams( params, "ownerPwd" );
            }
            else
            {
                auth = TPMToolsUtil.createTPM_SECRETFromParams( params, "counterPwd" );
            }

            // get keyHandle
            String keyHandleString = params.getString( "counterID" );
            if ( "all".equalsIgnoreCase( keyHandleString ) )
            {
                System.out.println( "Reading counter handles TPM 1.2 style" );
                TPM_KEY_HANDLE_LIST handlesList = TPMGetCapabilityFuncs.getHandles( TPMConsts.TPM_RT_COUNTER );
                System.out.println( handlesList.toString() );

                int[] handles = handlesList.getHandles();
                for ( int handle : handles )
                {
                    releaseCounter( handle, auth, useOwner );
                }
            }
            else
            {
                int keyHandle = params.getInt( "counterID" );
                releaseCounter( keyHandle, auth, useOwner );
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }

    /**
     * @param handle
     * @param auth
     * @param useOwner
     */
    public static void releaseCounter( int handle, TPM_SECRET auth,
        boolean useOwner )
    {
        try
        {
            System.out.print( "Releasing handle: 0x"
                + Integer.toHexString( handle ) + " ... " );

            if ( useOwner )
            {
                System.out.print( "using owner auth ... " );
                TPMCounterFuncs.TPM_ReleaseCounterOwner( handle, auth );
            }
            else
            {
                System.out.print( "using counter auth ... " );
                TPMCounterFuncs.TPM_ReleaseCounter( handle, auth );
            }
            System.out.println( "OK" );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

}
