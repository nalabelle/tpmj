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
package edu.mit.csail.tpmj.structs;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_TRANSPORT_LOG_OUT extends SimpleTaggedTPMStruct
{
    // NOTE: The TPM structures document says that this should be of type TPM_CURRENT_TICKS
    // However, the commands document seems to set this to the value of the currentTicks CT1
    // which is output by the ExecuteTransport command, which seems to mean
    // that this should be uint64
    private TPM_CURRENT_TICKS currentTicks;
    private TPM_DIGEST parameters;
    private int locality;

    public TPM_TRANSPORT_LOG_OUT( TPM_CURRENT_TICKS currentTicks,
        TPM_DIGEST parameters, int locality )
    {
        super( TPMConsts.TPM_TAG_TRANSPORT_LOG_OUT );
        this.currentTicks = currentTicks;
        this.parameters = parameters;
        this.locality = locality;
    }

    public TPM_CURRENT_TICKS getCurrentTicks()
    {
        return currentTicks;
    }

    public void setCurrentTicks( TPM_CURRENT_TICKS currentTicks )
    {
        this.currentTicks = currentTicks;
    }

    public int getLocality()
    {
        return locality;
    }

    public void setLocality( int locality )
    {
        this.locality = locality;
    }

    public TPM_DIGEST getParameters()
    {
        return parameters;
    }

    public void setParameters( TPM_DIGEST parameters )
    {
        this.parameters = parameters;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.currentTicks,
            this.parameters, this.locality );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.currentTicks = new TPM_CURRENT_TICKS();
        brw.readStruct( this.currentTicks );
        this.parameters = new TPM_DIGEST();
        this.setParameters( new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) ) );
        this.locality = brw.readInt32();
    }

    // This probably won't be useful anyway.
    @Override
    public String toString()
    {
        return "TPM_TRANSPORT_LOG_OUT:\n" 
            + "current Ticks: " + this.currentTicks + "\n"
            + "parameters: " + this.parameters + "\n"
            + "locality: " + this.locality + "\n";
    }

}
