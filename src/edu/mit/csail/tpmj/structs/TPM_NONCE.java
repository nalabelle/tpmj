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

import edu.mit.csail.tpmj.util.ByteArrayUtil;

// NOTE: This is effectively an immutable class
// its subclasses are too.
/**
 * An arbitrary 160-bit value (20 bytes).
 * <p>
 * To get the value of the nonce, just call toBytes().
 * Note that toBytes() returns a clone of the
 * internal data, and there are not setter methods.
 * Thus, this is an immutable class (unless you
 * supply a byte[] via the constructor and
 * change its values via an external pointer to the
 * same byte[]).
 * <p>
 * Also note that this is the superclass for all
 * 160-bit values, e.g., TPM_DIGEST.
 * This is not specified by the TCG spec, but 
 * makes sense from an OOP point-of-view.
 * Occasionally, you may need to "convert" a
 * TPM_NONCE to a subclass of TPM_NONCE (e.g.,
 * when calling TPM_Extend with an arbitrary value).
 * In this case, call the constructor of the 
 * subclass with the toBytes() of the original nonce.
 */
public class TPM_NONCE extends SimpleTPMStruct
{
    public static final int SIZE = 20;
    public static final TPM_NONCE NULL = TPM_SECRET.NULL;

    private byte[] bytes;

    public TPM_NONCE()
    {
        this.bytes = new byte[SIZE];
    }

    /**
     * This sets the internal array to point to the same array as source.
     * <p>
     * Note: if source is null, then it constructs a TPM_NONCE with
     * all zeros.  If source.length != SIZE, then this throws
     * IllegalArgumentException.
     * 
     * @param source -- the array to use as the internal array of this instance
     */
    public TPM_NONCE( byte[] source )
    {
        if ( source == null )
        {
            this.bytes = new byte[SIZE];
        }
        else if ( source.length != SIZE )
        {
            throw new IllegalArgumentException();
        }
        else
        {
            this.bytes = source;
        }
    }

    public byte[] toBytes()
    {
        return (byte[]) this.bytes.clone();
    }

    public void fromBytes( byte[] source, int offset )
    {
        System.arraycopy( source, offset, this.bytes, 0, SIZE );
    }

    public String toString()
    {
        return ByteArrayUtil.toHexString( this.bytes );
    }
}
