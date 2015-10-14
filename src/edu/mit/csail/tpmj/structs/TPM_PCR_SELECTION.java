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
 * @author lfgs
 */
public class TPM_PCR_SELECTION extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_PCR_SELECTION {
     *      UINT16 sizeOfSelect;
     *      [size_is(sizeOfSelect)] BYTE pcrSelect[];
     * } TPM_PCR_SELECTION;
     */

    private byte[] pcrSelect;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_PCR_SELECTION()
    {
        // do nothing
    }

//    Do NOT make this available because it can be confused for a constructor that calls fromBytes
//    public TPM_PCR_SELECTION( byte[] pcrSelect )
//    {
//        this.setPcrSelect( pcrSelect );
//    }
    
    /**
     * Creates an empty TPM_PCR_SELECTION with enough bytes for numPCRs PCRs.
     * 
     * @param numPCRs -- number of PCRs (16 for TPM 1.1, 24 for TPM 1.2)
     */
    public TPM_PCR_SELECTION( int numPCRs )
    {
        this.setPcrSelect(  new byte[numPCRs / 8] );
    }
    
    public TPM_PCR_SELECTION( int numPCRs, int... onPCRs )
    {
        this( numPCRs );
        this.setPCRsOn( onPCRs );
    }

    public int getNumPCRs()
    {
        return this.getSizeOfSelect() * 8;
    }
    
    public void setAllOff()
    {
        for ( int i = 0; i < this.pcrSelect.length; i++ )
        {
            this.pcrSelect[i] = 0;
        }
    }

    public void setAllOn()
    {
        for ( int i = 0; i < this.pcrSelect.length; i++ )
        {
            this.pcrSelect[i] = (byte) 0xff;
        }
    }
    
    public void setPCROn( int pcrNum )
    {
        int byteNum = pcrNum / 8;
        int bitNum = pcrNum % 8;
        byte mask = (byte) (1 << bitNum);
        this.pcrSelect[byteNum] |= mask;
    }

    public void setPCROff( int pcrNum )
    {
        int byteNum = pcrNum / 8;
        int bitNum = pcrNum % 8;
        byte mask = (byte) (1 << bitNum);
        this.pcrSelect[byteNum] &= ~mask;
    }
    
    public boolean isPCROn( int pcrNum )
    {
        int byteNum = pcrNum / 8;
        int bitNum = pcrNum % 8;
        byte mask = (byte) (1 << bitNum);
        byte bitOn = (byte) (this.pcrSelect[byteNum] & mask);
        return ( bitOn != 0 );
    }
    
    public void setPCRsOn( int... pcrNums )
    {
        for ( int i : pcrNums )
        {
            this.setPCROn( i );
        }
    }
    
    public void setPCRsOff( int... pcrNums )
    {
        for ( int i : pcrNums )
        {
            this.setPCROff( i );
        }
    }
    
    
    /**
     * Returns number of bytes of pcrSelect array.
     * 
     * @return
     */
    public short getSizeOfSelect()
    {
        return (short) this.pcrSelect.length;
    }

    public byte[] getPcrSelect()
    {
        return this.pcrSelect;
    }

    public void setPcrSelect( byte[] pcrSelect )
    {
        this.pcrSelect = pcrSelect;
    }
    
    public int getTotalStructSize()
    {
        // TODO: Think about making getTotalStructSize an idiom
        return 2 + this.pcrSelect.length;
    }

    @Override
    public byte[] toBytes()
    {
        short sizeOfSelect = this.getSizeOfSelect();
        return ByteArrayUtil.buildBuf( sizeOfSelect, this.pcrSelect );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        int numBytes = brw.readUInt16();
        this.pcrSelect = brw.readBytes( numBytes );
    }

    public String toString()
    {
        int numEntries = this.getNumPCRs();
        String list = "";
        for ( int i = 0; i < numEntries; i++ )
        {
            if ( this.isPCROn( i ) )
            {
                list = list + i + " ";
            }
        }
        String ret = "TPM_PCR_SELECTION for " + numEntries + " PCRs: "
            + list + "(0x" + ByteArrayUtil.toHexString( this.pcrSelect ) + ")";
        return ret;
    }

}
