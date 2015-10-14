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
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMAuthOutData;

public abstract class TPMAuth1CommandOutput extends ByteArrayTPMOutputStruct
{
    public boolean isNoAuth()
    {
        return (this.getTag() == TPMConsts.TPM_TAG_RSP_COMMAND);
    }
    
    /**
     * Returns the offset of the (first) AuthOutData structure.
     * The offset is computed by this.getParamSize() - TPMAuthOutData.STRUCT_SIZE
     * unless the tag is TPM_TAG_RSP_COMMAND, which means that there
     * is no auth footer, in which case, it is just getParamSize().
     *
     * @return
     */
    public int getAuthOutData1Offset()
    {
        if ( this.isNoAuth() )
        {
            return this.getParamSize();
        }
        else
        {
            return this.getParamSize() - TPMAuthOutData.STRUCT_SIZE;
        }
    }
    
    public TPMAuthOutData getAuthOutData1()
    {
        if ( this.isNoAuth() )
        {
            return null;
        }
        
        TPMAuthOutData authOutData = new TPMAuthOutData();
        this.getStruct( this.getAuthOutData1Offset(), authOutData );
        return authOutData;
    }
    
    /**
     * Returns the beginning of the data to be included in the
     * HMAC calculation, to be used for authorization sessions
     * as well as transport sessions. 
     * <p>
     * By default, this returns the position right after the return code
     * (i.e., 10).
     * However, in output structures where there are handles
     * that should not be included, this method should be overridden.
     * 
     * @return
     */
    public int getOutHMACDataParamsStartOffset()
    {
        return this.BODY_OFFSET; // this.RETURNCODE_OFFSET + 4;
    }
    
    /**
     * This should return a concatenation of all the output params to
     * be included in outParamDigest, as specific by the spec
     * (marked by S in the HMAC column of the output parameters)
     * <b>except</b> the return code and the ordinal.
     * <p>
     * The default implementation is to return all the bytes
     * from this.getOutHMACDataParamsStartOffset to this.getAuthOutData1Offset().
     * <p>
     * (Note: the return code is not included here because
     * it has to be concatenated before the ordinal, which
     * is not included in the output data structure.)
     *
     * @return
     */
    public byte[] getOutParamsForAuthDigest()
    {
        int paramOffset = this.getOutHMACDataParamsStartOffset(); // this.RETURNCODE_OFFSET + 4;
        int length = this.getAuthOutData1Offset() - paramOffset;
        return this.getBytes( paramOffset, length );
    }
}
