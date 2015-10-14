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

import edu.mit.csail.tpmj.structs.TPM_CURRENT_TICKS;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_ReleaseTransportSignedOutput extends TPMAuth2CommandOutput
{
    public static final int LOCALITY_OFFSET = 10;
    public static final int CURRENTTICKS_OFFSET = 14;
    public static final int SIGSIZE_OFFSET = 46;
    public static final int SIGNATURE_OFFSET = 50;

    public TPM_CURRENT_TICKS getCurrentTicks()
    {
        TPM_CURRENT_TICKS curTicks = new TPM_CURRENT_TICKS();
        this.getStruct( CURRENTTICKS_OFFSET, curTicks );
        return curTicks;
    }

    public int getLocality()
    {
        return this.getInt32( LOCALITY_OFFSET );
    }

    public int getSigSize()
    {
        return this.getInt32( SIGSIZE_OFFSET );
    }

    public byte[] getSignature()
    {
        return this.getBytes( SIGNATURE_OFFSET, this.getSigSize() );
    }

    @Override
    public String toString()
    {
        return "TPM_ReleaseTransportSigned output:\n" + " currentTicks = "
            + this.getCurrentTicks() + "\n" + "locality: " + this.getLocality()
            + "\n" + "sign size: " + this.getSigSize() + "\nsignature: "
            + ByteArrayUtil.toPrintableHexString( this.getSignature() );
    }

}