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
package edu.mit.csail.tpmj.funcs;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.linux.LinuxTPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMCounterFuncs extends TPMUtilityFuncs
{
    public static TPM_CreateCounterOutput TPM_CreateCounter(
        TPM_SECRET plainAuth, byte[] label, TPM_SECRET ownerAuth )
        throws TPMException
    {
        if ( plainAuth == null )
        {
            plainAuth = TPM_SECRET.NULL;
        }
        TPM_CreateCounter cmd = new TPM_CreateCounter( plainAuth, label );

        // NOTE: for TPM_ChangeAuthOwner, IBM's tpm-3.0.3 
        // seems to use 0 for the entity value here, not TPM_KH_OWNER
        // However, using TPM_KH_OWNER seems to work fine here for both 
        // the Broadcom 1.2 and Infineon 1.1 chips
        TPM_CreateCounterOutput output = (TPM_CreateCounterOutput) TPMOSAPSession.executeOSAPSession(
            tpmDriver, cmd, TPMConsts.TPM_ET_OWNER, TPMConsts.TPM_KH_OWNER,
            ownerAuth );
        return output;
    }

    public static TPM_CreateCounterOutput TPM_CreateCounter(
        TPM_SECRET plainAuth, String labelString, TPM_SECRET ownerAuth )
        throws TPMException
    {
        if ( plainAuth == null )
        {
            plainAuth = TPM_SECRET.NULL;
        }
        TPM_CreateCounter cmd = new TPM_CreateCounter( plainAuth, labelString );

        // NOTE: for TPM_ChangeAuthOwner, IBM's tpm-3.0.3 
        // seems to use 0 for the entity value here, not TPM_KH_OWNER
        // However, using TPM_KH_OWNER seems to work fine here for both 
        // the Broadcom 1.2 and Infineon 1.1 chips
        TPM_CreateCounterOutput output = (TPM_CreateCounterOutput) TPMOSAPSession.executeOSAPSession(
            tpmDriver, cmd, TPMConsts.TPM_ET_OWNER, TPMConsts.TPM_KH_OWNER,
            ownerAuth );
        return output;
    }

    public static TPM_COUNTER_VALUE TPM_IncrementCounter( int countID,
        TPM_SECRET auth ) throws TPMException
    {
        TPM_IncrementCounter cmd = new TPM_IncrementCounter( countID );
        if ( auth == null )
        {
            auth = TPM_SECRET.NULL;
        }
        TPM_IncrementCounterOutput output = (TPM_IncrementCounterOutput) TPMOIAPSession.executeOIAPSession(
            tpmDriver, cmd, auth );
        return output.getCount();
    }

    public static TPM_COUNTER_VALUE TPM_ReadCounter( int countID )
        throws TPMException
    {
        TPM_ReadCounter cmd = new TPM_ReadCounter( countID );
        TPM_ReadCounterOutput output = cmd.execute( tpmDriver );
        return output.getCount();
    }

    public static void TPM_ReleaseCounter( int countID, TPM_SECRET auth )
        throws TPMException
    {
        TPM_ReleaseCounter cmd = new TPM_ReleaseCounter( countID );
        if ( auth == null )
        {
            auth = TPM_SECRET.NULL;
        }
        TPM_ReleaseCounterOutput output = (TPM_ReleaseCounterOutput) TPMOIAPSession.executeOIAPSession(
            tpmDriver, cmd, auth );
    }

    public static void TPM_ReleaseCounterOwner( int countID, TPM_SECRET auth )
        throws TPMException
    {
        TPM_ReleaseCounterOwner cmd = new TPM_ReleaseCounterOwner( countID );
        TPM_ReleaseCounterOutput output = (TPM_ReleaseCounterOutput) TPMOIAPSession.executeOIAPSession(
            tpmDriver, cmd, auth );
    }
}
