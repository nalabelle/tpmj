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

public class TPM_CURRENT_TICKS extends SimpleTaggedTPMStruct
{
    private long currentTicks;
    private short tickRate;
    private TPM_NONCE tickNonce;

    public TPM_CURRENT_TICKS()
    {
        super( TPMConsts.TPM_TAG_CURRENT_TICKS );
    }

    public TPM_CURRENT_TICKS( long currentTicks, short tickRate,
        TPM_NONCE tickNonce )
    {
        super( TPMConsts.TPM_TAG_CURRENT_TICKS );
        this.currentTicks = currentTicks;
        this.tickRate = tickRate;
        this.tickNonce = tickNonce;
    }
    
    public long getMicroseconds()
    {
        return this.currentTicks * this.tickRate;
    }
    
    public double getMilliseconds()
    {
        return this.getMicroseconds() / 1000.0;
    }
    
    public double getSeconds()
    {
        return this.getMicroseconds() / 1000000.0;
    }

    public long getCurrentTicks()
    {
        return currentTicks;
    }

    public void setCurrentTicks( long currentTicks )
    {
        this.currentTicks = currentTicks;
    }

    public TPM_NONCE getTickNonce()
    {
        return tickNonce;
    }

    public void setTickNonce( TPM_NONCE tickNonce )
    {
        this.tickNonce = tickNonce;
    }

    public short getTickRate()
    {
        return tickRate;
    }

    public void setTickRate( short tickRate )
    {
        this.tickRate = tickRate;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.currentTicks, this.tickRate,
            this.tickNonce );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.setCurrentTicks( brw.readLong() );
        this.setTickRate( brw.readShort() );
        this.setTickNonce( new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) ) );
    }

    // This probably won't be useful anyway.
    @Override
    public String toString()
    {
        return "TPM_CURRENT_TICKS:\n" 
            + "current Ticks: " + this.currentTicks 
            + ", at " + this.tickRate + "us / tick\n"
            + "tickNonce: " + this.tickNonce;
    }

}
