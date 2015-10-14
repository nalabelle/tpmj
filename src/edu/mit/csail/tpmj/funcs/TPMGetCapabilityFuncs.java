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

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.TPM_GetCapability;
import edu.mit.csail.tpmj.commands.TPM_GetCapabilityOutput;
import edu.mit.csail.tpmj.structs.TPM_CAP_VERSION_INFO;
import edu.mit.csail.tpmj.structs.TPM_KEY_HANDLE_LIST;
import edu.mit.csail.tpmj.structs.TPM_PERMANENT_FLAGS;
import edu.mit.csail.tpmj.structs.TPM_STCLEAR_FLAGS;
import edu.mit.csail.tpmj.structs.TPM_STRUCT_VER;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPMGetCapabilityFuncs extends TPMUtilityFuncs
{
    public static TPM_STRUCT_VER getVersion11Style() throws TPMException
    {
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TCPA_CAP_VERSION );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        // if we reach here, then output should not be null
        // and there should be no errors.
        byte[] verBytes = output.getResp();
        TPM_STRUCT_VER structVer = new TPM_STRUCT_VER( verBytes );
        return structVer;
    }

    public static TPM_CAP_VERSION_INFO getVersion12Style()
        throws TPMException
    {
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_VERSION_VAL );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        // if we reach here, then output should not be null
        // and there should be no errors.
        byte[] verBytes = output.getResp();
        TPM_CAP_VERSION_INFO ret = new TPM_CAP_VERSION_INFO( verBytes );
        return ret;
    }

    public static int getManufacturer() throws TPMException
    {
        byte[] subCap = ByteArrayUtil.toBytesInt32BE( TPMConsts.TPM_CAP_PROP_MANUFACTURER );
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_PROPERTY, subCap );

        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        byte[] resp = output.getResp();
        return ByteArrayUtil.readInt32BE( resp, 0 );
    }

    public static int getNumPcrs() throws TPMException
    {
        byte[] subCap = ByteArrayUtil.toBytesInt32BE( TPMConsts.TPM_CAP_PROP_PCR );
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_PROPERTY, subCap );

        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        byte[] resp = output.getResp();
        return ByteArrayUtil.readInt32BE( resp, 0 );
    }
    
    public static TPM_KEY_HANDLE_LIST getKeyHandles() throws TPMException
    {
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_KEY_HANDLE, null );

        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        byte[] resp = output.getResp();
        TPM_KEY_HANDLE_LIST list = new TPM_KEY_HANDLE_LIST();
        list.fromBytes( resp, 0 );
        return list;
    }

    public static TPM_KEY_HANDLE_LIST getHandles( int resourceType ) throws TPMException
    {
        byte[] subCap = ByteArrayUtil.toBytesInt32BE( resourceType );
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_HANDLE, subCap );

        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        byte[] resp = output.getResp();
        TPM_KEY_HANDLE_LIST list = new TPM_KEY_HANDLE_LIST();
        list.fromBytes( resp, 0 );
        return list;
    }
    
    public static TPM_PERMANENT_FLAGS getPermanentFlags() throws TPMException
    {
        byte[] subCap = ByteArrayUtil.toBytesInt32BE( TPMConsts.TPM_CAP_FLAG_PERMANENT );
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_FLAG, subCap );
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );
        byte[] resp = output.getResp();
        TPM_PERMANENT_FLAGS permFlags = new TPM_PERMANENT_FLAGS();
        permFlags.fromBytes( resp, 0 );
        return permFlags;
        
    }
    
    public static TPM_STCLEAR_FLAGS getVolatileFlags() throws TPMException
    {
        byte[] subCap = ByteArrayUtil.toBytesInt32BE( TPMConsts.TPM_CAP_FLAG_VOLATILE );
        TPM_GetCapability cmd = new TPM_GetCapability( TPMConsts.TPM_CAP_FLAG, subCap );
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );
        byte[] resp = output.getResp();
        TPM_STCLEAR_FLAGS permFlags = new TPM_STCLEAR_FLAGS();
        permFlags.fromBytes( resp, 0 );
        return permFlags;
        
    }

}
