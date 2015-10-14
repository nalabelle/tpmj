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
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_UnsealOutput extends TPMAuth2CommandOutput
{
    public static final int SECRETSIZE_OFFSET = 10;
    public static final int SECRET_OFFSET = 14;

    public int getSecretSize()
    {
        return this.getInt32( this.SECRETSIZE_OFFSET );
    }

    /**
     * This returns the "payloadData" itself (not
     * the TPM_STORED_DATA structure.
     * 
     * @return
     */
    public byte[] getSecret()
    {
        int secretSize = this.getSecretSize();
        return this.getBytes( this.SECRET_OFFSET, secretSize );
    }

    @Override
    public String toString()
    {
        return "TPM_UnsealOutput output:\n" + " secret (" + this.getSecretSize() + " bytes) = "
        + ByteArrayUtil.toPrintableHexString( this.getSecret() );
    }
}
