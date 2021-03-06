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
import edu.mit.csail.tpmj.TPMErrorReturnCodeException;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_ReleaseTransportSigned;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMGetCapabilityFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPMAuthInData;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_KEY_HANDLE_LIST;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 * This is deprecated now.  Use TPMFlush t all instead.
 * 
 * @author lfgs
 */
@Deprecated
public class TPMReleaseTrans
{
    public static void main( String[] args )
    {
        SwitchParams params = new SwitchParams( args, "ownerAuth" );
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            //  FIXME: Default to Infineon-style for now
            TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "ownerAuth" );

            System.out.println( "Reading transport handles TPM 1.2 style" );
            TPM_KEY_HANDLE_LIST handlesList = TPMGetCapabilityFuncs.getHandles( TPMConsts.TPM_RT_TRANS );
            System.out.println( handlesList.toString() );

            int[] handleInts = handlesList.getHandles();
            for ( int handle : handleInts )
            {
                try
                {
                    System.out.print( "Releasing handle: 0x"
                        + Integer.toHexString( handle ) + " ... " );

                    System.out.print( "Calling TPM_ReleaseTransportSigned with bad auth data ... " );

                    TPM_ReleaseTransportSigned cmd = new TPM_ReleaseTransportSigned( handle, TPM_SECRET.NULL );
                    TPM_DIGEST nullDigest = new TPM_DIGEST( TPM_SECRET.NULL.toBytes() );
                    TPMAuthInData authInData = new TPMAuthInData( handle, TPM_SECRET.NULL, false, nullDigest );
                    cmd.setAuthInData1( authInData );
                    cmd.setAuthInData2( authInData );
                    cmd.execute( tpmDriver );
                    System.out.println( "OK" );
                }
                catch ( TPMException e )
                {
                    TPMToolsUtil.handleTPMException( e );
                }
            }

            System.out.println( "Reading transport handles TPM 1.2 style" );
            handlesList = TPMGetCapabilityFuncs.getHandles( TPMConsts.TPM_RT_TRANS );
            System.out.println( handlesList.toString() );
        
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }

}
