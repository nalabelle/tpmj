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

/**
 * An implementation of TPMOutputStruct using an internal byte array.
 * This is the class returned by the TPMDriver.transmit() method and
 * can also be used as the superclass for function-specific result structures 
 * returned by different functions.
 * 
 * @author lfgs
 */
public class ByteArrayTPMOutputStruct extends ByteArrayTaggedTPMStruct
    implements TPMOutputStruct
{
    public ByteArrayTPMOutputStruct()
    {
        super();
    }
    
    /**
     * Construct a TPMOutputStruct from the bytes returned by the TPM.
     * 
     * @param wholeData whole byte array returned by TPM
     */
    public ByteArrayTPMOutputStruct( byte[] wholeData )
    {
        super( wholeData );
    }

    /**
     * Return return code of TPM function.  Non-zero values indicate an error.
     * 
     * @return
     */
    public int getReturnCode()
    {
        return this.getInt32( TPMOutputStruct.RETURNCODE_OFFSET );
    }
    
    /**
     * Return true if there is an error.
     * 
     * @return
     */
    public boolean isError()
    {
        return this.getReturnCode() != 0;
    }

    public void setParamSize( int size )
    {
        this.setInt32( TPMIOStruct.PARAMSIZE_OFFSET, size );
    }

    /**
     * Return data size returned by TPM (includes the tag and the size.
     * (Note this field is called paramSize in the TPM spec.)
     * 
     * @return
     */
    public int getParamSize()
    {
        return this.getInt32( TPMIOStruct.PARAMSIZE_OFFSET );
    }

    /**
     * Returns a clone of the internal byte array, 
     * but resized to match the size given by getParamSize().
     * (This allows us to have an internal representation
     * using a large fixed byte array (e.g., of 4096) bytes,
     * but still be able to serialize it as the appropriate
     * number of bytes.
     */
    public byte[] toBytes()
    {
        int structSize = this.getParamSize();
        byte[] internalByteArray = this.getInternalByteArray();
        
        if ( structSize >= internalByteArray.length )
        {
            // just return clone
            return super.toBytes();
        }
        else
        {
            // Logical size of structure is less 
            // than internal allocated space.
            // Return truncated array.
            
            byte[] ret = new byte[structSize];
            System.arraycopy( internalByteArray, 0, ret, 0, structSize );
            return ret;
        }
    }

    /**
     * If internal byte array is bigger than paramSize, replace
     * it with an internal byte array of just the right size.
     */
    protected void pack()
    {
        this.setInternalByteArray( this.toBytes() );
    }

    /**
     * Returns a pointer to the internal byte array.
     * This is useful for efficiently getting the byte array
     * from the result of calling TPMDriver.transmit()
     * so that it can be used to construct another TPMOutputStruct
     * of the desired subtype.
     */
    @Override
    public byte[] getInternalByteArray()
    {
        return super.getInternalByteArray();
    }
}
