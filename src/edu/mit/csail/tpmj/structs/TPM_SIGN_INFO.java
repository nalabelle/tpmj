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
import edu.mit.csail.tpmj.util.CryptoUtil;

public class TPM_SIGN_INFO extends SimpleTaggedTPMStruct
{
    public static final byte[] FIXED_ADIG =
        { 'A', 'D', 'I', 'G' };
    public static final byte[] FIXED_SIGN =
        { 'S', 'I', 'G', 'N' };
    public static final byte[] FIXED_TRAN =
        { 'T', 'R', 'A', 'N' };
    public static final byte[] FIXED_TSTP =
        { 'T', 'S', 'T', 'P' };

    /*
     * typedef struct tdTPM_SIGN_INFO {
     * TPM_STRUCTURE_TAG tag;
     * BYTE fixed[4];
     * TPM_NONCE replay;
     * UINT32 dataLen;
     * [size_is (dataLen)] BYTE* data;
     * } TPM_SIGN_INFO;
     */

    private byte[] fixed;
    private TPM_NONCE replay;
    byte[] data;

    public TPM_SIGN_INFO()
    {
        super( TPMConsts.TPM_TAG_SIGNINFO );
    }

    public TPM_SIGN_INFO( byte[] fixed, TPM_NONCE replay, byte[] data )
    {
        this();
        this.fixed = fixed;
        this.replay = replay;
        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData( byte[] data )
    {
        this.data = data;
    }

    public byte[] getFixed()
    {
        return fixed;
    }

    public void setFixed( byte[] fixed )
    {
        this.fixed = fixed;
    }

    public TPM_NONCE getReplay()
    {
        return replay;
    }

    public void setReplay( TPM_NONCE replay )
    {
        this.replay = replay;
    }

    @Override
    public byte[] toBytes()
    {
        if ( data == null )
        {
            data = new byte[0];
        }

        return ByteArrayUtil.buildBuf( this.getTag(), this.fixed, this.replay,
            this.data.length, this.data );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.fixed = brw.readBytes( 4 );
        this.replay = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) );
        int dataSize = brw.readInt32();
        this.data = brw.readBytes( dataSize );
    }

    public String toString()
    {
        return "TPM_SIGN_INFO:\n" + "tag: " + this.getTag() + "\n" + "fixed: "
            + ByteArrayUtil.toPrintableHexString( this.fixed ) + "\n"
            + "replay: " + this.replay + "\n" + "data (" + this.data.length
            + " bytes): " + ByteArrayUtil.toHexString( this.data );
    }

}
