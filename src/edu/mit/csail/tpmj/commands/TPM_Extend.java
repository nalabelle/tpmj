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
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_Extend extends TPMCommand
{
    public static final int PCRNUM_OFFSET = 10;
    public static final int INDIGEST_OFFSET = 14;

    private int pcrNum;
    private TPM_NONCE inDigest;

    /**
     * Extends the selected PCR (i.e., PCRnew = SHA1( PCRold concatenated with inDigest).
     * <p>
     * Note that inDigest is typed as TPM_NONCE even though
     * it is typed as TPM_DIGEST in the spec.  This is to allow
     * PCRs to be extended with any 160-bit value.
     * 
     * @param pcrNum
     * @param inDigest
     */
    public TPM_Extend( int pcrNum, TPM_NONCE inDigest )
    {
        super( TPMConsts.TPM_TAG_RQU_COMMAND, 34, TPMConsts.TPM_ORD_Extend );
        this.setPcrNum( pcrNum );
        this.setInDigest( inDigest );
    }

    // Return Output Struct

    public TPM_NONCE getInDigest()
    {
        return inDigest;
    }

    public void setInDigest( TPM_NONCE inDigest )
    {
        this.inDigest = inDigest;
    }

    public int getPcrNum()
    {
        return pcrNum;
    }

    public void setPcrNum( int pcrNum )
    {
        this.pcrNum = pcrNum;
    }

    public Class getReturnType()
    {
        return TPM_PCRReadOutput.class;
    }

    @Override
    public TPM_PCRReadOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_PCRReadOutput) super.execute( tpmDriver );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.pcrNum, this.inDigest );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.setPcrNum( brw.readInt32() );
        // Since we have a choice here, we can choose to instantiate inDigest as a TPM_DIGEST to be consistent with the spec
        this.inDigest = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ));
    }
}
