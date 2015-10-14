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

/**
 * @author lfgs
 */
public class TPM_PCR_COMPOSITE extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_PCR_COMPOSITE {
     *      TPM_PCR_SELECTION select;
     *      UINT32 valueSize;
     *      [size_is(valueSize)] TPM_PCRVALUE pcrValue[];
     * } TPM_PCR_COMPOSITE;
     * 
     */

    private TPM_PCR_SELECTION select;
    private TPM_PCRVALUE[] pcrValues;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_PCR_COMPOSITE()
    {
        // do nothing
    }

    public TPM_PCR_COMPOSITE( byte[] pcrBytes )
    {
        this.fromBytes( pcrBytes, 0 );
    }
    
    
    /**
     * 
     * 
     * @param select
     * @param valueSize
     * @param pcrValues
     */
    public TPM_PCR_COMPOSITE( TPM_PCR_SELECTION select, TPM_PCRVALUE... pcrValues )
    {
        this.select = select;
        this.setPcrValues( pcrValues );
    }
    
    
    

    public TPM_PCRVALUE[] getPcrValues()
    {
        return pcrValues;
    }


    public void setPcrValues( TPM_PCRVALUE[] pcrValues )
    {
        this.pcrValues = pcrValues;
    }


    public TPM_PCR_SELECTION getSelect()
    {
        return select;
    }


    public void setSelect( TPM_PCR_SELECTION select )
    {
        this.select = select;
    }

    public int getNumPcrValues()
    {
        return this.pcrValues.length;
    }

    /**
     * Returns the number of bytes taken by the PCR data.
     * @return
     */
    public int getValueSize()
    {
        return this.pcrValues.length * TPM_PCRVALUE.SIZE;
    }

    public int getTotalStructSize()
    {
        return this.select.getTotalStructSize() 
            + 4 + this.getValueSize();
    }
    
    @Override
    public byte[] toBytes()
    {
        int numValues = this.pcrValues.length;
        int valueSize = numValues * TPM_PCRVALUE.SIZE;
        int totalStructSize = this.getTotalStructSize();
        byte[] buf = new byte[totalStructSize];
        
        ByteArrayReadWriter brw = new ByteArrayReadWriter( buf, 0 );

        brw.writeBytes( this.select );
        brw.writeInt32( valueSize );
        for ( int i = 0; i < numValues; i++ )
        {
            TPM_PCRVALUE pcrValue = this.pcrValues[i];
            brw.writeBytes( pcrValue );
        }
        return buf;
    }



    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.select = new TPM_PCR_SELECTION();
        brw.readStruct( this.select );
        int valueSize = brw.readInt32();
        int numValues = valueSize / TPM_PCRVALUE.SIZE;
        this.pcrValues = new TPM_PCRVALUE[numValues];
        for ( int i = 0; i < numValues; i++ )
        {
            this.pcrValues[i] = new TPM_PCRVALUE( brw.readBytes( TPM_PCRVALUE.SIZE ) );
        }
    }

    public String toString()
    {
        String ret = "TPM_PCR_COMPOSITE:\n" 
            + "select = " + this.select
            + "\nPcrValues (numValues=" + this.getNumPcrValues() 
            + ", valueSize=" + this.getValueSize() + "): " + "\n";
        for ( int i = 0;  i < this.pcrValues.length; i++ )
        {
            ret = ret + this.pcrValues[i] + "\n";
        }
        return ret;
    }

}
