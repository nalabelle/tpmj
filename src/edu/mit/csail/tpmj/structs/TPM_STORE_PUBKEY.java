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

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

/**
 * 10.4 TPM_STORE_PUBKEY
 * <p>
 * This structure can be used in conjunction with a corresponding TPM_KEY_PARMS to 
 * construct a public key which can be unambiguously used.
 * 
 * @author lfgs
 */
public class TPM_STORE_PUBKEY extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_STORE_PUBKEY {
     *      UINT32 keyLength;
     *      BYTE[] key;
     * } TPM_STORE_PUBKEY;
     */

    private byte[] keyBytes = new byte[0];

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_STORE_PUBKEY()
    {
        // do nothing
    }

    public TPM_STORE_PUBKEY( byte[] keyBytes )
    {
        this.setKeyBytes( keyBytes );
    }

    public int getKeyLength()
    {
        return this.keyBytes.length;
    }

    public byte[] getKeyBytes()
    {
        return keyBytes;
    }

    public void setKeyBytes( byte[] keyBytes )
    {
        this.keyBytes = keyBytes;
    }

    @Override
    public byte[] toBytes()
    {
        int keyLength = this.getKeyLength();
        return ByteArrayUtil.buildBuf( keyLength, this.keyBytes );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        int keyLength = brw.readInt32();
        this.setKeyBytes( brw.readBytes( keyLength ) );
    }

    public String toString()
    {
        return "TPM_STORE_PUBKEY (" + this.getKeyLength() + " bytes): "
            + ByteArrayUtil.toPrintableHexString( this.keyBytes );
    }
    
}
