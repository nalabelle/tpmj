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

public class TPM_SYMMETRIC_KEY_PARMS extends TPM_KEY_PARMS_Data
{
    /*
     * typedef struct tdTPM_SYMMETRIC_KEY_PARMS {
     *      UINT32 keyLength;
     *      UINT32 blockSize;
     *      UINT32 ivSize;
     *      [size_is(ivSize)] BYTE IV;
     * } TPM_SYMMETRIC_KEY_PARMS;
     */

    private int keyLength;
    private int blockSize;
    private byte[] iv;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_SYMMETRIC_KEY_PARMS()
    {
        // do nothing
    }

    public TPM_SYMMETRIC_KEY_PARMS( int keyLength, int blockSize, byte[] iv )
    {
        super();
        this.keyLength = keyLength;
        this.blockSize = blockSize;
        this.setIV( iv );
    }

    public TPM_SYMMETRIC_KEY_PARMS( byte[] source )
    {
        this.fromBytes( source, 0 );
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    public void setBlockSize( int blockSize )
    {
        this.blockSize = blockSize;
    }

    public int getIVSize()
    {
        return this.iv.length;
    }

    public byte[] getIV()
    {
        return iv;
    }

    public void setIV( byte[] iv )
    {
        this.iv = iv;
    }

    public int getKeyLength()
    {
        return keyLength;
    }

    public void setKeyLength( int keyLength )
    {
        this.keyLength = keyLength;
    }

    @Override
    public byte[] toBytes()
    {
        /*
         * typedef struct tdTPM_SYMMETRIC_KEY_PARMS {
         *      UINT32 keyLength;
         *      UINT32 blockSize;
         *      UINT32 ivSize;
         *      [size_is(ivSize)] BYTE IV;
         * } TPM_SYMMETRIC_KEY_PARMS;
         */

        return ByteArrayUtil.buildBuf( this.keyLength, this.blockSize,
            this.getIVSize(), this.iv );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.keyLength = brw.readInt32();
        this.blockSize = brw.readInt32();
        int ivSize = brw.readInt32();
        this.setIV( brw.readBytes( ivSize ) );
    }

    public String toString()
    {
        return "TPM_SYMMETRIC_KEY_PARMS: keyLength=" + this.keyLength
            + ", blockSize=" + this.blockSize 
            + ", IV (" + this.getIVSize() + "bytes):\n"
            + ByteArrayUtil.toHexString( this.iv );
    }

}
