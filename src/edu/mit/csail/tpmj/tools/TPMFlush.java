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
import edu.mit.csail.tpmj.funcs.TPMAdminFuncs;
import edu.mit.csail.tpmj.funcs.TPMCounterFuncs;
import edu.mit.csail.tpmj.funcs.TPMGetCapabilityFuncs;
import edu.mit.csail.tpmj.funcs.TPMStorageFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_KEY_HANDLE_LIST;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMFlush
{
    public static void usage()
    {
        System.out.println( "Usage: TPMFlush <type> <handle | \"all\">\n\n" + "Types:\n"
            + "a - authorization session\n" 
            + "c - context\n" 
            + "k - key\n"
            + "t - transport session\n\n"
            + "NOTE: This only works with TPM 1.2" );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        SwitchParams params = new SwitchParams( args, "type", "handle" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            String typeString = params.getString( "type" );
            if ( (typeString == null) || (typeString.length() != 1) )
            {
                usage();
            }

            char type = typeString.toLowerCase().charAt( 0 );

            int resourceType = 0;

            switch ( type )
            {
                case 'a':
                    resourceType = TPMConsts.TPM_RT_AUTH;
                    break;
                case 'c':
                    resourceType = TPMConsts.TPM_RT_CONTEXT;
                    break;
                case 'k':
                    resourceType = TPMConsts.TPM_RT_KEY;
                    break;
                case 't':
                    resourceType = TPMConsts.TPM_RT_TRANS;
                    break;
            }

            if ( resourceType == 0 )
            {
                usage();
            }

            // get keyHandle
            String keyHandleString = params.getString( "handle" );
            if ( "all".equalsIgnoreCase( keyHandleString ) )
            {
                TPM_KEY_HANDLE_LIST handlesList = TPMGetCapabilityFuncs.getHandles( resourceType );
                System.out.println( handlesList.toString() );

                int[] handles = handlesList.getHandles();
                for ( int handle : handles )
                {
                    flushHandle( handle, resourceType );
                }
            }
            else
            {
                int handle = params.getInt( "handle" );

                flushHandle( handle, resourceType );
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
     * @param resourceType
     * @throws TPMException
     */
    private static void flushHandle( int handle, int resourceType )
        throws TPMException
    {
        System.out.print( "Flushing handle 0x" + Integer.toHexString( handle )
            + "..." );

        TPMAdminFuncs.TPM_FlushSpecific( handle, resourceType );

        System.out.println( "OK." );
    }
}
