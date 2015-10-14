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
package edu.mit.csail.tpmj.funcs;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.structs.*;

/**
 * This provides a set of convenience functions for
 * the TPM Admin commands.
 * 
 * @author lfgs
 */
public class TPMAdminFuncs extends TPMUtilityFuncs
{
    public static TPM_PUBKEY TPM_ReadPubek( TPM_NONCE nonce )
        throws TPMException
    {
        TPM_ReadPubek readPubekCmd = new TPM_ReadPubek( nonce );
        TPM_ReadPubekOutput pubekOut = readPubekCmd.execute( tpmDriver );
        TPM_PUBKEY pubKey = pubekOut.getPubKey();
        return pubKey;
    }

    public static TPM_PUBKEY TPM_OwnerReadInternalPub( int keyHandle,
        TPM_SECRET ownerAuth ) throws TPMException
    {
        TPM_OwnerReadInternalPub cmd = new TPM_OwnerReadInternalPub( keyHandle );

        TPM_GetPubKeyOutput output = null;
        if ( ownerAuth == null )
        {
            throw new IllegalArgumentException(
                "TPMAdminFuncs.TPM_OwnerReadInternalPub: ownerAuth can't be null." );
        }
        else
        {
            output = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, ownerAuth );
        }
        TPM_PUBKEY pubKey = output.getPubKey();
        return pubKey;
    }

    public static void TPM_SelfTestFull() throws TPMException
    {
        TPM_SelfTestFull cmd = new TPM_SelfTestFull();
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static void TPM_ContinueSelfTest() throws TPMException
    {
        TPM_ContinueSelfTest cmd = new TPM_ContinueSelfTest();
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static byte[] TPM_GetTestResult() throws TPMException
    {
        TPM_GetTestResult cmd = new TPM_GetTestResult();
        TPM_GetTestResultOutput output = cmd.execute( tpmDriver );
        return output.getOutData();
    }

    public static void assertPhysicalPresence() throws TPMException
    {
        TSC_PhysicalPresence cmd = new TSC_PhysicalPresence(
            TPMConsts.TPM_PHYSICAL_PRESENCE_PRESENT );
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static void physicalActivate() throws TPMException
    {
        TPM_PhysicalSetDeactivated cmd = new TPM_PhysicalSetDeactivated( false );
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static void TPM_ForceClear() throws TPMException
    {
        TPM_ForceClear cmd = new TPM_ForceClear();
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static void TPM_PhysicalEnable() throws TPMException
    {
        TPM_PhysicalEnable cmd = new TPM_PhysicalEnable();
        TPMOutputStruct output = cmd.execute( tpmDriver );
    }

    public static void TPM_OwnerClear( TPM_SECRET ownerAuth )
        throws TPMException
    {
        TPM_OwnerClear cmd = new TPM_OwnerClear();
        TPM_OwnerClearOutput output = null;
        if ( ownerAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_OwnerClearOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, ownerAuth );
        }
    }

    public static void TPM_FlushSpecific( int handle, int resourceType )
        throws TPMException
    {
        TPM_FlushSpecific flushCmd = new TPM_FlushSpecific( handle,
            resourceType );
        flushCmd.execute( tpmDriver );
    }

}
