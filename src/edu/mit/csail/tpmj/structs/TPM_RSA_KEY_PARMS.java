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

import java.math.BigInteger;

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;

public class TPM_RSA_KEY_PARMS extends TPM_KEY_PARMS_Data
{
    /*
     * typedef struct tdTPM_RSA_KEY_PARMS {
     *      UINT32 keyLength;
     *      UINT32 numPrimes;
     *      UINT32 exponentSize;
     *      BYTE[] exponentBytes;
     * } TPM_RSA_KEY_PARMS;
     */

    private int keyLength;
    private int numPrimes;
    private byte[] exponentBytes;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_RSA_KEY_PARMS()
    {
        // do nothing
    }
    
    public TPM_RSA_KEY_PARMS( int keyLength, int numPrimes, byte[] exponentBytes )
    {
        this.keyLength = keyLength;
        this.numPrimes = numPrimes;
        this.setExponentBytes( exponentBytes );
    }

    public TPM_RSA_KEY_PARMS( int keyLength, int numPrimes, BigInteger exponent )
    {
        this.keyLength = keyLength;
        this.numPrimes = numPrimes;
        this.setExponent( exponent );
    }

    
    public TPM_RSA_KEY_PARMS( byte[] source )
    {
        this.fromBytes( source, 0 );
    }

    public byte[] getExponentBytes()
    {
        return exponentBytes;
    }

    public void setExponentBytes( byte[] exponent )
    {
        if ( exponent == null )
        {
            this.exponentBytes = new byte[0];
        }
        else
        {
            this.exponentBytes = exponent;
        }
    }

    /**
     * Returns a new instance of BigInteger representing
     * the exponent byte array.
     * 
     * @return
     */
    public BigInteger getExponent()
    {
        return CryptoUtil.createUnsignedBigInt( this.exponentBytes ); 
    }

    public void setExponent( BigInteger exponent )
    {
        this.setExponentBytes( CryptoUtil.getBytesFromUnsignedBigInt( exponent ) );
    }

    public int getExponentSize()
    {
        return this.exponentBytes.length;
    }

    public int getKeyLength()
    {
        return keyLength;
    }

    public void setKeyLength( int keyLength )
    {
        this.keyLength = keyLength;
    }

    public int getNumPrimes()
    {
        return numPrimes;
    }

    public void setNumPrimes( int numPrimes )
    {
        this.numPrimes = numPrimes;
    }

    @Override
    public byte[] toBytes()
    {
        /*
         * typedef struct tdTPM_RSA_KEY_PARMS {
         *      UINT32 keyLength;
         *      UINT32 numPrimes;
         *      UINT32 exponentSize;
         *      BYTE[] exponentBytes;
         * } TPM_RSA_KEY_PARMS;
         */

        return ByteArrayUtil.buildBuf( this.keyLength, this.numPrimes, 
            this.getExponentSize(), this.exponentBytes );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.keyLength = brw.readInt32();
        this.numPrimes = brw.readInt32();
        int exponentSize = brw.readInt32();
        this.setExponentBytes( brw.readBytes( exponentSize ) );
    }
    
    public String toString()
    {
        return "TPM_RSA_KEY_PARMS: keyLength=" + this.keyLength
            + ", numPrimes=" + this.numPrimes
            + ", exponent (" + this.getExponentSize() + " bytes): "
            + ByteArrayUtil.toHexString( this.exponentBytes );
    }

}
