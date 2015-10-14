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

import edu.mit.csail.tpmj.structs.TPM_PCR_COMPOSITE;
import edu.mit.csail.tpmj.util.ByteArrayStruct;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_QuoteOutput extends TPMAuth1CommandOutput
{
    // HACK: This is a difficult class to implement as a ByteArrayStruct
    // because the offset of sigSize and sig are not fixed
    // and can't be easily deduced as a constant offset
    // from either the beginning or the end of the struct.
    // Thus, I override fromBytes <b>and</b> recast
    // to compute the offset of sigSize.

    public static final int PCRDATA_OFFSET = 10;

    private int sigSizeOffset = PCRDATA_OFFSET;

    public TPM_PCR_COMPOSITE getPcrData()
    {
        TPM_PCR_COMPOSITE ret = new TPM_PCR_COMPOSITE();
        this.getStruct( PCRDATA_OFFSET, ret );
        return ret;
    }

    public int getSigSizeOffset()
    {
        return this.sigSizeOffset;
    }

    public int getSigOffset()
    {
        return this.getSigSizeOffset() + 4;
    }

    public int getSigSize()
    {
        return this.getInt32( this.getSigSizeOffset() );
    }

    public byte[] getSig()
    {
        int sigSize = this.getSigSize();
        return this.getBytes( this.sigSizeOffset + 4, sigSize );
    }
    
    protected void calculateSigSizeOffset()
    {
        // FIXME: It seems quite inefficient to have to call this.getPcrData() here.
        TPM_PCR_COMPOSITE pcrData = this.getPcrData();
        int pcrDataSize = pcrData.getTotalStructSize();
        this.sigSizeOffset = PCRDATA_OFFSET + pcrDataSize;
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        super.fromBytes( source, offset );
        this.calculateSigSizeOffset();
    }

    @Override
    public void recast( ByteArrayStruct source )
    {
        super.recast(source);
        this.calculateSigSizeOffset();
    }

    public String toString()
    {
        return "TPM_QuoteOutput (data params only):\n" 
            + "PCRData = \n" + this.getPcrData() + "\n" 
            + "Signature (" + this.getSigSize()
            + " bytes): " + ByteArrayUtil.toHexString( this.getSig() );
    }

}
